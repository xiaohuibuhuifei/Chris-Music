package com.ruchu.player.ui.screen.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ruchu.player.data.model.Song
import com.ruchu.player.data.repository.MusicRepository
import com.ruchu.player.ui.model.SongListRowModel
import com.ruchu.player.ui.model.toSongListRowModels
import com.ruchu.player.util.PlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val songs: List<Song> = emptyList(),
    val rows: List<SongListRowModel> = emptyList(),
    val isLoading: Boolean = songs.isEmpty()
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepo = MusicRepository(application)
    val playbackManager = PlaybackManager.getInstance(application)
    private val initialSongs = musicRepo.getAllSongs()

    private val _uiState = MutableStateFlow(
        buildUiState(
            songs = initialSongs,
            isLoading = initialSongs.isEmpty()
        )
    )
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            musicRepo.loadMusic()
            val loaded = musicRepo.getAllSongs()
            if (_uiState.value.songs != loaded) {
                _uiState.value = buildUiState(
                    songs = loaded,
                    isLoading = false
                )
            } else if (_uiState.value.isLoading) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun playAll() {
        val songs = _uiState.value.songs
        if (songs.isNotEmpty()) {
            playbackManager.playQueue(songs, shuffle = false)
        }
    }

    fun shuffleAll() {
        val songs = _uiState.value.songs
        if (songs.isNotEmpty()) {
            playbackManager.playQueue(songs, shuffle = true)
        }
    }

    private fun buildUiState(
        songs: List<Song>,
        isLoading: Boolean
    ): LibraryUiState {
        return LibraryUiState(
            songs = songs,
            rows = songs.toSongListRowModels(),
            isLoading = isLoading
        )
    }
}
