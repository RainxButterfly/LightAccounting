/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: PayNotificationParser.kt
 * 说明: 支付通知文本解析
 */

package com.starleaf.qingzhang.notify

import com.starleaf.qingzhang.data.TxnKind
import java.math.BigDecimal
import java.math.RoundingMode

object PayNotificationParser {

    data class Captured(val amountCents: Long, val kind: TxnKind, val text: String)

    private val yuanRegex = Regex("""([0-9][0-9,]*(?:\.[0-9]{1,2})?)\s*元""")
    private val symbolRegex = Regex("""[¥￥]\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)""")

    private val incomeWords = listOf("收款", "收到", "入账", "到账", "红包")
    private val expenseWords = listOf("支付", "付款", "消费", "扣款", "转账", "支出")
    private val paymentWords = incomeWords + expenseWords

    fun parse(packageName: String, title: String, content: String): Captured? {
        val combined = listOf(title, content)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()
        if (combined.length < 3) return null
        if (paymentWords.none { combined.contains(it) }) return null

        val raw = symbolRegex.find(combined)?.groupValues?.get(1)
            ?: yuanRegex.find(combined)?.groupValues?.get(1)
            ?: return null
        val cents = runCatching {
            BigDecimal(raw.replace(",", "")).movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP).toLong()
        }.getOrNull() ?: return null
        if (cents <= 0) return null

        val kind = when {
            incomeWords.any { combined.contains(it) } -> TxnKind.INCOME
            expenseWords.any { combined.contains(it) } -> TxnKind.EXPENSE
            else -> return null
        }
        return Captured(cents, kind, combined.take(120))
    }
}
