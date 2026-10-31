/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: AppRepository.kt
 * 说明: 数据仓库，统一读写与记账规则
 */

package com.starleaf.qingzhang.data

import androidx.room.withTransaction
import com.starleaf.qingzhang.importer.BillRow
import com.starleaf.qingzhang.importer.ImportCommit
import com.starleaf.qingzhang.logic.CategoryRules
import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {

    private val bookDao = db.bookDao()
    private val accountDao = db.accountDao()
    private val categoryDao = db.categoryDao()
    private val txnDao = db.txnDao()

    fun observeBooks(): Flow<List<Book>> = bookDao.observeAll()
    fun observeAccounts(): Flow<List<Account>> = accountDao.observeAll()
    fun observeAccountDeltas(): Flow<List<AccountDelta>> = txnDao.observeAccountDeltas()
    fun observeCategories(): Flow<List<Category>> = categoryDao.observeAll()
    fun observeMonth(bookId: Long, start: Long, end: Long): Flow<List<Txn>> =
        txnDao.observeRange(bookId, start, end)

    fun observePending(): Flow<List<Txn>> = txnDao.observePending()

    suspend fun seedIfNeeded() {
        db.withTransaction {
            if (bookDao.count() > 0) return@withTransaction

            bookDao.insert(Book(name = Seed.DEFAULT_BOOK_NAME))
            Seed.accounts.forEachIndexed { index, preset ->
                accountDao.insert(
                    Account(name = preset.name, type = preset.type, sortOrder = index)
                )
            }

            var expenseOrder = 0
            var incomeOrder = 0
            Seed.categories.forEach { preset ->
                val order = if (preset.kind == TxnKind.INCOME) incomeOrder++ else expenseOrder++
                categoryDao.insert(
                    Category(
                        name = preset.name,
                        iconKey = preset.iconKey,
                        colorIndex = preset.colorIndex,
                        kind = preset.kind,
                        sortOrder = order,
                        isPreset = true
                    )
                )
            }
        }
    }

    suspend fun firstBookId(): Long? = bookDao.all().firstOrNull()?.id

    suspend fun insertTxn(txn: Txn): Long = txnDao.insert(txn)

    suspend fun updateTxn(txn: Txn) = txnDao.update(txn)

    suspend fun deleteTxn(id: Long) = txnDao.deleteById(id)

    suspend fun txnById(id: Long): Txn? = txnDao.byId(id)

    suspend fun addBook(name: String): Long =
        bookDao.insert(Book(name = name.trim()))

    suspend fun renameBook(book: Book, newName: String) =
        bookDao.update(book.copy(name = newName.trim()))

    suspend fun deleteBook(id: Long): Boolean = db.withTransaction {
        if (bookDao.count() <= 1) {
            false
        } else {
            txnDao.deleteByBook(id)
            bookDao.delete(id)
            true
        }
    }

    suspend fun addAccount(name: String, type: AccountType, initialBalance: Long): Long {
        val order = accountDao.all().size
        return accountDao.insert(
            Account(
                name = name.trim(),
                type = type,
                initialBalance = initialBalance,
                sortOrder = order
            )
        )
    }

    suspend fun updateAccount(account: Account) = accountDao.update(account)

    suspend fun deleteAccount(id: Long): Boolean {
        if (txnDao.countForAccount(id) > 0) return false
        accountDao.delete(id)
        return true
    }

    suspend fun addCategory(name: String, kind: TxnKind, iconKey: String, colorIndex: Int): Long {
        val order = categoryDao.all().count { it.kind == kind }
        return categoryDao.insert(
            Category(
                name = name.trim(),
                iconKey = iconKey,
                colorIndex = colorIndex,
                kind = kind,
                sortOrder = order
            )
        )
    }

    suspend fun deleteCategory(id: Long): Boolean {
        if (txnDao.countForCategory(id) > 0) return false
        categoryDao.delete(id)
        return true
    }

    suspend fun confirmPending(id: Long) {
        val txn = txnDao.byId(id) ?: return
        txnDao.update(txn.copy(status = TxnStatus.CONFIRMED))
    }

    suspend fun discardPending(id: Long) = txnDao.deleteById(id)

    suspend fun captureNotification(
        type: AccountType,
        amount: Long,
        kind: TxnKind,
        rawText: String
    ): Boolean {
        val book = bookDao.all().firstOrNull() ?: return false
        val account = accountDao.firstByType(type.name) ?: return false
        val since = System.currentTimeMillis() - 2 * 60 * 1000L
        if (txnDao.countRecentNotification(account.id, amount, since) > 0) return false

        val categoryName = CategoryRules.match(kind, rawText)
        val category = categoryDao.find(categoryName, kind.name)
        txnDao.insert(
            Txn(
                bookId = book.id,
                accountId = account.id,
                categoryId = category?.id,
                kind = kind,
                amount = amount,
                time = System.currentTimeMillis(),
                note = rawText.take(120),
                source = TxnSource.NOTIFICATION,
                status = TxnStatus.PENDING
            )
        )
        return true
    }

    suspend fun commitImport(
        bookId: Long,
        accountId: Long,
        rows: List<BillRow>
    ): ImportCommit = db.withTransaction {
        val categories = categoryDao.all()
        var imported = 0
        var duplicates = 0

        rows.forEach { row ->
            val categoryName = CategoryRules.match(
                row.kind,
                row.counterparty,
                row.note,
                row.typeText
            )
            val category = categories.firstOrNull {
                it.name == categoryName && it.kind == row.kind
            }
            val id = txnDao.insert(
                Txn(
                    bookId = bookId,
                    accountId = accountId,
                    categoryId = category?.id,
                    kind = row.kind,
                    amount = row.amountCents,
                    time = row.time,
                    note = row.note,
                    counterparty = row.counterparty,
                    source = TxnSource.IMPORT,
                    externalId = row.externalId
                )
            )
            if (id == -1L) duplicates++ else imported++
        }
        ImportCommit(imported = imported, duplicates = duplicates)
    }
}
