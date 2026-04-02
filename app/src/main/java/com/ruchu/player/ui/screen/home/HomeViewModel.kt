package com.ruchu.player.ui.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ruchu.player.data.model.Album
import com.ruchu.player.data.model.Quote
import com.ruchu.player.data.repository.MusicRepository
import com.ruchu.player.data.repository.QuoteRepository
import com.ruchu.player.util.PlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val quote: Quote? = null,
    val albums: List<Album> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepo = MusicRepository(application)
    private val quoteRepo = QuoteRepository()
    val playbackManager = PlaybackManager.getInstance(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            musicRepo.loadMusic()
            _uiState.value = HomeUiState(
                quote = quoteRepo.getRandomQuote(),
                albums = musicRepo.getAlbums()
            )
        }
    }

    fun playAll() {
        val songs = musicRepo.getAllSongs()
        if (songs.isNotEmpty()) {
            playbackManager.playQueue(songs, shuffle = false)
        }
    }

    fun shuffleAll() {
        val songs = musicRepo.getAllSongs()
        if (songs.isNotEmpty()) {
            playbackManager.playQueue(songs, shuffle = true)
        }
    }

    fun refreshQuote() {
        _uiState.value = _uiState.value.copy(quote = quoteRepo.getRandomQuote())
    }
}
