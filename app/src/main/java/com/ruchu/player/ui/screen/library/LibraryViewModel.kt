package com.ruchu.player.ui.screen.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ruchu.player.data.model.Song
import com.ruchu.player.data.repository.MusicRepository
import com.ruchu.player.util.PlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val songs: List<Song> = emptyList()
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepo = MusicRepository(application)
    val playbackManager = PlaybackManager.getInstance(application)

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        playbackManager.connect(application)
        viewModelScope.launch {
            musicRepo.loadMusic()
            _uiState.value = LibraryUiState(
                songs = musicRepo.getAllSongs()
            )
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
}
