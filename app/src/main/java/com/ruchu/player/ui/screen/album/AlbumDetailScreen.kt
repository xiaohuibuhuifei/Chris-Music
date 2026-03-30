package com.ruchu.player.ui.screen.album

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchu.player.ui.components.AssetImage
import com.ruchu.player.ui.components.GlowingActionLabel
import com.ruchu.player.ui.components.MiniPlayer
import com.ruchu.player.ui.components.SongListItem
import com.ruchu.player.ui.theme.OnSurfaceVariant
import com.ruchu.player.ui.theme.Primary

@Composable
fun AlbumDetailScreen(
    albumId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    viewModel: AlbumDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playbackManager = viewModel.playbackManager
    val currentSong by playbackManager.currentSong.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val position by playbackManager.currentPosition.collectAsState()
    val duration by playbackManager.duration.collectAsState()

    // Load album data
    androidx.compose.runtime.LaunchedEffect(albumId) {
        viewModel.loadAlbum(albumId)
    }

    val album = uiState.album

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Top bar - same style as PlayerScreen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = album?.title ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(0.65f)
                        )
                        if (album != null) {
                            Text(
                                text = "${album.year} · ${album.songs.size} 首",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(0.65f)
                            )
                        }
                    }
                }

                // Album art header
                if (album != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AssetImage(
                            assetPath = album.artwork,
                            contentDescription = album.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                playbackManager.playQueue(album.songs, shuffle = false)
                                onNavigateToPlayer()
                            },
                            modifier = Modifier.weight(1f),
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
                                textSize = 13.sp
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                playbackManager.playQueue(album.songs, shuffle = true)
                                onNavigateToPlayer()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            GlowingActionLabel(
                                text = "随机播放",
                                icon = Icons.Default.Shuffle,
                                contentColor = Primary,
                                glowColor = Primary,
                                textSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Song list
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.songs) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = currentSong?.id == song.id,
                            onClick = {
                                playbackManager.playSong(song, uiState.songs)
                                onNavigateToPlayer()
                            }
                        )
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
