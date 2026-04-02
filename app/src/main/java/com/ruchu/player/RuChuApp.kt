package com.ruchu.player

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.ruchu.player.data.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RuChuApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        appScope.launch {
            MusicRepository(this@RuChuApp).loadMusic()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "音乐播放",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "如初音乐播放通知"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "ruchu_music_channel"
    }
}
