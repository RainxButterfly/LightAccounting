/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: AssetsScreen.kt
 * 说明: 资产与账户页面
 */

package com.starleaf.qingzhang.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.starleaf.qingzhang.data.Account
import com.starleaf.qingzhang.data.AccountType
import com.starleaf.qingzhang.ui.AccountUi
import com.starleaf.qingzhang.ui.AppViewModel
import com.starleaf.qingzhang.ui.components.AccountAvatar
import com.starleaf.qingzhang.ui.components.AppCard
import com.starleaf.qingzhang.ui.components.EmptyState
import com.starleaf.qingzhang.ui.theme.Amber
import com.starleaf.qingzhang.ui.theme.AmberDeep
import com.starleaf.qingzhang.ui.theme.AmberSoft
import com.starleaf.qingzhang.ui.theme.Card
import com.starleaf.qingzhang.ui.theme.Danger
import com.starleaf.qingzhang.ui.theme.Ink
import com.starleaf.qingzhang.ui.theme.InkFaint
import com.starleaf.qingzhang.ui.theme.InkSoft
import com.starleaf.qingzhang.ui.theme.Line
import com.starleaf.qingzhang.ui.theme.Paper
import com.starleaf.qingzhang.util.Money
import com.starleaf.qingzhang.util.displayName
import kotlin.math.abs

@Composable
fun AssetsScreen(viewModel: AppViewModel) {
    val accounts by viewModel.accountsUi.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<AccountUi?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    val total = accounts.sumOf { it.balance }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "资产",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Spacer(Modifier.height(14.dp))
                AppCard {
                    Column(Modifier.padding(20.dp)) {
                        Text("净资产", fontSize = 12.sp, color = InkSoft)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "¥",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkSoft,
                                modifier = Modifier.padding(bottom = 7.dp, end = 4.dp)
                            )
                            Text(
                                text = (if (total < 0) "-¥" else "¥") + Money.format(abs(total)),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (total < 0) Danger else Ink,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "${accounts.size} 个账户",
                            fontSize = 11.sp,
                            color = InkFaint
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "账户",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkSoft,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAdd = true }
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Add, null, tint = AmberDeep, modifier = Modifier.size(15.dp))
                        Text("添加", fontSize = 12.sp, color = AmberDeep)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        if (accounts.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    title = "还没有账户",
                    hint = "添加支付宝、微信或银行卡"
                )
            }
        } else {
            items(accounts, key = { it.account.id }) { item ->
                AccountCard(item = item, onClick = { editing = item })
                Spacer(Modifier.height(10.dp))
            }
        }
    }

    if (showAdd) {
        AccountDialog(
            title = "添加账户",
            initialName = "",
            initialType = AccountType.CASH,
            initialBalance = "",
            onDismiss = { showAdd = false },
            onConfirm = { name, type, balance ->
                viewModel.addAccount(name, type, balance) { showAdd = false }
            }
        )
    }

    editing?.let { item ->
        AccountDialog(
            title = "编辑账户",
            initialName = item.account.name,
            initialType = item.account.type,
            initialBalance = Money.centsToInput(item.account.initialBalance),
            onDismiss = { editing = null },
            onConfirm = { name, _, balance ->
                viewModel.updateAccount(item.account, name, balance)
                editing = null
            }
        )
    }
}

@Composable
private fun AccountCard(item: AccountUi, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccountAvatar(item.account.name, item.account.type.name, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.account.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink
                )
            }
            Text(
                text = (if (item.balance < 0) "-¥" else "¥") + Money.format(abs(item.balance)),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (item.balance < 0) Danger else Ink
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                null,
                tint = InkFaint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AccountDialog(
    title: String,
    initialName: String,
    initialType: AccountType,
    initialBalance: String,
    onDismiss: () -> Unit,
    onConfirm: (String, AccountType, Long) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialType) }
    var balance by remember { mutableStateOf(initialBalance) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Card,
        shape = RoundedCornerShape(24.dp),
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("账户名称") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountType.values().forEach { option ->
                        val selected = option == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) AmberSoft else Paper)
                                .clickable { type = option }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = option.displayName(),
                                fontSize = 12.sp,
                                color = if (selected) Ink else InkSoft,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("期初余额") },
                    singleLine = true,
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), type, Money.inputToCents(balance))
                    }
                }
            ) {
                Text("保存", color = AmberDeep, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = InkSoft)
            }
        }
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Amber,
    unfocusedBorderColor = Line,
    focusedLabelColor = AmberDeep,
    cursorColor = Amber
)
