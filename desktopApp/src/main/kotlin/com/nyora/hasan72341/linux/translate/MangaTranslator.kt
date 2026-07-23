package com.nyora.linux.translate

import com.nyora.linux.ai.onnx.MangaMt
import com.nyora.linux.ai.onnx.MangaOcr
import com.nyora.linux.ai.onnx.ColorizedPageCache
import com.nyora.linux.ai.onnx.OnnxColorizer
import com.nyora.linux.ai.onnx.OnnxDetector
import com.nyora.linux.ai.onnx.PaddleOcr
import com.nyora.linux.ai.onnx.SeriesGlossary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

/**
 * A translated speech bubble overlaid onto the manga page, in ORIGINAL image
 * pixels.
 *
 * [fillArgb] is the sampled balloon background (forced opaque) to repaint the
 * bubble with, and [textArgb] is the contrasting color to draw the translated
 * text in. Both are 0xAARRGGBB ints.
 */
data class TransBlock(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
    val original: String,
    val translated: String,
    val fillArgb: Int,
    val textArgb: Int,
    /**
     * The balloon-shaped fill region in ORIGINAL image pixels: 8 boundary points
     * (clockwise from North) found by ray-casting out of the text rect through the
     * light interior until the dark balloon border, so the repaint conforms to the
     * speech bubble instead of a crude rectangle. Empty if ray-casting found nothing.
     */
    val fillPolygon: List<Pair<Int, Int>>,
)

/**
 * Full translation result for a single page image.
 *
 * [ocrAvailable] is false only when no tesseract binary exists, which the UI
 * can surface as "OCR not installed". Coordinates are in ORIGINAL image pixels.
 */
data class PageTranslation(
    val imageWidth: Int,
    val imageHeight: Int,
    val blocks: List<TransBlock>,
    val ocrAvailable: Boolean,
)

/** Result of colourizing a reader page, kept explicit so the UI can distinguish
 * an unavailable model from an intentionally rejected oversized page. */
sealed class ColorizePageResult {
    data class Success(val path: String) : ColorizePageResult()
    object ModelUnavailable : ColorizePageResult()
    object InputTooLarge : ColorizePageResult()
    object Failed : ColorizePageResult()
}

/**
 * Orchestrates the OCR -> translate pipeline for a manga page. Every public
 * call fails soft: errors yield an empty translation rather than throwing.
 */
class MangaTranslator {

    private val http = OkHttpClient()

    private sealed class LoadedPage {
        data class Success(val bytes: ByteArray, val image: BufferedImage) : LoadedPage()
        object TooLarge : LoadedPage()
        object Failed : LoadedPage()
    }

    companion object {
        // A page download is untrusted input. Keep both its compressed size and
        // decoded pixel count bounded before OCR/ONNX allocate large buffers.
        private const val MAX_PAGE_DOWNLOAD_BYTES = 48L * 1024L * 1024L
        private const val MAX_PAGE_PIXELS = 24_000_000L
        private const val MAX_PAGE_EDGE = 16_384
    }

    /**
     * Download [imageUrl], OCR it with [ocrLangs], and translate every detected
     * bubble into [target]. For each bubble we sample the balloon background
     * from the ORIGINAL image and compute a contrasting text color so the
     * overlay can repaint the bubble naturally instead of stamping a dark box.
     */
    suspend fun translatePageImage(
        imageUrl: String,
        ocrLangs: String,
        target: String,
    ): PageTranslation = withContext(Dispatchers.IO) {
        try {
            val page = loadPageImage(imageUrl)
            if (page !is LoadedPage.Success) return@withContext PageTranslation(0, 0, emptyList(), true)
            val bytes = page.bytes
            // Decode the ORIGINAL image once; bubble boxes are in its pixel space.
            val image = page.image
            val width = image.width
            val height = image.height

            val (boxes, available) = TesseractOcr.recognize(bytes, ocrLangs)
            if (!available) {
                return@withContext PageTranslation(width, height, emptyList(), false)
            }

            val usable = boxes.filter { it.text.trim().isNotEmpty() }
            if (usable.isEmpty()) {
                return@withContext PageTranslation(width, height, emptyList(), true)
            }

            val translations = GoogleTranslate.translateAll(usable.map { it.text }, target)
            val blocks = usable.mapIndexedNotNull { index, box ->
                val translated = translations.getOrElse(index) { box.text }
                if (translated.isBlank()) return@mapIndexedNotNull null

                val fillRgb = sampleBubbleBackground(image, box)
                val r = (fillRgb shr 16) and 0xFF
                val g = (fillRgb shr 8) and 0xFF
                val b = fillRgb and 0xFF
                val fillArgb = (0xFF000000.toInt()) or fillRgb
                val luminance = 0.299 * r + 0.587 * g + 0.114 * b
                val textArgb = if (luminance > 140) 0xFF1A1A1A.toInt() else 0xFFF5F5F5.toInt()

                TransBlock(
                    left = box.left,
                    top = box.top,
                    width = box.width,
                    height = box.height,
                    original = box.text,
                    translated = translated,
                    fillArgb = fillArgb,
                    textArgb = textArgb,
                    fillPolygon = rayCastBubble(image, box, luminance),
                )
            }

            PageTranslation(width, height, blocks, true)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            PageTranslation(0, 0, emptyList(), true)
        }
    }

    /**
     * The nyora-web pipeline (core/translate/engine.js), run fully on-device via
     * onnxruntime: Manga-Bubble-YOLO detection → per-language OCR (manga-ocr for ja,
     * PP-OCR for zh/en/ko) → optional character-name glossary substitution → Google
     * gtx machine translation with manga repair (MangaMt) → optional LLM refinement.
     * Produces the same [PageTranslation] the overlay renders. Fails soft.
     */
    suspend fun translatePageImageOnnx(
        imageUrl: String,
        source: String,
        target: String,
        refineCfg: MangaMt.RefineCfg?,
        title: String,
        fandom: Boolean,
    ): PageTranslation = withContext(Dispatchers.IO) {
        try {
            val src = when {
                source.startsWith("ja") || source.startsWith("jp") -> "ja"
                source.startsWith("zh") || source.startsWith("ch") -> "zh"
                source.startsWith("ko") -> "ko"
                source.startsWith("en") -> "en"
                else -> "ja"
            }
            val ocrReady = OnnxDetector.isReady() &&
                if (src == "ja") MangaOcr.isReady() else PaddleOcr.isReady(src)
            val page = loadPageImage(imageUrl)
            if (page !is LoadedPage.Success) return@withContext PageTranslation(0, 0, emptyList(), true)
            val image = page.image
            val width = image.width
            val height = image.height
            if (!ocrReady) return@withContext PageTranslation(width, height, emptyList(), false)

            val boxes = OnnxDetector.detect(image)
            val ocrBoxes = boxes.mapNotNull { b ->
                val x = b.x.coerceIn(0, width - 1)
                val y = b.y.coerceIn(0, height - 1)
                val w = b.w.coerceIn(1, width - x)
                val h = b.h.coerceIn(1, height - y)
                if (w < 2 || h < 2) return@mapNotNull null
                val crop = image.getSubimage(x, y, w, h)
                val text = if (src == "ja") MangaOcr.recognize(crop) else PaddleOcr.recognize(crop, src)
                if (text.isBlank()) null else OcrBox(x, y, w, h, text.trim(), b.score)
            }
            if (ocrBoxes.isEmpty()) return@withContext PageTranslation(width, height, emptyList(), true)

            val rawTexts = ocrBoxes.map { it.text }
            val glossary = if (fandom) {
                try {
                    SeriesGlossary.resolve(title)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Throwable) {
                    null
                }
            } else null
            val hits = if (glossary != null) SeriesGlossary.detectNames(rawTexts, glossary.names) else emptyList()
            val srcTexts = SeriesGlossary.applyNames(rawTexts, hits)

            val mt = MangaMt.translateBatch(srcTexts, target, src)
            val finalTexts = if (refineCfg != null) {
                val ctx = SeriesGlossary.glossaryContext(glossary, hits)
                try {
                    MangaMt.refineBatch(srcTexts, mt, target, refineCfg.copy(context = ctx)) ?: mt
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Throwable) {
                    mt
                }
            } else mt

            val blocks = ocrBoxes.mapIndexedNotNull { index, box ->
                val translated = finalTexts.getOrElse(index) { box.text }
                if (translated.isBlank()) return@mapIndexedNotNull null
                val fillRgb = sampleBubbleBackground(image, box)
                val r = (fillRgb shr 16) and 0xFF
                val g = (fillRgb shr 8) and 0xFF
                val b = fillRgb and 0xFF
                val fillArgb = (0xFF000000.toInt()) or fillRgb
                val luminance = 0.299 * r + 0.587 * g + 0.114 * b
                val textArgb = if (luminance > 140) 0xFF1A1A1A.toInt() else 0xFFF5F5F5.toInt()
                TransBlock(
                    left = box.left, top = box.top, width = box.width, height = box.height,
                    original = box.text, translated = translated,
                    fillArgb = fillArgb, textArgb = textArgb,
                    fillPolygon = rayCastBubble(image, box, luminance),
                )
            }
            PageTranslation(width, height, blocks, true)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            PageTranslation(0, 0, emptyList(), true)
        }
    }

    /** Colorize one page on-device and store it in the bounded managed cache. */
    suspend fun colorizePageImage(imageUrl: String): ColorizePageResult = withContext(Dispatchers.IO) {
        try {
            if (!OnnxColorizer.isReady()) return@withContext ColorizePageResult.ModelUnavailable
            val page = loadPageImage(imageUrl)
            when (page) {
                LoadedPage.TooLarge -> return@withContext ColorizePageResult.InputTooLarge
                LoadedPage.Failed -> return@withContext ColorizePageResult.Failed
                is LoadedPage.Success -> Unit
            }
            val image = (page as LoadedPage.Success).image
            if (!OnnxColorizer.canColorize(image.width, image.height)) {
                return@withContext ColorizePageResult.InputTooLarge
            }
            ColorizePageResult.Success(ColorizedPageCache.write(OnnxColorizer.colorize(image)))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            ColorizePageResult.Failed
        }
    }

    private fun loadPageImage(imageUrl: String): LoadedPage {
        val request = Request.Builder().url(imageUrl).build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return LoadedPage.Failed
            val body = response.body ?: return LoadedPage.Failed
            if (body.contentLength() > MAX_PAGE_DOWNLOAD_BYTES) return LoadedPage.TooLarge
            val bytes = body.byteStream().use { input -> readBounded(input, MAX_PAGE_DOWNLOAD_BYTES) }
                ?: return LoadedPage.TooLarge
            return decodePage(bytes)
        }
    }

    private fun readBounded(input: InputStream, limit: Long): ByteArray? {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(1 shl 16)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (count == 0) continue
            total += count
            if (total > limit) return null
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun decodePage(bytes: ByteArray): LoadedPage {
        val stream = ImageIO.createImageInputStream(ByteArrayInputStream(bytes)) ?: return LoadedPage.Failed
        stream.use {
            val readers = ImageIO.getImageReaders(stream)
            if (!readers.hasNext()) return LoadedPage.Failed
            val reader = readers.next()
            try {
                reader.setInput(stream, true, true)
                val width = reader.getWidth(0)
                val height = reader.getHeight(0)
                if (
                    width <= 0 || height <= 0 ||
                    width > MAX_PAGE_EDGE || height > MAX_PAGE_EDGE ||
                    width.toLong() * height.toLong() > MAX_PAGE_PIXELS
                ) {
                    return LoadedPage.TooLarge
                }
                val image = reader.read(0) ?: return LoadedPage.Failed
                if (
                    image.width <= 0 || image.height <= 0 ||
                    image.width > MAX_PAGE_EDGE || image.height > MAX_PAGE_EDGE ||
                    image.width.toLong() * image.height.toLong() > MAX_PAGE_PIXELS
                ) {
                    return LoadedPage.TooLarge
                }
                return LoadedPage.Success(bytes, image)
            } catch (_: Throwable) {
                return LoadedPage.Failed
            } finally {
                reader.dispose()
            }
        }
    }

    /**
     * Sample the balloon's interior background from the ORIGINAL [image]. We read
     * four points inset ~18% from each corner of [box] (clamped to image bounds)
     * and pick the BRIGHTEST (max r+g+b) — that is the speech-balloon white/light
     * rather than a darker stroke or character art. Returns a 0xRRGGBB int.
     */
    private fun sampleBubbleBackground(image: BufferedImage, box: OcrBox): Int {
        val white = 0xFFFFFF
        return runCatching {
            val insetX = (box.width * 0.18f).toInt()
            val insetY = (box.height * 0.18f).toInt()
            val left = box.left + insetX
            val right = box.left + box.width - insetX
            val top = box.top + insetY
            val bottom = box.top + box.height - insetY

            val points = listOf(
                left to top,
                right to top,
                left to bottom,
                right to bottom,
            )

            var best: Int? = null
            var bestSum = -1
            for ((px, py) in points) {
                val x = px.coerceIn(0, image.width - 1)
                val y = py.coerceIn(0, image.height - 1)
                val rgb = image.getRGB(x, y) and 0xFFFFFF
                val sum = ((rgb shr 16) and 0xFF) + ((rgb shr 8) and 0xFF) + (rgb and 0xFF)
                if (sum > bestSum) {
                    bestSum = sum
                    best = rgb
                }
            }
            best ?: white
        }.getOrDefault(white)
    }

    /**
     * Ray-casts 8 directions out of [box]'s edges through the light balloon interior
     * until it hits the dark balloon border (or a distance cap), returning the 8
     * boundary points (clockwise from North) in ORIGINAL image pixels — the
     * speech-bubble-shaped fill region. A pixel darker than ~60% of the sampled
     * balloon background [bgLum] counts as the border.
     */
    private fun rayCastBubble(image: BufferedImage, box: OcrBox, bgLum: Double): List<Pair<Int, Int>> {
        val w = image.width
        val h = image.height
        if (w <= 0 || h <= 0) return emptyList()
        val cx = box.left + box.width / 2
        val cy = box.top + box.height / 2
        val left = box.left
        val right = box.left + box.width
        val top = box.top
        val bottom = box.top + box.height
        val darkThreshold = (bgLum * 0.6).coerceAtLeast(60.0)
        val cap = (maxOf(box.width, box.height) * 0.9).toInt().coerceIn(6, 240)
        // (startX, startY, dirX, dirY) — clockwise from North.
        val rays = listOf(
            intArrayOf(cx, top, 0, -1),
            intArrayOf(right, top, 1, -1),
            intArrayOf(right, cy, 1, 0),
            intArrayOf(right, bottom, 1, 1),
            intArrayOf(cx, bottom, 0, 1),
            intArrayOf(left, bottom, -1, 1),
            intArrayOf(left, cy, -1, 0),
            intArrayOf(left, top, -1, -1),
        )
        return rays.map { (sx, sy, dx, dy) ->
            var ex = sx.coerceIn(0, w - 1)
            var ey = sy.coerceIn(0, h - 1)
            var k = 1
            while (k <= cap) {
                val nx = sx + dx * k
                val ny = sy + dy * k
                if (nx < 0 || ny < 0 || nx >= w || ny >= h) break
                val rgb = image.getRGB(nx, ny)
                val lum = 0.299 * ((rgb shr 16) and 0xFF) +
                    0.587 * ((rgb shr 8) and 0xFF) +
                    0.114 * (rgb and 0xFF)
                if (lum < darkThreshold) break
                ex = nx
                ey = ny
                k++
            }
            ex to ey
        }
    }
}
