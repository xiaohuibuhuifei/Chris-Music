package com.ruchu.player.ui.screen.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ruchu.player.data.model.Song
import com.ruchu.player.data.repository.LyricRepository
import com.ruchu.player.data.repository.MusicRepository
import com.ruchu.player.ui.model.SongListRowModel
import com.ruchu.player.ui.model.toSongListRowModels
import com.ruchu.player.util.PlaybackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

data class LibraryUiState(
    val songs: List<Song> = emptyList(),
    val rows: List<SongListRowModel> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = ""
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val musicRepo = MusicRepository(application)
    private val lyricRepo = LyricRepository(application)
    val playbackManager = PlaybackManager.getInstance(application)

    private var allSongs: List<Song> = emptyList()
    private val lyricsCache = ConcurrentHashMap<String, String>()

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            musicRepo.loadMusic()
            allSongs = musicRepo.getAllSongs()
            _uiState.value = buildUiState(allSongs, "")

            launch(Dispatchers.IO) {
                allSongs.forEach { song ->
                    if (song.lyricsFile.isNotBlank()) {
                        val lyrics = lyricRepo.getLyrics(song)
                        if (lyrics.isNotEmpty()) {
                            lyricsCache[song.id] = lyrics.joinToString("\n") { it.text }
                        }
                    }
                }
                // Re-apply active search with complete lyrics
                val query = _uiState.value.searchQuery
                if (query.isNotBlank()) {
                    onSearchQueryChanged(query)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val filtered = if (query.isBlank()) {
            allSongs
        } else {
            allSongs.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                    lyricsCache[song.id]?.contains(query, ignoreCase = true) == true
            }
        }
        _uiState.value = buildUiState(filtered, query)
    }

    fun playFromSearch(songId: String) {
        val songs = _uiState.value.songs
        val song = songs.find { it.id == songId } ?: return
        val startIndex = songs.indexOf(song).coerceAtLeast(0)
        playbackManager.playQueue(songs, startIndex = startIndex, shuffle = false)
        playbackManager.setRepeatMode(1) // 全曲循环
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

    private fun buildUiState(songs: List<Song>, searchQuery: String): LibraryUiState {
        return LibraryUiState(
            songs = songs,
            rows = songs.toSongListRowModels(),
            isLoading = false,
            searchQuery = searchQuery
        )
    }
}
