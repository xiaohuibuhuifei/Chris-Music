package com.ruchu.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.ruchu.player.ui.theme.RuChuTheme

@Composable
fun AppTopBar(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground,
    showSubtitle: Boolean = true,
    includeStatusBarPadding: Boolean = false,
    horizontalPadding: Dp = 24.dp,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val tokens = RuChuTheme.tokens
    val containerModifier = if (includeStatusBarPadding) {
        Modifier.statusBarsPadding()
    } else {
        Modifier
    }
    Box(
        modifier = containerModifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = tokens.spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.xs),
            content = actions
        )

        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = titleStyle,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.65f)
            )
            if (showSubtitle) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.65f)
                )
            }
        }
    }
}

@Composable
fun PrimaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = RuChuTheme.tokens
    val buttonTextStyle = MaterialTheme.typography.labelLarge.copy(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = tokens.touch.large),
        contentPadding = PaddingValues(horizontal = tokens.spacing.sm, vertical = tokens.spacing.xs),
        colors = buttonPalette(primary = true),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = tokens.elevation.level2,
            pressedElevation = tokens.elevation.level1,
            disabledElevation = tokens.elevation.level0
        ),
        shape = RoundedCornerShape(tokens.radius.lg),
        content = {
            ProvideTextStyle(buttonTextStyle) { content() }
        }
    )
}

@Composable
fun SecondaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = RuChuTheme.tokens
    val buttonTextStyle = MaterialTheme.typography.labelLarge.copy(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = tokens.touch.large),
        contentPadding = PaddingValues(horizontal = tokens.spacing.sm, vertical = tokens.spacing.xs),
        colors = buttonPalette(primary = false),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.25.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
        ),
        shape = RoundedCornerShape(tokens.radius.lg),
        content = {
            ProvideTextStyle(buttonTextStyle) { content() }
        }
    )
}

@Composable
private fun buttonPalette(primary: Boolean): ButtonColors {
    return if (primary) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PillToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val tokens = RuChuTheme.tokens
    val bg = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = tokens.opacity.medium)
    } else {
        RuChuTheme.extended.lyricsOverlay
    }
    val fg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(tokens.radius.pill))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = tokens.spacing.lg, vertical = tokens.spacing.xs),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon?.invoke()
        if (leadingIcon != null) {
            Spacer(modifier = Modifier.width(tokens.spacing.xs))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fg
        )
    }
}
