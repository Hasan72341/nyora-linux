package com.nyora.linux.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyora.linux.AppState
import com.nyora.linux.NavDest
import com.nyora.linux.ui.screen.HomeScreen
import com.nyora.linux.ui.screen.BookmarksScreen
import com.nyora.linux.ui.screen.CatalogSheet
import com.nyora.linux.ui.screen.DetailsScreen
import com.nyora.linux.ui.screen.DownloadsScreen
import com.nyora.linux.ui.screen.ExploreScreen
import com.nyora.linux.ui.screen.FavouritesScreen
import com.nyora.linux.ui.screen.GlobalSearchScreen
import com.nyora.linux.ui.screen.HistoryScreen
import com.nyora.linux.ui.screen.LocalFilesScreen
import com.nyora.linux.ui.screen.ReaderScreen
import com.nyora.linux.ui.screen.SettingsScreen
import com.nyora.linux.ui.screen.StatsScreen
import com.nyora.linux.ui.screen.SuggestionsScreen
import com.nyora.linux.ui.screen.UpdatesScreen
import com.nyora.linux.ui.theme.NyoraTheme
import com.nyora.linux.ui.theme.NyoraTokens

@Composable
fun App(state: AppState) {
    NyoraTheme(appearance = state.appearance, accent = state.accent) {
        Box(
            modifier = Modifier.fillMaxSize().background(NyoraTokens.bg).onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when {
                    // Ctrl+Shift+F → global search
                    event.key == Key.F && event.isCtrlPressed && event.isShiftPressed -> {
                        state.showGlobalSearch = true; true
                    }
                    // Ctrl+Comma → open Settings
                    event.key == Key.Comma && event.isCtrlPressed -> {
                        state.destination = NavDest.SETTINGS; true
                    }
                    // Escape → close overlays
                    event.key == Key.Escape -> {
                        when {
                            state.showGlobalSearch -> { state.showGlobalSearch = false; true }
                            state.showCatalog      -> { state.showCatalog = false; true }
                            state.showReader       -> { state.showReader = false; true }
                            state.showDetails      -> { state.showDetails = false; true }
                            else -> false
                        }
                    }
                    else -> false
                }
            },
        ) {
            when {
                state.showWelcome      -> WelcomeScreen(state)
                state.showReader       -> ReaderScreen(state)
                state.showGlobalSearch -> GlobalSearchScreen(state)
                state.showCatalog      -> CatalogSheet(state)
                state.showDetails      -> DetailsScreen(state)
                else                   -> MainLayout(state)
            }

            // Snackbar status banner
            state.statusMessage?.let { msg ->
                Box(Modifier.fillMaxSize().padding(bottom = 16.dp), contentAlignment = Alignment.BottomCenter) {
                    Snackbar { Text(msg) }
                }
            }

            // Cloudflare manual-clearance dialog (overlays everything, incl. the reader)
            if (state.cloudflareHost != null) {
                CloudflareDialog(state)
            }
        }
    }
}

@Composable
private fun MainLayout(state: AppState) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 900.dp

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = NyoraTokens.bg,
        ) {
            MainContent(state, isCompact)
        }
    }
}

@Composable
private fun MainContent(state: AppState, isCompact: Boolean) {
    Row(Modifier.fillMaxSize()) {
        // Wide, full-height navigation sidebar (flush left).
        NyoraSidebar(
            current = state.destination,
            onSelect = {
                state.destination = it
                when (it) {
                    NavDest.HOME -> { state.refreshLibrary(); state.loadAnilistFeed() }
                    NavDest.DOWNLOADS -> state.loadDownloads()
                    NavDest.FAVOURITES -> state.loadCategories()
                    NavDest.STATS -> state.loadStats()
                    NavDest.SUGGESTIONS -> state.loadSuggestions()
                    else -> Unit
                }
            },
            onSearch = { state.showGlobalSearch = true },
            isCompact = isCompact
        )

        // Single hairline separating the rail from the content.
        Box(Modifier.fillMaxHeight().width(1.dp).background(NyoraTokens.hairlineFaint))

        // Content column: persistent top bar + the active screen.
        Column(Modifier.weight(1f).fillMaxHeight()) {
            TopBar(state)
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (state.destination) {
                    NavDest.HOME -> HomeScreen(state)
                    NavDest.EXPLORE -> ExploreScreen(state)
                    NavDest.FAVOURITES -> FavouritesScreen(state)
                    NavDest.HISTORY -> HistoryScreen(state)
                    NavDest.BOOKMARKS -> BookmarksScreen(state)
                    NavDest.UPDATES -> UpdatesScreen(state)
                    NavDest.LOCAL_FILES -> LocalFilesScreen(state)
                    NavDest.DOWNLOADS -> DownloadsScreen(state)
                    NavDest.SETTINGS -> SettingsScreen(state)
                    NavDest.STATS -> StatsScreen(state)
                    NavDest.SUGGESTIONS -> SuggestionsScreen(state)
                }
            }
        }
    }
}

/**
 * Persistent header above all main screens: a wide pill that triggers global search,
 * and (when a source is active) a chip naming the current source.
 */
@Composable
private fun TopBar(state: AppState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 28.dp, end = 28.dp, top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Global search pill (left-aligned, capped width).
        Box(Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NyoraTokens.surface1)
                    .border(1.dp, NyoraTokens.hairlineStrong, RoundedCornerShape(24.dp))
                    .clickable { state.showGlobalSearch = true }
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Filled.Search, null, tint = NyoraTokens.onSurfaceFaint, modifier = Modifier.size(19.dp))
                Text("Search all sources…", color = NyoraTokens.onSurfaceFaint, fontSize = 14.sp)
            }
        }

        // Active-source chip.
        state.activeSource?.let { src ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(NyoraTokens.surface1)
                    .border(1.dp, NyoraTokens.hairlineStrong, RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 11.dp),
            ) {
                Text(src.name, color = NyoraTokens.onSurfaceHigh, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
