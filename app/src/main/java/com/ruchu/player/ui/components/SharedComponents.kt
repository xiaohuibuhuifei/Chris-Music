package com.ruchu.player.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchu.player.data.model.Album
import com.ruchu.player.data.model.Song
import com.ruchu.player.ui.theme.RuChuTheme
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object AssetImageMemoryCache {
    private val images = ConcurrentHashMap<String, ImageBitmap>()

    fun get(path: String): ImageBitmap? = images[path]

    fun put(path: String, bitmap: ImageBitmap) {
        images[path] = bitmap
    }
}

@Composable
fun AssetImage(
    assetPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var bitmap by remember(assetPath) {
        mutableStateOf(AssetImageMemoryCache.get(assetPath))
    }

    LaunchedEffect(assetPath) {
        if (bitmap != null) return@LaunchedEffect

        val appContext = context.applicationContext
        bitmap = withContext(Dispatchers.IO) {
            AssetImageMemoryCache.get(assetPath) ?: runCatching {
                appContext.assets.open(assetPath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                }
            }.getOrNull()?.also { loadedBitmap ->
                AssetImageMemoryCache.put(assetPath, loadedBitmap)
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contentDescription ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return
    val tokens = RuChuTheme.tokens

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
    ) {
        AssetImage(
            assetPath = song.albumArtwork,
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = tokens.opacity.subtle },
            contentScale = ContentScale.Crop
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = tokens.opacity.medium)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.spacing.sm, vertical = tokens.spacing.xs + 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssetImage(
                assetPath = song.albumArtwork,
                contentDescription = song.title,
                modifier = Modifier
                    .size(tokens.touch.large)
                    .clip(RoundedCornerShape(tokens.radius.sm)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(tokens.spacing.xs + 2.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.albumTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onPrevious, modifier = Modifier.size(tokens.touch.regular)) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "上一曲",
                    modifier = Modifier.size(tokens.icon.lg),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onTogglePlay, modifier = Modifier.size(tokens.touch.regular)) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(tokens.icon.lg),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onNext, modifier = Modifier.size(tokens.touch.regular)) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "下一曲",
                    modifier = Modifier.size(tokens.icon.lg),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GlowingActionLabel(
    text: String,
    icon: ImageVector,
    contentColor: Color,
    glowColor: Color = contentColor,
    textSize: TextUnit = 14.sp,
    modifier: Modifier = Modifier
) {
    val tokens = RuChuTheme.tokens
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(tokens.icon.sm)
        )

        Spacer(modifier = Modifier.width(tokens.spacing.xs - 2.dp))

        Text(
            text = text,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium.merge(
                TextStyle(
                    fontSize = textSize,
                    shadow = Shadow(
                        color = glowColor.copy(alpha = tokens.opacity.medium + 0.08f),
                        blurRadius = 8f,
                        offset = androidx.compose.ui.geometry.Offset.Zero
                    )
                )
            )
        )
    }
}

@Composable
fun QuoteCard(
    quoteText: String,
    quoteSource: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = RuChuTheme.tokens
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.radius.lg))
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        RuChuTheme.extended.quoteGradientStart,
                        RuChuTheme.extended.quoteGradientEnd
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(tokens.spacing.lg)
    ) {
        Column {
            Text(
                text = quoteText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = com.ruchu.player.ui.theme.CreativeFont
                ),
                color = MaterialTheme.colorScheme.onBackground,
                minLines = 2
            )
            Spacer(modifier = Modifier.height(tokens.spacing.xs))
            Text(
                text = "\u2014\u2014 $quoteSource",
                style = MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.End
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AlbumCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = RuChuTheme.tokens
    Column(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        AssetImage(
            assetPath = album.artwork,
            contentDescription = album.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(tokens.radius.sm)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(tokens.spacing.xxs))
        Text(
            text = album.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.year?.toString() ?: "${album.songs.size}首",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
