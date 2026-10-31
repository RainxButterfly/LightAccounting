/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: UpdateDialog.kt
 * 说明: 新版本提示、下载进度与安装入口
 */

package com.starleaf.qingzhang.ui.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starleaf.qingzhang.update.UpdateUiState
import com.starleaf.qingzhang.ui.theme.Amber
import com.starleaf.qingzhang.ui.theme.AmberDeep
import com.starleaf.qingzhang.ui.theme.Card
import com.starleaf.qingzhang.ui.theme.Ink
import com.starleaf.qingzhang.ui.theme.InkSoft
import com.starleaf.qingzhang.ui.theme.Line

@Composable
fun UpdateDialog(
    state: UpdateUiState,
    appVersion: String,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    when (state) {
        UpdateUiState.Idle, UpdateUiState.Checking -> return

        is UpdateUiState.Available -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Card,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "发现新版本 v${state.info.version}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )
            },
            text = {
                Column {
                    Text(
                        text = "当前版本 v$appVersion",
                        fontSize = 11.sp,
                        color = InkSoft
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = state.info.notes.ifBlank { "本次更新包含问题修复和体验改进。" },
                        fontSize = 13.sp,
                        color = InkSoft,
                        lineHeight = 20.sp,
                        modifier = Modifier
                            .heightIn(max = 220.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDownload) {
                    Text("下载更新", color = AmberDeep, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("稍后", color = InkSoft)
                }
            }
        )

        is UpdateUiState.Downloading -> AlertDialog(
            onDismissRequest = {},
            containerColor = Card,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "正在下载 v${state.info.version}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )
            },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = {
                            if (state.progress < 0) 0f else state.progress / 100f
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Amber,
                        trackColor = Line
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (state.progress < 0) "正在连接下载镜像…" else "${state.progress}%",
                        fontSize = 12.sp,
                        color = InkSoft
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onCancelDownload) {
                    Text("取消", color = InkSoft)
                }
            }
        )

        is UpdateUiState.Ready -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Card,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "下载完成",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )
            },
            text = {
                Text(
                    text = "v${state.info.version} 已下载，安装后完成更新。",
                    fontSize = 13.sp,
                    color = InkSoft,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = onInstall) {
                    Text("安装", color = AmberDeep, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("稍后", color = InkSoft)
                }
            }
        )

        is UpdateUiState.Error -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Card,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "更新失败",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )
            },
            text = {
                Text(
                    text = "${state.message}\n可以稍后重试，轻账会依次尝试其他镜像。",
                    fontSize = 13.sp,
                    color = InkSoft,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = onDownload) {
                    Text("重试", color = AmberDeep, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("关闭", color = InkSoft)
                }
            }
        )
    }
}
