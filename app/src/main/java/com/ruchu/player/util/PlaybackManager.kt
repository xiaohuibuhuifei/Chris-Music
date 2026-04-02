package com.ruchu.player.util

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.ruchu.player.data.model.PlaybackSnapshot
import com.ruchu.player.data.model.Song
import com.ruchu.player.data.repository.MusicRepository
import com.ruchu.player.data.repository.PlaybackStateStore
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

    private var appContext: Context? = null
    private var stateStore: PlaybackStateStore? = null
    private var lastKnownPosition: Long = 0L

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
    private var pendingShuffleSongs: List<Song>? = null

    private val _navigateToPlayer = MutableStateFlow(false)
    val navigateToPlayer: StateFlow<Boolean> = _navigateToPlayer.asStateFlow()

    fun clearNavigateToPlayer() {
        _navigateToPlayer.value = false
    }

    fun connect(context: Context) {
        if (mediaController != null || isConnecting) return

        val appCtx = context.applicationContext
        appContext = appCtx
        stateStore = PlaybackStateStore(appCtx)

        val sessionToken = SessionToken(
            appCtx,
            ComponentName(appCtx, MusicService::class.java)
        )
        val controllerFuture = MediaController.Builder(appCtx, sessionToken).buildAsync()
        isConnecting = true

        controllerFuture.addListener({
            try {
                val controller = controllerFuture.get()
                if (mediaController == null) {
                    mediaController = controller
                    controller.addListener(playerListener)
                    syncPlaybackUiState()
                    startProgressUpdates()
                    Log.d(TAG, "Connected to MusicService")

                    // Handle pending shuffle from shortcut
                    pendingShuffleSongs?.let { songs ->
                        pendingShuffleSongs = null
                        playQueue(songs, shuffle = true)
                        _navigateToPlayer.value = true
                    }
                } else {
                    controller.release()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to connect to MusicService", e)
            } finally {
                isConnecting = false
            }
        }, ContextCompat.getMainExecutor(appCtx))
    }

    fun reconnectIfNeeded(context: Context) {
        if (isConnecting) return

        val controller = mediaController
        if (controller != null) {
            return
        }
        Log.d(TAG, "Controller missing, reconnecting...")
        connect(context)
    }

    fun shuffleAllFromShortcut(context: Context) {
        scope.launch {
            val repo = MusicRepository(context)
            repo.loadMusic()
            val songs = repo.getAllSongs()
            if (songs.isEmpty()) return@launch

            if (mediaController != null) {
                playQueue(songs, shuffle = true)
                _navigateToPlayer.value = true
            } else {
                pendingShuffleSongs = songs
            }
        }
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
        applyRepeatMode()
    }

    fun releaseController() {
        persistSnapshot()
        lastKnownPosition = _currentPosition.value
        _isPlaying.value = false
        progressJob?.cancel()
        progressJob = null
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
        isConnecting = false
        Log.d(TAG, "Controller released, state preserved")
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "onIsPlayingChanged: $isPlaying")
            syncPlaybackUiState()
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            Log.d(TAG, "onPlayWhenReadyChanged: playWhenReady=$playWhenReady reason=$reason")
            syncPlaybackUiState()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            Log.d(TAG, "onPlaybackStateChanged: state=$playbackState")
            syncPlaybackUiState()
            if (playbackState != Player.STATE_ENDED) {
                isSeeking = false
                return
            }
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
            Log.w(TAG, "Player error: ${error.errorCodeName} (code=${error.errorCode})", error)
            val shouldSkip = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
                PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
                PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED -> true
                else -> false
            }
            if (shouldSkip) next() else {
                _isPlaying.value = false
            }
        }
    }

    private fun syncPlaybackUiState() {
        mediaController?.let { controller ->
            _isPlaying.value = controller.playWhenReady || controller.isPlaying
            _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
            val d = controller.duration
            if (d > 0) _duration.value = d
            persistSnapshot()
        }
    }

    private fun playCurrentSong(song: Song) {
        _currentSong.value = song
        _currentPosition.value = 0L
        _duration.value = song.duration.toLong() * 1000

        val uri = Uri.parse("asset:///${song.fileName}")
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.albumTitle)

        if (song.albumArtwork.isNotEmpty()) {
            metadataBuilder.setArtworkUri(Uri.parse("asset:///${song.albumArtwork}"))
        }

        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaId(song.id)
            .setMediaMetadata(metadataBuilder.build())
            .build()

        mediaController?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
        _isPlaying.value = true
        persistSnapshot()
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
                delay(500)
            }
        }
    }

    fun getQueue(): List<Song> = queue
    fun getQueueIndex(): Int = queueIndex

    /**
     * Restore playback state after service restart (START_STICKY).
     * Called from MusicService.onCreate() when a snapshot exists.
     */
    fun restoreFromSnapshot(context: Context, snapshot: PlaybackSnapshot) {
        if (_currentSong.value != null) return  // already playing

        appContext = context.applicationContext
        stateStore = PlaybackStateStore(context.applicationContext)

        scope.launch {
            val repo = MusicRepository(context.applicationContext)
            repo.loadMusic()
            val allSongs = repo.getAllSongs()
            if (allSongs.isEmpty()) return@launch

            // Rebuild queue from saved song IDs
            val songMap = allSongs.associateBy { it.id }
            val restoredQueue = snapshot.queueIds.mapNotNull { songMap[it] }
            if (restoredQueue.isEmpty()) return@launch

            val targetSong = songMap[snapshot.songId] ?: return@launch

            sourceQueue = restoredQueue
            queue = restoredQueue
            queueIndex = snapshot.queueIndex.coerceIn(restoredQueue.indices)
            _shuffleMode.value = snapshot.shuffleEnabled
            _repeatMode.value = snapshot.repeatMode
            _currentSong.value = targetSong
            _currentPosition.value = snapshot.positionMs
            _duration.value = targetSong.duration.toLong() * 1000

            // Connect to service and play
            connect(context)
            // Wait for controller to connect, then seek to saved position
            scope.launch {
                var attempts = 0
                while (mediaController == null && attempts < 20) {
                    delay(100)
                    attempts++
                }
                mediaController?.let { controller ->
                    val uri = Uri.parse("asset:///${targetSong.fileName}")
                    val metadataBuilder = MediaMetadata.Builder()
                        .setTitle(targetSong.title)
                        .setArtist(targetSong.albumTitle)
                    if (targetSong.albumArtwork.isNotEmpty()) {
                        metadataBuilder.setArtworkUri(Uri.parse("asset:///${targetSong.albumArtwork}"))
                    }
                    val mediaItem = MediaItem.Builder()
                        .setUri(uri)
                        .setMediaId(targetSong.id)
                        .setMediaMetadata(metadataBuilder.build())
                        .build()
                    controller.setMediaItem(mediaItem)
                    controller.prepare()
                    controller.seekTo(snapshot.positionMs)
                    // Don't auto-play: restore state only, user decides when to resume
                    Log.d(TAG, "restoreFromSnapshot: restored ${targetSong.title} at ${snapshot.positionMs}ms")
                }
            }
        }
    }

    private fun persistSnapshot() {
        val song = _currentSong.value ?: return
        val store = stateStore ?: return
        val queueIds = queue.map { it.id }
        store.save(
            PlaybackSnapshot(
                songId = song.id,
                queueIds = queueIds,
                queueIndex = queueIndex,
                positionMs = _currentPosition.value,
                shuffleEnabled = _shuffleMode.value,
                repeatMode = _repeatMode.value,
                playWhenReady = _isPlaying.value
            )
        )
    }

    companion object {
        private const val TAG = "PlaybackManager"

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
