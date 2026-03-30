package com.ruchu.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.ruchu.player.data.local.ManifestParser
import com.ruchu.player.util.PlaybackManager

class ShuffleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playbackManager = PlaybackManager.getInstance(this)
        playbackManager.connect(this)
        val albums = ManifestParser.parse(this)
        val allSongs = albums.flatMap { it.songs }
        if (allSongs.isNotEmpty()) {
            playbackManager.playQueue(allSongs, shuffle = true)
        }

        finish()
    }
}
