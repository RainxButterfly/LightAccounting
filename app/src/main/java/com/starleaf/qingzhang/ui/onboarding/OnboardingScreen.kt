/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: OnboardingScreen.kt
 * 说明: 首次使用引导
 */

package com.starleaf.qingzhang.ui.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.starleaf.qingzhang.AppInfo
import com.starleaf.qingzhang.R
import com.starleaf.qingzhang.ui.components.PrimaryButton
import com.starleaf.qingzhang.ui.theme.Amber
import com.starleaf.qingzhang.ui.theme.AmberDeep
import com.starleaf.qingzhang.ui.theme.AmberSoft
import com.starleaf.qingzhang.ui.theme.Ink
import com.starleaf.qingzhang.ui.theme.InkFaint
import com.starleaf.qingzhang.ui.theme.InkSoft
import com.starleaf.qingzhang.ui.theme.Line
import com.starleaf.qingzhang.ui.theme.Paper
import kotlinx.coroutines.launch

private data class GuidePage(
    val icon: ImageVector?,
    val title: String,
    val body: String,
    val imageRes: Int? = null
)

private val guidePages = listOf(
    GuidePage(
        icon = null,
        imageRes = R.drawable.ic_brand,
        title = "欢迎使用轻账",
        body = "把每一笔收入和支出记清楚。账本、账户和流水都只保存在这台设备上，不需要注册账号。"
    ),
    GuidePage(
        icon = Icons.AutoMirrored.Rounded.ReceiptLong,
        title = "记账与账本",
        body = "点底部中间的加号记一笔，支出、收入和转账分开统计。账户余额由流水自动汇总，也可以建立多个账本。"
    ),
    GuidePage(
        icon = Icons.Rounded.UploadFile,
        title = "导入支付宝 / 微信账单",
        body = "支付宝：我的 → 账单 → 开具交易流水证明\n微信：我 → 服务 → 钱包 → 账单 → 下载账单\n\n导出的 CSV 或 ZIP 在「我的 → 账单导入」中选择，自动分类，并按交易单号去重。"
    ),
    GuidePage(
        icon = Icons.Rounded.NotificationsActive,
        title = "通知自动记账",
        body = "在「我的 → 通知自动记账」中开启系统通知使用权。轻账只读取微信支付和支付宝的付款通知，不读取聊天内容。\n\n捕获到的记录先进入待确认列表，确认后才计入账本。通知内容被隐藏时可能无法识别。"
    )
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    BackHandler(onBack = onFinish)
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { guidePages.size })
    val scope = rememberCoroutineScope()
    var notificationEnabled by remember {
        mutableStateOf(
            NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
        )
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationEnabled = NotificationManagerCompat.getEnabledListenerPackages(context)
                    .contains(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onFinish) {
                Text("跳过", fontSize = 13.sp, color = InkSoft)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            GuidePageContent(guidePages[index])
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(guidePages.size) { index ->
                val selected = index == pagerState.currentPage
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (selected) 18.dp else 6.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) Amber else Line)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (pagerState.currentPage == guidePages.lastIndex) {
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InkSoft)
                ) {
                    Text(
                        text = if (notificationEnabled) "通知权限已开启" else "去开启通知权限",
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            PrimaryButton(
                text = if (pagerState.currentPage == guidePages.lastIndex) "开始使用" else "下一步",
                onClick = {
                    if (pagerState.currentPage == guidePages.lastIndex) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))
            Text(
                text = "轻账 · ${AppInfo.author}",
                fontSize = 10.sp,
                color = InkFaint,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GuidePageContent(page: GuidePage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(118.dp)
                .clip(CircleShape)
                .background(AmberSoft),
            contentAlignment = Alignment.Center
        ) {
            if (page.imageRes != null) {
                Image(
                    painter = painterResource(page.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = page.icon ?: Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = AmberDeep,
                    modifier = Modifier.size(52.dp)
                )
            }
        }
        Spacer(Modifier.height(30.dp))
        Text(
            text = page.title,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = page.body,
            fontSize = 13.sp,
            color = InkSoft,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}
