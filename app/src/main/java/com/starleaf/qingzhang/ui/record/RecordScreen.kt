/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: RecordScreen.kt
 * 说明: 记账与编辑页面
 */

package com.starleaf.qingzhang.ui.record

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.starleaf.qingzhang.data.Category
import com.starleaf.qingzhang.data.TxnKind
import com.starleaf.qingzhang.data.TxnSource
import com.starleaf.qingzhang.ui.AccountUi
import com.starleaf.qingzhang.ui.AppViewModel
import com.starleaf.qingzhang.ui.components.AccountAvatar
import com.starleaf.qingzhang.ui.components.CategoryAvatar
import com.starleaf.qingzhang.ui.components.ConfirmDialog
import com.starleaf.qingzhang.ui.components.TextInputDialog
import com.starleaf.qingzhang.ui.theme.Amber
import com.starleaf.qingzhang.ui.theme.AmberDeep
import com.starleaf.qingzhang.ui.theme.Card
import com.starleaf.qingzhang.ui.theme.Danger
import com.starleaf.qingzhang.ui.theme.Ink
import com.starleaf.qingzhang.ui.theme.InkFaint
import com.starleaf.qingzhang.ui.theme.InkSoft
import com.starleaf.qingzhang.ui.theme.Line
import com.starleaf.qingzhang.ui.theme.Paper
import com.starleaf.qingzhang.util.Dates
import com.starleaf.qingzhang.util.Money
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    viewModel: AppViewModel,
    editId: Long?,
    onClose: () -> Unit
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val accounts by viewModel.accountsUi.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var kind by remember { mutableStateOf(TxnKind.EXPENSE) }
    var amountInput by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf(0L) }
    var toAccountId by remember { mutableStateOf<Long?>(null) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var time by remember { mutableStateOf(System.currentTimeMillis()) }
    var note by remember { mutableStateOf("") }
    var counterparty by remember { mutableStateOf("") }
    var source by remember { mutableStateOf(TxnSource.MANUAL) }
    var error by remember { mutableStateOf<String?>(null) }

    var showAccountSheet by remember { mutableStateOf(false) }
    var accountSheetForTarget by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(editId) {
        if (editId != null) {
            viewModel.getTxn(editId)?.let { txn ->
                kind = txn.kind
                amountInput = BigDecimalInput.fromCents(txn.amount)
                accountId = txn.accountId
                toAccountId = txn.toAccountId
                categoryId = txn.categoryId
                time = txn.time
                note = txn.note
                counterparty = txn.counterparty
                source = txn.source
            }
        }
    }

    LaunchedEffect(accounts) {
        if (accountId <= 0L) accountId = accounts.firstOrNull()?.account?.id ?: 0L
        if (toAccountId == null) {
            toAccountId = accounts.getOrNull(1)?.account?.id ?: accounts.firstOrNull()?.account?.id
        }
    }

    LaunchedEffect(categories, kind) {
        if (kind == TxnKind.TRANSFER) return@LaunchedEffect
        val listForKind = categories.filter { it.kind == kind }
        if (listForKind.none { it.id == categoryId }) {
            categoryId = listForKind.firstOrNull()?.id
        }
    }

    val amountCents = Money.inputToCents(amountInput)
    val account = accounts.firstOrNull { it.account.id == accountId }
    val toAccount = accounts.firstOrNull { it.account.id == toAccountId }
    val display = if (amountInput.isBlank()) "0.00" else amountInput
    val title = when {
        source == TxnSource.NOTIFICATION && editId != null -> "确认账单"
        editId != null -> "编辑记录"
        else -> "记一笔"
    }

    fun selectAccount(id: Long) {
        if (accountSheetForTarget) toAccountId = id else accountId = id
        showAccountSheet = false
    }

    fun pickDate() {
        val current = Dates.toLocalDateTime(time)
        DatePickerDialog(
            context,
            { _, year, month, day ->
                time = LocalDateTime.of(
                    LocalDate.of(year, month + 1, day),
                    current.toLocalTime()
                ).atZone(Dates.zone).toInstant().toEpochMilli()
            },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth
        ).show()
    }

    Column(Modifier.fillMaxSize().background(Paper)) {
        Box(Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
            ) {
                Icon(Icons.Rounded.Close, "关闭", tint = Ink)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                modifier = Modifier.align(Alignment.Center)
            )
            if (editId != null) {
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                ) {
                    Icon(Icons.Rounded.DeleteOutline, "删除", tint = InkSoft)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            KindTab("支出", kind == TxnKind.EXPENSE) { kind = TxnKind.EXPENSE }
            KindTab("收入", kind == TxnKind.INCOME) { kind = TxnKind.INCOME }
            KindTab("转账", kind == TxnKind.TRANSFER) { kind = TxnKind.TRANSFER }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "¥",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkSoft,
                    modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
                )
                Text(
                    text = display,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (amountCents > 0) Ink else InkFaint,
                    letterSpacing = (-0.5).sp
                )
            }
            if (error != null) {
                Spacer(Modifier.height(4.dp))
                Text(error!!, fontSize = 12.sp, color = Danger)
            }
        }

        if (kind == TxnKind.TRANSFER) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Card,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TransferAccount(
                            label = "转出",
                            account = account,
                            modifier = Modifier.weight(1f)
                        ) {
                            accountSheetForTarget = false
                            showAccountSheet = true
                        }
                        Text("→", fontSize = 20.sp, color = InkFaint)
                        TransferAccount(
                            label = "转入",
                            account = toAccount,
                            modifier = Modifier.weight(1f)
                        ) {
                            accountSheetForTarget = true
                            showAccountSheet = true
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "账户之间转移资金，不计入收入与支出",
                    fontSize = 12.sp,
                    color = InkFaint,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            val listForKind = categories.filter { it.kind == kind }
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(listForKind, key = { it.id }) { category ->
                    CategoryCell(
                        category = category,
                        selected = category.id == categoryId,
                        onClick = { categoryId = category.id }
                    )
                }
            }
        }

        Surface(
            color = Card,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (kind != TxnKind.TRANSFER) {
                        Chip(
                            text = account?.account?.name ?: "账户",
                            icon = Icons.Rounded.AccountBalanceWallet,
                            modifier = Modifier.weight(1f)
                        ) {
                            accountSheetForTarget = false
                            showAccountSheet = true
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Chip(
                        text = dateLabel(time),
                        modifier = Modifier.weight(if (kind == TxnKind.TRANSFER) 1f else 0.8f)
                    ) { pickDate() }
                    Spacer(Modifier.width(8.dp))
                    Chip(
                        text = note.ifBlank { "备注" },
                        modifier = Modifier.weight(1.3f)
                    ) { showNoteDialog = true }
                }

                Spacer(Modifier.height(8.dp))

                Keypad(
                    onKey = { key ->
                        error = null
                        amountInput = applyKey(amountInput, key)
                    },
                    onDone = {
                        if (amountCents <= 0) {
                            error = "请先输入金额"
                            return@Keypad
                        }
                        if (accountId <= 0) {
                            error = "请先添加一个账户"
                            return@Keypad
                        }
                        if (kind == TxnKind.TRANSFER && (toAccountId == null || toAccountId == accountId)) {
                            error = "转出和转入不能是同一个账户"
                            return@Keypad
                        }
                        viewModel.saveTxn(
                            editId = editId,
                            kind = kind,
                            amount = amountCents,
                            accountId = accountId,
                            toAccountId = toAccountId,
                            categoryId = categoryId,
                            time = time,
                            note = note,
                            counterparty = counterparty
                        )
                        onClose()
                    },
                    doneEnabled = amountCents > 0 && accountId > 0
                )
            }
        }
    }

    if (showAccountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAccountSheet = false },
            sheetState = sheetState,
            containerColor = Card,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        ) {
            Column(Modifier.padding(bottom = 28.dp)) {
                Text(
                    text = if (accountSheetForTarget) "选择转入账户" else "选择账户",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                )
                accounts.forEach { item ->
                    AccountSheetRow(
                        item = item,
                        selected = if (accountSheetForTarget) item.account.id == toAccountId
                        else item.account.id == accountId
                    ) { selectAccount(item.account.id) }
                }
            }
        }
    }

    if (showNoteDialog) {
        TextInputDialog(
            title = "备注",
            label = "写点什么",
            initialValue = note,
            onDismiss = { showNoteDialog = false },
            onConfirm = { note = it }
        )
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "删除这条记录？",
            text = "删除后无法恢复。",
            confirmText = "删除",
            danger = true,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                editId?.let { viewModel.deleteTxn(it) }
                onClose()
            }
        )
    }
}

@Composable
private fun KindTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Ink else InkFaint
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .width(18.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) Amber else Color.Transparent)
        )
    }
}

@Composable
private fun CategoryCell(category: Category, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CategoryAvatar(
            iconKey = category.iconKey,
            colorIndex = category.colorIndex,
            size = 46.dp,
            selected = selected
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = category.name,
            fontSize = 11.sp,
            color = if (selected) Ink else InkSoft,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TransferAccount(
    label: String,
    account: AccountUi?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.sp, color = InkFaint)
        Spacer(Modifier.height(8.dp))
        AccountAvatar(
            name = account?.account?.name ?: "账户",
            type = account?.account?.type?.name ?: "OTHER",
            size = 38.dp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = account?.account?.name ?: "请选择",
            fontSize = 13.sp,
            color = Ink,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "¥" + Money.format(account?.balance ?: 0),
            fontSize = 11.sp,
            color = InkFaint
        )
    }
}

@Composable
private fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Paper)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = InkFaint, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = text,
            fontSize = 12.sp,
            color = InkSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AccountSheetRow(item: AccountUi, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AccountAvatar(item.account.name, item.account.type.name, size = 38.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.account.name, fontSize = 15.sp, color = Ink, fontWeight = FontWeight.Medium)
            Text(
                "余额 ¥" + Money.format(item.balance),
                fontSize = 11.sp,
                color = InkFaint
            )
        }
        if (selected) {
            Icon(Icons.Rounded.Check, null, tint = Amber, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun Keypad(
    onKey: (String) -> Unit,
    onDone: () -> Unit,
    doneEnabled: Boolean
) {
    Row(Modifier.fillMaxWidth().height(228.dp)) {
        Column(Modifier.weight(3f).fillMaxHeight()) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf(".", "0", "del")
            )
            rows.forEach { keys ->
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    keys.forEach { key ->
                        if (key == "del") {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onKey("del") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Backspace,
                                    "删除",
                                    tint = InkSoft,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onKey(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Ink
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(18.dp))
                .background(if (doneEnabled) Amber else Line)
                .clickable(enabled = doneEnabled, onClick = onDone),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "完成",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (doneEnabled) Ink else InkFaint
            )
        }
    }
}

private fun applyKey(current: String, key: String): String {
    if (key == "del") return current.dropLast(1)
    if (key == ".") {
        if (current.contains('.')) return current
        return if (current.isEmpty()) "0." else "$current."
    }
    if (current.contains('.')) {
        if (current.substringAfter('.').length >= 2) return current
        return current + key
    }
    if (current.length >= 7) return current
    if (current == "0") return key
    return current + key
}

private fun dateLabel(millis: Long): String {
    val date = Dates.toLocalDate(millis)
    val today = LocalDate.now()
    return when (date) {
        today -> "今天"
        today.minusDays(1) -> "昨天"
        else -> "${date.monthValue}月${date.dayOfMonth}日"
    }
}

private object BigDecimalInput {
    fun fromCents(cents: Long): String {
        if (cents <= 0) return ""
        val value = java.math.BigDecimal.valueOf(cents, 2).stripTrailingZeros()
        return value.toPlainString()
    }
}
