/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: Components.kt
 * 说明: 通用界面组件
 */

package com.starleaf.qingzhang.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starleaf.qingzhang.ui.theme.Amber
import com.starleaf.qingzhang.ui.theme.AmberDeep
import com.starleaf.qingzhang.ui.theme.Card
import com.starleaf.qingzhang.ui.theme.CategoryIcons
import com.starleaf.qingzhang.ui.theme.Danger
import com.starleaf.qingzhang.ui.theme.Ink
import com.starleaf.qingzhang.ui.theme.InkFaint
import com.starleaf.qingzhang.ui.theme.InkSoft
import com.starleaf.qingzhang.ui.theme.Line
import com.starleaf.qingzhang.ui.theme.Paper
import com.starleaf.qingzhang.ui.theme.categoryColor
import java.time.YearMonth

@Composable
fun CategoryAvatar(
    iconKey: String,
    colorIndex: Int,
    size: Dp = 44.dp,
    selected: Boolean = false
) {
    val color = categoryColor(colorIndex)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (selected) color else color.copy(alpha = 0.13f))
            .then(
                if (selected) Modifier
                else Modifier.border(1.dp, color.copy(alpha = 0.16f), CircleShape)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = CategoryIcons.icon(iconKey),
            contentDescription = null,
            tint = if (selected) Color.White else color,
            modifier = Modifier.size(size * 0.46f)
        )
    }
}

@Composable
fun AccountAvatar(name: String, type: String, size: Dp = 40.dp) {
    val color = when (type) {
        "ALIPAY" -> Color(0xFF3D8BFD)
        "WECHAT" -> Color(0xFF2FBF71)
        "BANK" -> Color(0xFFD9A441)
        "CASH" -> Color(0xFF8C9BB0)
        else -> Color(0xFF9C88F0)
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(color.copy(alpha = 0.13f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.4f).sp
        )
    }
}

@Composable
fun QianBottomBar(
    selectedRoute: String,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Card)
            .navigationBarsPadding()
            .height(78.dp)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(62.dp),
            color = Card,
            shadowElevation = 18.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomItem(
                    "home",
                    "明细",
                    Icons.AutoMirrored.Rounded.ReceiptLong,
                    selectedRoute,
                    onSelect
                )
                BottomItem("charts", "图表", Icons.Rounded.PieChart, selectedRoute, onSelect)
                Spacer(Modifier.weight(1f))
                BottomItem(
                    "assets",
                    "资产",
                    Icons.Rounded.AccountBalanceWallet,
                    selectedRoute,
                    onSelect
                )
                BottomItem("profile", "我的", Icons.Rounded.Person, selectedRoute, onSelect)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(54.dp)
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(Amber)
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "记一笔",
                tint = Ink,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun RowScope.BottomItem(
    route: String,
    label: String,
    icon: ImageVector,
    selectedRoute: String,
    onSelect: (String) -> Unit
) {
    val selected = route == selectedRoute
    Column(
        modifier = Modifier
            .weight(1f)
            .height(62.dp)
            .clickable { onSelect(route) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Ink else InkFaint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (selected) Ink else InkFaint,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun TopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "返回",
                    tint = Ink
                )
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            modifier = Modifier.weight(1f)
        )
        actions()
        Spacer(Modifier.width(6.dp))
    }
}

@Composable
fun MonthSwitcher(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    canNext: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(32.dp)) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "上个月", tint = InkSoft)
        }
        Text(
            text = "${month.year}年${month.monthValue}月",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        IconButton(
            onClick = onNext,
            enabled = canNext,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                "下个月",
                tint = if (canNext) InkSoft else InkFaint.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Card)
            .border(1.dp, Line, RoundedCornerShape(22.dp)),
        content = content
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Amber,
            contentColor = Ink,
            disabledContainerColor = Line,
            disabledContentColor = InkFaint
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    tint: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val resolvedTint = tint ?: InkSoft
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(resolvedTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = resolvedTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        if (onClick != null) {
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                null,
                tint = InkFaint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    hint: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Paper),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = InkFaint, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = InkSoft)
        Spacer(Modifier.height(6.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = InkFaint
        )
    }
}

@Composable
fun TextInputDialog(
    title: String,
    label: String,
    initialValue: String = "",
    confirmText: String = "确定",
    keyboardType: KeyboardType = KeyboardType.Text,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Card,
        shape = RoundedCornerShape(24.dp),
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber,
                    unfocusedBorderColor = Line,
                    focusedLabelColor = AmberDeep,
                    cursorColor = Amber
                )
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(value.trim())
                onDismiss()
            }) {
                Text(confirmText, color = AmberDeep, fontWeight = FontWeight.SemiBold)
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
fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String = "确定",
    danger: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Card,
        shape = RoundedCornerShape(24.dp),
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(text, style = MaterialTheme.typography.bodyMedium, color = InkSoft) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(
                    confirmText,
                    color = if (danger) Danger else AmberDeep,
                    fontWeight = FontWeight.SemiBold
                )
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
fun AmountText(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = color
    )
}

@Composable
fun GroupTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = InkFaint,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(start = 20.dp, bottom = 8.dp)
    )
}
