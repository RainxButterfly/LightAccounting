/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: BillImporter.kt
 * 说明: 支付宝与微信账单文件解析
 */

package com.starleaf.qingzhang.importer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.starleaf.qingzhang.data.TxnKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.io.File
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object BillImporter {

    private val timePatterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy/M/d HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy/M/d HH:mm",
        "yyyy-MM-dd'T'HH:mm:ss"
    ).map { DateTimeFormatter.ofPattern(it) }

    suspend fun load(context: Context, uri: Uri, password: String): ParsedBill =
        withContext(Dispatchers.IO) {
            val name = queryName(context, uri) ?: "账单文件"
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw BillImportException("无法读取这个文件，请重新选择")

            val text = if (isZip(bytes)) {
                extractCsv(context, bytes, password)
            } else {
                decode(bytes)
            }
            parse(text, name)
        }

    private fun queryName(context: Context, uri: Uri): String? =
        runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else null
            }
        }.getOrNull()

    private fun isZip(bytes: ByteArray): Boolean =
        bytes.size > 4 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()

    private fun extractCsv(context: Context, bytes: ByteArray, password: String): String {
        val temp = File(context.cacheDir, "bill_${System.currentTimeMillis()}.zip")
        temp.writeBytes(bytes)
        try {
            ZipFile(temp).use { zip ->
                if (zip.isEncrypted) {
                    if (password.isBlank()) {
                        throw BillImportException(
                            "这个压缩包有密码。微信账单的解压密码会通过微信服务通知发给你，查看后填进来再试一次。"
                        )
                    }
                    zip.setPassword(password.toCharArray())
                }
                val header = zip.fileHeaders.firstOrNull {
                    !it.isDirectory && it.fileName.endsWith(".csv", ignoreCase = true)
                } ?: throw BillImportException("压缩包里没有找到 CSV 账单文件")
                val csvBytes = zip.getInputStream(header).use { it.readBytes() }
                return decode(csvBytes)
            }
        } catch (e: BillImportException) {
            throw e
        } catch (e: ZipException) {
            throw BillImportException("压缩包打开失败，密码可能不正确，请重新输入")
        } catch (e: Exception) {
            throw BillImportException("解压账单失败，请确认文件没有损坏")
        } finally {
            temp.delete()
        }
    }

    private fun decode(bytes: ByteArray): String {
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()
        ) {
            return String(bytes, 3, bytes.size - 3, StandardCharsets.UTF_8)
        }
        val decoder = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        return runCatching { decoder.decode(ByteBuffer.wrap(bytes)).toString() }
            .getOrElse { String(bytes, charset("GBK")) }
    }

    internal fun parse(text: String, fileName: String): ParsedBill {
        if (text.isBlank()) throw BillImportException("账单文件是空的")

        val table = parseCsv(text)
        val headerIndex = table.indexOfFirst { row ->
            row.firstOrNull()?.trim() == "交易时间"
        }
        if (headerIndex < 0) {
            throw BillImportException("没有找到账单表头，请选择支付宝或微信导出的账单 CSV")
        }

        val header = table[headerIndex].map { it.trim() }
        fun col(vararg names: String): Int =
            header.indexOfFirst { cell -> names.any { cell.startsWith(it) } }

        val timeCol = col("交易时间")
        val directionCol = col("收/支")
        val amountCol = col("金额")
        val partyCol = col("交易对方")
        val noteCol = col("商品说明", "商品")
        val orderCol = col("交易订单号", "交易单号")
        val statusCol = col("当前状态", "交易状态")
        val typeCol = col("交易分类", "交易类型")

        if (timeCol < 0 || directionCol < 0 || amountCol < 0) {
            throw BillImportException("账单格式不完整，缺少时间、收支或金额列")
        }

        val isAlipay = header.any { it.startsWith("交易订单号") || it.startsWith("交易分类") }
        val sourceLabel = when {
            isAlipay -> "支付宝账单"
            header.any { it.startsWith("交易单号") } -> "微信账单"
            else -> "账单"
        }

        val rows = ArrayList<BillRow>()
        var skipped = 0
        val zone = ZoneId.systemDefault()

        for (i in headerIndex + 1 until table.size) {
            val row = table[i]
            if (row.all { it.isBlank() }) continue

            val timeText = row.getOrNull(timeCol)?.trim().orEmpty()
            val time = parseTime(timeText) ?: continue
            val direction = row.getOrNull(directionCol)?.trim().orEmpty()
            val kind = when (direction) {
                "支出" -> TxnKind.EXPENSE
                "收入" -> TxnKind.INCOME
                else -> {
                    skipped++
                    continue
                }
            }
            val amount = parseAmount(row.getOrNull(amountCol).orEmpty())
            if (amount == null || amount <= 0) {
                skipped++
                continue
            }
            val status = row.getOrNull(statusCol)?.trim().orEmpty()
            if (status.contains("退款") || status.contains("关闭") ||
                status.contains("失败") || status.contains("取消")
            ) {
                skipped++
                continue
            }

            val party = row.getOrNull(partyCol)?.trim().orEmpty()
            val goods = row.getOrNull(noteCol)?.trim().orEmpty()
            val typeText = row.getOrNull(typeCol)?.trim().orEmpty()
            val orderId = row.getOrNull(orderCol)?.trim().orEmpty()
            val millis = time.atZone(zone).toInstant().toEpochMilli()

            val externalId = if (orderId.isNotBlank()) {
                "bill:$orderId"
            } else {
                "bill:" + sha1("$millis|$party|$goods|$amount|$kind")
            }

            rows += BillRow(
                time = millis,
                amountCents = amount,
                kind = kind,
                counterparty = party,
                note = goods,
                typeText = typeText,
                externalId = externalId
            )
        }

        if (rows.isEmpty()) {
            throw BillImportException("账单里没有可以导入的收支记录")
        }
        return ParsedBill(rows = rows, skipped = skipped, sourceLabel = sourceLabel)
    }

    private fun parseTime(text: String): LocalDateTime? {
        if (text.isBlank()) return null
        for (pattern in timePatterns) {
            runCatching { return LocalDateTime.parse(text, pattern) }
        }
        return null
    }

    private fun parseAmount(text: String): Long? {
        val cleaned = text.replace("¥", "").replace("￥", "")
            .replace(",", "").replace("元", "").replace(" ", "").trim()
        if (cleaned.isBlank()) return null
        return runCatching {
            BigDecimal(cleaned).movePointRight(2)
                .setScale(0, java.math.RoundingMode.HALF_UP).toLong()
        }.getOrNull()
    }

    private fun sha1(value: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun parseCsv(text: String): List<List<String>> {
        val rows = ArrayList<List<String>>()
        val row = ArrayList<String>()
        val cell = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                inQuotes -> {
                    if (c == '"') {
                        if (i + 1 < text.length && text[i + 1] == '"') {
                            cell.append('"')
                            i++
                        } else {
                            inQuotes = false
                        }
                    } else {
                        cell.append(c)
                    }
                }

                c == '"' -> inQuotes = true
                c == ',' -> {
                    row.add(cell.toString())
                    cell.clear()
                }

                c == '\r' -> Unit
                c == '\n' -> {
                    row.add(cell.toString())
                    cell.clear()
                    rows.add(ArrayList(row))
                    row.clear()
                }

                else -> cell.append(c)
            }
            i++
        }
        if (cell.isNotEmpty() || row.isNotEmpty()) {
            row.add(cell.toString())
            rows.add(ArrayList(row))
        }
        return rows
    }
}
