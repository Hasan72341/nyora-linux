package com.nyora.linux.ai.onnx

import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection

/**
 * Shared model download + integrity cache for the on-device ONNX engines
 * (colorizer + translation vision stack). Mirrors nyora-web's model.js /
 * fetchWithProgress: pin every model to a commit-SHA URL, verify its SHA-256
 * BEFORE it is used, and cache the verified bytes on disk so later sessions
 * start offline-fast. A tampered or truncated download is rejected here rather
 * than failing deep inside the ONNX parser.
 */
object OnnxModels {

    private const val MIB = 1024L * 1024L
    private const val MAX_MODEL_DOWNLOAD_BYTES = 256L * MIB
    private const val MIN_DOWNLOAD_SLACK_BYTES = 8L * MIB
    private const val MAX_REDIRECTS = 5
    private val SHA256_PATTERN = Regex("^[a-fA-F0-9]{64}$")

    /** ~/.nyora/models — verified model bytes, keyed by their SHA-256. */
    val cacheDir: File by lazy {
        File(System.getProperty("user.home"), ".nyora/models").apply { mkdirs() }
    }

    fun modelFile(sha256: String): File {
        require(SHA256_PATTERN.matches(sha256)) { "Model checksum must be a SHA-256 digest" }
        return File(cacheDir, "$sha256.onnx")
    }

    /** True when the model is present on disk and its bytes still match [sha256]. */
    fun isCached(sha256: String): Boolean = runCatching {
        val f = modelFile(sha256)
        // A damaged, unreadable, or swapped cache entry should make readiness
        // false so the UI can offer a clean verified re-download—not crash a
        // reader/settings refresh.
        f.isFile && f.length() <= MAX_MODEL_DOWNLOAD_BYTES && sha256(f).equals(sha256, ignoreCase = true)
    }.getOrDefault(false)

    /**
     * Returns a model only when its existing bytes have passed integrity
     * verification. Inference code uses this instead of [ensure] so opening a
     * reader can never trigger an unannounced model download.
     */
    fun verifiedModelFile(sha256: String): File {
        if (!isCached(sha256)) throw IOException("Model is not downloaded and verified")
        return modelFile(sha256)
    }

    /**
     * Ensure the model at [url] is present and verified locally; download it with
     * [onProgress] (0..100) otherwise. Returns the verified local file. Idempotent,
     * and safe to call concurrently for the same model (last writer wins on rename).
     */
    @Synchronized
    fun ensure(url: String, sha256: String, sizeHint: Long = 0L, onProgress: (Int) -> Unit = {}): File {
        val f = modelFile(sha256)
        if (isCached(sha256)) { onProgress(100); return f }

        if (!cacheDir.exists() && !cacheDir.mkdirs()) throw IOException("Could not create model cache")
        if (!cacheDir.isDirectory) throw IOException("Model cache is not a directory")

        val tmp = Files.createTempFile(cacheDir.toPath(), "$sha256-", ".part").toFile()
        var conn: HttpsURLConnection? = null
        try {
            conn = openHttpsConnection(url)
            val maxBytes = maxDownloadBytes(sizeHint)
            val contentLength = conn.contentLengthLong
            if (contentLength > maxBytes) {
                throw IOException("Model download exceeds its allowed size")
            }
            val total = if (contentLength > 0) contentLength else sizeHint
            conn.inputStream.use { input ->
                tmp.outputStream().use { out ->
                    val buf = ByteArray(1 shl 16)
                    var got = 0L
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        if (n == 0) continue
                        got += n
                        if (got > maxBytes) throw IOException("Model download exceeds its allowed size")
                        out.write(buf, 0, n)
                        if (total > 0) onProgress(minOf(100, (got * 100 / total).toInt()))
                    }
                }
            }
            val actual = sha256(tmp)
            if (!actual.equals(sha256, ignoreCase = true)) {
                throw IOException("Model failed its integrity check — download rejected")
            }
            moveReplacing(tmp, f)
            onProgress(100)
            return f
        } finally {
            conn?.disconnect()
            tmp.delete()
        }
    }

    fun sha256(f: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        f.inputStream().use { s ->
            val b = ByteArray(1 shl 16)
            var n: Int
            while (s.read(b).also { n = it } >= 0) md.update(b, 0, n)
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    private fun maxDownloadBytes(sizeHint: Long): Long {
        if (sizeHint <= 0L) return MAX_MODEL_DOWNLOAD_BYTES
        val withSlack = maxOf(sizeHint + MIN_DOWNLOAD_SLACK_BYTES, sizeHint + sizeHint / 2L)
        return minOf(withSlack, MAX_MODEL_DOWNLOAD_BYTES)
    }

    /** Follows only HTTPS redirects so model fetches cannot silently downgrade. */
    private fun openHttpsConnection(rawUrl: String): HttpsURLConnection {
        var url = URL(rawUrl)
        repeat(MAX_REDIRECTS + 1) {
            if (!url.protocol.equals("https", ignoreCase = true) || url.host.isBlank()) {
                throw IOException("Model downloads must use HTTPS")
            }
            val connection = (url.openConnection() as? HttpsURLConnection)
                ?: throw IOException("Model downloads must use HTTPS")
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000
            connection.instanceFollowRedirects = false
            when (val code = connection.responseCode) {
                in 200..299 -> return connection
                in 300..399 -> {
                    val location = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (location.isNullOrBlank()) throw IOException("Model download redirect has no location")
                    url = URL(url, location)
                }
                else -> {
                    connection.disconnect()
                    throw IOException("Model download failed ($code)")
                }
            }
        }
        throw IOException("Model download exceeded redirect limit")
    }

    private fun moveReplacing(source: File, target: File) {
        try {
            Files.move(
                source.toPath(), target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
