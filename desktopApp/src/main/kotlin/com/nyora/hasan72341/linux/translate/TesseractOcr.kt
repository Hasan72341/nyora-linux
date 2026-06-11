package com.nyora.linux.translate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp
import java.awt.image.RescaleOp
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

/** A merged speech-bubble text block detected by Tesseract, in ORIGINAL image pixel coordinates. */
data class OcrBox(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
    val text: String,
    val conf: Float,
)

/**
 * Thin wrapper around the Tesseract 5.x CLI. Everything fails soft so that an
 * unavailable or misbehaving binary never crashes the reader.
 *
 * The recognition pipeline mirrors the nyora-android approach:
 *  1. Preprocess (upscale 1.5x -> grayscale -> aggressive contrast boost) so text
 *     pops over screentone/halftone art before OCR.
 *  2. Run tesseract in sparse-text mode (--psm 11) and parse word-level TSV rows.
 *  3. Filter out halftone gibberish (low confidence, too-short, symbol-only).
 *  4. Cluster surviving words into speech bubbles by spatial proximity.
 *  5. Drop page-sized "bubbles" (art) and blank ones.
 * All returned boxes are in ORIGINAL image pixels.
 */
object TesseractOcr {

    /** Upscale factor applied before OCR; boxes are divided back out afterwards. */
    private const val SCALE = 1.5f

    private val CANDIDATE_PATHS = listOf(
        "tesseract",
        "/opt/homebrew/bin/tesseract",
        "/usr/local/bin/tesseract",
        "/usr/bin/tesseract",
    )

    /**
     * Locate a working tesseract binary by probing `<bin> --version`. Returns
     * the first candidate that runs, or null if none are available.
     */
    fun findBinary(): String? {
        for (path in CANDIDATE_PATHS) {
            val ok = runCatching {
                val process = ProcessBuilder(path, "--version")
                    .redirectErrorStream(true)
                    .start()
                if (!process.waitFor(10, TimeUnit.SECONDS)) {
                    process.destroyForcibly()
                    false
                } else {
                    process.exitValue() == 0
                }
            }.getOrDefault(false)
            if (ok) return path
        }
        return null
    }

    /**
     * Recognize text in [imageBytes] using the given [langs] (Tesseract "+"
     * notation, e.g. "jpn+eng").
     *
     * Returns `(bubbles, available)`:
     * - `available` is false only when no tesseract binary could be found.
     * - On any recognition error the binary is present, so an empty list is
     *   returned with `available == true`.
     *
     * Bubble boxes are clustered and reported in ORIGINAL image pixel coordinates.
     */
    suspend fun recognize(
        imageBytes: ByteArray,
        langs: String = "jpn+eng",
    ): Pair<List<OcrBox>, Boolean> = withContext(Dispatchers.IO) {
        val bin = findBinary() ?: return@withContext Pair(emptyList(), false)

        runCatching {
            val original = ImageIO.read(ByteArrayInputStream(imageBytes))
                ?: return@runCatching emptyList<OcrBox>()
            val imgW = original.width
            val imgH = original.height
            if (imgW <= 0 || imgH <= 0) return@runCatching emptyList<OcrBox>()

            val preprocessed = preprocess(original)

            val tmp = Files.createTempFile("nyora_ocr_", ".png")
            try {
                ImageIO.write(preprocessed, "png", tmp.toFile())

                val process = ProcessBuilder(
                    bin,
                    tmp.toAbsolutePath().toString(),
                    "stdout",
                    "-l", langs,
                    // psm 11 (sparse text) finds scattered bubble text far better
                    // than psm 6 (uniform block), which mis-groups the whole page.
                    "--psm", "11",
                    "tsv",
                ).redirectErrorStream(true).start()

                val stdout = process.inputStream.bufferedReader().use { it.readText() }
                if (!process.waitFor(30, TimeUnit.SECONDS)) {
                    process.destroyForcibly()
                    return@runCatching emptyList<OcrBox>()
                }

                // Words come back in PREPROCESSED (1.5x) coords; convert to original.
                val words = parseWords(stdout)
                clusterIntoBubbles(words, imgW, imgH)
            } finally {
                runCatching { Files.deleteIfExists(tmp) }
            }
        }.getOrDefault(emptyList()) to true
    }

    /**
     * Preprocess the page for OCR: upscale [SCALE]x (bilinear), convert to
     * grayscale, then apply an aggressive contrast boost centered on mid-grey so
     * dialogue text separates cleanly from screentone/halftone backgrounds.
     */
    private fun preprocess(original: BufferedImage): BufferedImage {
        val targetW = (original.width * SCALE).toInt().coerceAtLeast(1)
        val targetH = (original.height * SCALE).toInt().coerceAtLeast(1)

        // 1. Upscale (bilinear) into an RGB canvas.
        val scaled = BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB)
        val scaleX = targetW.toDouble() / original.width
        val scaleY = targetH.toDouble() / original.height
        val scaleOp = AffineTransformOp(
            AffineTransform.getScaleInstance(scaleX, scaleY),
            AffineTransformOp.TYPE_BILINEAR,
        )
        scaleOp.filter(toRgb(original), scaled)

        // 2. Grayscale.
        val grayConv = ColorConvertOp(
            java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_GRAY),
            null,
        )
        val gray = BufferedImage(targetW, targetH, BufferedImage.TYPE_BYTE_GRAY)
        grayConv.filter(scaled, gray)

        // 3. Aggressive contrast boost around mid-grey:
        //    out = in * 1.7 + offset, where offset keeps 128 roughly fixed:
        //    offset = (1 - 1.7) * 128 = -89.6 -> clamped by RescaleOp to [0,255].
        val contrast = 1.35f
        val offset = (1f - contrast) * 128f
        val rescale = RescaleOp(contrast, offset, null)
        val out = BufferedImage(targetW, targetH, BufferedImage.TYPE_BYTE_GRAY)
        rescale.filter(gray, out)
        return out
    }

    /** Ensure we operate on a plain RGB image (drops alpha / odd color models). */
    private fun toRgb(src: BufferedImage): BufferedImage {
        if (src.type == BufferedImage.TYPE_INT_RGB) return src
        val rgb = BufferedImage(src.width, src.height, BufferedImage.TYPE_INT_RGB)
        val g = rgb.createGraphics()
        try {
            g.drawImage(src, 0, 0, null)
        } finally {
            g.dispose()
        }
        return rgb
    }

    /**
     * Parse word-level (level==5) rows from a Tesseract TSV dump, applying the
     * noise filters, and convert each box from preprocessed (1.5x) to ORIGINAL
     * image pixels.
     */
    private fun parseWords(tsv: String): List<Word> {
        val lines = tsv.split('\n')
        if (lines.size <= 1) return emptyList()

        // Column indices per the standard Tesseract TSV header:
        // level page_num block_num par_num line_num word_num left top width height conf text
        val words = ArrayList<Word>()
        for (i in 1 until lines.size) {
            val raw = lines[i]
            if (raw.isBlank()) continue
            val cols = raw.split('\t')
            if (cols.size < 12) continue

            val level = cols[0].toIntOrNull() ?: continue
            if (level != 5) continue

            val conf = cols[10].toFloatOrNull() ?: continue
            if (conf <= 40f) continue

            val text = cols[11].trim()
            if (text.length < 2) continue
            // Must contain at least one Unicode letter; kills pure-symbol/number
            // halftone noise (e.g. "9 =| ...").
            if (!text.any { it.isLetter() }) continue

            val pLeft = cols[6].toIntOrNull() ?: continue
            val pTop = cols[7].toIntOrNull() ?: continue
            val pW = cols[8].toIntOrNull() ?: continue
            val pH = cols[9].toIntOrNull() ?: continue
            if (pW <= 0 || pH <= 0) continue

            // Convert preprocessed (1.5x) coords back to original image pixels.
            val left = (pLeft / SCALE).toInt()
            val top = (pTop / SCALE).toInt()
            val width = (pW / SCALE).toInt().coerceAtLeast(1)
            val height = (pH / SCALE).toInt().coerceAtLeast(1)

            words.add(Word(left, top, width, height, text, conf))
        }
        return words
    }

    /**
     * Cluster word boxes into speech bubbles by spatial proximity ([isClose]),
     * merging transitively. Each cluster becomes one [OcrBox] (merged bounding
     * box + reading-order text + average confidence). Page-sized clusters (area
     * > 55% of the page) and blank clusters are dropped.
     */
    private fun clusterIntoBubbles(words: List<Word>, imgW: Int, imgH: Int): List<OcrBox> {
        if (words.isEmpty()) return emptyList()

        val pageArea = imgW.toLong() * imgH.toLong()

        // Process in reading order so transitive merge is stable.
        val sorted = words.indices.sortedWith(
            compareBy({ words[it].top }, { words[it].left }),
        )
        val used = HashSet<Int>()
        val bubbles = ArrayList<OcrBox>()

        for (seed in sorted) {
            if (seed in used) continue
            val cluster = ArrayList<Word>()
            cluster.add(words[seed])
            used.add(seed)

            // Iteratively absorb any unused word close to the growing cluster.
            var changed: Boolean
            do {
                changed = false
                for (j in sorted) {
                    if (j in used) continue
                    val candidate = words[j]
                    if (cluster.any { isClose(it, candidate) }) {
                        cluster.add(candidate)
                        used.add(j)
                        changed = true
                    }
                }
            } while (changed)

            val minLeft = cluster.minOf { it.left }
            val minTop = cluster.minOf { it.top }
            val maxRight = cluster.maxOf { it.left + it.width }
            val maxBottom = cluster.maxOf { it.top + it.height }

            val text = cluster
                .sortedWith(compareBy({ it.top }, { it.left }))
                .joinToString(" ") { it.text }
                .trim()
            if (text.isEmpty()) continue

            val area = (maxRight - minLeft).toLong() * (maxBottom - minTop).toLong()
            if (area > pageArea * 0.55) continue

            bubbles.add(
                OcrBox(
                    left = minLeft,
                    top = minTop,
                    width = (maxRight - minLeft).coerceAtLeast(1),
                    height = (maxBottom - minTop).coerceAtLeast(1),
                    text = text,
                    conf = cluster.map { it.conf }.average().toFloat(),
                ),
            )
        }
        return bubbles
    }

    /**
     * Two boxes belong to the same bubble if they are vertically adjacent with
     * horizontal overlap, horizontally adjacent with vertical overlap, or they
     * intersect. Mirrors the nyora-android `isClose` heuristic.
     */
    private fun isClose(a: Word, b: Word): Boolean {
        val aRight = a.left + a.width
        val aBottom = a.top + a.height
        val bRight = b.left + b.width
        val bBottom = b.top + b.height

        val hOverlap = (minOf(aRight, bRight) - maxOf(a.left, b.left)).coerceAtLeast(0)
        val vOverlap = (minOf(aBottom, bBottom) - maxOf(a.top, b.top)).coerceAtLeast(0)

        val vGap = when {
            aBottom < b.top -> b.top - aBottom
            bBottom < a.top -> a.top - bBottom
            else -> 0
        }
        val hGap = when {
            aRight < b.left -> b.left - aRight
            bRight < a.left -> a.left - bRight
            else -> 0
        }

        val minW = minOf(a.width, b.width).toFloat()
        val minH = minOf(a.height, b.height).toFloat()
        val avgW = (a.width + b.width) / 2f
        val avgH = (a.height + b.height) / 2f

        val verticalNeighbor = vGap <= maxOf(40f, avgH * 0.9f) && hOverlap >= minW * 0.2f
        val horizontalNeighbor = hGap <= maxOf(40f, avgW * 0.6f) && vOverlap >= minH * 0.2f
        val intersects = hOverlap > 0 && vOverlap > 0

        return verticalNeighbor || horizontalNeighbor || intersects
    }

    /** A single OCR'd word, already converted to ORIGINAL image pixels. */
    private data class Word(
        val left: Int,
        val top: Int,
        val width: Int,
        val height: Int,
        val text: String,
        val conf: Float,
    )
}
