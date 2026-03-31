package com.ruchu.player.data.repository

import android.content.Context
import android.util.LruCache
import com.ruchu.player.data.local.LrcParser
import com.ruchu.player.data.model.LyricLine
import com.ruchu.player.data.model.Song

class LyricRepository(private val context: Context) {

    private val cache = LruCache<String, List<LyricLine>>(10)

    fun getLyrics(song: Song): List<LyricLine> {
        if (song.lyricsFile.isBlank()) return emptyList()

        cache.get(song.lyricsFile)?.let { return it }

        return try {
            context.assets.open(song.lyricsFile).use { input ->
                LrcParser.parse(input).also { cache.put(song.lyricsFile, it) }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
