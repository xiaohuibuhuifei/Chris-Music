package com.ruchu.player.ui.screen.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchu.player.ui.components.GlowingActionLabel
import com.ruchu.player.ui.components.MiniPlayer
import com.ruchu.player.ui.components.SongListItem
import com.ruchu.player.ui.theme.Primary
import com.ruchu.player.util.PlaybackManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    viewModel: LibraryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playbackManager = viewModel.playbackManager
    val currentSong by playbackManager.currentSong.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("全部歌曲") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            playbackManager.playQueue(uiState.songs, shuffle = false)
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
                            playbackManager.playQueue(uiState.songs, shuffle = true)
                            onNavigateToPlayer()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
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

                Spacer(modifier = Modifier.height(4.dp))

                // Song list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = if (currentSong != null) 72.dp else 16.dp)
                ) {
                    items(uiState.songs, key = { it.id }) { song ->
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

            // MiniPlayer at bottom - isolated to avoid progress updates recomposing the song list
            LibraryMiniPlayer(
                playbackManager = playbackManager,
                onClick = onNavigateToPlayer,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun LibraryMiniPlayer(
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
            onClick = onClick,
            modifier = modifier
        )
    }
}