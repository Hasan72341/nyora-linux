package com.nyora.linux.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.nyora.linux.ui.GoogleLogo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.SyncAlt
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyora.linux.AppState
import com.nyora.linux.NavDest
import com.nyora.linux.ReaderMode
import com.nyora.linux.ui.theme.Accent
import com.nyora.linux.ui.theme.AppearanceMode
import com.nyora.linux.ui.theme.LocalNyoraAccent
import com.nyora.linux.ui.theme.NyoraTokens
import com.nyora.linux.ui.theme.SectionHeader
import com.nyora.linux.ui.theme.SystemTag
import com.nyora.linux.ui.theme.accentGradientSubtle
import com.nyora.linux.ui.theme.glassCard
import com.nyora.linux.ui.theme.glowBorder
import com.nyora.hasan72341.shared.repository.SqlDelightLibraryRepository

/** App version surfaced in the About section; no build system constant is wired yet. */
const val VERSION: String = "1.0.0"

// ── Dropdown option lists ──────────────────────────────────────────────────────────────

private val READER_MODE_OPTIONS = listOf(
    ReaderMode.PAGED    to "Paged",
    ReaderMode.WEBTOON  to "Webtoon",
    ReaderMode.VERTICAL to "Vertical",
)

private val HISTORY_SORT_OPTIONS = listOf(
    "recent" to "Recently Read",
    "alpha"  to "Alphabetical",
    "added"  to "Date Added",
)

private val TARGET_LANG_OPTIONS = listOf(
    "en" to "English",
    "es" to "Spanish",
    "fr" to "French",
    "de" to "German",
    "pt" to "Portuguese",
    "it" to "Italian",
    "ru" to "Russian",
    "zh" to "Chinese (Simplified)",
    "ko" to "Korean",
    "ar" to "Arabic",
)

private val OCR_LANG_OPTIONS = listOf(
    "jpn"                  to "Japanese",
    "eng"                  to "English",
    "kor"                  to "Korean",
    "chi_sim"              to "Chinese (Simplified)",
    "chi_tra"              to "Chinese (Traditional)",
    "ara"                  to "Arabic",
    "rus"                  to "Russian",
    "deu"                  to "German",
    "fra"                  to "French",
    "spa"                  to "Spanish",
    "jpn+eng"              to "Japanese + English",
    "chi_sim+chi_sim_vert" to "Chinese (Simplified + Vertical)",
)

private val SCALE_MODE_OPTIONS = listOf(
    "fit_center" to "Fit Screen",
    "fit_width"  to "Fit Width",
    "fit_height" to "Fit Height",
    "fill"       to "Fill Page",
)

private val PROGRESS_INDICATOR_OPTIONS = listOf(
    "circular" to "Circular",
    "bar"      to "Bar",
    "off"      to "Off",
)

private val DOWNLOAD_FORMAT_OPTIONS = listOf(
    "auto"   to "Auto",
    "folder" to "Folder of Images",
    "cbz"    to "CBZ Archive",
    "zip"    to "ZIP Archive",
)

private val TRANSLATION_TIER_OPTIONS = listOf(
    "fast"     to "Fast",
    "tuned"    to "Tuned",
    "balanced" to "Balanced",
    "quality"  to "Quality",
)

/**
 * "Midnight Sakura" Settings — full mac-parity.
 *
 * A flat-surface settings screen. Most of the time it renders the scrolling settings
 * list; tapping a navigation row swaps the whole surface for a full-screen sub-panel
 * (Network / Backup / AniList) which calls back to dismiss. Statistics is a top-level
 * nav destination rather than a panel, so it routes through [AppState.destination].
 */
@Composable
fun SettingsScreen(state: AppState) {
    // null → the root category menu (Android-style); a key → that category's detail
    // page, or a full-screen panel (Network / Backup / Tracker) that calls back to dismiss.
    var panel by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (panel) {
            null      -> SettingsRoot(state) { panel = it }
            "network" -> NetworkSettingsScreen(state) { panel = null }
            "backup"  -> BackupScreen(state) { panel = null }
            "tracker" -> TrackerScreen(state) { panel = null }
            else      -> SettingsDetail(state, panel!!) { panel = null }
        }
    }
}

// ── Root category menu ───────────────────────────────────────────────────────────────
//
// Mirrors the nyora-android pattern: a single list of category rows. Tapping one opens
// that category's own page (or a dedicated full-screen panel) — nothing is shown all at
// once.

private data class SettingsCategory(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)

private val SETTINGS_CATEGORIES = listOf(
    SettingsCategory("appearance",    "Appearance",        "Theme, accent & layout",      Icons.Rounded.Palette),
    SettingsCategory("reader",        "Reader",            "Modes, scaling & navigation", Icons.Rounded.AutoStories),
    SettingsCategory("library",       "Library & History", "Retention, sorting & NSFW",   Icons.Rounded.History),
    SettingsCategory("translation",   "Translation",       "OCR, languages & quality",    Icons.Rounded.Translate),
    SettingsCategory("notifications", "Notifications",     "New chapter alerts",          Icons.Rounded.Notifications),
    SettingsCategory("downloads",     "Downloads",         "Concurrency & format",        Icons.Rounded.Download),
    SettingsCategory("sync",          "Cloud Sync",        "Google sign-in & restore",    Icons.Rounded.Cloud),
    SettingsCategory("parsers",       "Parser Updates",    "OTA bundle & version",        Icons.Rounded.Update),
    SettingsCategory("network",       "Network",           "Proxy, DoH & mirrors",        Icons.Rounded.Cloud),
    SettingsCategory("tracker",       "Tracker",           "AniList sync",                Icons.Rounded.SyncAlt),
    SettingsCategory("backup",        "Backup & Restore",  "Export or import library",    Icons.Rounded.Storage),
    SettingsCategory("privacy",       "Privacy",           "Incognito & quit",            Icons.Rounded.Lock),
    SettingsCategory("about",         "About",             "Version & engine",            Icons.Rounded.Info),
)

@Composable
private fun SettingsRoot(state: AppState, open: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SectionHeader(title = "Settings", subtitle = "Tune Nyora")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(shape = RoundedCornerShape(24.dp), fill = NyoraTokens.surface1)
                .padding(vertical = 4.dp),
        ) {
            SETTINGS_CATEGORIES.forEachIndexed { i, cat ->
                NavRow(cat.title, cat.subtitle, cat.icon) { open(cat.key) }
                if (i != SETTINGS_CATEGORIES.lastIndex) HairlineDivider()
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** A back affordance for a category detail page. The category's own section header below
 *  doubles as the page title, so this stays minimal. */
@Composable
private fun DetailHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(NyoraTokens.surface1)
            .clickable { onBack() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.ChevronLeft, "Back", tint = NyoraTokens.onSurfaceHigh, modifier = Modifier.size(24.dp))
    }
}

// ── Category detail page ─────────────────────────────────────────────────────────────

@Composable
private fun SettingsDetail(state: AppState, key: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        DetailHeader(onBack = onBack)

        // (1) APPEARANCE ──────────────────────────────────────────────────────────────────
        if (key == "appearance") SettingsSection(eyebrow = "Look & Feel", title = "Appearance", icon = Icons.Rounded.Palette) {
            // Theme — Amoled / Light segmented control with accentGradientSubtle on selected
            SettingsRow("Theme") {
                AppearanceSegmented(
                    selected  = state.appearance,
                    onSelect  = { state.setAppearance(it) },
                )
            }
            HairlineDivider()
            // Accent swatches
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
                Text(
                    "Accent",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NyoraTokens.onSurfaceHigh,
                )
                Spacer(Modifier.height(14.dp))
                AccentSwatchRow(
                    selected = state.accent,
                    onSelect = { state.setAccent(it) },
                )
            }
            HairlineDivider()
            // Manga list & details
            SettingsToggle("Show Unread Badge on Covers", state.showUnreadBadge) {
                state.showUnreadBadge = it; state.persistSettings()
            }
            HairlineDivider()
            SettingsToggle("Quick Filter Chips", state.quickFilters) {
                state.quickFilters = it; state.persistSettings()
            }
            HairlineDivider()
            SettingsRow("Reading Progress Indicator") {
                NyoraDropdown(state.progressIndicator, PROGRESS_INDICATOR_OPTIONS) {
                    state.progressIndicator = it; state.persistSettings()
                }
            }
            HairlineDivider()
            SettingsToggle("Pages Thumbnail Tab", state.pagesThumbnailTab) {
                state.pagesThumbnailTab = it; state.persistSettings()
            }
            HairlineDivider()
            SettingsToggle("Labels in Sidebar", state.sidebarLabels) {
                state.sidebarLabels = it; state.persistSettings()
            }
        }

        // (2) READER ──────────────────────────────────────────────────────────────────────
        if (key == "reader") SettingsSection(eyebrow = "Reading", title = "Reader", icon = Icons.Rounded.AutoStories) {
            // Default reader mode dropdown
            SettingsRow("Default Reader Mode") {
                NyoraDropdown(
                    selected = state.defaultReaderMode,
                    options  = READER_MODE_OPTIONS,
                    onSelect = {
                        state.defaultReaderMode = it
                        state.persistSettings()
                    },
                )
            }
            HairlineDivider()
            // Auto-detect reader mode
            SettingsToggle("Auto-Detect Reader Mode", state.autoDetectReaderMode) {
                state.autoDetectReaderMode = it
                state.persistSettings()
            }
            HairlineDivider()
            // Reader background segmented
            SettingsRow("Reader Background") {
                ThreeSegmented(
                    options  = listOf("auto" to "Auto", "dark" to "Dark", "light" to "Light"),
                    selected = state.readerBackground,
                    onSelect = { state.setReaderBackground(it) },
                )
            }
            HairlineDivider()
            // Scale mode
            SettingsRow("Scale Mode") {
                NyoraDropdown(state.readerScaleMode, SCALE_MODE_OPTIONS) {
                    state.readerScaleMode = it; state.persistSettings()
                }
            }
            HairlineDivider()
            // Right-to-left manga
            SettingsToggle("Right-to-Left (Manga)", state.readerRtl) {
                state.readerRtl = it; state.persistSettings()
            }
            HairlineDivider()
            // Webtoon pinch zoom + default zoom-out
            SettingsToggle("Webtoon Pinch-Zoom", state.webtoonZoom) {
                state.webtoonZoom = it; state.persistSettings()
            }
            if (state.webtoonZoom) {
                HairlineDivider()
                SliderRow(
                    label = "Default Zoom-Out",
                    value = state.webtoonZoomOut,
                    valueText = "${(state.webtoonZoomOut * 100).toInt()}%",
                    range = 0f..0.5f,
                    steps = 9,
                    onChange = { state.webtoonZoomOut = it },
                    onCommit = { state.persistSettings() },
                )
            }
            HairlineDivider()
            SettingsToggle("Gaps Between Webtoon Pages", state.webtoonGaps) {
                state.webtoonGaps = it; state.persistSettings()
            }
            HairlineDivider()
            // Navigation
            SettingsToggle("Tap Zones", state.tapZones) {
                state.tapZones = it; state.persistSettings()
            }
            HairlineDivider()
            SettingsToggle("Tap Zones Left-to-Right", state.tapZonesLtr) {
                state.tapZonesLtr = it; state.persistSettings()
            }
            HairlineDivider()
            SettingsToggle("Invert Page Direction", state.invertNavigation) {
                state.invertNavigation = it; state.persistSettings()
            }
            HairlineDivider()
            // Show zoom buttons
            SettingsToggle("Show Zoom Buttons", state.showZoomButtons) {
                state.showZoomButtons = it
                state.persistSettings()
            }
            HairlineDivider()
            // Two-page in landscape
            SettingsToggle("Two Pages in Landscape", state.twoPageLandscape) {
                state.twoPageLandscape = it
                state.persistSettings()
            }
            HairlineDivider()
            // Auto-hide controls
            SettingsToggle("Auto-Hide Controls", state.autoHideControls) {
                state.autoHideControls = it
                state.persistSettings()
            }
            HairlineDivider()
            // Keep screen on
            SettingsToggle("Keep Screen On", state.keepScreenOn) {
                state.keepScreenOn = it
                state.persistSettings()
            }
            HairlineDivider()
            // Prefetch next pages (existing)
            SettingsToggle("Prefetch Next Pages", state.prefetchEnabled) { state.prefetchEnabled = it }
            HairlineDivider()
            // Show page numbers (existing)
            SettingsToggle("Show Page Numbers", state.showPageNumbers) { state.showPageNumbers = it }
            HairlineDivider()
            // Description collapse
            SettingsToggle("Collapse Description by Default", state.descriptionCollapse) {
                state.descriptionCollapse = it
                state.persistSettings()
            }
            HairlineDivider()
            // Grid size slider 120-220
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Cover Grid Size",
                        style = MaterialTheme.typography.bodyLarge,
                        color = NyoraTokens.onSurfaceHigh,
                    )
                    Text(
                        "${state.gridSize} dp",
                        style = MaterialTheme.typography.labelLarge,
                        color = LocalNyoraAccent.current.color,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Slider(
                    value       = state.gridSize.toFloat(),
                    onValueChange = { state.gridSize = it.toInt() },
                    onValueChangeFinished = { state.persistSettings() },
                    valueRange  = 120f..220f,
                    steps       = 9, // 10 dp increments → 10 steps
                    colors      = SliderDefaults.colors(
                        thumbColor          = LocalNyoraAccent.current.color,
                        activeTrackColor    = LocalNyoraAccent.current.color,
                        inactiveTrackColor  = NyoraTokens.surface2,
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("120", style = MaterialTheme.typography.labelSmall, color = NyoraTokens.onSurfaceFaint)
                    Text("220", style = MaterialTheme.typography.labelSmall, color = NyoraTokens.onSurfaceFaint)
                }
            }
            HairlineDivider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Color correction lives in the reader — open any chapter and tap the palette.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NyoraTokens.onSurfaceMuted,
                )
            }
        }

        // (3) LIBRARY & HISTORY ───────────────────────────────────────────────────────────
        if (key == "library") SettingsSection(eyebrow = "Library", title = "Library & History", icon = Icons.Rounded.History) {
            // Hide NSFW content (existing)
            SettingsToggle("Hide NSFW Content", state.nsfwFilter) { state.nsfwFilter = it }
            HairlineDivider()
            // Hide NSFW sources (new)
            SettingsToggle("Hide NSFW Sources", state.hideNsfwSources) {
                state.hideNsfwSources = it
                state.persistSettings()
            }
            HairlineDivider()
            // History retention slider 0-365 step 30
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "History Retention",
                        style = MaterialTheme.typography.bodyLarge,
                        color = NyoraTokens.onSurfaceHigh,
                    )
                    Text(
                        if (state.historyRetentionDays == 0) "Forever"
                        else "${state.historyRetentionDays} days",
                        style = MaterialTheme.typography.labelLarge,
                        color = LocalNyoraAccent.current.color,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(8.dp))
                // steps value: 0, 30, 60 … 360 → 13 stops, steps param = 11 (between first & last)
                Slider(
                    value       = state.historyRetentionDays.toFloat(),
                    onValueChange = {
                        // Snap to nearest 30-day multiple (0 = Forever)
                        val snapped = (Math.round(it / 30f) * 30).coerceIn(0, 360)
                        state.historyRetentionDays = snapped
                    },
                    onValueChangeFinished = { state.persistSettings() },
                    valueRange  = 0f..360f,
                    steps       = 11,
                    colors      = SliderDefaults.colors(
                        thumbColor          = LocalNyoraAccent.current.color,
                        activeTrackColor    = LocalNyoraAccent.current.color,
                        inactiveTrackColor  = NyoraTokens.surface2,
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Forever", style = MaterialTheme.typography.labelSmall, color = NyoraTokens.onSurfaceFaint)
                    Text("360 days", style = MaterialTheme.typography.labelSmall, color = NyoraTokens.onSurfaceFaint)
                }
            }
            HairlineDivider()
            // Group history by date
            SettingsToggle("Group History by Date", state.groupHistoryByDate) {
                state.groupHistoryByDate = it
                state.persistSettings()
            }
            HairlineDivider()
            // History sort order dropdown
            SettingsRow("History Sort Order") {
                NyoraDropdown(
                    selected = state.historySortOrder,
                    options  = HISTORY_SORT_OPTIONS,
                    onSelect = {
                        state.historySortOrder = it
                        state.persistSettings()
                    },
                )
            }
        }

        // (4) TRANSLATION ──────────────────────────────────────────────────────────────────
        if (key == "translation") SettingsSection(eyebrow = "OCR / MT", title = "Translation", icon = Icons.Rounded.Translate) {
            // Translation enabled
            SettingsToggle("Enable In-Reader Translation", state.translateEnabled) {
                state.translateEnabled = it
            }
            HairlineDivider()
            // Instant translate on chapter open
            SettingsToggle("Instant Translate on Chapter Open", state.instantTranslate) {
                state.instantTranslate = it
                state.persistSettings()
            }
            HairlineDivider()
            // Target language dropdown
            SettingsRow("Target Language") {
                NyoraDropdown(
                    selected = state.translateTarget,
                    options  = TARGET_LANG_OPTIONS,
                    onSelect = { state.changeTranslateTarget(it) },
                )
            }
            HairlineDivider()
            // OCR source language
            SettingsRow("OCR Source Language") {
                NyoraDropdown(
                    selected = state.translateLangs,
                    options  = OCR_LANG_OPTIONS,
                    onSelect = { state.changeTranslateLangs(it) },
                )
            }
            HairlineDivider()
            // Response size
            SliderRow(
                label = "Response Size",
                value = state.translationResponseScale,
                valueText = "${(state.translationResponseScale * 100).toInt()}%",
                range = 0.75f..1.6f,
                steps = 16,
                onChange = { state.translationResponseScale = it },
                onCommit = { state.persistSettings() },
            )
            HairlineDivider()
            // Speed vs quality
            SettingsRow("Speed vs Quality") {
                NyoraDropdown(state.translationTier, TRANSLATION_TIER_OPTIONS) {
                    state.translationTier = it; state.persistSettings()
                }
            }
            HairlineDivider()
            // Debug HUD
            SettingsToggle("Translation debug overlay", state.debugHud) {
                state.debugHud = it; state.persistSettings()
            }
            HairlineDivider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Text(
                    "Install Tesseract + language data to enable OCR. The binary must be on your PATH.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NyoraTokens.onSurfaceMuted,
                )
            }
        }

        // (4b) NOTIFICATIONS ────────────────────────────────────────────────────────────────
        if (key == "notifications") SettingsSection(eyebrow = "Alerts", title = "Notifications", icon = Icons.Rounded.Notifications) {
            SettingsToggle("New Chapter Notifications", state.newChapterNotifications) {
                state.newChapterNotifications = it; state.persistSettings()
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp)) {
                Text(
                    "Banner when a tracked manga has new chapters.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NyoraTokens.onSurfaceMuted,
                )
            }
        }

        // (4c) DOWNLOADS ────────────────────────────────────────────────────────────────────
        if (key == "downloads") SettingsSection(eyebrow = "Offline", title = "Downloads", icon = Icons.Rounded.Download) {
            SliderRow(
                label = "Max Concurrent Downloads",
                value = state.maxConcurrentDownloads.toFloat(),
                valueText = "${state.maxConcurrentDownloads}",
                range = 1f..8f,
                steps = 6,
                onChange = { state.maxConcurrentDownloads = it.toInt() },
                onCommit = { state.persistSettings() },
            )
            HairlineDivider()
            SettingsRow("Save Chapters As") {
                NyoraDropdown(state.downloadFormat, DOWNLOAD_FORMAT_OPTIONS) {
                    state.downloadFormat = it; state.persistSettings()
                }
            }
        }

        // (4d) CLOUD SYNC ─────────────────────────────────────────────────────────────────
        if (key == "sync") CloudSyncSection(state)

        // (4e) PARSER UPDATES ─────────────────────────────────────────────────────────────
        if (key == "parsers") ParserUpdatesSection(state)

        // (5) PRIVACY ──────────────────────────────────────────────────────────────────────
        if (key == "privacy") SettingsSection(eyebrow = "Security", title = "Privacy", icon = Icons.Rounded.Lock) {
            // Incognito (side-effect setter)
            SettingsToggle("Incognito Mode", state.incognito) {
                state.setIncognito(it)
            }
            HairlineDivider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp),
            ) {
                Text(
                    "Reading is not recorded to history while incognito.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NyoraTokens.onSurfaceMuted,
                )
            }
            HairlineDivider()
            // Confirm before quitting
            SettingsToggle("Confirm Before Quitting", state.confirmBeforeQuit) {
                state.confirmBeforeQuit = it
                state.persistSettings()
            }
        }

        // (7) ABOUT ────────────────────────────────────────────────────────────────────────
        if (key == "about") SettingsSection(eyebrow = "Info", title = "About", icon = Icons.Rounded.AutoStories) {
            SettingsRow("Build") {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Nyora • Linux",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NyoraTokens.onSurfaceHigh,
                    )
                    Text(
                        "v$VERSION",
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalNyoraAccent.current.color,
                    )
                }
            }
            HairlineDivider()
            SettingsRow("Website") {
                Text(
                    "nyora.pages.dev",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalNyoraAccent.current.color,
                    modifier = Modifier.clickable { state.openExternalUrl("https://nyora.pages.dev") },
                )
            }
            HairlineDivider()
            SettingsRow("Source code") {
                Text(
                    "Hasan72341/nyora-linux",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalNyoraAccent.current.color,
                    modifier = Modifier.clickable { state.openExternalUrl("https://github.com/Hasan72341/nyora-linux") },
                )
            }
            HairlineDivider()
            SettingsRow("Platform") {
                Text(
                    "${System.getProperty("os.name")} (${System.getProperty("os.arch")})",
                    style = MaterialTheme.typography.labelSmall,
                    color = NyoraTokens.onSurfaceMuted,
                )
            }
            HairlineDivider()
            SettingsRow("OCR Engine") {
                Text("Tesseract", style = MaterialTheme.typography.labelSmall, color = NyoraTokens.onSurfaceMuted)
            }
            HairlineDivider()
            SettingsRow("Translation") {
                Text("Google Translate", style = MaterialTheme.typography.labelSmall, color = NyoraTokens.onSurfaceMuted)
            }
            HairlineDivider()
            SettingsRow("Database") {
                Text(
                    text = SqlDelightLibraryRepository.defaultDatabasePath().toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = NyoraTokens.onSurfaceFaint,
                )
            }
            HairlineDivider()
            SettingsRow("Developer") {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Md Hasan Raza",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NyoraTokens.onSurfaceHigh,
                    )
                    Text(
                        "Creator of Nyora",
                        style = MaterialTheme.typography.labelSmall,
                        color = NyoraTokens.onSurfaceMuted,
                    )
                }
            }
            HairlineDivider()
            SettingsRow("Instagram") {
                Text(
                    "@md_hasan_raza____",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalNyoraAccent.current.color,
                    modifier = Modifier.clickable { state.openExternalUrl("https://www.instagram.com/md_hasan_raza____?igsh=MXZ6eTk2Y3FsNGs3aQ==") },
                )
            }
            HairlineDivider()
            SettingsRow("LinkedIn") {
                Text(
                    "md-hasan-raza",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalNyoraAccent.current.color,
                    modifier = Modifier.clickable { state.openExternalUrl("https://www.linkedin.com/in/md-hasan-raza-8817372a7/") },
                )
            }
            HairlineDivider()
            SettingsRow("GitHub") {
                Text(
                    "Hasan72341",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalNyoraAccent.current.color,
                    modifier = Modifier.clickable { state.openExternalUrl("https://github.com/Hasan72341") },
                )
            }
            HairlineDivider()
            SettingsRow("Email") {
                Text(
                    "hasanraza96@outlook.com",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalNyoraAccent.current.color,
                    modifier = Modifier.clickable { state.openExternalUrl("mailto:hasanraza96@outlook.com") },
                )
            }
            HairlineDivider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp),
            ) {
                Text(
                    "Nyora — your manga library, everywhere. Available on Android, Windows, macOS, Linux, iOS and the web.",
                    style = MaterialTheme.typography.labelSmall,
                    color = NyoraTokens.onSurfaceMuted,
                )
            }
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun CloudSyncSection(state: AppState) {
    LaunchedEffect(Unit) { state.refreshCloudSyncStatus() }
    val status = state.cloudSyncStatus
    val busy = state.cloudSyncBusy
    val statusText = when {
        status == null -> "Checking..."
        !status.isConfigured -> "Not configured"
        status.isAuthenticated -> "Signed in"
        else -> "Signed out"
    }
    SettingsSection(eyebrow = "Cloud", title = "Nyora Sync", icon = Icons.Rounded.Cloud) {
        SettingsRow("Status") {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    statusText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (status?.isAuthenticated == true) LocalNyoraAccent.current.color else NyoraTokens.onSurfaceMuted,
                )
                val subtitle = status?.email?.takeIf { it.isNotBlank() }
                    ?: status?.userId?.takeIf { it.isNotBlank() }?.take(8)
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = NyoraTokens.onSurfaceFaint,
                    )
                }
            }
        }
        HairlineDivider()
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (status?.isAuthenticated == true) {
                Button(onClick = { state.cloudSyncNow() }, enabled = !busy) {
                    Text(if (busy) "Working..." else "Sync Now")
                }
                OutlinedButton(onClick = { state.cloudRestoreFromCloud() }, enabled = !busy) {
                    Text("Restore From Cloud")
                }
                TextButton(onClick = { state.cloudSignOut() }, enabled = !busy) {
                    Text("Sign Out")
                }
            } else {
                Button(onClick = { state.cloudSignInWithGoogle() }, enabled = !busy && status?.isConfigured != false) {
                    if (!busy) {
                        Image(GoogleLogo, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (busy) "Opening..." else "Sign in with Google")
                }
            }
        }
    }
}

@Composable
private fun ParserUpdatesSection(state: AppState) {
    LaunchedEffect(Unit) { state.refreshOtaStatus() }
    val status = state.otaStatus
    val busy = state.otaBusy
    val statusText = when {
        status == null -> "Checking..."
        status.isActive -> "OTA v${status.otaVersion} active"
        else -> "Bundled v${status.bundledVersion}"
    }
    SettingsSection(eyebrow = "Parsers", title = "Parser Updates", icon = Icons.Rounded.Update) {
        SettingsRow("Version") {
            Text(
                statusText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (status?.isActive == true) LocalNyoraAccent.current.color else NyoraTokens.onSurfaceMuted,
            )
        }
        HairlineDivider()
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { state.otaCheckNow() }, enabled = !busy) {
                Text(if (busy) "Checking..." else "Check for Updates")
            }
            if (status?.isActive == true) {
                Text(
                    "Restart the app to apply newly downloaded parsers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NyoraTokens.onSurfaceMuted,
                )
            }
        }
    }
}

// ── Section shell ─────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    eyebrow: String,
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = LocalNyoraAccent.current.color,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column {
                SystemTag(text = eyebrow)
                Spacer(Modifier.height(3.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = NyoraTokens.onSurfaceHigh,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(shape = RoundedCornerShape(24.dp), fill = NyoraTokens.surface1)
                .padding(vertical = 4.dp),
            content = content,
        )
    }
}

// ── Generic rows ──────────────────────────────────────────────────────────────────

@Composable
private fun SettingsRow(label: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = NyoraTokens.onSurfaceHigh,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(Modifier.width(16.dp))
        trailing()
    }
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    SettingsRow(label) {
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = LocalNyoraAccent.current.color,
                uncheckedThumbColor = NyoraTokens.onSurfaceMuted,
                uncheckedTrackColor = NyoraTokens.surface1,
                uncheckedBorderColor = NyoraTokens.hairlineStrong,
            ),
        )
    }
}

@Composable
private fun NavRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NyoraTokens.surface1),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = LocalNyoraAccent.current.color,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = NyoraTokens.onSurfaceHigh,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = NyoraTokens.onSurfaceMuted,
            )
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = NyoraTokens.onSurfaceFaint,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun HairlineDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 18.dp),
        color = NyoraTokens.hairlineFaint,
    )
}

/** A labelled slider row with a live accent-colored value readout. */
@Composable
private fun SliderRow(
    label: String,
    value: Float,
    valueText: String,
    range: ClosedRange<Float>,
    steps: Int,
    onChange: (Float) -> Unit,
    onCommit: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = NyoraTokens.onSurfaceHigh)
            Text(
                valueText,
                style = MaterialTheme.typography.labelLarge,
                color = LocalNyoraAccent.current.color,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(6.dp))
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            onValueChangeFinished = onCommit,
            valueRange = range.start..range.endInclusive,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor         = LocalNyoraAccent.current.color,
                activeTrackColor   = LocalNyoraAccent.current.color,
                inactiveTrackColor = NyoraTokens.surface2,
            ),
        )
    }
}

// ── Appearance segmented control (Amoled / Light) ──────────────────────────────────
//
// Uses accentGradientSubtle() as the fill of the selected segment, matching the
// design-language rule: "accentGradientSubtle on selected chips/segmented fills".

@Composable
private fun AppearanceSegmented(
    selected: AppearanceMode,
    onSelect: (AppearanceMode) -> Unit,
) {
    val subtleBrush = accentGradientSubtle()
    val accent      = LocalNyoraAccent.current.color
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NyoraTokens.surface1)
            .border(width = 1.dp, color = NyoraTokens.hairlineFaint, shape = RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AppearanceMode.entries.forEach { mode ->
            val isSelected = mode == selected
            val label = when (mode) {
                AppearanceMode.AMOLED -> "Amoled"
                AppearanceMode.LIGHT  -> "Light"
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .then(
                        if (isSelected) Modifier.background(subtleBrush)
                        else Modifier
                    )
                    .then(
                        if (isSelected) Modifier.border(
                            width = 1.dp,
                            color = accent.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(11.dp),
                        ) else Modifier
                    )
                    .clickable { onSelect(mode) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color      = if (isSelected) accent else NyoraTokens.onSurfaceMuted,
                )
            }
        }
    }
}

// ── Three-option segmented control (e.g. reader background) ───────────────────────

@Composable
private fun ThreeSegmented(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    val subtleBrush = accentGradientSubtle()
    val accent      = LocalNyoraAccent.current.color
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NyoraTokens.surface1)
            .border(width = 1.dp, color = NyoraTokens.hairlineFaint, shape = RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .then(
                        if (isSelected) Modifier.background(subtleBrush)
                        else Modifier
                    )
                    .then(
                        if (isSelected) Modifier.border(
                            width = 1.dp,
                            color = accent.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(11.dp),
                        ) else Modifier
                    )
                    .clickable { onSelect(value) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color      = if (isSelected) accent else NyoraTokens.onSurfaceMuted,
                )
            }
        }
    }
}

// ── Nyora-styled dropdown ─────────────────────────────────────────────────────────
//
// A chip-style trigger that opens a DropdownMenu. The selected item label is shown
// in the chip; options are pairs of (value, displayLabel). Works for any <T> by
// using an index-matched approach so the generic stays clean.

@Composable
private fun <T> NyoraDropdown(
    selected: T,
    options: List<Pair<T, String>>,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = options.firstOrNull { it.first == selected }?.second ?: selected.toString()
    val accent = LocalNyoraAccent.current.color

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(NyoraTokens.surface2)
                .border(width = 1.dp, color = NyoraTokens.hairlineStrong, shape = RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                currentLabel,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color      = NyoraTokens.onSurfaceHigh,
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp),
            )
        }

        DropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (value, label) ->
                val isSelected = value == selected
                DropdownMenuItem(
                    text = {
                        Text(
                            label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color      = if (isSelected) accent else NyoraTokens.onSurfaceHigh,
                        )
                    },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

// ── Accent swatch row ───────────────────────────────────────────────────────────────

@Composable
private fun AccentSwatchRow(
    selected: Accent,
    onSelect: (Accent) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Accent.entries.forEach { accent ->
            AccentSwatch(
                accent     = accent,
                isSelected = accent == selected,
                onClick    = { onSelect(accent) },
            )
        }
    }
}

@Composable
private fun AccentSwatch(
    accent: Accent,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val size by androidx.compose.animation.core.animateDpAsState(
        targetValue    = if (isSelected) 38.dp else 32.dp,
        animationSpec  = androidx.compose.animation.core.spring(dampingRatio = 0.7f, stiffness = 300f),
        label          = "swatchSize",
    )
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(accent.color)
                    .glowBorder(color = accent.color, shape = CircleShape),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(accent.color)
                    .border(
                        width = 1.dp,
                        color = NyoraTokens.hairlineStrong,
                        shape = CircleShape,
                    ),
            )
        }
    }
}
