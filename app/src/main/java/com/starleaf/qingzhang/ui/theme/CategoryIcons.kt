/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: CategoryIcons.kt
 * 说明: 分类图标映射
 */

package com.starleaf.qingzhang.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIcons {
    val keys = listOf(
        "restaurant", "bus", "shopping", "basket", "movie", "medical", "school",
        "house", "phone", "gift", "pets", "flight", "fitness", "spa", "more",
        "salary", "award", "parttime", "invest", "redpacket", "refund", "more_income"
    )

    fun icon(key: String): ImageVector = when (key) {
        "restaurant" -> Icons.Rounded.Restaurant
        "bus" -> Icons.Rounded.DirectionsBus
        "shopping" -> Icons.Rounded.ShoppingBag
        "basket" -> Icons.Rounded.ShoppingBasket
        "movie" -> Icons.Rounded.Movie
        "medical" -> Icons.Rounded.MedicalServices
        "school" -> Icons.Rounded.School
        "house" -> Icons.Rounded.Home
        "phone" -> Icons.Rounded.Smartphone
        "gift" -> Icons.Rounded.CardGiftcard
        "pets" -> Icons.Rounded.Pets
        "flight" -> Icons.Rounded.Flight
        "fitness" -> Icons.Rounded.FitnessCenter
        "spa" -> Icons.Rounded.Spa
        "more" -> Icons.Rounded.MoreHoriz
        "salary" -> Icons.Rounded.AccountBalanceWallet
        "award" -> Icons.Rounded.EmojiEvents
        "parttime" -> Icons.Rounded.Work
        "invest" -> Icons.AutoMirrored.Rounded.TrendingUp
        "redpacket" -> Icons.Rounded.Redeem
        "refund" -> Icons.Rounded.Replay
        "more_income" -> Icons.Rounded.MoreHoriz
        "receipt" -> Icons.AutoMirrored.Rounded.ReceiptLong
        else -> Icons.Rounded.Category
    }

    fun label(key: String): String = when (key) {
        "restaurant" -> "餐饮"
        "bus" -> "交通"
        "shopping" -> "购物"
        "basket" -> "日用"
        "movie" -> "娱乐"
        "medical" -> "医疗"
        "school" -> "教育"
        "house" -> "住房"
        "phone" -> "通讯"
        "gift" -> "礼物"
        "pets" -> "宠物"
        "flight" -> "旅行"
        "fitness" -> "运动"
        "spa" -> "美容"
        "salary" -> "工资"
        "award" -> "奖金"
        "parttime" -> "兼职"
        "invest" -> "理财"
        "redpacket" -> "红包"
        "refund" -> "退款"
        "more_income" -> "其他"
        else -> "其他"
    }
}
