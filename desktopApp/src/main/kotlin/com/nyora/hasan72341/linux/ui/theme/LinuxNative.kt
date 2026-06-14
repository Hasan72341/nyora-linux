package com.nyora.linux.ui.theme

import androidx.compose.ui.graphics.Color
import java.util.concurrent.TimeUnit

/**
 * Reads desktop-environment appearance hints on Linux so the app can follow the
 * user's wallpaper/system accent colour. Everything is best-effort and wrapped in
 * [runCatching]; any failure returns `null` and the caller falls back to a default.
 */
object LinuxNative {

    val isLinux: Boolean =
        System.getProperty("os.name")?.lowercase()?.contains("linux") == true

    /**
     * The desktop/wallpaper accent colour, or `null` when it cannot be determined.
     *
     * Resolution order:
     *  1. The XDG desktop portal `org.freedesktop.appearance accent-color` setting
     *     (a `(ddd)` tuple of 0..1 doubles) — the cross-DE standard.
     *  2. GNOME's `org.gnome.desktop.interface accent-color` named colour (GNOME 47+).
     */
    val accentColor: Color? by lazy {
        if (!isLinux) return@lazy null
        portalAccent() ?: gnomeNamedAccent()
    }

    // ── XDG desktop portal ──────────────────────────────────────────────────────────
    private fun portalAccent(): Color? = runCatching {
        val out = run(
            "gdbus", "call", "--session",
            "--dest", "org.freedesktop.portal.Desktop",
            "--object-path", "/org/freedesktop/portal/desktop",
            "--method", "org.freedesktop.portal.Settings.Read",
            "org.freedesktop.appearance", "accent-color",
        ) ?: return@runCatching null
        // Example reply: (<<(0.2, 0.4, 0.8)>>,)
        val nums = Regex("[0-9]*\\.?[0-9]+")
            .findAll(out)
            .map { it.value.toDouble() }
            .toList()
        // Take the last three doubles (the r,g,b triple), guarding against version noise.
        if (nums.size < 3) return@runCatching null
        val (r, g, b) = nums.takeLast(3)
        if (r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1) return@runCatching null
        Color(r.toFloat(), g.toFloat(), b.toFloat())
    }.getOrNull()

    // ── GNOME named accent (GNOME 47+) ────────────────────────────────────────────────
    private fun gnomeNamedAccent(): Color? = runCatching {
        val raw = run("gsettings", "get", "org.gnome.desktop.interface", "accent-color")
            ?.trim()?.trim('\'', '"')?.lowercase() ?: return@runCatching null
        when (raw) {
            "blue"   -> Color(0xFF3584E4)
            "teal"   -> Color(0xFF2190A4)
            "green"  -> Color(0xFF3A944A)
            "yellow" -> Color(0xFFC88800)
            "orange" -> Color(0xFFED5B00)
            "red"    -> Color(0xFFE62D42)
            "pink"   -> Color(0xFFD56199)
            "purple" -> Color(0xFF9141AC)
            "slate"  -> Color(0xFF6F8396)
            else     -> null
        }
    }.getOrNull()

    private fun run(vararg cmd: String): String? = runCatching {
        val proc = ProcessBuilder(*cmd)
            .redirectErrorStream(false)
            .start()
        val finished = proc.waitFor(2, TimeUnit.SECONDS)
        if (!finished) { proc.destroyForcibly(); return@runCatching null }
        if (proc.exitValue() != 0) return@runCatching null
        proc.inputStream.bufferedReader().readText()
    }.getOrNull()
}
