package com.ruchu.player.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ruchu.player.ui.theme.RuChuTheme

@Preview(showBackground = true)
@Composable
private fun CoreComponentsV2Preview() {
    com.ruchu.player.ui.theme.RuChuTheme {
        Column(modifier = Modifier.padding(RuChuTheme.tokens.spacing.md)) {
            AppTopBar(title = "页面标题", subtitle = "副标题")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(RuChuTheme.tokens.spacing.xs)
            ) {
                PrimaryActionButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    androidx.compose.material3.Text("播放全部")
                }
                SecondaryActionButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    androidx.compose.material3.Text("随机播放")
                }
            }
            PillToggleButton(
                text = "歌词",
                selected = true,
                onClick = {},
                leadingIcon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Lyrics,
                        contentDescription = null
                    )
                }
            )
        }
    }
}
