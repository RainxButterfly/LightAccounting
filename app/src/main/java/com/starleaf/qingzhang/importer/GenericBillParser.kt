/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-11-01
 * 协议: Apache License 2.0
 *
 * 文件: GenericBillParser.kt
 * 说明: 钱迹等第三方记账应用的 CSV 与 JSON 账单解析
 */

package com.starleaf.qingzhang.importer

import com.starleaf.qingzhang.data.TxnKind
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

object GenericBillParser {

    private val timeAliases = setOf(
        "日期时间", "日期", "时间", "记账日期", "记账时间", "交易时间", "记录时间", "创建时间",
        "datetime", "date", "time", "timestamp",
        "recordtime", "createdat", "createdtime", "tradetime", "billtime", "occurtime"
    )
    private val amountAliases = setOf(
        "金额", "交易金额", "发生金额", "amount", "money", "total"
    )
    private val expenseAmountAliases = setOf(
        "支出金额", "支出", "expense", "debit"
    )
    private val incomeAmountAliases = setOf(
        "收入金额", "收入", "income", "credit"
    )
    private val typeAliases = setOf(
        "收支类型", "收/支", "收支", "类型", "交易类型", "方向", "direction", "type", "kind"
    )
    private val categoryAliases = setOf("一级分类", "分类", "类别", "category")
    private val subCategoryAliases = setOf("二级分类", "子分类", "子类别", "subcategory")
    private val accountAliases = setOf(
        "账户名称", "账户", "资产账户", "支付账户", "account", "wallet"
    )
    private val noteAliases = setOf(
        "备注信息", "备注", "说明", "摘要", "note", "remark", "memo", "description"
    )
    private val counterpartyAliases = setOf(
        "对方户名", "交易对方", "对方", "商户名称", "商户", "merchant", "counterparty", "payee"
    )
    private val idAliases = setOf(
        "交易单号", "交易订单号", "订单号", "单号", "transaction_id", "transactionid",
        "order_id", "recordid", "billid", "uuid", "id"
    )
    private val ignoreAliases = setOf("不计收支", "不计入收支")

    private val truthyValues = setOf("是", "true", "1", "yes", "y")

    fun parseCsv(table: List<List<String>>, fileName: String): ParsedBill {
        val headerIndex = findHeader(table)
        if (headerIndex < 0) {
            throw BillImportException(
                "未识别账单格式，请选择支付宝、微信、钱迹等应用导出的 CSV 或 JSON"
            )
        }

        val header = table[headerIndex].map { normalizeHeader(it) }
        fun column(aliases: Set<String>): Int = header.indexOfFirst { matches(it, aliases) }

        val timeCol = column(timeAliases)
        val amountCol = column(amountAliases)
        val expenseCol = column(expenseAmountAliases)
        val incomeCol = column(incomeAmountAliases)
        val typeCol = column(typeAliases)
        val categoryCol = column(categoryAliases)
        val subCategoryCol = column(subCategoryAliases)
        val accountCol = column(accountAliases)
        val noteCol = column(noteAliases)
        val counterpartyCol = column(counterpartyAliases)
        val idCol = column(idAliases)
        val ignoreCol = column(ignoreAliases)

        if (timeCol < 0) {
            throw BillImportException("账单缺少日期或时间列")
        }
        if (amountCol < 0 && expenseCol < 0 && incomeCol < 0) {
            throw BillImportException("账单缺少金额列")
        }

        val rows = ArrayList<BillRow>()
        var skipped = 0

        for (index in headerIndex + 1 until table.size) {
            val cells = table[index]
            if (cells.all { it.isBlank() }) continue

            fun cell(columnIndex: Int): String =
                if (columnIndex < 0) "" else cells.getOrNull(columnIndex)?.trim().orEmpty()

            val time = BillValues.parseTime(cell(timeCol))
            if (time == null) {
                skipped++
                continue
            }
            if (cell(ignoreCol).lowercase() in truthyValues) {
                skipped++
                continue
            }

            val typeText = cell(typeCol)
            var kind = parseKind(typeText)
            if (kind == TxnKind.TRANSFER) {
                skipped++
                continue
            }

            val rawAmount = cell(amountCol)
            if (kind == null) {
                val signed = BillValues.parseAmount(rawAmount)
                kind = when {
                    signed != null && signed < 0 -> TxnKind.EXPENSE
                    signed != null -> TxnKind.INCOME
                    cell(expenseCol).isNotBlank() -> TxnKind.EXPENSE
                    cell(incomeCol).isNotBlank() -> TxnKind.INCOME
                    else -> null
                }
            }
            if (kind == null) {
                skipped++
                continue
            }

            val specificAmount = when (kind) {
                TxnKind.INCOME -> cell(incomeCol)
                TxnKind.EXPENSE -> cell(expenseCol)
                else -> ""
            }
            val amount = BillValues.parseAmount(
                if (specificAmount.isNotBlank()) specificAmount else rawAmount
            )?.let { abs(it) } ?: 0L
            if (amount <= 0) {
                skipped++
                continue
            }

            val category = cell(categoryCol)
            val subCategory = cell(subCategoryCol)
            val categoryName = when {
                category.isBlank() -> subCategory
                subCategory.isBlank() -> category
                else -> "$category/$subCategory"
            }
            val accountName = cell(accountCol)
            val note = cell(noteCol)
            val counterparty = cell(counterpartyCol)
            val sourceId = cell(idCol)

            val externalId = if (sourceId.isNotBlank()) {
                "import:$sourceId"
            } else {
                "import:" + BillValues.sha1(
                    "$time|$kind|$amount|$accountName|$categoryName|$note|$counterparty"
                )
            }

            rows += BillRow(
                time = time,
                amountCents = amount,
                kind = kind,
                counterparty = counterparty,
                note = note,
                typeText = typeText,
                externalId = externalId,
                categoryName = categoryName.takeIf { it.isNotBlank() },
                accountName = accountName.takeIf { it.isNotBlank() }
            )
        }

        if (rows.isEmpty()) {
            throw BillImportException("账单里没有可以导入的收支记录")
        }

        val headerText = table[headerIndex].joinToString("|")
        return ParsedBill(
            rows = rows,
            skipped = skipped,
            sourceLabel = sourceLabel(fileName, headerText),
            containsAccounts = accountCol >= 0
        )
    }

    fun parseJson(text: String, fileName: String): ParsedBill {
        val trimmed = text.trimStart('\uFEFF')
        val root: Any = if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else {
            JSONObject(trimmed)
        }

        val records = findRecordArray(root, 0)
            ?: throw BillImportException("JSON 里没有找到账单记录")

        val rows = ArrayList<BillRow>()
        var skipped = 0
        var hasAccounts = false

        for (index in 0 until records.length()) {
            val record = records.optJSONObject(index) ?: run {
                skipped++
                null
            } ?: continue

            val time = parseTimeValue(valueOf(record, timeAliases))
            if (time == null) {
                skipped++
                continue
            }
            if (asText(valueOf(record, ignoreAliases)).lowercase() in truthyValues) {
                skipped++
                continue
            }

            val typeText = asText(valueOf(record, typeAliases))
            var kind = parseKind(typeText)
            if (kind == TxnKind.TRANSFER) {
                skipped++
                continue
            }

            val rawAmount = valueOf(record, amountAliases)
            if (kind == null) {
                val signed = BillValues.parseAmount(BillValues.normalizeNumber(rawAmount))
                kind = when {
                    signed != null && signed < 0 -> TxnKind.EXPENSE
                    signed != null -> TxnKind.INCOME
                    asText(valueOf(record, expenseAmountAliases)).isNotBlank() -> TxnKind.EXPENSE
                    asText(valueOf(record, incomeAmountAliases)).isNotBlank() -> TxnKind.INCOME
                    else -> null
                }
            }
            if (kind == null) {
                skipped++
                continue
            }

            val specific = when (kind) {
                TxnKind.INCOME -> valueOf(record, incomeAmountAliases)
                TxnKind.EXPENSE -> valueOf(record, expenseAmountAliases)
                else -> null
            }
            val amountText = BillValues.normalizeNumber(specific).ifBlank {
                BillValues.normalizeNumber(rawAmount)
            }
            val amount = BillValues.parseAmount(amountText)?.let { abs(it) } ?: 0L
            if (amount <= 0) {
                skipped++
                continue
            }

            val category = asText(valueOf(record, categoryAliases))
            val subCategory = asText(valueOf(record, subCategoryAliases))
            val categoryName = when {
                category.isBlank() -> subCategory
                subCategory.isBlank() -> category
                else -> "$category/$subCategory"
            }
            val accountName = asText(valueOf(record, accountAliases))
            if (accountName.isNotBlank()) hasAccounts = true
            val note = asText(valueOf(record, noteAliases))
            val counterparty = asText(valueOf(record, counterpartyAliases))
            val sourceId = asText(valueOf(record, idAliases))

            val externalId = if (sourceId.isNotBlank()) {
                "import:$sourceId"
            } else {
                "import:" + BillValues.sha1(
                    "$time|$kind|$amount|$accountName|$categoryName|$note|$counterparty"
                )
            }

            rows += BillRow(
                time = time,
                amountCents = amount,
                kind = kind,
                counterparty = counterparty,
                note = note,
                typeText = typeText,
                externalId = externalId,
                categoryName = categoryName.takeIf { it.isNotBlank() },
                accountName = accountName.takeIf { it.isNotBlank() }
            )
        }

        if (rows.isEmpty()) {
            throw BillImportException("JSON 里没有可以导入的收支记录")
        }

        return ParsedBill(
            rows = rows,
            skipped = skipped,
            sourceLabel = if (fileName.contains("钱迹") || fileName.contains("qianji", ignoreCase = true)) {
                "钱迹账单"
            } else {
                "JSON 账单"
            },
            containsAccounts = hasAccounts
        )
    }

    private fun findHeader(table: List<List<String>>): Int {
        val limit = minOf(table.size, 40)
        for (index in 0 until limit) {
            val cells = table[index].map { normalizeHeader(it) }
            if (cells.isEmpty()) continue
            val hasTime = cells.any { matches(it, timeAliases) }
            val hasAmount = cells.any {
                matches(it, amountAliases) ||
                    matches(it, expenseAmountAliases) ||
                    matches(it, incomeAmountAliases)
            }
            if (hasTime && hasAmount) return index
        }
        return -1
    }

    private fun findRecordArray(value: Any?, depth: Int): JSONArray? {
        if (depth > 3) return null
        return when (value) {
            is JSONArray -> value
            is JSONObject -> {
                for (key in listOf("records", "transactions", "bills", "items", "list", "details", "data")) {
                    val actualKey = findKey(value, setOf(key)) ?: continue
                    val child = value.opt(actualKey)
                    if (child is JSONArray) return child
                    if (child is JSONObject) {
                        findRecordArray(child, depth + 1)?.let { return it }
                    }
                }
                val keys = value.keys()
                while (keys.hasNext()) {
                    val child = value.opt(keys.next())
                    if (child is JSONArray && child.length() > 0 && child.opt(0) is JSONObject) {
                        return child
                    }
                    if (child is JSONObject) {
                        findRecordArray(child, depth + 1)?.let { return it }
                    }
                }
                if (looksLikeRecord(value)) JSONArray().put(value) else null
            }
            else -> null
        }
    }

    private fun looksLikeRecord(record: JSONObject): Boolean =
        valueOf(record, timeAliases) != null &&
            (valueOf(record, amountAliases) != null ||
                valueOf(record, expenseAmountAliases) != null ||
                valueOf(record, incomeAmountAliases) != null)

    private fun findKey(record: JSONObject, aliases: Set<String>): String? {
        val keys = record.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (matches(normalizeHeader(key), aliases)) return key
        }
        return null
    }

    private fun valueOf(record: JSONObject, aliases: Set<String>): Any? {
        val key = findKey(record, aliases) ?: return null
        return record.opt(key)
    }

    private fun asText(value: Any?): String = when (value) {
        null, JSONObject.NULL -> ""
        is JSONObject -> listOf("name", "title", "label", "text")
            .firstNotNullOfOrNull { key ->
                value.optString(key).takeIf { it.isNotBlank() }
            }.orEmpty()
        is JSONArray -> (0 until value.length())
            .mapNotNull { index -> asText(value.opt(index)).takeIf { it.isNotBlank() } }
            .joinToString("、")
        else -> value.toString()
    }

    private fun parseTimeValue(value: Any?): Long? = when (value) {
        null, JSONObject.NULL -> null
        is Number, is String -> BillValues.parseTime(value.toString())
        else -> null
    }

    private fun parseKind(value: String): TxnKind? {
        val text = value.trim().lowercase()
        if (text.isBlank() || text == "/") return null
        return when {
            text.contains("转账") || text.contains("transfer") ||
                text.contains("还款") || text.contains("借入") ||
                text.contains("借出") || text.contains("不计") -> TxnKind.TRANSFER

            text.contains("收入") || text.contains("income") ||
                text.contains("收") -> TxnKind.INCOME

            text.contains("支出") || text.contains("expense") ||
                text.contains("支") || text.contains("pay") -> TxnKind.EXPENSE

            else -> null
        }
    }

    private fun sourceLabel(fileName: String, headerText: String): String {
        val text = "$fileName|$headerText".lowercase()
        return when {
            text.contains("钱迹") || text.contains("qianji") -> "钱迹账单"
            text.contains("随手记") -> "随手记账单"
            text.contains("挖财") -> "挖财账单"
            else -> "通用账单"
        }
    }

    private fun normalizeHeader(value: String): String =
        value.trim()
            .removePrefix("\uFEFF")
            .lowercase()
            .replace(" ", "")
            .replace("\u00A0", "")
            .removeSuffix("*")

    private fun matches(value: String, aliases: Set<String>): Boolean =
        aliases.any { alias -> value == alias || value.startsWith(alias) }
}
