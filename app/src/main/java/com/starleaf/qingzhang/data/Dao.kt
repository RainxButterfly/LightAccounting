/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: Dao.kt
 * 说明: Room 数据访问接口
 */

package com.starleaf.qingzhang.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<Book>>

    @Query("SELECT * FROM books ORDER BY createdAt ASC")
    suspend fun all(): List<Book>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun count(): Int

    @Insert
    suspend fun insert(book: Book): Long

    @Update
    suspend fun update(book: Book)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<Account>>

    @Query("SELECT * FROM accounts ORDER BY sortOrder ASC, id ASC")
    suspend fun all(): List<Account>

    @Query("SELECT * FROM accounts WHERE type = :type ORDER BY sortOrder ASC LIMIT 1")
    suspend fun firstByType(type: String): Account?

    @Insert
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY kind ASC, sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY kind ASC, sortOrder ASC, id ASC")
    suspend fun all(): List<Category>

    @Query("SELECT * FROM categories WHERE name = :name AND kind = :kind LIMIT 1")
    suspend fun find(name: String, kind: String): Category?

    @Insert
    suspend fun insert(category: Category): Long

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface TxnDao {
    @Query(
        "SELECT * FROM txns WHERE bookId = :bookId AND status = 'CONFIRMED' " +
            "AND time >= :start AND time < :end ORDER BY time DESC, id DESC"
    )
    fun observeRange(bookId: Long, start: Long, end: Long): Flow<List<Txn>>

    @Query("SELECT * FROM txns WHERE status = 'PENDING' ORDER BY time DESC")
    fun observePending(): Flow<List<Txn>>

    @Query("SELECT * FROM txns WHERE id = :id")
    suspend fun byId(id: Long): Txn?

    @Query("SELECT * FROM txns WHERE bookId = :bookId AND externalId = :externalId LIMIT 1")
    suspend fun findByExternalId(bookId: Long, externalId: String): Txn?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(txn: Txn): Long

    @Update
    suspend fun update(txn: Txn)

    @Delete
    suspend fun delete(txn: Txn)

    @Query("DELETE FROM txns WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM txns WHERE bookId = :bookId")
    suspend fun deleteByBook(bookId: Long)

    @Query("SELECT COUNT(*) FROM txns WHERE accountId = :accountId OR toAccountId = :accountId")
    suspend fun countForAccount(accountId: Long): Int

    @Query("SELECT COUNT(*) FROM txns WHERE categoryId = :categoryId")
    suspend fun countForCategory(categoryId: Long): Int

    @Query(
        "SELECT COUNT(*) FROM txns WHERE source = 'NOTIFICATION' AND accountId = :accountId " +
            "AND amount = :amount AND time >= :since"
    )
    suspend fun countRecentNotification(accountId: Long, amount: Long, since: Long): Int

    @Query(
        "SELECT accountId, SUM(delta) AS delta FROM (" +
            "SELECT accountId AS accountId, CASE WHEN kind = 'INCOME' THEN amount ELSE -amount END AS delta " +
            "FROM txns WHERE status = 'CONFIRMED' AND kind IN ('EXPENSE', 'INCOME') " +
            "UNION ALL " +
            "SELECT accountId AS accountId, -amount AS delta FROM txns " +
            "WHERE status = 'CONFIRMED' AND kind = 'TRANSFER' " +
            "UNION ALL " +
            "SELECT toAccountId AS accountId, amount AS delta FROM txns " +
            "WHERE status = 'CONFIRMED' AND kind = 'TRANSFER' AND toAccountId IS NOT NULL" +
            ") GROUP BY accountId"
    )
    fun observeAccountDeltas(): Flow<List<AccountDelta>>
}
