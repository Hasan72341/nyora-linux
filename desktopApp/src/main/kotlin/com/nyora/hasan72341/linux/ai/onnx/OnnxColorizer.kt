package com.nyora.linux.ai.onnx

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.FloatBuffer
import kotlin.math.ceil

/**
 * On-device manga colorizer — a JVM/onnxruntime port of nyora-web's
 * core/colorize/worker.js, running manga-colorization-v2 (fp16 ONNX, ~62 MB, a
 * GAN trained on manga/anime art) entirely locally. Pages never leave the device.
 *
 * Pipeline (identical to the web reference):
 *   1. resize_pad to SIZE=576 (portrait → width=SIZE, landscape → height=SIZE*1.5),
 *      padded to a multiple of 32 with white.
 *   2. grayscale → input float32 [1,5,H,W] (ch0 = luma/255, ch1-4 = 0 = automatic).
 *   3. session → rgb float32 [1,3,H,W] in 0..1.
 *   4. upscale model colour to the original size, then luminance-combine: keep the
 *      SOURCE Y (crisp line art), take Cb/Cr from the model colour scaled by SAT=1.28.
 */
object OnnxColorizer {

    const val MODEL_URL =
        "https://huggingface.co/Faridzar/manga-colorization-v2-onnx/resolve/5515e06d31b08ffd107af686cba5e98e95e8d4cf/manga-colorize-fp16.onnx"
    const val MODEL_SHA256 = "39660d0047ea6f1a0ddee6aa89054997f95ea566f4d56ff762f66dbcf1a1a7ef"
    const val MODEL_BYTES = 61_650_260L

    private const val SIZE = 576
    private const val SAT = 1.28
    // The pipeline holds the source plus several full-size RGB images and model
    // tensors at once. Reject pathological pages before allocating those buffers.
    private const val MAX_SOURCE_PIXELS = 12_000_000L
    private const val MAX_MODEL_PIXELS = 1_500_000L
    private const val MAX_MODEL_EDGE = 4_096

    private val env: OrtEnvironment by lazy { OrtEnvironment.getEnvironment() }
    @Volatile private var session: OrtSession? = null

    /** True once the model is downloaded + verified (gate the Colorize toggle on this). */
    fun isReady(): Boolean = OnnxModels.isCached(MODEL_SHA256)

    /** Download + verify the model with progress, without spinning up the session. */
    fun downloadModel(onProgress: (Int) -> Unit = {}) {
        OnnxModels.ensure(MODEL_URL, MODEL_SHA256, MODEL_BYTES, onProgress)
    }

    private fun ensureSession(): OrtSession {
        session?.let { return it }
        synchronized(this) {
            session?.let { return it }
            val f = OnnxModels.verifiedModelFile(MODEL_SHA256)
            val opts = OrtSession.SessionOptions().apply {
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            }
            return env.createSession(f.absolutePath, opts).also { session = it }
        }
    }

    /** Whether [width] × [height] is safe for this in-memory colourization pipeline. */
    fun canColorize(width: Int, height: Int): Boolean = modelDimensions(width, height) != null

    private data class ModelDimensions(
        val validWidth: Int,
        val validHeight: Int,
        val modelWidth: Int,
        val modelHeight: Int,
    )

    private fun modelDimensions(width: Int, height: Int): ModelDimensions? {
        if (width <= 0 || height <= 0 || width.toLong() * height.toLong() > MAX_SOURCE_PIXELS) return null

        val validWidth: Long
        val validHeight: Long
        if (height < width) {
            validHeight = (SIZE * 1.5).toLong()
            validWidth = ceil(width.toDouble() * validHeight / height.toDouble()).toLong()
        } else {
            validWidth = SIZE.toLong()
            validHeight = ceil(height.toDouble() * validWidth / width.toDouble()).toLong()
        }
        val modelWidth = maxOf(32L, ((validWidth + 31L) / 32L) * 32L)
        val modelHeight = maxOf(32L, ((validHeight + 31L) / 32L) * 32L)
        if (
            validWidth <= 0L || validHeight <= 0L ||
            modelWidth > MAX_MODEL_EDGE || modelHeight > MAX_MODEL_EDGE ||
            modelWidth * modelHeight > MAX_MODEL_PIXELS
        ) return null

        return ModelDimensions(
            validWidth.toInt(), validHeight.toInt(), modelWidth.toInt(), modelHeight.toInt(),
        )
    }

    /** Colorize [src] → a new coloured [BufferedImage] at the original resolution. */
    fun colorize(src: BufferedImage, onProgress: (Int) -> Unit = {}): BufferedImage {
        val ow = src.width
        val oh = src.height
        val dimensions = modelDimensions(ow, oh)
            ?: throw IllegalArgumentException("Page dimensions exceed the colorizer safety limit")
        val vw = dimensions.validWidth
        val vh = dimensions.validHeight
        val mw = dimensions.modelWidth
        val mh = dimensions.modelHeight

        val modelCanvas = BufferedImage(mw, mh, BufferedImage.TYPE_INT_RGB)
        modelCanvas.createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            color = Color.WHITE
            fillRect(0, 0, mw, mh)
            drawImage(src, 0, 0, vw, vh, null)
            dispose()
        }

        // 2. grayscale → 5-channel input (ch1-4 stay 0 = automatic colourisation).
        val plane = mw * mh
        val input = FloatArray(5 * plane)
        var i = 0
        for (y in 0 until mh) {
            for (x in 0 until mw) {
                val rgb = modelCanvas.getRGB(x, y)
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF
                input[i++] = ((0.299 * r + 0.587 * g + 0.114 * b) / 255.0).toFloat()
            }
        }

        // 3. inference → rgb [1,3,mh,mw] in 0..1.
        val session = ensureSession()
        val inName = session.inputNames.first()
        val rgb: FloatArray
        OnnxTensor.createTensor(env, FloatBuffer.wrap(input), longArrayOf(1, 5, mh.toLong(), mw.toLong())).use { tensor ->
            session.run(mapOf(inName to tensor)).use { out ->
                val t = out.get(0) as OnnxTensor
                rgb = FloatArray(3 * plane)
                t.floatBuffer.get(rgb)
            }
        }

        // model colour → mw×mh image
        val colorCanvas = BufferedImage(mw, mh, BufferedImage.TYPE_INT_RGB)
        for (p in 0 until plane) {
            val r = clamp255(rgb[p] * 255f)
            val g = clamp255(rgb[plane + p] * 255f)
            val b = clamp255(rgb[2 * plane + p] * 255f)
            colorCanvas.setRGB(p % mw, p / mw, (r shl 16) or (g shl 8) or b)
        }

        // upscale the VALID region of the model colour to the original size
        val colorFull = BufferedImage(ow, oh, BufferedImage.TYPE_INT_RGB)
        colorFull.createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            drawImage(colorCanvas.getSubimage(0, 0, vw.coerceAtMost(mw), vh.coerceAtMost(mh)), 0, 0, ow, oh, null)
            dispose()
        }

        // 4. combine: source Y (crisp lines) + model Cb/Cr * SAT.
        val outImg = BufferedImage(ow, oh, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until oh) {
            for (x in 0 until ow) {
                val o = src.getRGB(x, y)
                val or = (o shr 16) and 0xFF
                val og = (o shr 8) and 0xFF
                val ob = o and 0xFF
                val Y = 0.299 * or + 0.587 * og + 0.114 * ob

                val c = colorFull.getRGB(x, y)
                val cr = (c shr 16) and 0xFF
                val cg = (c shr 8) and 0xFF
                val cb = c and 0xFF
                val Cb = (-0.168736 * cr - 0.331264 * cg + 0.5 * cb) * SAT
                val Cr = (0.5 * cr - 0.418688 * cg - 0.081312 * cb) * SAT

                val rr = clamp255((Y + 1.402 * Cr).toFloat())
                val gg = clamp255((Y - 0.344136 * Cb - 0.714136 * Cr).toFloat())
                val bb = clamp255((Y + 1.772 * Cb).toFloat())
                outImg.setRGB(x, y, (rr shl 16) or (gg shl 8) or bb)
            }
        }
        return outImg
    }

    private fun clamp255(v: Float): Int = when {
        v < 0f -> 0
        v > 255f -> 255
        else -> v.toInt()
    }
}
