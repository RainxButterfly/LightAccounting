/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: CategoryRulesTest.kt
 * 说明: 分类规则单元测试
 */

package com.starleaf.qingzhang.logic

import com.starleaf.qingzhang.data.TxnKind
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryRulesTest {

    @Test
    fun `food merchants map to food category`() {
        assertEquals("餐饮", CategoryRules.match(TxnKind.EXPENSE, "肯德基订单"))
        assertEquals("餐饮", CategoryRules.match(TxnKind.EXPENSE, "美团外卖"))
        assertEquals("餐饮", CategoryRules.match(TxnKind.EXPENSE, "星巴克"))
    }

    @Test
    fun `transport merchants map to transport category`() {
        assertEquals("交通", CategoryRules.match(TxnKind.EXPENSE, "滴滴出行"))
        assertEquals("交通", CategoryRules.match(TxnKind.EXPENSE, "地铁乘车码"))
    }

    @Test
    fun `salary maps to income category`() {
        assertEquals("工资", CategoryRules.match(TxnKind.INCOME, "10月工资"))
        assertEquals("退款", CategoryRules.match(TxnKind.INCOME, "订单退款"))
    }

    @Test
    fun `unknown merchant falls back`() {
        assertEquals("其他", CategoryRules.match(TxnKind.EXPENSE, "一家没有见过的店"))
        assertEquals("其他收入", CategoryRules.match(TxnKind.INCOME, "未知来源"))
    }
}
