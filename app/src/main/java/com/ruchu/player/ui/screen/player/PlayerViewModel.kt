package com.ruchu.player.ui.screen.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ruchu.player.data.model.LyricLine
import com.ruchu.player.data.repository.LyricRepository
import com.ruchu.player.util.PlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    val playbackManager = PlaybackManager.getInstance(application)
    private val lyricRepo = LyricRepository(application)

    private val _lyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyrics: StateFlow<List<LyricLine>> = _lyrics.asStateFlow()

    init {
        viewModelScope.launch {
            playbackManager.currentSong.collect { song ->
                song?.let {
                    _lyrics.value = lyricRepo.getLyrics(it)
                }
            }
        }
    }
}
