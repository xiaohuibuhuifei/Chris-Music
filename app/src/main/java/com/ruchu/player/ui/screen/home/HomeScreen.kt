package com.ruchu.player.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchu.player.ui.components.AlbumCard
import com.ruchu.player.ui.components.GlowingActionLabel
import com.ruchu.player.ui.components.MiniPlayer
import com.ruchu.player.ui.components.QuoteCard
import com.ruchu.player.ui.theme.CreativeFont
import com.ruchu.player.util.PlaybackManager

@Composable
fun HomeScreen(
    onNavigateToLibrary: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playbackManager = viewModel.playbackManager
    val currentSong by playbackManager.currentSong.collectAsState()
    val sleepTimerRemaining by playbackManager.sleepTimerRemaining.collectAsState()
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = if (currentSong != null) 72.dp else 16.dp)
            ) {
                // Title: 如初 with glow + sleep timer button
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.size(56.dp))
                        Text(
                            text = "如·初",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = CreativeFont,
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.42f),
                                    blurRadius = 16f,
                                    offset = androidx.compose.ui.geometry.Offset.Zero
                                )
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            onClick = { showSleepTimerDialog = true },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = "定时关闭",
                                tint = if (sleepTimerRemaining != 0L) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Quote card
                item {
                    if (uiState.quote != null) {
                        QuoteCard(
                            quoteText = uiState.quote!!.lyric,
                            quoteSource = "${uiState.quote!!.songTitle} · ${uiState.quote!!.year}",
                            onClick = { viewModel.refreshQuote() },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Action buttons
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToLibrary,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            GlowingActionLabel(
                                text = "全部歌曲",
                                icon = Icons.Default.LibraryMusic,
                                contentColor = MaterialTheme.colorScheme.primary,
                                glowColor = MaterialTheme.colorScheme.primary,
                                textSize = 12.sp
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.playAll()
                                onNavigateToPlayer()
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
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
                                textSize = 12.sp
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.shuffleAll()
                                onNavigateToPlayer()
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            GlowingActionLabel(
                                text = "随机播放",
                                icon = Icons.Default.Shuffle,
                                contentColor = MaterialTheme.colorScheme.primary,
                                glowColor = MaterialTheme.colorScheme.primary,
                                textSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Album grid (2 columns)
                val albums = uiState.albums
                items(
                    count = albums.chunked(2).size,
                    key = { rowIndex ->
                        albums.chunked(2)[rowIndex].joinToString(",") { it.id }
                    },
                    contentType = { "album_row" }
                ) { rowIndex ->
                    val rowAlbums = albums.chunked(2)[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowAlbums.forEach { album ->
                            AlbumCard(
                                album = album,
                                onClick = remember(album.id) {
                                    { onNavigateToAlbum(album.id) }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // If odd number, fill remaining space with empty spacer
                        if (rowAlbums.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // MiniPlayer at bottom - isolated to avoid progress updates recomposing the list
            HomeMiniPlayer(
                playbackManager = playbackManager,
                onClick = onNavigateToPlayer,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            remaining = sleepTimerRemaining,
            onSelectDuration = { millis ->
                playbackManager.startSleepTimer(millis)
                showSleepTimerDialog = false
            },
            onCancel = {
                playbackManager.cancelSleepTimer()
                showSleepTimerDialog = false
            },
            onDismiss = { showSleepTimerDialog = false }
        )
    }
}

@Composable
private fun HomeMiniPlayer(
    playbackManager: PlaybackManager,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by playbackManager.currentSong.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val position by playbackManager.currentPosition.collectAsState()
    val duration by playbackManager.duration.collectAsState()

    if (currentSong != null) {
        MiniPlayer(
            song = currentSong,
            isPlaying = isPlaying,
            progress = if (duration > 0) position.toFloat() / duration else 0f,
            onTogglePlay = { playbackManager.togglePlayPause() },
            onPrevious = { playbackManager.previous() },
            onNext = { playbackManager.next() },
            onClick = onClick,
            modifier = modifier
        )
    }
}

@Composable
private fun SleepTimerDialog(
    remaining: Long,
    onSelectDuration: (Long) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val isTimerActive = remaining != 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("定时关闭") },
        text = {
            if (isTimerActive) {
                val totalSeconds = (remaining / 1000).coerceAtLeast(0)
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%02d:%02d".format(minutes, seconds),
                        style = MaterialTheme.typography.displayMedium.copy(
                            letterSpacing = 8.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            if (isTimerActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("关闭")
                    }
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("取消定时")
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15 to "15′", 30 to "30′", 45 to "45′").forEach { (min, label) ->
                            OutlinedButton(
                                onClick = { onSelectDuration(min * 60_000L) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    label,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(60 to "60′", 90 to "90′", 120 to "120′").forEach { (min, label) ->
                            OutlinedButton(
                                onClick = { onSelectDuration(min * 60_000L) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    label,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
