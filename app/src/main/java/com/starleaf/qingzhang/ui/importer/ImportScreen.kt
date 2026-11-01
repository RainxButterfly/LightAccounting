/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: ImportScreen.kt
 * 说明: 账单导入页面
 */

package com.starleaf.qingzhang.ui.importer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.starleaf.qingzhang.data.TxnKind
import com.starleaf.qingzhang.importer.BillImportException
import com.starleaf.qingzhang.importer.ImportCommit
import com.starleaf.qingzhang.importer.ParsedBill
import com.starleaf.qingzhang.ui.AccountUi
import com.starleaf.qingzhang.ui.AppViewModel
import com.starleaf.qingzhang.ui.components.AccountAvatar
import com.starleaf.qingzhang.ui.components.AppCard
import com.starleaf.qingzhang.ui.components.PrimaryButton
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
import com.starleaf.qingzhang.ui.theme.Line
import com.starleaf.qingzhang.ui.theme.Paper
import com.starleaf.qingzhang.util.Dates
import com.starleaf.qingzhang.util.Money
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val accounts by viewModel.accountsUi.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var parsing by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<ParsedBill?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var importing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ImportCommit?>(null) }
    var showAccountSheet by remember { mutableStateOf(false) }

    var accountId by remember { mutableStateOf(0L) }
    val selectedAccount = accounts.firstOrNull { it.account.id == accountId }
        ?: accounts.firstOrNull()
    val effectiveAccountId = selectedAccount?.account?.id ?: 0L

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            fileUri = uri
            fileName = displayName(context, uri) ?: "账单文件"
            preview = null
            result = null
            error = null
        }
    }

    fun startParse() {
        val uri = fileUri ?: return
        error = null
        parsing = true
        scope.launch {
            try {
                preview = viewModel.parseBill(context, uri, password)
            } catch (e: BillImportException) {
                error = e.message
            } catch (e: Exception) {
                error = "解析失败，请确认选择的是支付宝或微信导出的账单文件"
            } finally {
                parsing = false
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = "账单导入", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                end = 20.dp,
                bottom = 28.dp
            )
        ) {
            item {
                Column {
                AppCard {
                    Column(Modifier.padding(18.dp)) {
                        Text(
                            "怎么导出账单",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink
                        )
                        Spacer(Modifier.height(12.dp))
                        StepText(
                            "1",
                            "支付宝",
                            "我的 → 账单 → 右上角「…」→ 开具交易流水证明 → 用于个人对账，文件会发送到邮箱。"
                        )
                        Spacer(Modifier.height(10.dp))
                        StepText(
                            "2",
                            "微信",
                            "我 → 服务 → 钱包 → 账单 → 右上角「常见问题」→ 下载账单 → 用于个人对账，解压密码会在微信服务通知里下发。"
                        )
                        Spacer(Modifier.height(10.dp))
                        StepText(
                            "3",
                            "钱迹 / 其他应用",
                            "在应用的数据导出中选择 CSV 或 JSON。轻账会按列名识别日期、金额、收支类型、分类和账户。"
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "文件只在本机解析，不会上传。",
                            fontSize = 11.sp,
                            color = InkFaint
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                AppCard(modifier = Modifier.clickable {
                    picker.launch(arrayOf("*/*"))
                }) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(AmberSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.UploadFile,
                                null,
                                tint = AmberDeep,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = if (fileName.isBlank()) "选择账单文件" else fileName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (fileName.isBlank()) "支持 CSV、JSON 和 ZIP 压缩包" else "点击重新选择",
                                fontSize = 11.sp,
                                color = InkFaint
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            null,
                            tint = InkFaint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("解压密码（加密 ZIP 需要）") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Amber,
                        unfocusedBorderColor = Line,
                        focusedLabelColor = AmberDeep,
                        cursorColor = Amber,
                        focusedContainerColor = Card,
                        unfocusedContainerColor = Card
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                AppCard(modifier = Modifier.clickable { showAccountSheet = true }) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedAccount != null) {
                            AccountAvatar(
                                selectedAccount.account.name,
                                selectedAccount.account.type.name,
                                size = 36.dp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("默认账户", fontSize = 11.sp, color = InkFaint)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                selectedAccount?.account?.name ?: "请先添加账户",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Ink
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            null,
                            tint = InkFaint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "文件包含账户时按名称匹配或新建；未包含时使用默认账户。",
                    fontSize = 10.sp,
                    color = InkFaint,
                    lineHeight = 15.sp
                )

                Spacer(Modifier.height(18.dp))

                if (result == null) {
                    PrimaryButton(
                        text = if (parsing) "正在解析…" else "解析账单",
                        onClick = { startParse() },
                        enabled = fileUri != null && !parsing && effectiveAccountId > 0,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (parsing) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Amber,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                error?.let {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = Danger,
                        lineHeight = 18.sp
                    )
                }

                val currentPreview = preview
                if (currentPreview != null && result == null) {
                    Spacer(Modifier.height(18.dp))
                    AppCard {
                        Column(Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentPreview.sourceLabel,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    "共 ${currentPreview.rows.size} 笔",
                                    fontSize = 11.sp,
                                    color = InkFaint
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Row {
                                PreviewStat("支出", "¥" + Money.format(currentPreview.expenseTotal), Ink)
                                Spacer(Modifier.width(24.dp))
                                PreviewStat("收入", "¥" + Money.format(currentPreview.incomeTotal), Income)
                                Spacer(Modifier.weight(1f))
                                PreviewStat("已忽略", currentPreview.skipped.toString(), InkFaint)
                            }
                            Spacer(Modifier.height(14.dp))
                            currentPreview.rows.take(5).forEach { row ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = row.counterparty.ifBlank { row.note.ifBlank { "未命名交易" } },
                                            fontSize = 12.sp,
                                            color = Ink,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = buildString {
                                                row.categoryName?.takeIf { it.isNotBlank() }
                                                    ?.let { append(it).append(" · ") }
                                                row.accountName?.takeIf { it.isNotBlank() }
                                                    ?.let { append(it).append(" · ") }
                                                val date = Dates.toLocalDate(row.time)
                                                append(Dates.timeLabel(row.time))
                                                append(" · ")
                                                append(date.monthValue)
                                                append("月")
                                                append(date.dayOfMonth)
                                                append("日")
                                            },
                                            fontSize = 10.sp,
                                            color = InkFaint
                                        )
                                    }
                                    Text(
                                        text = (if (row.kind == TxnKind.INCOME) "+" else "-") + Money.format(row.amountCents),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (row.kind == TxnKind.INCOME) Income else Ink
                                    )
                                }
                            }
                            if (currentPreview.rows.size > 5) {
                                Text(
                                    "还有 ${currentPreview.rows.size - 5} 笔…",
                                    fontSize = 10.sp,
                                    color = InkFaint,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "转账和不计收支的记录会被忽略；退款、关闭状态的支付宝/微信记录也会跳过。同一份账单重复导入不会产生重复记录。",
                                fontSize = 10.sp,
                                color = InkFaint,
                                lineHeight = 16.sp
                            )
                            Spacer(Modifier.height(14.dp))
                            PrimaryButton(
                                text = if (importing) "正在导入…" else "确认导入 ${currentPreview.rows.size} 笔",
                                onClick = {
                                    importing = true
                                    viewModel.commitImport(
                                        rows = currentPreview.rows,
                                        accountId = effectiveAccountId
                                    ) { commit ->
                                        importing = false
                                        result = commit
                                    }
                                },
                                enabled = !importing && effectiveAccountId > 0,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                val commit = result
                if (commit != null) {
                    Spacer(Modifier.height(18.dp))
                    AppCard {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 26.dp, horizontal = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(AmberSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Check,
                                    null,
                                    tint = AmberDeep,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "导入完成",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "新增 ${commit.imported} 笔，跳过重复 ${commit.duplicates} 笔",
                                fontSize = 12.sp,
                                color = InkSoft
                            )
                            Spacer(Modifier.height(18.dp))
                            PrimaryButton(
                                text = "完成",
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                }
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
                    "导入到账户",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    color = Ink,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                )
                accounts.forEach { item ->
                    AccountChoiceRow(
                        item = item,
                        selected = item.account.id == effectiveAccountId
                    ) {
                        accountId = item.account.id
                        showAccountSheet = false
                    }
                }
            }
        }
    }
}

@Composable
private fun StepText(index: String, title: String, body: String) {
    Row {
        Box(
            Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(AmberSoft),
            contentAlignment = Alignment.Center
        ) {
            Text(index, fontSize = 10.sp, color = AmberDeep, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Ink)
            Spacer(Modifier.height(2.dp))
            Text(body, fontSize = 11.sp, color = InkSoft, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun PreviewStat(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column {
        Text(label, fontSize = 10.sp, color = InkFaint)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun AccountChoiceRow(item: AccountUi, selected: Boolean, onClick: () -> Unit) {
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
            Text("余额 ¥" + Money.format(item.balance), fontSize = 11.sp, color = InkFaint)
        }
        if (selected) {
            Icon(Icons.Rounded.Check, null, tint = Amber, modifier = Modifier.size(20.dp))
        }
    }
}

private fun displayName(context: Context, uri: Uri): String? = runCatching {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
}.getOrNull()
