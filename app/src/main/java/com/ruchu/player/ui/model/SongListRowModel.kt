package com.ruchu.player.ui.model

import androidx.compose.runtime.Immutable
import com.ruchu.player.data.model.Song
import com.ruchu.player.util.formatDuration

@Immutable
data class SongListRowModel(
    val id: String,
    val title: String,
    val durationText: String,
    val isPlayable: Boolean = true
)

fun List<Song>.toSongListRowModels(): List<SongListRowModel> = map { song ->
    SongListRowModel(
        id = song.id,
        title = song.title,
        durationText = formatDuration(song.duration)
    )
}
