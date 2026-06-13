package com.nyora.linux

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.nyora.linux.ui.App
import com.nyora.hasan72341.shared.HelperMain
import com.nyora.hasan72341.shared.data.ExtensionInstaller
import com.nyora.hasan72341.shared.data.SourceCatalogClient
import com.nyora.hasan72341.shared.proxy.NyoraRestServer
import com.nyora.hasan72341.shared.reader.PageImageLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

fun main() {
    // ── Display scaling ───────────────────────────────────────────────────────
    // JBR/Skiko on some Linux setups mis-detect the display (e.g. GDK_SCALE=2 or a
    // high Xft.dpi) and render the whole UI at 200%. Force 1:1 by default so the UI
    // is the expected size; this MUST run before any AWT/Java2D init (i.e. first
    // thing in main). Users on a genuine HiDPI panel can opt back into scaling with
    // NYORA_UI_SCALE=2 (or -Dsun.java2d.uiScale=2), which is respected here.
    if (System.getProperty("sun.java2d.uiScale").isNullOrBlank()) {
        val override = System.getenv("NYORA_UI_SCALE")?.takeIf { it.isNotBlank() }
        System.setProperty("sun.java2d.uiScale", override ?: "1")
    }

    // Bootstrap the shared logic: DB, migrations, Supabase sync, and network config.
    // This ensures parity with the mac helper and deployable web targets.
    val boot = HelperMain.bootstrap()
    val facade = boot.facade
    val networkConfig = boot.networkConfig

    // Start the REST server in-process. The image proxy endpoint requires it,
    // and keeping the same HTTP surface means future CLI / remote clients work too.
    val server = NyoraRestServer(
        facade = facade,
        catalog = SourceCatalogClient(networkConfig = networkConfig),
        installer = ExtensionInstaller(networkConfig = networkConfig),
        pageLoader = PageImageLoader(networkConfig = networkConfig),
        downloads = boot.downloads,
        networkConfig = networkConfig,
    )
    val baseUrl = server.start()

    val appState = AppState(facade = facade, imageBaseUrl = baseUrl)

    Runtime.getRuntime().addShutdownHook(Thread { server.stop() })

    application {
        // ── (1) Confirm-before-quit ───────────────────────────────────────────
        // showQuitDialog tracks whether the confirmation AlertDialog is visible.
        var showQuitDialog by remember { mutableStateOf(false) }

        // Size the window to the user's actual display so it never overflows on a
        // small or HiDPI screen. WindowState is in dp and AWT's screenSize is in the
        // same logical coordinate space, so fit to ~88% of it (always ≤ screen) and
        // centre it. The minimum size is clamped to the screen too.
        val screen = remember { java.awt.Toolkit.getDefaultToolkit().screenSize }
        val windowState = rememberWindowState(
            width = (screen.width * 0.88).toInt().coerceIn(880, 1760).coerceAtMost(screen.width - 40).dp,
            height = (screen.height * 0.88).toInt().coerceIn(560, 1040).coerceAtMost(screen.height - 60).dp,
            position = WindowPosition(Alignment.Center),
        )

        Window(
            onCloseRequest = {
                if (appState.confirmBeforeQuit) {
                    // Don't exit immediately — ask first.
                    showQuitDialog = true
                } else {
                    exitApplication()
                }
            },
            title = "Nyora",
            icon = painterResource("nyora.png"),
            state = windowState,
            resizable = true,
        ) {
            // Native minimum size, never larger than the screen itself.
            LaunchedEffect(Unit) {
                window.minimumSize = java.awt.Dimension(
                    minOf(880, screen.width - 40),
                    minOf(560, screen.height - 60),
                )
            }
            App(state = appState)

            // Confirmation dialog rendered inside the window's composition scope.
            if (showQuitDialog) {
                AlertDialog(
                    onDismissRequest = { showQuitDialog = false },
                    title   = { Text("Quit Nyora?") },
                    text    = { Text("Are you sure you want to quit?") },
                    confirmButton = {
                        Button(onClick = { showQuitDialog = false; exitApplication() }) {
                            Text("Quit")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showQuitDialog = false }) {
                            Text("Cancel")
                        }
                    },
                )
            }

            // ── (2) Keep-screen-on heartbeat ──────────────────────────────────
            // While the reader is open and keepScreenOn is enabled, nudge the
            // mouse pointer by ±1 px every 30 s to prevent the OS screensaver /
            // power manager from activating. Wrapped in runCatching so it is a
            // complete no-op when java.awt.Robot is unavailable (headless, Wayland
            // without XWayland, container environments, etc.).
            val keepActive = appState.keepScreenOn && appState.showReader
            LaunchedEffect(keepActive) {
                if (!keepActive) return@LaunchedEffect
                runCatching {
                    val robot = java.awt.Robot()
                    while (isActive) {
                        delay(30_000L)
                        if (!isActive) break
                        runCatching {
                            val pos = java.awt.MouseInfo.getPointerInfo().location
                            robot.mouseMove(pos.x + 1, pos.y)
                            robot.mouseMove(pos.x,     pos.y)
                        }
                    }
                }
            }
        }
    }
}
