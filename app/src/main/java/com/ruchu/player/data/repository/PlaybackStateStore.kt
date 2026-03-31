package com.ruchu.player.data.repository

import android.content.Context
import com.google.gson.Gson
import com.ruchu.player.data.model.PlaybackSnapshot

class PlaybackStateStore(context: Context) {

    private val prefs = context.getSharedPreferences("playback_state", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun save(snapshot: PlaybackSnapshot) {
        prefs.edit().putString(KEY, gson.toJson(snapshot)).apply()
    }

    fun load(): PlaybackSnapshot? {
        val json = prefs.getString(KEY, null) ?: return null
        return try {
            gson.fromJson(json, PlaybackSnapshot::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        private const val KEY = "snapshot"
    }
}
