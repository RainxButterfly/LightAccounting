/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: ProfileScreen.kt
 * 说明: 我的页面
 */

package com.starleaf.qingzhang.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.starleaf.qingzhang.AppInfo
import com.starleaf.qingzhang.R
import com.starleaf.qingzhang.ui.AppViewModel
import com.starleaf.qingzhang.ui.changelog.Changelog
import com.starleaf.qingzhang.ui.components.AppCard
import com.starleaf.qingzhang.ui.components.GroupTitle
import com.starleaf.qingzhang.ui.components.SettingsRow
import com.starleaf.qingzhang.ui.theme.AmberDeep
import com.starleaf.qingzhang.ui.theme.AmberSoft
import com.starleaf.qingzhang.ui.theme.Card
import com.starleaf.qingzhang.ui.theme.Ink
import com.starleaf.qingzhang.ui.theme.InkFaint
import com.starleaf.qingzhang.ui.theme.InkSoft
import com.starleaf.qingzhang.ui.theme.Line
import com.starleaf.qingzhang.ui.theme.ThemeMode
import com.starleaf.qingzhang.update.UpdateUiState

@Composable
fun ProfileScreen(
    viewModel: AppViewModel,
    onOpenBooks: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenInbox: () -> Unit,
    onOpenChangelog: () -> Unit
) {
    val book by viewModel.currentBook.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val currentChangelog = Changelog.entries.firstOrNull { it.version == viewModel.appVersion }
    val context = LocalContext.current
    var notificationEnabled by remember {
        mutableStateOf(viewModel.isNotificationAccessEnabled(context))
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationEnabled = viewModel.isNotificationAccessEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showAbout by remember { mutableStateOf(false) }
    var showTheme by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "我的",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Spacer(Modifier.height(14.dp))
                AppCard(modifier = Modifier.clickable(onClick = onOpenBooks)) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AmberSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (book?.name ?: "账").take(1),
                                color = AmberDeep,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = book?.name ?: "日常账本",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink
                            )
                            Spacer(Modifier.height(2.dp))
                            Text("当前账本", fontSize = 11.sp, color = InkFaint)
                        }
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            null,
                            tint = InkFaint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                GroupTitle("记账")
                AppCard {
                    SettingsRow(
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        title = "账本管理",
                        onClick = onOpenBooks
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Rounded.Category,
                        title = "分类管理",
                        onClick = onOpenCategories
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Rounded.UploadFile,
                        title = "账单导入",
                        value = "CSV / JSON",
                        tint = AmberDeep,
                        onClick = onOpenImport
                    )
                }
                Spacer(Modifier.height(20.dp))
                GroupTitle("自动记账")
                AppCard {
                    SettingsRow(
                        icon = Icons.Rounded.NotificationsActive,
                        title = "通知自动记账",
                        value = if (notificationEnabled) "已开启" else "去开启",
                        tint = AmberDeep,
                        onClick = { openNotificationSettings(context) }
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Rounded.NotificationsNone,
                        title = "待确认账单",
                        value = if (pendingCount > 0) "$pendingCount 笔" else "无",
                        tint = if (pendingCount > 0) AmberDeep else InkSoft,
                        onClick = onOpenInbox
                    )
                    Text(
                        text = "开启后会读取微信支付、支付宝的付款通知，先进入待确认列表，确认后才计入账本。",
                        fontSize = 11.sp,
                        color = InkFaint,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                GroupTitle("外观")
                AppCard {
                    SettingsRow(
                        icon = Icons.Rounded.Palette,
                        title = "主题",
                        value = themeMode.label,
                        tint = AmberDeep,
                        onClick = { showTheme = true }
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        title = "使用引导",
                        onClick = viewModel::openGuide
                    )
                }
                Spacer(Modifier.height(20.dp))
                GroupTitle("其他")
                AppCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { viewModel.checkForUpdates(true) }
                                .padding(start = 16.dp, end = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AmberDeep.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.SystemUpdate,
                                    null,
                                    tint = AmberDeep,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "检查更新",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Ink
                                )
                                Text(
                                    text = when {
                                        updateState is UpdateUiState.Checking -> "检查中…"
                                        updateState is UpdateUiState.Available ||
                                            updateState is UpdateUiState.Downloading ||
                                            updateState is UpdateUiState.Ready -> "有新版本"
                                        else -> "v${viewModel.appVersion}"
                                    },
                                    fontSize = 10.sp,
                                    color = InkFaint
                                )
                            }
                        }

                        Box(
                            Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(Line)
                        )

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(onClick = onOpenChangelog)
                                .padding(start = 10.dp, end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(InkSoft.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.History,
                                    null,
                                    tint = InkSoft,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "更新日志",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Ink,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                null,
                                tint = InkFaint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Divider()
                    SettingsRow(
                        icon = Icons.Rounded.Info,
                        title = "关于轻账",
                        value = "v${viewModel.appVersion}",
                        onClick = { showAbout = true }
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = AppInfo.author,
                    fontSize = 11.sp,
                    color = InkFaint,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = AppInfo.EMAIL,
                    fontSize = 11.sp,
                    color = AmberDeep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sendFeedbackEmail(context) },
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "账本数据仅保存在这台设备上",
                    fontSize = 11.sp,
                    color = InkFaint,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            containerColor = Card,
            shape = RoundedCornerShape(24.dp),
            title = { Text("关于轻账", style = MaterialTheme.typography.titleLarge, color = Ink) },
            text = {
                Column(
                    Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_brand),
                            contentDescription = null,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "轻账 v${viewModel.appVersion}" +
                            (currentChangelog?.let { " · ${it.date}" } ?: ""),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "作者：${AppInfo.author}",
                        fontSize = 12.sp,
                        color = InkSoft,
                        lineHeight = 19.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "反馈邮箱：${AppInfo.EMAIL}",
                        fontSize = 12.sp,
                        color = InkSoft
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "开源协议：${AppInfo.LICENSE}",
                        fontSize = 12.sp,
                        color = InkSoft
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "账本和流水只保存在本机，联网仅用于检查更新，不会上传记账数据。\n\n账单导入支持支付宝、微信官方账单，以及钱迹等应用导出的 CSV / JSON；通知自动记账只读取微信支付和支付宝的付款通知，不读取聊天内容。",
                        fontSize = 12.sp,
                        color = InkSoft,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { sendFeedbackEmail(context) }) {
                    Text("邮件反馈", color = AmberDeep, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbout = false }) {
                    Text("关闭", color = InkSoft)
                }
            }
        )
    }

    if (showTheme) {
        AlertDialog(
            onDismissRequest = { showTheme = false },
            containerColor = Card,
            shape = RoundedCornerShape(24.dp),
            title = { Text("主题", style = MaterialTheme.typography.titleLarge, color = Ink) },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showTheme = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mode == themeMode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showTheme = false
                                }
                            )
                            Text(
                                text = mode.label,
                                fontSize = 14.sp,
                                color = Ink,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTheme = false }) {
                    Text("关闭", color = InkSoft)
                }
            }
        )
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 60.dp)
            .height(1.dp)
            .background(Line)
    )
}

private fun sendFeedbackEmail(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${AppInfo.EMAIL}")).apply {
        putExtra(Intent.EXTRA_SUBJECT, "轻账反馈")
    }
    runCatching { context.startActivity(intent) }
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    runCatching { context.startActivity(intent) }
        .onFailure {
            runCatching { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }
        }
}
