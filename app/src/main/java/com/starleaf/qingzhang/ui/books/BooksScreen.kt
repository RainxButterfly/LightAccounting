/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: BooksScreen.kt
 * 说明: 账本管理页面
 */

package com.starleaf.qingzhang.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.starleaf.qingzhang.data.Book
import com.starleaf.qingzhang.ui.AppViewModel
import com.starleaf.qingzhang.ui.components.AppCard
import com.starleaf.qingzhang.ui.components.ConfirmDialog
import com.starleaf.qingzhang.ui.components.PrimaryButton
import com.starleaf.qingzhang.ui.components.TextInputDialog
import com.starleaf.qingzhang.ui.components.TopBar
import com.starleaf.qingzhang.ui.theme.AmberDeep
import com.starleaf.qingzhang.ui.theme.AmberSoft
import com.starleaf.qingzhang.ui.theme.Ink
import com.starleaf.qingzhang.ui.theme.InkFaint
import com.starleaf.qingzhang.ui.theme.InkSoft

@Composable
fun BooksScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val books by viewModel.books.collectAsStateWithLifecycle()
    val current by viewModel.currentBook.collectAsStateWithLifecycle()

    var showAdd by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<Book?>(null) }
    var deleting by remember { mutableStateOf<Book?>(null) }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = "账本管理", onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            items(books, key = { it.id }) { book ->
                AppCard(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .clickable { viewModel.selectBook(book.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = book.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Ink
                                )
                                if (book.id == current?.id) {
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AmberSoft)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "当前",
                                            fontSize = 9.sp,
                                            color = AmberDeep,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "点击切换到这个账本",
                                fontSize = 11.sp,
                                color = InkFaint
                            )
                        }
                        IconButton(onClick = { renaming = book }) {
                            Icon(
                                Icons.Rounded.Edit,
                                "重命名",
                                tint = InkSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = { deleting = book }) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                "删除",
                                tint = InkSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = "新建账本",
                    onClick = { showAdd = true },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showAdd) {
        TextInputDialog(
            title = "新建账本",
            label = "账本名称",
            confirmText = "创建",
            onDismiss = { showAdd = false },
            onConfirm = { viewModel.addBook(it) }
        )
    }

    renaming?.let { book ->
        TextInputDialog(
            title = "重命名账本",
            label = "账本名称",
            initialValue = book.name,
            onDismiss = { renaming = null },
            onConfirm = { viewModel.renameBook(book, it) }
        )
    }

    deleting?.let { book ->
        ConfirmDialog(
            title = "删除「${book.name}」？",
            text = "账本里的所有记录会一起删除，且无法恢复。",
            confirmText = "删除",
            danger = true,
            onDismiss = { deleting = null },
            onConfirm = { viewModel.deleteBook(book.id) }
        )
    }
}
