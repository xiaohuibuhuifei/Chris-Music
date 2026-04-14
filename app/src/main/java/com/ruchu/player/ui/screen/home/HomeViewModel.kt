package com.ruchu.player.ui.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ruchu.player.data.model.Album
import com.ruchu.player.data.model.Quote
import com.ruchu.player.data.model.UpdateInfo
import com.ruchu.player.data.model.UpdateState
import com.ruchu.player.data.repository.MusicRepository
import com.ruchu.player.data.repository.QuoteRepository
import com.ruchu.player.data.repository.UpdateRepository
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

    private val updateRepo = UpdateRepository(application)
    val updateState: StateFlow<UpdateState> = updateRepo.updateState

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

        // 检查更新
        viewModelScope.launch {
            val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
            val currentVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
            updateRepo.checkForUpdate(currentVersionCode)
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

    fun startUpdate(info: UpdateInfo) {
        viewModelScope.launch {
            updateRepo.downloadAndInstall(info)
        }
    }

    fun dismissUpdate() {
        updateRepo.dismiss()
    }
}
