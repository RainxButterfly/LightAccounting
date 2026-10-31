/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: PayNotificationParserTest.kt
 * 说明: 支付通知解析单元测试
 */

package com.starleaf.qingzhang.notify

import com.starleaf.qingzhang.data.TxnKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PayNotificationParserTest {

    @Test
    fun `wechat collection is income`() {
        val captured = PayNotificationParser.parse(
            "com.tencent.mm",
            "微信支付",
            "微信支付收款 0.01 元"
        )
        assertEquals(1L, captured?.amountCents)
        assertEquals(TxnKind.INCOME, captured?.kind)
    }

    @Test
    fun `alipay payment is expense`() {
        val captured = PayNotificationParser.parse(
            "com.eg.android.AlipayGphone",
            "支付宝",
            "你已成功付款 12.50 元"
        )
        assertEquals(1_250L, captured?.amountCents)
        assertEquals(TxnKind.EXPENSE, captured?.kind)
    }

    @Test
    fun `regular chat message is ignored`() {
        assertNull(
            PayNotificationParser.parse("com.tencent.mm", "张三", "晚上一起吃饭吗")
        )
    }

    @Test
    fun `payment without amount is ignored`() {
        assertNull(
            PayNotificationParser.parse("com.eg.android.AlipayGphone", "支付宝", "支付成功")
        )
    }

    @Test
    fun `currency symbol amount is parsed`() {
        val captured = PayNotificationParser.parse(
            "com.tencent.mm",
            "微信支付",
            "已支付 ¥1,024.30"
        )
        assertEquals(102_430L, captured?.amountCents)
        assertEquals(TxnKind.EXPENSE, captured?.kind)
    }
}
