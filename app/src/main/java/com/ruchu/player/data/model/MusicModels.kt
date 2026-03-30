package com.ruchu.player.data.model

import com.google.gson.annotations.SerializedName

// === Manifest JSON models ===

data class Manifest(
    val albums: List<AlbumManifest>
)

data class AlbumManifest(
    val id: String,
    val title: String,
    val year: Int,
    val artwork: String,
    val songs: List<SongManifest>
)

data class SongManifest(
    val id: String,
    val title: String,
    val fileName: String,
    val lyricsFile: String = "",
    val duration: Int = 0,
    val trackNumber: Int = 0
)

// === App domain models ===

data class Song(
    val id: String,
    val title: String,
    val albumId: String,
    val fileName: String,
    val lyricsFile: String,
    val duration: Int,
    val trackNumber: Int,
    val albumTitle: String = "",
    val albumArtwork: String = ""
)

data class Album(
    val id: String,
    val title: String,
    val year: Int,
    val artwork: String,
    val songs: List<Song>
)

data class LyricLine(
    val timestamp: Long,
    val text: String
)

data class Quote(
    val lyric: String,
    val songTitle: String,
    val year: Int? = null
)

// === Playback state ===

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val shuffleMode: Boolean = false,
    val repeatMode: Int = 1,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = 0
)
