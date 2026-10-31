/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: Seed.kt
 * 说明: 默认账本、账户与分类
 */

package com.starleaf.qingzhang.data

import com.starleaf.qingzhang.logic.CategoryRules

data class PresetCategory(
    val name: String,
    val iconKey: String,
    val colorIndex: Int,
    val kind: TxnKind
)

object Seed {

    val expenseCategories = listOf(
        PresetCategory("餐饮", "restaurant", 0, TxnKind.EXPENSE),
        PresetCategory("交通", "bus", 2, TxnKind.EXPENSE),
        PresetCategory("购物", "shopping", 5, TxnKind.EXPENSE),
        PresetCategory("日用", "basket", 3, TxnKind.EXPENSE),
        PresetCategory("娱乐", "movie", 4, TxnKind.EXPENSE),
        PresetCategory("医疗", "medical", 20, TxnKind.EXPENSE),
        PresetCategory("教育", "school", 11, TxnKind.EXPENSE),
        PresetCategory("住房", "house", 8, TxnKind.EXPENSE),
        PresetCategory("通讯", "phone", 1, TxnKind.EXPENSE),
        PresetCategory("人情", "gift", 9, TxnKind.EXPENSE),
        PresetCategory("宠物", "pets", 15, TxnKind.EXPENSE),
        PresetCategory("旅行", "flight", 6, TxnKind.EXPENSE),
        PresetCategory("运动", "fitness", 10, TxnKind.EXPENSE),
        PresetCategory("美容", "spa", 5, TxnKind.EXPENSE),
        PresetCategory("其他", "more", 14, TxnKind.EXPENSE)
    )

    val incomeCategories = listOf(
        PresetCategory("工资", "salary", 3, TxnKind.INCOME),
        PresetCategory("奖金", "award", 1, TxnKind.INCOME),
        PresetCategory("兼职", "parttime", 2, TxnKind.INCOME),
        PresetCategory("理财", "invest", 15, TxnKind.INCOME),
        PresetCategory("红包", "redpacket", 0, TxnKind.INCOME),
        PresetCategory("退款", "refund", 6, TxnKind.INCOME),
        PresetCategory("其他收入", "more_income", 14, TxnKind.INCOME)
    )

    val categories: List<PresetCategory> = expenseCategories + incomeCategories

    data class PresetAccount(val name: String, val type: AccountType)

    val accounts = listOf(
        PresetAccount("支付宝", AccountType.ALIPAY),
        PresetAccount("微信钱包", AccountType.WECHAT),
        PresetAccount("银行卡", AccountType.BANK),
        PresetAccount("现金", AccountType.CASH)
    )

    const val DEFAULT_BOOK_NAME = "日常账本"

    fun fallbackName(kind: TxnKind): String = when (kind) {
        TxnKind.INCOME -> "其他收入"
        else -> "其他"
    }
}
