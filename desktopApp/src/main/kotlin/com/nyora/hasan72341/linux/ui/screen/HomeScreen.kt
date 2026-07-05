package com.nyora.linux.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyora.linux.AppState
import com.nyora.linux.bridge.AniListFeedMedia
import com.nyora.linux.ui.theme.*
import com.nyora.hasan72341.shared.repository.HistoryRow

@Composable
fun HomeScreen(state: AppState) {
    LaunchedEffect(Unit) {
        state.refreshLibrary()
        state.loadAnilistFeed()
    }

    val history = state.history.take(4)
    val suggestions = state.anilistFeed

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        // --- Header ---
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(
                title = "Welcome Back",
                subtitle = "Pick up where you left off",
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // --- Recently Read (History) ---
        if (history.isNotEmpty()) {
            items(history, key = { "hist-${it.manga.id}" }) { row ->
                HomeHistoryCard(row, state)
            }
        } else {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .glassCard(shape = RoundedCornerShape(16.dp), fill = NyoraTokens.surface1),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recent history. Start reading to see it here!", color = NyoraTokens.onSurfaceMuted)
                }
            }
        }

        // --- Suggestions Header ---
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(16.dp))
            SectionHeader(
                title = "Discover",
                subtitle = "Popular on MangaBaka",
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // --- Suggestions Grid ---
        if (state.anilistFeedLoading && suggestions.isEmpty()) {
            items(6) {
                Box(Modifier.aspectRatio(0.7f).shimmerPlaceholder(RoundedCornerShape(22.dp)))
            }
        } else {
            itemsIndexed(suggestions, key = { idx, m -> "sug-${m.id}-$idx" }) { index, media ->
                val onClick = {
                    state.globalSearch(state.anilistFeedTitle(media))
                    state.showGlobalSearch = true
                }
                SuggestionCard(
                    media = media,
                    title = state.anilistFeedTitle(media),
                    heavy = false,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun HomeHistoryCard(row: HistoryRow, state: AppState) {
    val cardShape = RoundedCornerShape(22.dp)
    val accent = LocalNyoraAccent.current.color

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .hoverLift(shape = cardShape, glowColor = accent)
            .glassCard(shape = cardShape, fill = NyoraTokens.surface1)
            .clickable { state.openDetails(row.manga) },
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            AnimeAsyncImage(
                model = state.coverProxyUrl(row.manga.coverUrl, state.sourceFor(row.manga)),
                contentDescription = row.manga.title,
                modifier = Modifier.fillMaxHeight().width(60.dp),
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Text(
                    text = row.manga.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NyoraTokens.onSurfaceHigh,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                SystemTag(text = row.chapterTitle, color = accent)
            }
        }
        // Progress bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(row.percent.coerceIn(0.01f, 1f))
                .height(3.dp)
                .background(accent),
        )
    }
}

// Re-using SuggestionCard from SuggestionsScreen logic but since it is private there, 
// I will redefine a minimal version here or just use a basic one.
@Composable
private fun SuggestionCard(
    media: AniListFeedMedia,
    title: String,
    heavy: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .hoverLift(shape = shape)
            .clip(shape)
            .clickable { onClick() }
    ) {
        AnimeAsyncImage(
            model = media.coverImage.extraLarge ?: media.coverImage.large,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .background(NyoraTokens.bg.copy(alpha = 0.7f)),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = NyoraTokens.onSurfaceHigh,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
        )
    }
}
