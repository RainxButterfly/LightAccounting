/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-11-01
 * 协议: Apache License 2.0
 *
 * 文件: CsvParser.kt
 * 说明: CSV 文本解析
 */

package com.starleaf.qingzhang.importer

object CsvParser {

    fun parse(text: String): List<List<String>> {
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
