/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: Format.kt
 * 说明: 金额与日期格式化工具
 */

package com.starleaf.qingzhang.util

import com.starleaf.qingzhang.data.AccountType
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Money {
    private val format = DecimalFormat("#,##0.00")

    fun format(cents: Long): String = format.format(BigDecimal.valueOf(cents, 2))

    fun inputToCents(input: String): Long =
        input.toBigDecimalOrNull()
            ?.movePointRight(2)
            ?.setScale(0, RoundingMode.HALF_UP)
            ?.toLong() ?: 0L

    fun centsToInput(cents: Long): String =
        if (cents == 0L) "" else BigDecimal.valueOf(cents, 2).stripTrailingZeros().toPlainString()
}

fun AccountType.displayName(): String = when (this) {
    AccountType.ALIPAY -> "支付宝"
    AccountType.WECHAT -> "微信"
    AccountType.BANK -> "银行卡"
    AccountType.CASH -> "现金"
    AccountType.OTHER -> "其他"
}

object Dates {
    val zone: ZoneId = ZoneId.systemDefault()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun monthStart(month: YearMonth): Long =
        month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()

    fun monthEndExclusive(month: YearMonth): Long = monthStart(month.plusMonths(1))

    fun monthLabel(month: YearMonth): String = "${month.year}年${month.monthValue}月"

    fun toLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun toLocalDateTime(millis: Long): LocalDateTime =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime()

    fun timeLabel(millis: Long): String = toLocalDateTime(millis).format(timeFormatter)

    fun dayLabel(date: LocalDate): String {
        val week = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        return "${date.monthValue}月${date.dayOfMonth}日 ${week[date.dayOfWeek.value - 1]}"
    }

    fun fromLocalDate(date: LocalDate, time: LocalDateTime): Long =
        date.atTime(time.toLocalTime()).atZone(zone).toInstant().toEpochMilli()
}
