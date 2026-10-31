/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: HomeScreen.kt
 * 说明: 明细首页
 */

package com.starleaf.qingzhang.ui.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.starleaf.qingzhang.data.Book
import com.starleaf.qingzhang.data.TxnKind
import com.starleaf.qingzhang.ui.AccountUi
import com.starleaf.qingzhang.ui.AppViewModel
import com.starleaf.qingzhang.ui.MonthSummary
import com.starleaf.qingzhang.ui.TxnUi
import com.starleaf.qingzhang.ui.components.AmountText
import com.starleaf.qingzhang.ui.components.AppCard
import com.starleaf.qingzhang.ui.components.CategoryAvatar
import com.starleaf.qingzhang.ui.components.EmptyState
import com.starleaf.qingzhang.ui.components.MonthSwitcher
import com.starleaf.qingzhang.ui.theme.Amber
import com.starleaf.qingzhang.ui.theme.AmberDeep
import com.starleaf.qingzhang.ui.theme.Card
import com.starleaf.qingzhang.ui.theme.Danger
import com.starleaf.qingzhang.ui.theme.Ink
import com.starleaf.qingzhang.ui.theme.InkFaint
import com.starleaf.qingzhang.ui.theme.InkSoft
import com.starleaf.qingzhang.ui.theme.Line
import com.starleaf.qingzhang.ui.theme.categoryColor
import com.starleaf.qingzhang.util.Dates
import com.starleaf.qingzhang.util.Money
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onEditTxn: (Long) -> Unit,
    onOpenInbox: () -> Unit,
    onManageBooks: () -> Unit
) {
    val book by viewModel.currentBook.collectAsStateWithLifecycle()
    val books by viewModel.books.collectAsStateWithLifecycle()
    val month by viewModel.month.collectAsStateWithLifecycle()
    val canNext by viewModel.canGoNextMonth.collectAsStateWithLifecycle()
    val txns by viewModel.txns.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()

    var showBooks by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item(key = "header") {
            HomeHeader(
                book = book,
                books = books,
                month = month,
                canNext = canNext,
                summary = summary,
                pendingCount = pendingCount,
                onPreviousMonth = viewModel::previousMonth,
                onNextMonth = viewModel::nextMonth,
                onCurrentMonth = viewModel::goToCurrentMonth,
                onOpenBooks = { showBooks = true },
                onOpenInbox = onOpenInbox
            )
        }

        if (txns.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    title = "这个月还没有记录",
                    hint = "点下面的 + 记下第一笔",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            val groups = txns.groupBy { Dates.toLocalDate(it.txn.time) }
            groups.forEach { (date, list) ->
                item(key = "day-$date") {
                    DayHeader(date, list)
                }
                items(list, key = { "txn-${it.txn.id}" }) { item ->
                    TxnRow(item = item, onClick = { onEditTxn(item.txn.id) })
                }
            }
        }
    }

    if (showBooks) {
        ModalBottomSheet(
            onDismissRequest = { showBooks = false },
            sheetState = sheetState,
            containerColor = Card,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        ) {
            Column(Modifier.padding(bottom = 28.dp)) {
                Text(
                    text = "切换账本",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    color = Ink,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                )
                books.forEach { item ->
                    BookRow(
                        book = item,
                        selected = item.id == book?.id,
                        onClick = {
                            viewModel.selectBook(item.id)
                            showBooks = false
                        }
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .height(1.dp)
                        .background(Line)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showBooks = false
                            onManageBooks()
                        }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Tune, null, tint = InkSoft, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("管理账本", style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, color = InkSoft)
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    book: Book?,
    books: List<Book>,
    month: YearMonth,
    canNext: Boolean,
    summary: MonthSummary,
    pendingCount: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
    onOpenBooks: () -> Unit,
    onOpenInbox: () -> Unit
) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "轻账",
                    fontSize = 11.sp,
                    color = InkFaint,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenBooks)
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = book?.name ?: "日常账本",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "切换账本",
                        tint = InkFaint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            InboxButton(pendingCount, onOpenInbox)
        }

        Spacer(Modifier.height(16.dp))

        AppCard {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonthSwitcher(
                        month = month,
                        onPrevious = onPreviousMonth,
                        onNext = onNextMonth,
                        canNext = canNext
                    )
                    Spacer(Modifier.weight(1f))
                    if (month != YearMonth.now()) {
                        Text(
                            text = "回到本月",
                            fontSize = 11.sp,
                            color = AmberDeep,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onCurrentMonth)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("本月支出", fontSize = 12.sp, color = InkSoft)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "¥",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkSoft,
                        modifier = Modifier.padding(bottom = 7.dp, end = 4.dp)
                    )
                    Text(
                        text = Money.format(summary.expense),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        letterSpacing = (-0.5).sp
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row {
                    SmallSummary("收入", summary.income, Ink)
                    Spacer(Modifier.width(28.dp))
                    SmallSummary(
                        label = "结余",
                        amount = summary.balance,
                        color = if (summary.balance < 0) Danger else Ink
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${books.size} 个账本",
                        fontSize = 11.sp,
                        color = InkFaint,
                        modifier = Modifier.align(Alignment.Bottom)
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun SmallSummary(label: String, amount: Long, color: Color) {
    Column {
        Text(label, fontSize = 11.sp, color = InkFaint)
        Spacer(Modifier.height(2.dp))
        Text(
            text = (if (amount < 0) "-¥" else "¥") + Money.format(kotlin.math.abs(amount)),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun InboxButton(count: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Card)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Rounded.NotificationsNone,
            contentDescription = "待确认账单",
            tint = InkSoft,
            modifier = Modifier.size(21.dp)
        )
        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 7.dp, end = 7.dp)
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(Danger),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DayHeader(date: LocalDate, items: List<TxnUi>) {
    val expense = items.filter { it.txn.kind == TxnKind.EXPENSE }.sumOf { it.txn.amount }
    val income = items.filter { it.txn.kind == TxnKind.INCOME }.sumOf { it.txn.amount }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = Dates.dayLabel(date),
            fontSize = 12.sp,
            color = InkSoft,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.weight(1f))
        if (expense > 0) {
            Text("支出 ${Money.format(expense)}", fontSize = 11.sp, color = InkFaint)
        }
        if (income > 0) {
            if (expense > 0) Spacer(Modifier.width(8.dp))
            Text("收入 ${Money.format(income)}", fontSize = 11.sp, color = InkFaint)
        }
    }
}

@Composable
private fun TxnRow(item: TxnUi, onClick: () -> Unit) {
    val txn = item.txn
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (txn.kind == TxnKind.TRANSFER) {
            val color = categoryColor(6)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "转",
                    color = color,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            CategoryAvatar(
                iconKey = item.category?.iconKey ?: "more",
                colorIndex = item.category?.colorIndex ?: 14,
                size = 42.dp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            val title = when {
                txn.kind == TxnKind.TRANSFER -> "转账"
                item.category != null -> item.category.name
                txn.counterparty.isNotBlank() -> txn.counterparty
                else -> "未分类"
            }
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            val detail = when {
                txn.kind == TxnKind.TRANSFER -> {
                    val from = item.account?.name ?: "账户"
                    val to = item.toAccount?.name ?: "账户"
                    "$from → $to · ${Dates.timeLabel(txn.time)}"
                }

                else -> {
                    val hint = if (txn.note.isNotBlank()) txn.note else txn.counterparty
                    listOf(hint, Dates.timeLabel(txn.time))
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                }
            }
            Text(
                text = detail,
                fontSize = 11.sp,
                color = InkFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(10.dp))

        val prefix = when (txn.kind) {
            TxnKind.EXPENSE -> "-"
            TxnKind.INCOME -> "+"
            TxnKind.TRANSFER -> ""
        }
        AmountText(
            text = prefix + Money.format(txn.amount),
            color = when (txn.kind) {
                TxnKind.INCOME -> com.starleaf.qingzhang.ui.theme.Income
                TxnKind.TRANSFER -> InkSoft
                else -> Ink
            }
        )
    }
}

@Composable
private fun BookRow(book: Book, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = book.name,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            color = Ink,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        if (selected) {
            Icon(Icons.Rounded.Check, "当前账本", tint = Amber, modifier = Modifier.size(20.dp))
        }
    }
}
