/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: InboxScreen.kt
 * 说明: 待确认账单页面
 */

package com.starleaf.qingzhang.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.starleaf.qingzhang.data.TxnKind
import com.starleaf.qingzhang.ui.AppViewModel
import com.starleaf.qingzhang.ui.TxnUi
import com.starleaf.qingzhang.ui.components.AppCard
import com.starleaf.qingzhang.ui.components.CategoryAvatar
import com.starleaf.qingzhang.ui.components.ConfirmDialog
import com.starleaf.qingzhang.ui.components.EmptyState
import com.starleaf.qingzhang.ui.components.TopBar
import com.starleaf.qingzhang.ui.theme.Amber
import com.starleaf.qingzhang.ui.theme.AmberDeep
import com.starleaf.qingzhang.ui.theme.AmberSoft
import com.starleaf.qingzhang.ui.theme.Card
import com.starleaf.qingzhang.ui.theme.Danger
import com.starleaf.qingzhang.ui.theme.Income
import com.starleaf.qingzhang.ui.theme.Ink
import com.starleaf.qingzhang.ui.theme.InkFaint
import com.starleaf.qingzhang.ui.theme.InkSoft
import com.starleaf.qingzhang.util.Dates
import com.starleaf.qingzhang.util.Money

@Composable
fun InboxScreen(
    viewModel: AppViewModel,
    onEdit: (Long) -> Unit,
    onBack: () -> Unit
) {
    val pending by viewModel.pending.collectAsStateWithLifecycle()
    var discarding by remember { mutableStateOf<TxnUi?>(null) }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = "待确认账单", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .background(AmberSoft)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Card),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("!", color = AmberDeep, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "这些记录来自支付通知，确认后才会计入账本。",
                        fontSize = 11.sp,
                        color = InkSoft,
                        lineHeight = 17.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            if (pending.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.NotificationsNone,
                        title = "没有待确认的账单",
                        hint = "开启通知自动记账后，付款通知会出现在这里"
                    )
                }
            } else {
                items(pending, key = { it.txn.id }) { item ->
                    PendingCard(
                        item = item,
                        onEdit = { onEdit(item.txn.id) },
                        onDiscard = { discarding = item }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }

    discarding?.let { item ->
        ConfirmDialog(
            title = "忽略这笔记录？",
            text = "忽略后不会计入账本，之后也不会再提醒。",
            confirmText = "忽略",
            danger = true,
            onDismiss = { discarding = null },
            onConfirm = { viewModel.discardPending(item.txn.id) }
        )
    }
}

@Composable
private fun PendingCard(item: TxnUi, onEdit: () -> Unit, onDiscard: () -> Unit) {
    val txn = item.txn
    AppCard(modifier = Modifier.clickable(onClick = onEdit)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryAvatar(
                    iconKey = item.category?.iconKey ?: "more",
                    colorIndex = item.category?.colorIndex ?: 14,
                    size = 42.dp
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = (if (txn.kind == TxnKind.INCOME) "+¥" else "-¥") + Money.format(txn.amount),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (txn.kind == TxnKind.INCOME) Income else Ink
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = item.category?.name ?: "未分类",
                            fontSize = 11.sp,
                            color = InkFaint,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "${item.account?.name ?: "账户"} · ${Dates.timeLabel(txn.time)}",
                        fontSize = 11.sp,
                        color = InkFaint
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = txn.note,
                fontSize = 12.sp,
                color = InkSoft,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDiscard) {
                    Text("忽略", fontSize = 12.sp, color = InkFaint)
                }
                Spacer(Modifier.width(4.dp))
                Box(
                    Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                        .background(Amber)
                        .clickable(onClick = onEdit)
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        "去确认",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )
                }
            }
        }
    }
}
