/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: Models.kt
 * 说明: 数据实体与类型转换器
 */

package com.starleaf.qingzhang.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class AccountType { ALIPAY, WECHAT, BANK, CASH, OTHER }

enum class TxnKind { EXPENSE, INCOME, TRANSFER }

enum class TxnSource { MANUAL, IMPORT, NOTIFICATION }

enum class TxnStatus { CONFIRMED, PENDING }

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val initialBalance: Long = 0,
    val sortOrder: Int = 0
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String,
    val colorIndex: Int,
    val kind: TxnKind,
    val sortOrder: Int = 0,
    val isPreset: Boolean = false
)

@Entity(
    tableName = "txns",
    indices = [
        Index("bookId"),
        Index("accountId"),
        Index("time"),
        Index(value = ["bookId", "externalId"], unique = true)
    ]
)
data class Txn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val accountId: Long,
    val toAccountId: Long? = null,
    val categoryId: Long? = null,
    val kind: TxnKind,
    val amount: Long,
    val time: Long,
    val note: String = "",
    val counterparty: String = "",
    val source: TxnSource = TxnSource.MANUAL,
    val status: TxnStatus = TxnStatus.CONFIRMED,
    val externalId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class AccountDelta(val accountId: Long, val delta: Long)

class Converters {
    @TypeConverter
    fun fromAccountType(value: AccountType): String = value.name

    @TypeConverter
    fun toAccountType(value: String): AccountType =
        runCatching { AccountType.valueOf(value) }.getOrDefault(AccountType.OTHER)

    @TypeConverter
    fun fromTxnKind(value: TxnKind): String = value.name

    @TypeConverter
    fun toTxnKind(value: String): TxnKind =
        runCatching { TxnKind.valueOf(value) }.getOrDefault(TxnKind.EXPENSE)

    @TypeConverter
    fun fromTxnSource(value: TxnSource): String = value.name

    @TypeConverter
    fun toTxnSource(value: String): TxnSource =
        runCatching { TxnSource.valueOf(value) }.getOrDefault(TxnSource.MANUAL)

    @TypeConverter
    fun fromTxnStatus(value: TxnStatus): String = value.name

    @TypeConverter
    fun toTxnStatus(value: String): TxnStatus =
        runCatching { TxnStatus.valueOf(value) }.getOrDefault(TxnStatus.CONFIRMED)
}
