package com.ruchu.player.ui.screen.album

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ruchu.player.data.model.Song
import com.ruchu.player.ui.components.AssetImage
import com.ruchu.player.ui.components.MiniPlayer
import com.ruchu.player.ui.components.SongListActionRow
import com.ruchu.player.ui.components.SongListPane
import com.ruchu.player.util.PlaybackManager

@Composable
fun AlbumDetailScreen(
    albumId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    viewModel: AlbumDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playbackManager = viewModel.playbackManager
    val currentSongId = playbackManager.currentSong.collectAsState().value?.id
    val songs = uiState.songs
    val songsById = remember(songs) {
        songs.associateBy(Song::id)
    }
    val onSongClick: (String) -> Unit = remember(playbackManager, songs, songsById, onNavigateToPlayer) {
        { songId: String ->
            songsById[songId]?.let { song ->
                playbackManager.playSong(song, songs)
                onNavigateToPlayer()
            }
            Unit
        }
    }

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
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = album?.title ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(0.65f)
                        )
                        if (album != null) {
                            Text(
                                text = if (album.year != null) "${album.year} · ${album.songs.size} 首" else "${album.songs.size} 首",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(0.65f)
                            )
                        }
                    }
                }

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

                    SongListActionRow(
                        onPlayAll = {
                            playbackManager.playQueue(songs, shuffle = false)
                            onNavigateToPlayer()
                        },
                        onShuffleAll = {
                            playbackManager.playQueue(songs, shuffle = true)
                            onNavigateToPlayer()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                SongListPane(
                    rows = uiState.rows,
                    isLoading = uiState.isLoading,
                    currentSongId = currentSongId,
                    isMiniPlayerVisible = currentSongId != null,
                    onSongClick = onSongClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // MiniPlayer at bottom - isolated to avoid progress updates recomposing the list
            AlbumMiniPlayer(
                playbackManager = playbackManager,
                onClick = onNavigateToPlayer,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun AlbumMiniPlayer(
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
