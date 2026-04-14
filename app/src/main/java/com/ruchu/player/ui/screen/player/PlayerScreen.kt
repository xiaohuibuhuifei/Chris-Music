package com.ruchu.player.ui.screen.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchu.player.data.model.LyricLine
import com.ruchu.player.data.model.Song
import com.ruchu.player.ui.components.AppTopBar
import com.ruchu.player.ui.components.AssetImage
import com.ruchu.player.ui.components.PillToggleButton
import com.ruchu.player.ui.theme.RuChuTheme
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
    val tokens = RuChuTheme.tokens

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
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        // Faint album art background
        currentSong?.let { song ->
            AssetImage(
                assetPath = song.albumArtwork,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = tokens.opacity.faint },
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = tokens.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppTopBar(
                title = currentSong?.title ?: "未在播放",
                subtitle = currentSong?.albumTitle ?: "等待播放",
                horizontalPadding = 0.dp,
                onBack = onNavigateBack
            )

            // Album art area (fixed weight, never shifts)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AlbumArtWithAnimation(
                    song = currentSong,
                    isPlaying = isPlaying,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                )

                // Lyrics overlay on top of the record
                if (showLyrics && lyrics.isNotEmpty()) {
                    LyricsView(
                        lyrics = lyrics,
                        currentPosition = position,
                        onSeek = { playbackManager.seekTo(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = tokens.opacity.overlay))
                    )
                }

                // Current lyric line preview (overlay at bottom of this box)
                val currentLyricText = remember(lyrics, position) {
                    lyrics.indices.reversed()
                        .find { position >= lyrics[it].timestamp }
                        ?.let { lyrics[it].text } ?: ""
                }
                if (!(showLyrics && lyrics.isNotEmpty()) && currentLyricText.isNotEmpty()) {
                    AnimatedContent(
                        targetState = currentLyricText,
                        transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                        label = "lyricPreview",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = tokens.spacing.xs)
                    ) { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(tokens.spacing.lg))

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
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f), CircleShape)
                            )
                            // Middle glow
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), CircleShape)
                            )
                            // Solid center
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(MaterialTheme.colorScheme.onBackground, CircleShape)
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
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.primary)
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        .size(tokens.touch.compact)
                        .glowClickSubtle(
                            onClick = { playbackManager.toggleShuffle() },
                            glowColor = if (shuffleMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "随机",
                        tint = if (shuffleMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(tokens.icon.md)
                    )
                }

                // Previous
                Box(
                    modifier = Modifier
                        .size(tokens.touch.large)
                        .glowClickSubtle(
                            onClick = { playbackManager.previous() },
                            glowColor = MaterialTheme.colorScheme.onBackground
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "上一首",
                        modifier = Modifier.size(tokens.icon.xl),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Play/Pause - large center button with strong glow
                Box(
                    modifier = Modifier
                        .size(tokens.touch.primary + tokens.spacing.sm)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f), CircleShape)
                        .glowClick(
                            onClick = { playbackManager.togglePlayPause() },
                            glowColor = MaterialTheme.colorScheme.primary,
                            glowRadius = 26.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(47.dp)
                    )
                }

                // Next
                Box(
                    modifier = Modifier
                        .size(tokens.touch.large)
                        .glowClickSubtle(
                            onClick = { playbackManager.next() },
                            glowColor = MaterialTheme.colorScheme.onBackground
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "下一首",
                        modifier = Modifier.size(tokens.icon.xl),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Repeat
                Box(
                    modifier = Modifier
                        .size(tokens.touch.compact)
                        .glowClickSubtle(
                            onClick = { playbackManager.cycleRepeatMode() },
                            glowColor = if (repeatMode > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                        tint = if (repeatMode > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(tokens.icon.md)
                    )
                }
            }

            Spacer(modifier = Modifier.height(tokens.spacing.lg))

            // Secondary buttons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = tokens.spacing.xl),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillToggleButton(
                    text = "歌词",
                    selected = showLyrics,
                    onClick = { showLyrics = !showLyrics },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lyrics,
                            contentDescription = null,
                            tint = if (showLyrics) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(tokens.icon.sm)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AlbumArtWithAnimation(
    song: Song?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    // Rotation: Animatable for play/pause control
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(isPlaying, song) {
        if (isPlaying && song != null) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rotation.stop()
        }
    }

    // Tonearm swing: -45° = playing (needle on record), -60° = paused (off record)
    val tonearmAngle by animateFloatAsState(
        targetValue = if (isPlaying && song != null) -42f else -65f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "tonearmRotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Vinyl record with song switch animation
        AnimatedContent(
            targetState = song?.id,
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                    initialOffsetX = { it }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                    targetOffsetX = { -it }
                )
            },
            label = "recordSwitch"
        ) { songId ->
            val currentSong = song
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(RuChuTheme.extended.vinylOuter)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(RuChuTheme.extended.vinylInner)
                    .then(
                        if (currentSong != null && songId == currentSong.id)
                            Modifier.rotate(rotation.value)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Grooves
                for (i in 1..4) {
                    Box(
                        modifier = Modifier
                            .size((180 - i * 28).dp)
                            .clip(CircleShape)
                            .background(RuChuTheme.extended.vinylGroove)
                    )
                }

                // Album art in center
                if (currentSong != null && songId == currentSong.id) {
                    AssetImage(
                        assetPath = currentSong.albumArtwork,
                        contentDescription = currentSong.title,
                        modifier = Modifier
                            .fillMaxSize(0.6f)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Tonearm — pivot centered above the record, pushed down slightly
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 28.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = tonearmAngle
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                val canvasWidth = maxWidth * 0.35f
                val canvasHeight = maxHeight * 0.72f

                val onBackgroundColor = MaterialTheme.colorScheme.onBackground

                Canvas(
                    modifier = Modifier
                        .size(canvasWidth, canvasHeight)
                ) {
                val pivotX = size.width / 2f
                val pivotY = 0f

                // Dimensions
                val armThickness = 4.dp.toPx()
                val straightLength = size.height * 0.45f
                val bendRadius = 14.dp.toPx()
                val headLength = size.height * 0.08f
                val headWidth = 8.dp.toPx()
                val tonearmColor = onBackgroundColor.copy(alpha = 0.9f)

                // --- Pivot base (single merged circle at top) ---
                drawCircle(
                    color = onBackgroundColor.copy(alpha = 0.95f),
                    radius = 8.dp.toPx(),
                    center = Offset(pivotX, pivotY)
                )
                drawCircle(
                    color = onBackgroundColor.copy(alpha = 0.4f),
                    radius = 5.dp.toPx(),
                    center = Offset(pivotX, pivotY)
                )

                // --- Arm shaft (straight, from pivot downward) ---
                drawLine(
                    color = tonearmColor,
                    start = Offset(pivotX, pivotY),
                    end = Offset(pivotX, straightLength),
                    strokeWidth = armThickness,
                    cap = StrokeCap.Round
                )

                // --- Bend: 2 segments curving left toward record center ---
                val bendMidX = pivotX - bendRadius * 0.7f
                val bendMidY = straightLength + bendRadius * 0.7f
                val bendEndX = pivotX - bendRadius
                val bendEndY = straightLength + bendRadius

                drawLine(
                    color = tonearmColor,
                    start = Offset(pivotX, straightLength),
                    end = Offset(bendMidX, bendMidY),
                    strokeWidth = armThickness,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tonearmColor,
                    start = Offset(bendMidX, bendMidY),
                    end = Offset(bendEndX, bendEndY),
                    strokeWidth = armThickness,
                    cap = StrokeCap.Round
                )

                // --- Head shell (after bend, angled down-left) ---
                val headAngle = 45f
                val headEndX = bendEndX - headLength * kotlin.math.cos(Math.toRadians(headAngle.toDouble())).toFloat()
                val headEndY = bendEndY + headLength * kotlin.math.sin(Math.toRadians(headAngle.toDouble())).toFloat()

                drawLine(
                    color = tonearmColor,
                    start = Offset(bendEndX, bendEndY),
                    end = Offset(headEndX, headEndY),
                    strokeWidth = armThickness,
                    cap = StrokeCap.Round
                )

                // --- Cartridge head (wider at the tip) ---
                drawLine(
                    color = onBackgroundColor.copy(alpha = 0.85f),
                    start = Offset(headEndX, headEndY),
                    end = Offset(headEndX - headWidth * 0.3f, headEndY + headWidth * 0.5f),
                    strokeWidth = headWidth,
                    cap = StrokeCap.Round
                )

                // --- Needle tip ---
                val needleBaseX = headEndX - headWidth * 0.3f
                val needleBaseY = headEndY + headWidth * 0.5f
                drawLine(
                    color = onBackgroundColor.copy(alpha = 0.7f),
                    start = Offset(needleBaseX, needleBaseY),
                    end = Offset(needleBaseX - 1.dp.toPx(), needleBaseY + 4.dp.toPx()),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                }
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
                color = if (isCurrentLine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeek(line.timestamp) }
                    .padding(vertical = 8.dp)
            )
        }
    }
}
