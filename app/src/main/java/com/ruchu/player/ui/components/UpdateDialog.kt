package com.ruchu.player.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.ruchu.player.data.model.UpdateInfo
import com.ruchu.player.data.model.UpdateState
import com.ruchu.player.ui.theme.RuChuTheme

@Composable
fun UpdateDialog(
    state: UpdateState,
    onConfirm: (UpdateInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = RuChuTheme.tokens

    when (state) {
        is UpdateState.Available -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text("发现新版本 ${state.info.versionName}")
                },
                text = {
                    Column {
                        Text(
                            text = state.info.releaseNotes.take(500),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                        Spacer(modifier = Modifier.height(tokens.spacing.sm))
                        Text(
                            text = "大小: ${"%.1f".format(state.info.fileSize / 1024.0 / 1024.0)} MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    PrimaryActionButton(onClick = { onConfirm(state.info) }) {
                        Text("立即更新")
                    }
                },
                dismissButton = {
                    SecondaryActionButton(onClick = onDismiss) {
                        Text("稍后再说")
                    }
                }
            )
        }

        is UpdateState.Downloading -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("正在下载更新") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(tokens.spacing.sm))
                        Text(
                            text = "${(state.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }

        is UpdateState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("更新失败") },
                text = {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    PrimaryActionButton(onClick = onDismiss) {
                        Text("确定")
                    }
                },
                dismissButton = null
            )
        }

        else -> { /* Idle, Checking, ReadyToInstall — 不显示对话框 */ }
    }
}
