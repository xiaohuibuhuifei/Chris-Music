package com.ruchu.player.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.ruchu.player.ui.theme.Primary

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
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val position by playbackManager.currentPosition.collectAsState()
    val duration by playbackManager.duration.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = if (currentSong != null) 72.dp else 16.dp)
            ) {
                // Title: 如初 with glow
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "如初",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = CreativeFont,
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Primary.copy(alpha = 0.42f),
                                    blurRadius = 16f,
                                    offset = androidx.compose.ui.geometry.Offset.Zero
                                )
                            ),
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Quote card
                item {
                    if (uiState.quote != null) {
                        QuoteCard(
                            quoteText = uiState.quote!!.lyric,
                            quoteSource = "${uiState.quote!!.songTitle} · ${uiState.quote!!.year}",
                            onClick = {},
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
                                contentColor = Primary
                            )
                        ) {
                            GlowingActionLabel(
                                text = "全部歌曲",
                                icon = Icons.Default.LibraryMusic,
                                contentColor = Primary,
                                glowColor = Primary,
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
                                containerColor = Primary,
                                contentColor = Color.Black
                            )
                        ) {
                            GlowingActionLabel(
                                text = "播放全部",
                                icon = Icons.Default.PlayArrow,
                                contentColor = Color.Black,
                                glowColor = Color.White,
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
                                contentColor = Primary
                            )
                        ) {
                            GlowingActionLabel(
                                text = "随机播放",
                                icon = Icons.Default.Shuffle,
                                contentColor = Primary,
                                glowColor = Primary,
                                textSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Album grid (2 columns)
                val albums = uiState.albums
                items(albums.chunked(2).size) { rowIndex ->
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
                                onClick = { onNavigateToAlbum(album.id) },
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

            // MiniPlayer at bottom
            if (currentSong != null) {
                MiniPlayer(
                    song = currentSong,
                    isPlaying = isPlaying,
                    progress = if (duration > 0) position.toFloat() / duration else 0f,
                    onTogglePlay = { playbackManager.togglePlayPause() },
                    onClick = onNavigateToPlayer,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
