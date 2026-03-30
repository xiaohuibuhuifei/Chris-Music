package com.ruchu.player.data.local

import android.content.Context
import com.google.gson.Gson
import com.ruchu.player.data.model.Album
import com.ruchu.player.data.model.Manifest
import com.ruchu.player.data.model.Song
import java.io.InputStreamReader

object ManifestParser {

    private val gson = Gson()

    fun parse(context: Context): List<Album> {
        val inputStream = context.assets.open("manifest.json")
        val reader = InputStreamReader(inputStream, Charsets.UTF_8)
        val manifest = gson.fromJson(reader, Manifest::class.java)
        reader.close()

        return manifest.albums.map { albumManifest ->
            Album(
                id = albumManifest.id,
                title = albumManifest.title,
                year = albumManifest.year,
                artwork = albumManifest.artwork,
                songs = albumManifest.songs.map { songManifest ->
                    Song(
                        id = songManifest.id,
                        title = songManifest.title,
                        albumId = albumManifest.id,
                        fileName = songManifest.fileName,
                        lyricsFile = songManifest.lyricsFile,
                        duration = songManifest.duration,
                        trackNumber = songManifest.trackNumber,
                        albumTitle = albumManifest.title,
                        albumArtwork = albumManifest.artwork
                    )
                }
            )
        }
    }
}
