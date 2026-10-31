/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: BillModels.kt
 * 说明: 账单导入数据模型
 */

package com.starleaf.qingzhang.importer

import com.starleaf.qingzhang.data.TxnKind

data class BillRow(
    val time: Long,
    val amountCents: Long,
    val kind: TxnKind,
    val counterparty: String,
    val note: String,
    val typeText: String,
    val externalId: String
)

data class ParsedBill(
    val rows: List<BillRow>,
    val skipped: Int,
    val sourceLabel: String
) {
    val expenseTotal: Long get() = rows.filter { it.kind == TxnKind.EXPENSE }.sumOf { it.amountCents }
    val incomeTotal: Long get() = rows.filter { it.kind == TxnKind.INCOME }.sumOf { it.amountCents }
}

data class ImportCommit(val imported: Int, val duplicates: Int)

class BillImportException(message: String) : Exception(message)
