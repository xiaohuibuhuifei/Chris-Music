package com.ruchu.player.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchu.player.ui.model.SongListRowModel

private val FooterSpacing = 72.dp

@Composable
fun SongListPane(
    rows: List<SongListRowModel>,
    isLoading: Boolean,
    currentSongId: String?,
    isMiniPlayerVisible: Boolean,
    onSongClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    headerContent: LazyListScope.() -> Unit = {}
) {
    val listState = rememberLazyListState()
    var hasScrolledToTop by remember { mutableStateOf(false) }

    // 仅在首次数据到达时滚动到顶部，避免与用户滚动冲突
    LaunchedEffect(rows.isNotEmpty()) {
        if (rows.isNotEmpty() && !hasScrolledToTop) {
            hasScrolledToTop = true
            listState.scrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        headerContent()

        if (isLoading && rows.isEmpty()) {
            item(
                key = "song_list_loading",
                contentType = "song_list_loading"
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            items(
                items = rows,
                key = { it.id },
                contentType = { "song_row" }
            ) { row ->
                SongListRowItem(
                    row = row,
                    isPlaying = currentSongId == row.id,
                    onClick = onSongClick
                )
            }
        }

        item(
            key = "song_list_footer",
            contentType = "song_list_footer"
        ) {
            Spacer(modifier = Modifier.height(FooterSpacing))
        }
    }
}

@Composable
fun SongListActionRow(
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onPlayAll,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            GlowingActionLabel(
                text = "播放全部",
                icon = Icons.Default.PlayArrow,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                glowColor = MaterialTheme.colorScheme.onPrimary,
                textSize = 13.sp
            )
        }
        OutlinedButton(
            onClick = onShuffleAll,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            GlowingActionLabel(
                text = "随机播放",
                icon = Icons.Default.Shuffle,
                contentColor = MaterialTheme.colorScheme.primary,
                glowColor = MaterialTheme.colorScheme.primary,
                textSize = 13.sp
            )
        }
    }
}

@Composable
private fun SongListRowItem(
    row: SongListRowModel,
    isPlaying: Boolean,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = row.isPlayable,
                indication = null,
                interactionSource = null
            ) { onClick(row.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = row.durationText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier
                .width(48.dp)
                .padding(start = 12.dp)
        )
    }
}
