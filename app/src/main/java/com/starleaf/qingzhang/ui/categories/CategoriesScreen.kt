/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: CategoriesScreen.kt
 * 说明: 分类管理页面
 */

package com.starleaf.qingzhang.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.starleaf.qingzhang.data.Category
import com.starleaf.qingzhang.data.TxnKind
import com.starleaf.qingzhang.ui.AppViewModel
import com.starleaf.qingzhang.ui.components.CategoryAvatar
import com.starleaf.qingzhang.ui.components.ConfirmDialog
import com.starleaf.qingzhang.ui.components.TopBar
import com.starleaf.qingzhang.ui.theme.Amber
import com.starleaf.qingzhang.ui.theme.AmberDeep
import com.starleaf.qingzhang.ui.theme.AmberSoft
import com.starleaf.qingzhang.ui.theme.Card
import com.starleaf.qingzhang.ui.theme.CategoryColors
import com.starleaf.qingzhang.ui.theme.CategoryIcons
import com.starleaf.qingzhang.ui.theme.Ink
import com.starleaf.qingzhang.ui.theme.InkFaint
import com.starleaf.qingzhang.ui.theme.InkSoft
import com.starleaf.qingzhang.ui.theme.Line
import com.starleaf.qingzhang.ui.theme.Paper
import com.starleaf.qingzhang.ui.theme.categoryColor

@Composable
fun CategoriesScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var kind by remember { mutableStateOf(TxnKind.EXPENSE) }
    var showAdd by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Category?>(null) }

    val list = categories.filter { it.kind == kind }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = "分类管理", onBack = onBack)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Card)
                .padding(4.dp)
        ) {
            TabPill("支出", kind == TxnKind.EXPENSE, Modifier.weight(1f)) {
                kind = TxnKind.EXPENSE
            }
            TabPill("收入", kind == TxnKind.INCOME, Modifier.weight(1f)) {
                kind = TxnKind.INCOME
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(list, key = { it.id }) { category ->
                CategoryTile(
                    category = category,
                    onDelete = { deleting = category }
                )
            }
            item {
                AddTile { showAdd = true }
            }
            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showAdd) {
        AddCategoryDialog(
            kind = kind,
            nextColorIndex = list.size % CategoryColors.size,
            onDismiss = { showAdd = false },
            onConfirm = { name, iconKey ->
                viewModel.addCategory(name, kind, iconKey, list.size % CategoryColors.size)
            }
        )
    }

    deleting?.let { category ->
        ConfirmDialog(
            title = "删除「${category.name}」？",
            text = "只有没有被记录使用的分类才能删除。",
            confirmText = "删除",
            danger = true,
            onDismiss = { deleting = null },
            onConfirm = { viewModel.deleteCategory(category.id) }
        )
    }
}

@Composable
private fun TabPill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) AmberSoft else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Ink else InkSoft
        )
    }
}

@Composable
private fun CategoryTile(category: Category, onDelete: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CategoryAvatar(
                iconKey = category.iconKey,
                colorIndex = category.colorIndex,
                size = 48.dp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = category.name,
                fontSize = 11.sp,
                color = InkSoft,
                maxLines = 1
            )
        }
        if (!category.isPreset) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 6.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Card)
                    .border(1.dp, Line, CircleShape)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "删除分类",
                    tint = InkFaint,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

@Composable
private fun AddTile(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(1.dp, Line, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Add, "添加分类", tint = InkFaint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text("添加", fontSize = 11.sp, color = InkFaint)
    }
}

@Composable
private fun AddCategoryDialog(
    kind: TxnKind,
    nextColorIndex: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var iconKey by remember { mutableStateOf("more") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Card,
        shape = RoundedCornerShape(24.dp),
        title = { Text("新建" + (if (kind == TxnKind.INCOME) "收入" else "支出") + "分类", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Amber,
                        unfocusedBorderColor = Line,
                        focusedLabelColor = AmberDeep,
                        cursorColor = Amber
                    )
                )
                Spacer(Modifier.height(14.dp))
                Text("图标", fontSize = 12.sp, color = InkSoft)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryIcons.keys.forEachIndexed { index, key ->
                        val selected = key == iconKey
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) categoryColor(nextColorIndex).copy(alpha = 0.16f)
                                    else Paper
                                )
                                .then(
                                    if (selected) Modifier.border(
                                        1.5.dp,
                                        categoryColor(nextColorIndex),
                                        CircleShape
                                    ) else Modifier
                                )
                                .clickable { iconKey = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CategoryIcons.icon(key),
                                contentDescription = null,
                                tint = categoryColor(index),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), iconKey)
                        onDismiss()
                    }
                }
            ) {
                Text("创建", color = AmberDeep, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = InkSoft)
            }
        }
    )
}
