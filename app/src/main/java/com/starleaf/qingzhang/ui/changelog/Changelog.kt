/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-11-01
 * 协议: Apache License 2.0
 *
 * 文件: Changelog.kt
 * 说明: 应用内更新日志数据
 */

package com.starleaf.qingzhang.ui.changelog

data class ChangelogEntry(
    val version: String,
    val date: String,
    val changes: List<String>
)

object Changelog {

    val entries = listOf(
        ChangelogEntry(
            version = "1.0.1",
            date = "2026-11-01",
            changes = listOf(
                "更换应用图标与欢迎页品牌形象",
                "支持导入钱迹等应用导出的 CSV / JSON 账单",
                "新增更新日志页面，检查更新与更新日志合并到同一入口",
                "导入账单时按名称匹配或新建分类和账户",
                "修正若干界面细节"
            )
        ),
        ChangelogEntry(
            version = "1.0.0",
            date = "2026-10-31",
            changes = listOf(
                "多账本、账户、分类与收支转账记录",
                "支付宝、微信账单导入，自动分类并按交易单号去重",
                "微信支付、支付宝通知监听与待确认账单",
                "月度汇总、分类环形图与近六个月趋势",
                "浅色、深色与跟随系统主题",
                "首次使用引导与多源检查更新"
            )
        )
    )
}
