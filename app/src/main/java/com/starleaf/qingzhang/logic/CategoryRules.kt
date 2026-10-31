/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: CategoryRules.kt
 * 说明: 商户关键词到分类的匹配规则
 */

package com.starleaf.qingzhang.logic

import com.starleaf.qingzhang.data.Seed
import com.starleaf.qingzhang.data.TxnKind

object CategoryRules {

    private data class Rule(val keywords: List<String>, val category: String)

    private val expenseRules = listOf(
        Rule(
            listOf(
                "美团", "饿了么", "肯德基", "麦当劳", "星巴克", "瑞幸", "咖啡", "奶茶", "蜜雪",
                "火锅", "烧烤", "面馆", "快餐", "餐厅", "饭店", "食堂", "外卖", "便利店", "全家",
                "罗森", "7788", "餐饮", "美食", "小吃", "早餐", "午饭", "晚饭"
            ),
            "餐饮"
        ),
        Rule(
            listOf(
                "地铁", "公交", "滴滴", "出租车", "高铁", "12306", "加油", "停车", "哈啰", "青桔",
                "共享单车", "航空", "车票", "交通", "出行", "打车", "ETC", "高速"
            ),
            "交通"
        ),
        Rule(
            listOf(
                "淘宝", "天猫", "京东", "拼多多", "唯品会", "苏宁", "抖音商城", "得物", "闲鱼",
                "网易严选", "购物", "商场", "专卖店", "旗舰店"
            ),
            "购物"
        ),
        Rule(
            listOf(
                "超市", "沃尔玛", "永辉", "盒马", "名创优品", "日用", "百货", "洗衣", "快递",
                "顺丰", "菜鸟", "日用品", "五金"
            ),
            "日用"
        ),
        Rule(
            listOf(
                "电影", "影院", "猫眼", "淘票票", "腾讯视频", "爱奇艺", "优酷", "哔哩哔哩", "B站",
                "网易云", "QQ音乐", "游戏", "Steam", "KTV", "演出", "门票", "娱乐", "会员"
            ),
            "娱乐"
        ),
        Rule(
            listOf("医院", "药房", "药店", "诊所", "挂号", "体检", "医疗", "口腔", "眼科"),
            "医疗"
        ),
        Rule(
            listOf(
                "学费", "当当", "图书", "书店", "课程", "培训", "学校", "教育", "考试", "文具",
                "资料", "网课"
            ),
            "教育"
        ),
        Rule(
            listOf("房租", "物业", "水费", "电费", "燃气", "自如", "链家", "贝壳", "租房", "住房"),
            "住房"
        ),
        Rule(
            listOf("中国移动", "中国联通", "中国电信", "话费", "流量", "宽带", "通讯", "充值"),
            "通讯"
        ),
        Rule(
            listOf("红包", "礼金", "份子", "礼物", "转账给", "人情", "随礼"),
            "人情"
        ),
        Rule(listOf("宠物", "猫粮", "狗粮", "宠物医院", "宠物店"), "宠物"),
        Rule(
            listOf(
                "酒店", "携程", "飞猪", "去哪儿", "机票", "民宿", "汉庭", "如家", "亚朵",
                "旅行", "旅游", "景区", "迪士尼"
            ),
            "旅行"
        ),
        Rule(listOf("健身", "Keep", "游泳", "球场", "瑜伽", "运动", "球馆"), "运动"),
        Rule(
            listOf("理发", "美发", "美容", "护肤", "屈臣氏", "丝芙兰", "美甲", "化妆品"),
            "美容"
        )
    )

    private val incomeRules = listOf(
        Rule(listOf("工资", "代发", "薪资", "薪酬"), "工资"),
        Rule(listOf("奖金", "绩效", "年终奖", "补贴"), "奖金"),
        Rule(listOf("兼职", "稿费", "报酬", "劳务", "外快"), "兼职"),
        Rule(listOf("收益", "利息", "基金", "分红", "余额宝", "理财", "定期"), "理财"),
        Rule(listOf("红包", "压岁钱", "礼金"), "红包"),
        Rule(listOf("退款", "退货", "赔偿", "退费"), "退款")
    )

    fun match(kind: TxnKind, vararg texts: String): String {
        val bucket = when (kind) {
            TxnKind.INCOME -> incomeRules
            else -> expenseRules
        }
        val haystack = texts.filter { it.isNotBlank() }.joinToString(" ").lowercase()
        if (haystack.isBlank()) return Seed.fallbackName(kind)
        bucket.firstOrNull { rule -> rule.keywords.any { haystack.contains(it.lowercase()) } }
            ?.let { return it.category }
        return Seed.fallbackName(kind)
    }
}
