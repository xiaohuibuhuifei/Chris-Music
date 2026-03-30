package com.ruchu.player.data.repository

import android.content.Context
import com.ruchu.player.data.local.LrcParser
import com.ruchu.player.data.model.LyricLine
import com.ruchu.player.data.model.Song

class LyricRepository(private val context: Context) {

    fun getLyrics(song: Song): List<LyricLine> {
        if (song.lyricsFile.isBlank()) return emptyList()

        return try {
            val inputStream = context.assets.open(song.lyricsFile)
            LrcParser.parse(inputStream).also {
                inputStream.close()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
