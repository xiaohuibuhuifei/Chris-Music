package com.ruchu.player.util

fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

fun formatDuration(seconds: Int): String = formatDuration(seconds.toLong() * 1000)
