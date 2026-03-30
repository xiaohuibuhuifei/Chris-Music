package com.ruchu.player.util

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.ruchu.player.data.model.Song
import com.ruchu.player.service.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackManager private constructor() {
    private var mediaController: MediaController? = null
    private var isConnecting = false
    private var progressJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(1)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private var sourceQueue: List<Song> = emptyList()
    private var queue: List<Song> = emptyList()
    private var queueIndex = 0
    private var isSeeking = false

    fun connect(context: Context) {
        if (mediaController != null || isConnecting) return

        val appContext = context.applicationContext
        val sessionToken = SessionToken(
            appContext,
            ComponentName(appContext, MusicService::class.java)
        )
        val controllerFuture = MediaController.Builder(appContext, sessionToken).buildAsync()
        isConnecting = true

        controllerFuture.addListener({
            try {
                val controller = controllerFuture.get()
                if (mediaController == null) {
                    mediaController = controller
                    controller.addListener(playerListener)
                    restorePendingSongIfNeeded()
                    syncPlaybackUiState()
                    startProgressUpdates()
                } else {
                    controller.release()
                }
            } catch (_: Exception) {
            } finally {
                isConnecting = false
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    fun playSong(song: Song, songQueue: List<Song> = emptyList()) {
        val baseQueue = if (songQueue.isNotEmpty()) songQueue else listOf(song)
        sourceQueue = baseQueue
        queue = buildQueue(
            songs = baseQueue,
            currentSong = song,
            shuffle = _shuffleMode.value
        )
        queueIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playCurrentSong(song)
    }

    fun playQueue(songQueue: List<Song>, startIndex: Int = 0, shuffle: Boolean = _shuffleMode.value) {
        if (songQueue.isEmpty()) return

        sourceQueue = songQueue
        _shuffleMode.value = shuffle
        applyShuffleMode()

        if (shuffle) {
            queue = songQueue.shuffled()
            queueIndex = 0
        } else {
            val normalizedIndex = startIndex.coerceIn(songQueue.indices)
            queue = songQueue
            queueIndex = normalizedIndex
        }
        playCurrentSong(queue[queueIndex])
    }

    fun play() {
        _isPlaying.value = true
        mediaController?.play()
    }

    fun pause() {
        _isPlaying.value = false
        mediaController?.pause()
    }

    fun togglePlayPause() {
        if (mediaController?.playWhenReady == true || _isPlaying.value) pause() else play()
    }

    fun seekTo(position: Long) {
        isSeeking = true
        val maxPos = (_duration.value - 1000).coerceAtLeast(0)
        val clamped = position.coerceIn(0, maxPos)
        _currentPosition.value = clamped
        mediaController?.seekTo(clamped)
    }

    fun next() {
        advanceQueue(step = 1, allowWrap = _repeatMode.value == 1)
    }

    fun previous() {
        advanceQueue(step = -1, allowWrap = _repeatMode.value == 1)
    }

    fun toggleShuffle() {
        setShuffleMode(!_shuffleMode.value)
    }

    fun cycleRepeatMode() {
        _repeatMode.value = (_repeatMode.value + 1) % 3
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            syncPlaybackUiState()
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            syncPlaybackUiState()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            syncPlaybackUiState()
            if (playbackState != Player.STATE_ENDED) {
                isSeeking = false
                return
            }
            // STATE_ENDED: ignore if user is seeking (prevents accidental song skip)
            if (isSeeking) {
                isSeeking = false
                mediaController?.seekTo(0)
                mediaController?.play()
                return
            }
            when (_repeatMode.value) {
                2 -> mediaController?.seekTo(0)?.let { mediaController?.play() }
                1 -> advanceQueue(step = 1, allowWrap = true)
                else -> {
                    if (!advanceQueue(step = 1, allowWrap = false)) {
                        _isPlaying.value = false
                    }
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            next()
        }
    }

    private fun syncPlaybackUiState() {
        mediaController?.let { controller ->
            _isPlaying.value = controller.playWhenReady || controller.isPlaying
            _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
            val d = controller.duration
            if (d > 0) _duration.value = d
        }
    }

    private fun restorePendingSongIfNeeded() {
        val song = _currentSong.value ?: return
        val controller = mediaController ?: return
        if (controller.currentMediaItem == null) {
            playCurrentSong(song)
        }
    }

    private fun playCurrentSong(song: Song) {
        _currentSong.value = song
        _currentPosition.value = 0L
        _duration.value = song.duration.toLong() * 1000

        val uri = Uri.parse("asset:///${song.fileName}")
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.albumTitle)
                    .build()
            )
            .build()

        mediaController?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
        _isPlaying.value = true
    }

    private fun setShuffleMode(enabled: Boolean) {
        _shuffleMode.value = enabled
        applyShuffleMode()
        rebuildQueuePreservingCurrentSong()
    }

    private fun applyShuffleMode() {
        mediaController?.shuffleModeEnabled = _shuffleMode.value
    }

    private fun applyRepeatMode() {
        // Always OFF: let STATE_ENDED fire so custom queue handles all repeat logic
        mediaController?.repeatMode = Player.REPEAT_MODE_OFF
    }

    private fun rebuildQueuePreservingCurrentSong() {
        val current = _currentSong.value ?: return
        val baseQueue = when {
            sourceQueue.isNotEmpty() -> sourceQueue
            queue.isNotEmpty() -> queue
            else -> listOf(current)
        }
        queue = buildQueue(
            songs = baseQueue,
            currentSong = current,
            shuffle = _shuffleMode.value
        )
        queueIndex = queue.indexOfFirst { it.id == current.id }.coerceAtLeast(0)
    }

    private fun buildQueue(
        songs: List<Song>,
        currentSong: Song,
        shuffle: Boolean
    ): List<Song> {
        if (!shuffle || songs.size <= 1) return songs

        val remainingSongs = songs.filterNot { it.id == currentSong.id }
        return listOf(currentSong) + remainingSongs.shuffled()
    }

    private fun advanceQueue(step: Int, allowWrap: Boolean): Boolean {
        if (queue.isEmpty()) return false

        val targetIndex = queueIndex + step
        when {
            targetIndex in queue.indices -> {
                queueIndex = targetIndex
                playCurrentSong(queue[queueIndex])
                return true
            }

            allowWrap && queue.size > 1 -> {
                if (_shuffleMode.value && sourceQueue.size > 1) {
                    queue = sourceQueue.shuffled()
                    queueIndex = 0
                } else {
                    queueIndex = if (step > 0) 0 else queue.lastIndex
                }
                playCurrentSong(queue[queueIndex])
                return true
            }

            else -> return false
        }
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return

        progressJob = scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
                    val d = controller.duration
                    if (d > 0) _duration.value = d
                }
                delay(200)
            }
        }
    }

    fun getQueue(): List<Song> = queue
    fun getQueueIndex(): Int = queueIndex

    companion object {
        @Volatile
        private var instance: PlaybackManager? = null

        fun getInstance(context: Context): PlaybackManager {
            return instance ?: synchronized(this) {
                instance ?: PlaybackManager().also {
                    instance = it
                }
            }
        }
    }
}
