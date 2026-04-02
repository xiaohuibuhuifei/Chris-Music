package com.ruchu.player.ui.screen.album

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ruchu.player.data.model.Album
import com.ruchu.player.data.model.Song
import com.ruchu.player.data.repository.MusicRepository
import com.ruchu.player.ui.model.SongListRowModel
import com.ruchu.player.ui.model.toSongListRowModels
import com.ruchu.player.util.PlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlbumDetailUiState(
    val album: Album? = null,
    val songs: List<Song> = emptyList(),
    val rows: List<SongListRowModel> = emptyList(),
    val isLoading: Boolean = false
)

class AlbumDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepo = MusicRepository(application)
    val playbackManager = PlaybackManager.getInstance(application)

    private val _uiState = MutableStateFlow(AlbumDetailUiState())
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    fun loadAlbum(albumId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            musicRepo.loadMusic()
            val album = musicRepo.getAlbum(albumId)
            val songs = album?.songs ?: emptyList()
            _uiState.value = AlbumDetailUiState(
                album = album,
                songs = songs,
                rows = songs.toSongListRowModels(),
                isLoading = false
            )
        }
    }
}
