package com.ruchu.player.ui.screen.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchu.player.data.model.LyricLine
import com.ruchu.player.data.model.Song
import com.ruchu.player.ui.components.AssetImage
import com.ruchu.player.ui.theme.OnSurfaceVariant
import com.ruchu.player.ui.theme.Primary
import com.ruchu.player.ui.util.glowClick
import com.ruchu.player.ui.util.glowClickSubtle
import com.ruchu.player.util.formatDuration

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val playbackManager = viewModel.playbackManager
    val currentSong by playbackManager.currentSong.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val position by playbackManager.currentPosition.collectAsState()
    val duration by playbackManager.duration.collectAsState()
    val shuffleMode by playbackManager.shuffleMode.collectAsState()
    val repeatMode by playbackManager.repeatMode.collectAsState()
    val lyrics by viewModel.lyrics.collectAsState()

    var showLyrics by remember { mutableStateOf(false) }
    var seekingValue by remember { mutableStateOf<Float?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) awaitPointerEvent()
                }
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1035),
                        Color(0xFF0D0D1A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Faint album art background
        currentSong?.let { song ->
            AssetImage(
                assetPath = song.albumArtwork,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.06f },
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 12.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.18f))
            )

            // Top bar - back button + centered title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .pointerInput(onNavigateBack) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 12f) {
                                onNavigateBack()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentSong?.title ?: "未在播放",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.65f)
                    )
                    Text(
                        text = currentSong?.albumTitle ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.65f)
                    )
                }
            }

            // Album art or Lyrics
            if (showLyrics && lyrics.isNotEmpty()) {
                LyricsView(
                    lyrics = lyrics,
                    currentPosition = position,
                    onSeek = { playbackManager.seekTo(it) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                AlbumArtWithAnimation(
                    song = currentSong,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(20.dp))

            // Progress bar
            val sliderProgress = if (duration > 0) position.toFloat() / duration else 0f
            val displayProgress = seekingValue ?: sliderProgress

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = displayProgress,
                    onValueChange = { seekingValue = it },
                    onValueChangeFinished = {
                        seekingValue?.let { pos ->
                            playbackManager.seekTo((pos * duration).toLong())
                        }
                        seekingValue = null
                    },
                    thumb = {
                        val infiniteTransition = rememberInfiniteTransition(label = "thumbBreathe")
                        val breatheScale by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "breatheScale"
                        )
                        Box(contentAlignment = Alignment.Center) {
                            // Outer glow - breathing
                            Box(
                                modifier = Modifier
                                    .size(20.dp * breatheScale)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            )
                            // Middle glow
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            )
                            // Solid center
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.White, CircleShape)
                            )
                        }
                    },
                    track = { sliderState ->
                        val fraction = (sliderState.value - sliderState.valueRange.start) /
                            (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Primary)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(
                            seekingValue?.let { (it * duration).toLong() } ?: position
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = formatDuration(duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Playback controls with glow + press animation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .glowClickSubtle(
                            onClick = { playbackManager.toggleShuffle() },
                            glowColor = if (shuffleMode) Primary else OnSurfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "随机",
                        tint = if (shuffleMode) Primary else OnSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Previous
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .glowClickSubtle(
                            onClick = { playbackManager.previous() },
                            glowColor = Color.White
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "上一首",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                // Play/Pause - large center button with strong glow
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                        .glowClick(
                            onClick = { playbackManager.togglePlayPause() },
                            glowColor = Primary,
                            glowRadius = 20.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .glowClickSubtle(
                            onClick = { playbackManager.next() },
                            glowColor = Color.White
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "下一首",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                // Repeat
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .glowClickSubtle(
                            onClick = { playbackManager.cycleRepeatMode() },
                            glowColor = if (repeatMode > 0) Primary else OnSurfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (repeatMode) {
                            1 -> Icons.Default.Repeat
                            2 -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "循环",
                        tint = if (repeatMode > 0) Primary else OnSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Secondary buttons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (showLyrics) Primary.copy(alpha = 0.2f) else Color(0xFF2A2A2A)
                        )
                        .glowClickSubtle(
                            onClick = { showLyrics = !showLyrics },
                            glowColor = if (showLyrics) Primary else OnSurfaceVariant,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lyrics,
                            contentDescription = null,
                            tint = if (showLyrics) Primary else OnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "歌词",
                            color = if (showLyrics) Primary else OnSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumArtWithAnimation(
    song: Song?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Vinyl record with thick black border
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(Color(0xFF0A0A0A))
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A1A1A))
                .then(
                    if (song != null) Modifier.rotate(rotation) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            // Grooves
            for (i in 1..4) {
                Box(
                    modifier = Modifier
                        .size((180 - i * 28).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF252525))
                )
            }

            // Album art in center
            if (song != null) {
                AssetImage(
                    assetPath = song.albumArtwork,
                    contentDescription = song.title,
                    modifier = Modifier
                        .fillMaxSize(0.6f)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun LyricsView(
    lyrics: List<LyricLine>,
    currentPosition: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val currentLineIndex = remember(lyrics, currentPosition) {
        var idx = 0
        for (i in lyrics.indices.reversed()) {
            if (currentPosition >= lyrics[i].timestamp) {
                idx = i
                break
            }
        }
        idx
    }

    LaunchedEffect(currentLineIndex) {
        if (lyrics.isNotEmpty()) {
            listState.animateScrollToItem(
                index = (currentLineIndex - 3).coerceAtLeast(0),
                scrollOffset = 0
            )
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        itemsIndexed(lyrics) { index, line ->
            val isCurrentLine = index == currentLineIndex
            Text(
                text = line.text,
                style = if (isCurrentLine) {
                    MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp
                    )
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                color = if (isCurrentLine) Primary else OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeek(line.timestamp) }
                    .padding(vertical = 8.dp)
            )
        }
    }
}
