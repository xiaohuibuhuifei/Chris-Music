package com.ruchu.player.ui.screen.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruchu.player.data.model.Song
import com.ruchu.player.ui.components.MiniPlayer
import com.ruchu.player.ui.components.SongListActionRow
import com.ruchu.player.ui.components.SongListPane
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
                SongListActionRow(
                    onPlayAll = {
                        viewModel.playAll()
                        onNavigateToPlayer()
                    },
                    onShuffleAll = {
                        viewModel.shuffleAll()
                        onNavigateToPlayer()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                SongListPane(
                    rows = uiState.rows,
                    isLoading = uiState.isLoading,
                    currentSongId = currentSongId,
                    isMiniPlayerVisible = currentSongId != null,
                    onSongClick = onSongClick,
                    modifier = Modifier.fillMaxSize()
                )
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
            onPrevious = { playbackManager.previous() },
            onNext = { playbackManager.next() },
            onClick = onClick,
            modifier = modifier
        )
    }
}
