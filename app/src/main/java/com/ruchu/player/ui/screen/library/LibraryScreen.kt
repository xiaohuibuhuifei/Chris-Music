package com.ruchu.player.ui.screen.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ruchu.player.data.model.Song
import com.ruchu.player.ui.components.AppTopBar
import com.ruchu.player.ui.components.MiniPlayer
import com.ruchu.player.ui.components.SongListActionRow
import com.ruchu.player.ui.components.SongListPane
import com.ruchu.player.ui.theme.RuChuTheme
import com.ruchu.player.util.PlaybackManager

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
    val isSearching = uiState.searchQuery.isNotBlank()

    val onSongClick: (String) -> Unit = remember(playbackManager, songs, songsById, onNavigateToPlayer, isSearching) {
        { songId: String ->
            if (isSearching) {
                viewModel.playFromSearch(songId)
            } else {
                songsById[songId]?.let { song ->
                    playbackManager.playSong(song, songs)
                }
            }
            onNavigateToPlayer()
            Unit
        }
    }

    val subtitle = if (isSearching) "找到 ${songs.size} 首" else "共 ${songs.size} 首"
    val tokens = RuChuTheme.tokens

    Scaffold(
        topBar = {
            AppTopBar(
                title = "全部歌曲",
                subtitle = subtitle,
                includeStatusBarPadding = true,
                onBack = onNavigateBack
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
                        .padding(horizontal = tokens.spacing.md, vertical = tokens.spacing.xs)
                )

                SongSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.spacing.md, vertical = tokens.spacing.xs)
                )

                Spacer(modifier = Modifier.height(tokens.spacing.xxs))

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
private fun SongSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = RuChuTheme.tokens
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("搜索歌曲或歌词") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "清除")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(tokens.radius.lg),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    )
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
