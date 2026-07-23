package com.nyora.linux.ai.onnx

import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import okhttp3.ResponseBody

/**
 * Safe UTF-8 response reader for AI/translation providers.
 *
 * Provider responses are untrusted even when no credentials are attached. This
 * caps both an advertised length and streamed/decompressed bytes before callers
 * hand content to a JSON parser. Checking the coroutine between chunks keeps
 * reader cancellation structured instead of silently continuing a stale job.
 */
object BoundedHttpBody {
    const val DEFAULT_MAX_BYTES = 4 * 1024 * 1024

    suspend fun readUtf8(body: ResponseBody, maxBytes: Int = DEFAULT_MAX_BYTES): String {
        require(maxBytes > 0) { "Response body limit must be positive" }
        if (body.contentLength() > maxBytes.toLong()) {
            throw IOException("HTTP response exceeds the ${maxBytes / (1024 * 1024)} MiB limit")
        }

        val expected = body.contentLength().coerceIn(0L, 32L * 1024L).toInt()
        val output = ByteArrayOutputStream(expected)
        body.byteStream().use { input ->
            val buffer = ByteArray(32 * 1024)
            var total = 0
            while (true) {
                currentCoroutineContext().ensureActive()
                val count = input.read(buffer)
                if (count < 0) break
                if (count == 0) continue
                total += count
                if (total > maxBytes) {
                    throw IOException("HTTP response exceeds the ${maxBytes / (1024 * 1024)} MiB limit")
                }
                output.write(buffer, 0, count)
            }
        }
        return output.toString(Charsets.UTF_8)
    }
}
