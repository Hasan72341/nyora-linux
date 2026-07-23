package com.nyora.linux.ai.onnx

import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermission
import javax.imageio.ImageIO

/**
 * Small, explicit disk cache for rendered colourized pages.
 *
 * `deleteOnExit()` is deliberately not used here: desktop reader sessions can
 * colourize many large pages and JVM exit hooks keep every temporary filename
 * alive until process exit. This cache is bounded, pruned by age, and entries
 * are explicitly released when a reader chapter closes or changes.
 */
object ColorizedPageCache {
    private const val MIB = 1024L * 1024L
    private const val MAX_ENTRIES = 24
    private const val MAX_TOTAL_BYTES = 256L * MIB
    private const val MAX_FILE_BYTES = 48L * MIB
    private const val MAX_AGE_MILLIS = 7L * 24L * 60L * 60L * 1000L

    private val appDirectory: File by lazy { File(System.getProperty("user.home"), ".nyora") }
    private val directory: File by lazy { File(appDirectory, "colorized-pages") }

    /** Writes [image] atomically and returns its managed cache path. */
    @Synchronized
    @Throws(IOException::class)
    fun write(image: BufferedImage): String {
        ensureDirectory()
        trim()

        val temp = Files.createTempFile(directory.toPath(), "page-", ".tmp").toFile()
        var target: File? = null
        try {
            if (!ImageIO.write(image, "png", temp)) {
                throw IOException("No PNG image writer is available")
            }
            if (temp.length() > MAX_FILE_BYTES) {
                throw IOException("Colorized page exceeds the cache file limit")
            }

            val output = Files.createTempFile(directory.toPath(), "page-", ".png").toFile()
            target = output
            // createTempFile creates the target; replace it atomically with the
            // fully written PNG so readers never observe a partial image.
            moveReplacing(temp, output)
            trim()
            return output.absolutePath
        } catch (error: Throwable) {
            temp.delete()
            target?.delete()
            throw error
        }
    }

    /** Touches an entry for LRU-like eviction and returns whether it still exists. */
    @Synchronized
    fun touch(path: String): Boolean {
        val file = managedFile(path) ?: return false
        if (!file.isFile) return false
        file.setLastModified(System.currentTimeMillis())
        return true
    }

    /** Removes only files that are inside this cache directory. */
    @Synchronized
    fun release(paths: Iterable<String>) {
        paths.forEach { managedFile(it)?.delete() }
        trim()
    }

    @Synchronized
    private fun ensureDirectory() {
        val appPath = appDirectory.toPath().toAbsolutePath().normalize()
        val cachePath = directory.toPath().toAbsolutePath().normalize()
        ensureOwnedDirectory(appPath, "Nyora cache root")
        ensureOwnedDirectory(cachePath, "Colorized-page cache")
    }

    /**
     * The cache contains reader images, so never follow a symlink when creating,
     * reading, touching, or deleting it. The app-owned root is intentionally
     * separate from the page directory so a pre-existing `~/.nyora` symlink is
     * rejected as well.
     */
    private fun ensureOwnedDirectory(path: java.nio.file.Path, label: String) {
        if (Files.exists(path, NOFOLLOW_LINKS)) {
            if (Files.isSymbolicLink(path) || !Files.isDirectory(path, NOFOLLOW_LINKS)) {
                throw IOException("$label is not a safe directory")
            }
        } else {
            Files.createDirectory(path)
        }
        if (Files.isSymbolicLink(path) || !Files.isDirectory(path, NOFOLLOW_LINKS)) {
            throw IOException("$label is not a safe directory")
        }
        // Linux supports POSIX permissions. Tighten existing directories too;
        // files created by Files.createTempFile are already owner-only.
        runCatching {
            Files.setPosixFilePermissions(
                path,
                setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                ),
            )
        }
    }

    /** Removes expired entries first, then the oldest entries until all limits fit. */
    @Synchronized
    private fun trim() {
        val appPath = appDirectory.toPath().toAbsolutePath().normalize()
        val cachePath = directory.toPath().toAbsolutePath().normalize()
        if (
            Files.isSymbolicLink(appPath) ||
            !Files.isDirectory(appPath, NOFOLLOW_LINKS) ||
            !Files.isDirectory(cachePath, NOFOLLOW_LINKS) ||
            Files.isSymbolicLink(cachePath)
        ) return
        val now = System.currentTimeMillis()
        val files = directory.listFiles { file ->
            file.isFile && file.name.startsWith("page-") && file.extension.equals("png", ignoreCase = true)
        }?.toMutableList() ?: return

        files.filter { now - it.lastModified() > MAX_AGE_MILLIS }.forEach { it.delete() }
        val remaining = files.filter { it.isFile }.sortedBy { it.lastModified() }.toMutableList()
        var total = remaining.sumOf { it.length() }
        while (remaining.size > MAX_ENTRIES || total > MAX_TOTAL_BYTES) {
            val oldest = remaining.removeFirstOrNull() ?: break
            total -= oldest.length()
            oldest.delete()
        }
    }

    private fun managedFile(path: String): File? = runCatching {
        val appRoot = appDirectory.toPath().toAbsolutePath().normalize()
        val root = directory.toPath().toAbsolutePath().normalize()
        val file = File(path).toPath().toAbsolutePath().normalize()
        if (
            Files.isSymbolicLink(appRoot) ||
            !Files.isDirectory(appRoot, NOFOLLOW_LINKS) ||
            Files.isSymbolicLink(root) ||
            !Files.isDirectory(root, NOFOLLOW_LINKS) ||
            file.parent != root ||
            Files.isSymbolicLink(file) ||
            !Files.isRegularFile(file, NOFOLLOW_LINKS) ||
            !file.fileName.toString().startsWith("page-") ||
            !file.fileName.toString().endsWith(".png", ignoreCase = true)
        ) null else file.toFile()
    }.getOrNull()

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
