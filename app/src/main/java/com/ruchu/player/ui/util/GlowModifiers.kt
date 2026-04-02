package com.ruchu.player.ui.util

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ruchu.player.ui.theme.DarkPrimary

private data class GlowStyle(
    val blur: Dp,
    val spread: Dp,
    val alpha: Float
)

/**
 * 真实的柔和泛光：按形状做外发光模糊，不再画一圈一圈的描边。
 */
private fun Modifier.softGlow(
    glowColor: Color,
    shape: Shape,
    style: GlowStyle
): Modifier = this.drawBehind {
    if (size.width <= 0f || size.height <= 0f) return@drawBehind

    drawGlowLayer(
        glowColor = glowColor.copy(alpha = style.alpha * 0.55f),
        shape = shape,
        expansion = style.spread.toPx(),
        blurRadius = style.blur.toPx()
    )
    drawGlowLayer(
        glowColor = glowColor.copy(alpha = style.alpha),
        shape = shape,
        expansion = style.spread.toPx() * 0.45f,
        blurRadius = style.blur.toPx() * 0.55f
    )
}

private fun DrawScope.drawGlowLayer(
    glowColor: Color,
    shape: Shape,
    expansion: Float,
    blurRadius: Float
) {
    if (glowColor.alpha <= 0f || blurRadius <= 0f) return

    val outline = shape.createOutline(
        size = Size(
            width = size.width + expansion * 2f,
            height = size.height + expansion * 2f
        ),
        layoutDirection = layoutDirection,
        density = this
    )
    val glowPath = outline.asPath()

    translate(left = -expansion, top = -expansion) {
        drawIntoCanvas { canvas ->
            val frameworkPaint = Paint().apply {
                isAntiAlias = true
                color = glowColor.toArgb()
                style = Paint.Style.FILL
                maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.OUTER)
            }

            canvas.nativeCanvas.drawPath(glowPath.asAndroidPath(), frameworkPaint)
            frameworkPaint.maskFilter = null
        }
    }
}

private fun Outline.asPath(): Path = when (this) {
    is Outline.Rectangle -> Path().apply { addRect(rect) }
    is Outline.Rounded -> Path().apply { addRoundRect(roundRect) }
    is Outline.Generic -> path
}

@Composable
private fun Modifier.glowPressable(
    onClick: () -> Unit,
    glowColor: Color,
    shape: Shape,
    idleStyle: GlowStyle,
    pressedStyle: GlowStyle,
    idleScale: Float,
    pressedScale: Float,
    animationMillis: Int
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else idleScale,
        animationSpec = tween(durationMillis = animationMillis),
        label = "glowScale"
    )
    val blur by animateDpAsState(
        targetValue = if (isPressed) pressedStyle.blur else idleStyle.blur,
        animationSpec = tween(durationMillis = animationMillis),
        label = "glowBlur"
    )
    val spread by animateDpAsState(
        targetValue = if (isPressed) pressedStyle.spread else idleStyle.spread,
        animationSpec = tween(durationMillis = animationMillis),
        label = "glowSpread"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) pressedStyle.alpha else idleStyle.alpha,
        animationSpec = tween(durationMillis = animationMillis),
        label = "glowAlpha"
    )

    return this
        .softGlow(
            glowColor = glowColor,
            shape = shape,
            style = GlowStyle(
                blur = blur,
                spread = spread,
                alpha = glowAlpha
            )
        )
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = if (isPressed) 0.94f else 1f
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * 主操作按钮：保留明显按压反馈，但 glow 仍然是柔和泛光。
 */
@Composable
fun Modifier.glowClick(
    onClick: () -> Unit,
    glowColor: Color = DarkPrimary,
    glowRadius: Dp = 18.dp,
    shape: Shape = CircleShape
): Modifier = glowPressable(
    onClick = onClick,
    glowColor = glowColor,
    shape = shape,
    idleStyle = GlowStyle(
        blur = glowRadius,
        spread = 2.dp,
        alpha = 0.34f
    ),
    pressedStyle = GlowStyle(
        blur = glowRadius + 8.dp,
        spread = 5.dp,
        alpha = 0.56f
    ),
    idleScale = 1f,
    pressedScale = 0.9f,
    animationMillis = 110
)

/**
 * 次要按钮：轻泛光，按下时稍微增强。
 */
@Composable
fun Modifier.glowClickSubtle(
    onClick: () -> Unit,
    glowColor: Color = DarkPrimary,
    shape: Shape = CircleShape
): Modifier = glowPressable(
    onClick = onClick,
    glowColor = glowColor,
    shape = shape,
    idleStyle = GlowStyle(
        blur = 10.dp,
        spread = 1.dp,
        alpha = 0.18f
    ),
    pressedStyle = GlowStyle(
        blur = 16.dp,
        spread = 4.dp,
        alpha = 0.32f
    ),
    idleScale = 1f,
    pressedScale = 0.88f,
    animationMillis = 95
)

/**
 * 非交互式轻泛光，用在首页/列表页这类安静界面。
 */
fun Modifier.buttonGlow(
    glowColor: Color = DarkPrimary,
    blurRadius: Float = 14f,
    shape: Shape = CircleShape
): Modifier = this.softGlow(
    glowColor = glowColor,
    shape = shape,
    style = GlowStyle(
        blur = blurRadius.dp,
        spread = 1.dp,
        alpha = 0.16f
    )
)

fun Modifier.contentGlow(
    glowColor: Color = DarkPrimary,
    blurRadius: Float = 10f,
    alpha: Float = 0.2f,
    shape: Shape = CircleShape
): Modifier = this.softGlow(
    glowColor = glowColor,
    shape = shape,
    style = GlowStyle(
        blur = blurRadius.dp,
        spread = 0.5.dp,
        alpha = alpha
    )
)
