/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: BillImporterTest.kt
 * 说明: 账单解析单元测试
 */

package com.starleaf.qingzhang.importer

import com.starleaf.qingzhang.data.TxnKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BillImporterTest {

    private val alipayBill = """
        支付宝交易流水明细
        账号:test@example.com
        起始日期:[2026-09-01 00:00:00]    终止日期:[2026-10-31 23:59:59]
        --------------------------------交易明细列表----------------------------------------
        交易时间,交易分类,交易对方,对方账号,商品说明,收/支,金额,收/付款方式,交易状态,交易订单号,商家订单号,备注
        2026-10-30 12:31:22,餐饮美食,肯德基,test@example.com,肯德基订单,支出,38.50,余额宝,交易成功,2026103022001,MC001,
        2026-10-29 09:12:00,交通出行,滴滴出行,test@example.com,快车行程,支出,23.00,花呗,交易成功,2026102922002,MC002,
        2026-10-28 18:00:00,转账红包,张三,test@example.com,转账,不计收支,100.00,余额,交易成功,2026102822003,MC003,
        2026-10-27 10:00:00,商业服务,某某科技有限公司,test@example.com,10月工资,收入,8000.00,银行卡,交易成功,2026102722004,MC004,
        2026-10-26 20:20:00,餐饮美食,美团,test@example.com,外卖订单,支出,45.80,余额宝,交易成功,2026102622005,MC005,
        2026-10-25 15:10:00,日用百货,沃尔玛,test@example.com,超市购物,支出,120.00,余额宝,交易成功,2026102522006,MC006,
    """.trimIndent()

    private val wechatBill = """
        微信支付账单明细
        微信昵称:[测试]
        ----------------------微信支付账单明细列表--------------------
        交易时间,交易类型,交易对方,商品,收/支,金额(元),支付方式,当前状态,交易单号,商户单号,备注
        2026-10-31 08:30:00,商户消费,便利店,早餐,支出,¥12.00,零钱,支付成功,4200002001,10001,
        2026-10-30 19:00:00,转账,李四,转账备注,收入,¥200.00,零钱,已存入零钱,4200002002,10002,
        2026-10-29 12:00:00,零钱提现,本人,提现,/,¥500.00,零钱,已到账,4200002003,10003,
        2026-10-28 12:00:00,商户消费,某商店,退货,支出,¥30.00,零钱,已全额退款,4200002004,10004,
    """.trimIndent()

    @Test
    fun `alipay bill keeps income and expense, skips transfers`() {
        val parsed = BillImporter.parse(alipayBill, "alipay.csv")

        assertEquals(5, parsed.rows.size)
        assertEquals(1, parsed.skipped)
        assertEquals(22_730L, parsed.expenseTotal)
        assertEquals(800_000L, parsed.incomeTotal)
        assertEquals("支付宝账单", parsed.sourceLabel)
        assertEquals(TxnKind.INCOME, parsed.rows.single { it.kind == TxnKind.INCOME }.kind)
        assertEquals("bill:2026103022001", parsed.rows.first().externalId)
    }

    @Test
    fun `wechat bill handles currency symbol slash and refunds`() {
        val parsed = BillImporter.parse(wechatBill, "wechat.csv")

        assertEquals(2, parsed.rows.size)
        assertEquals(2, parsed.skipped)
        assertEquals(1_200L, parsed.expenseTotal)
        assertEquals(20_000L, parsed.incomeTotal)
        assertEquals("微信账单", parsed.sourceLabel)
        assertEquals("便利店", parsed.rows.first().counterparty)
    }

    @Test
    fun `invalid file is rejected`() {
        val error = runCatching { BillImporter.parse("hello,world", "x.csv") }.exceptionOrNull()
        assertTrue(error is BillImportException)
    }

    @Test
    fun `empty bill is rejected`() {
        val text = "交易时间,交易分类,交易对方,商品说明,收/支,金额,收/付款方式,交易状态,交易订单号\n"
        val error = runCatching { BillImporter.parse(text, "empty.csv") }.exceptionOrNull()
        assertTrue(error is BillImportException)
    }

    @Test
    fun `qianji csv keeps category account and skips transfers`() {
        val text = """
            日期,账户,类别,金额,类型,备注,对方户名
            2026-10-30 12:30:00,支付宝,餐饮/午餐,38.50,支出,午餐,肯德基
            2026-10-29 09:00:00,微信,交通/地铁,23.00,支出,地铁,
            2026-10-28 10:00:00,银行卡,工资,8000.00,收入,10月工资,某某公司
            2026-10-27 10:00:00,支付宝,内部转账,100.00,转账,转账,
        """.trimIndent()

        val parsed = BillImporter.parse(text, "钱迹导出.csv")

        assertEquals(3, parsed.rows.size)
        assertEquals(1, parsed.skipped)
        assertEquals(6_150L, parsed.expenseTotal)
        assertEquals(800_000L, parsed.incomeTotal)
        assertEquals("钱迹账单", parsed.sourceLabel)
        assertTrue(parsed.containsAccounts)

        val first = parsed.rows.first()
        assertEquals("支付宝", first.accountName)
        assertEquals("餐饮/午餐", first.categoryName)
        assertTrue(first.externalId.startsWith("import:"))
    }

    @Test
    fun `qianji json reads nested account and category`() {
        val text = """
            {
              "records": [
                {
                  "date": "2026-10-30 12:30:00",
                  "account": {"name": "支付宝"},
                  "category": {"name": "餐饮"},
                  "amount": 38.5,
                  "type": "支出",
                  "note": "午餐",
                  "counterparty": "肯德基"
                },
                {
                  "date": "2026-10-28 10:00:00",
                  "account": "银行卡",
                  "category": "工资",
                  "amount": "8000.00",
                  "type": "收入",
                  "note": "工资"
                },
                {
                  "date": "2026-10-27 10:00:00",
                  "account": "微信",
                  "amount": 100,
                  "type": "转账"
                }
              ]
            }
        """.trimIndent()

        val parsed = BillImporter.parse(text, "钱迹备份.json")

        assertEquals(2, parsed.rows.size)
        assertEquals(1, parsed.skipped)
        assertEquals(3_850L, parsed.expenseTotal)
        assertEquals(800_000L, parsed.incomeTotal)
        assertEquals("钱迹账单", parsed.sourceLabel)
        assertTrue(parsed.containsAccounts)
        assertEquals("支付宝", parsed.rows.first().accountName)
        assertEquals("餐饮", parsed.rows.first().categoryName)
    }

    @Test
    fun `generic csv supports split income and expense columns`() {
        val text = """
            日期,账户,支出金额,收入金额,分类,备注
            2026-10-30,支付宝,38.50,,餐饮,午餐
            2026-10-28,银行卡,,8000.00,工资,工资
        """.trimIndent()

        val parsed = BillImporter.parse(text, "账单.csv")

        assertEquals(2, parsed.rows.size)
        assertEquals(3_850L, parsed.expenseTotal)
        assertEquals(800_000L, parsed.incomeTotal)
        assertEquals(TxnKind.EXPENSE, parsed.rows[0].kind)
        assertEquals(TxnKind.INCOME, parsed.rows[1].kind)
        assertEquals("支付宝", parsed.rows[0].accountName)
    }
}
