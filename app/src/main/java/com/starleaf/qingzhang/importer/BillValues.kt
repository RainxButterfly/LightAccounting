/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-11-01
 * 协议: Apache License 2.0
 *
 * 文件: BillValues.kt
 * 说明: 账单时间、金额与单号解析
 */

package com.starleaf.qingzhang.importer

import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object BillValues {

    private val zone: ZoneId = ZoneId.systemDefault()

    private val dateTimePatterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy/M/d HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy/M/d HH:mm",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy年M月d日 HH:mm:ss",
        "yyyy年M月d日 HH:mm",
        "yyyyMMdd HHmmss"
    ).map { DateTimeFormatter.ofPattern(it) }

    private val datePatterns = listOf(
        "yyyy-MM-dd",
        "yyyy/M/d",
        "yyyy.MM.dd",
        "yyyy年M月d日"
    ).map { DateTimeFormatter.ofPattern(it) }

    fun parseTime(text: String): Long? {
        val raw = text.trim().removeSurrounding("\"")
        if (raw.isBlank()) return null

        if (raw.all(Char::isDigit)) {
            when (raw.length) {
                13 -> return raw.toLongOrNull()
                10 -> return raw.toLongOrNull()?.times(1000)
                8 -> runCatching {
                    return LocalDate.parse(raw, DateTimeFormatter.BASIC_ISO_DATE)
                        .atStartOfDay(zone).toInstant().toEpochMilli()
                }
            }
        }

        for (pattern in dateTimePatterns) {
            runCatching {
                return LocalDateTime.parse(raw, pattern)
                    .atZone(zone).toInstant().toEpochMilli()
            }
        }
        for (pattern in datePatterns) {
            runCatching {
                return LocalDate.parse(raw, pattern)
                    .atStartOfDay(zone).toInstant().toEpochMilli()
            }
        }
        return null
    }

    fun parseAmount(text: String): Long? {
        var value = text.trim()
            .replace("¥", "")
            .replace("￥", "")
            .replace("元", "")
            .replace(",", "")
            .replace(" ", "")
            .replace("\u00A0", "")
        if (value.isBlank()) return null

        var negative = false
        if ((value.startsWith("(") && value.endsWith(")")) ||
            (value.startsWith("（") && value.endsWith("）"))
        ) {
            negative = true
            value = value.substring(1, value.length - 1)
        }
        if (value.startsWith("+")) value = value.drop(1)

        val cents = value.toBigDecimalOrNull()
            ?.movePointRight(2)
            ?.setScale(0, RoundingMode.HALF_UP)
            ?.toLong() ?: return null
        return if (negative) -cents else cents
    }

    fun normalizeNumber(value: Any?): String = when (value) {
        null -> ""
        is BigDecimal -> value.stripTrailingZeros().toPlainString()
        is Number -> value.toString()
        else -> value.toString()
    }

    fun sha1(value: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
