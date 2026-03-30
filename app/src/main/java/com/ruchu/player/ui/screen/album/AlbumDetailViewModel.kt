package com.ruchu.player.ui.screen.album

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ruchu.player.data.model.Album
import com.ruchu.player.data.model.Song
import com.ruchu.player.data.repository.MusicRepository
import com.ruchu.player.util.PlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlbumDetailUiState(
    val album: Album? = null,
    val songs: List<Song> = emptyList()
)

class AlbumDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepo = MusicRepository(application)
    val playbackManager = PlaybackManager.getInstance(application)

    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    init {
        playbackManager.connect(application)
    }

    fun loadAlbum(albumId: String) {
        viewModelScope.launch {
            musicRepo.loadMusic()
            val album = musicRepo.getAlbum(albumId)
            _uiState.value = AlbumDetailUiState(
                album = album,
                songs = album?.songs ?: emptyList()
            )
        }
    }
}
