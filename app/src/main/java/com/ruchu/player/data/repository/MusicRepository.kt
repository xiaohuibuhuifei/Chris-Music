package com.ruchu.player.data.repository

import android.content.Context
import com.ruchu.player.data.local.ManifestParser
import com.ruchu.player.data.model.Album
import com.ruchu.player.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class MusicRepository(private val context: Context) {

    suspend fun loadMusic() {
        if (isLoaded) return

        loadMutex.withLock {
            if (isLoaded) return@withLock

            val parsedAlbums = withContext(Dispatchers.IO) {
                ManifestParser.parse(context.applicationContext)
            }
            albums = parsedAlbums
            allSongs = parsedAlbums.flatMap { it.songs }
            isLoaded = true
        }
    }

    fun getAlbums(): List<Album> = albums

    fun getAlbum(albumId: String): Album? = albums.find { it.id == albumId }

    fun getAllSongs(): List<Song> = allSongs

    fun getSong(songId: String): Song? = allSongs.find { it.id == songId }

    fun getSongsByAlbum(albumId: String): List<Song> =
        albums.find { it.id == albumId }?.songs ?: emptyList()

    private companion object {
        private val loadMutex = Mutex()

        @Volatile
        private var isLoaded = false

        @Volatile
        private var albums: List<Album> = emptyList()

        @Volatile
        private var allSongs: List<Song> = emptyList()
    }
}
