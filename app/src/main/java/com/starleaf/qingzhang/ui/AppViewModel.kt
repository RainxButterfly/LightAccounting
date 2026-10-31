/*
 * 轻账 - 本地记账应用
 *
 * 作者: StarLeaf·Roxy (星河一叶Roxy)
 * 邮箱: ck_xuv@vip.qq.com
 * 日期: 2026-10-31
 * 协议: Apache License 2.0
 *
 * 文件: AppViewModel.kt
 * 说明: 界面状态与业务操作
 */

package com.starleaf.qingzhang.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.starleaf.qingzhang.QingZhangApp
import com.starleaf.qingzhang.data.Account
import com.starleaf.qingzhang.data.AccountType
import com.starleaf.qingzhang.data.Book
import com.starleaf.qingzhang.data.Category
import com.starleaf.qingzhang.data.Txn
import com.starleaf.qingzhang.data.TxnKind
import com.starleaf.qingzhang.data.TxnStatus
import com.starleaf.qingzhang.importer.BillImporter
import com.starleaf.qingzhang.importer.BillRow
import com.starleaf.qingzhang.importer.ImportCommit
import com.starleaf.qingzhang.importer.ParsedBill
import com.starleaf.qingzhang.ui.theme.ThemeMode
import com.starleaf.qingzhang.update.InstallResult
import com.starleaf.qingzhang.update.UpdateCheckResult
import com.starleaf.qingzhang.update.UpdateChecker
import com.starleaf.qingzhang.update.UpdateInstaller
import com.starleaf.qingzhang.update.UpdateUiState
import com.starleaf.qingzhang.util.Dates
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class AccountUi(val account: Account, val balance: Long)

data class TxnUi(
    val txn: Txn,
    val category: Category?,
    val account: Account?,
    val toAccount: Account?
)

data class MonthSummary(val expense: Long, val income: Long) {
    val balance: Long get() = income - expense
}

data class CategorySlice(
    val category: Category?,
    val amount: Long,
    val fraction: Float,
    val colorIndex: Int
)

data class ChartData(
    val expenseTotal: Long,
    val incomeTotal: Long,
    val expense: List<CategorySlice>,
    val income: List<CategorySlice>
)

data class MonthBar(val label: String, val expense: Long, val income: Long)

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as QingZhangApp).container.repository
    private val prefs = application.getSharedPreferences("qingzhang_ui", Context.MODE_PRIVATE)

    val appVersion: String = runCatching {
        application.packageManager.getPackageInfo(application.packageName, 0).versionName
    }.getOrNull()?.takeIf { it.isNotBlank() } ?: "1.0.0"

    private val _themeMode = MutableStateFlow(
        ThemeMode.from(prefs.getString("theme_mode", null))
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _showGuide = MutableStateFlow(!prefs.getBoolean("guide_done", false))
    val showGuide: StateFlow<Boolean> = _showGuide.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateState: StateFlow<UpdateUiState> = _updateState.asStateFlow()

    private var checkJob: Job? = null
    private var downloadJob: Job? = null
    private var downloadToken = 0

    val books: StateFlow<List<Book>> = repo.observeBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<Category>> = repo.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val accounts: StateFlow<List<Account>> = repo.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val accountDeltas = repo.observeAccountDeltas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val accountsUi: StateFlow<List<AccountUi>> = combine(accounts, accountDeltas) { list, deltas ->
        val deltaMap = deltas.associate { it.accountId to it.delta }
        list.map { AccountUi(it, it.initialBalance + (deltaMap[it.id] ?: 0L)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val storedBookId = MutableStateFlow(prefs.getLong("book_id", 0L))
    val currentBookId: StateFlow<Long> = storedBookId.asStateFlow()

    private val validBookId: StateFlow<Long> = combine(books, storedBookId) { list, id ->
        when {
            list.isEmpty() -> 0L
            list.any { it.id == id } -> id
            else -> list.first().id
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val currentBook: StateFlow<Book?> = combine(books, validBookId) { list, id ->
        list.firstOrNull { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val month = MutableStateFlow(YearMonth.now())

    val canGoNextMonth: StateFlow<Boolean> = month
        .map { it < YearMonth.now() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val rawTxns: StateFlow<List<Txn>> =
        combine(validBookId, month) { id, m -> id to m }
            .flatMapLatest { (id, m) ->
                if (id <= 0) flowOf(emptyList()) else repo.observeMonth(
                    id,
                    Dates.monthStart(m),
                    Dates.monthEndExclusive(m)
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val txns: StateFlow<List<TxnUi>> =
        combine(rawTxns, categories, accountsUi) { list, cats, accs ->
            val catMap = cats.associateBy { it.id }
            val accMap = accs.associateBy { it.account.id }
            list.map { txn ->
                TxnUi(
                    txn = txn,
                    category = txn.categoryId?.let(catMap::get),
                    account = accMap[txn.accountId]?.account,
                    toAccount = txn.toAccountId?.let(accMap::get)?.account
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val summary: StateFlow<MonthSummary> = txns
        .map { list ->
            MonthSummary(
                expense = list.filter { it.txn.kind == TxnKind.EXPENSE }.sumOf { it.txn.amount },
                income = list.filter { it.txn.kind == TxnKind.INCOME }.sumOf { it.txn.amount }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthSummary(0, 0))

    val pending: StateFlow<List<TxnUi>> =
        combine(repo.observePending(), categories, accountsUi) { list, cats, accs ->
            val catMap = cats.associateBy { it.id }
            val accMap = accs.associateBy { it.account.id }
            list.map { txn ->
                TxnUi(
                    txn = txn,
                    category = txn.categoryId?.let(catMap::get),
                    account = accMap[txn.accountId]?.account,
                    toAccount = txn.toAccountId?.let(accMap::get)?.account
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pendingCount: StateFlow<Int> = pending
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val chartData: StateFlow<ChartData> = txns
        .map { buildChart(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChartData(0, 0, emptyList(), emptyList()))

    val sixMonths: StateFlow<List<MonthBar>> =
        combine(validBookId, month) { id, m -> id to m }
            .flatMapLatest { (id, m) ->
                if (id <= 0) {
                    flowOf(emptyList())
                } else {
                    repo.observeMonth(
                        id,
                        Dates.monthStart(m.minusMonths(5)),
                        Dates.monthEndExclusive(m)
                    ).map { list -> buildMonths(list, m) }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            repo.seedIfNeeded()
            val saved = prefs.getLong("book_id", 0L)
            val first = repo.firstBookId() ?: 0L
            if (saved <= 0) selectBook(first)
        }
        viewModelScope.launch {
            delay(1_500)
            performUpdateCheck(manual = false)
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun openGuide() {
        _showGuide.value = true
    }

    fun dismissGuide() {
        prefs.edit().putBoolean("guide_done", true).apply()
        _showGuide.value = false
    }

    fun checkForUpdates(manual: Boolean) {
        if (checkJob?.isActive == true) return
        if (_updateState.value is UpdateUiState.Downloading) return
        if (manual) _updateState.value = UpdateUiState.Checking
        checkJob = viewModelScope.launch {
            performUpdateCheck(manual)
        }
    }

    private suspend fun performUpdateCheck(manual: Boolean) {
        when (val result = UpdateChecker.check(getApplication())) {
            is UpdateCheckResult.Available -> {
                _updateState.value = UpdateUiState.Available(result.info)
            }

            is UpdateCheckResult.UpToDate -> {
                if (_updateState.value is UpdateUiState.Checking) {
                    _updateState.value = UpdateUiState.Idle
                }
                if (manual) _message.value = "已经是最新版本"
            }

            is UpdateCheckResult.Failed -> {
                if (_updateState.value is UpdateUiState.Checking) {
                    _updateState.value = UpdateUiState.Idle
                }
                if (manual) _message.value = "检查更新失败：${result.message}"
            }
        }
    }

    fun downloadUpdate() {
        val info = when (val state = _updateState.value) {
            is UpdateUiState.Available -> state.info
            is UpdateUiState.Error -> state.info
            else -> return
        }
        downloadJob?.cancel()
        val token = ++downloadToken
        _updateState.value = UpdateUiState.Downloading(info, -1)
        downloadJob = viewModelScope.launch {
            try {
                val file = UpdateInstaller.download(getApplication(), info) { progress ->
                    if (token == downloadToken) {
                        _updateState.value = UpdateUiState.Downloading(info, progress)
                    }
                }
                if (token == downloadToken) {
                    _updateState.value = UpdateUiState.Ready(info, file)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (token == downloadToken) {
                    _updateState.value = UpdateUiState.Error(
                        info,
                        "下载失败，请检查网络后重试"
                    )
                }
            }
        }
    }

    fun cancelDownload() {
        downloadToken++
        downloadJob?.cancel()
        downloadJob = null
        _updateState.value = UpdateUiState.Idle
    }

    fun dismissUpdate() {
        if (_updateState.value is UpdateUiState.Downloading) return
        _updateState.value = UpdateUiState.Idle
    }

    fun installUpdate() {
        val ready = _updateState.value as? UpdateUiState.Ready ?: return
        when (UpdateInstaller.install(getApplication(), ready.file)) {
            InstallResult.STARTED -> _updateState.value = UpdateUiState.Idle
            InstallResult.NEED_PERMISSION ->
                _message.value = "请在系统设置中允许轻账安装应用后重试"

            InstallResult.FAILED ->
                _message.value = "无法打开系统安装程序"
        }
    }

    fun selectBook(id: Long) {
        storedBookId.value = id
        prefs.edit().putLong("book_id", id).apply()
    }

    fun previousMonth() {
        month.value = month.value.minusMonths(1)
    }

    fun nextMonth() {
        if (month.value < YearMonth.now()) month.value = month.value.plusMonths(1)
    }

    fun goToCurrentMonth() {
        month.value = YearMonth.now()
    }

    fun saveTxn(
        editId: Long?,
        kind: TxnKind,
        amount: Long,
        accountId: Long,
        toAccountId: Long?,
        categoryId: Long?,
        time: Long,
        note: String,
        counterparty: String = ""
    ) {
        if (amount <= 0 || accountId <= 0) return
        viewModelScope.launch {
            val cleanTo = if (kind == TxnKind.TRANSFER) toAccountId else null
            val cleanCategory = if (kind == TxnKind.TRANSFER) null else categoryId
            if (editId != null) {
                val old = repo.txnById(editId) ?: return@launch
                repo.updateTxn(
                    old.copy(
                        accountId = accountId,
                        toAccountId = cleanTo,
                        categoryId = cleanCategory,
                        kind = kind,
                        amount = amount,
                        time = time,
                        note = note,
                        counterparty = counterparty,
                        status = TxnStatus.CONFIRMED
                    )
                )
            } else {
                val bookId = validBookId.value
                if (bookId <= 0) return@launch
                repo.insertTxn(
                    Txn(
                        bookId = bookId,
                        accountId = accountId,
                        toAccountId = cleanTo,
                        categoryId = cleanCategory,
                        kind = kind,
                        amount = amount,
                        time = time,
                        note = note,
                        counterparty = counterparty,
                        status = TxnStatus.CONFIRMED
                    )
                )
            }
        }
    }

    fun deleteTxn(id: Long) {
        viewModelScope.launch { repo.deleteTxn(id) }
    }

    suspend fun getTxn(id: Long): Txn? = repo.txnById(id)

    fun addBook(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repo.addBook(name)
            if (id > 0) selectBook(id)
        }
    }

    fun renameBook(book: Book, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repo.renameBook(book, name) }
    }

    fun deleteBook(id: Long) {
        viewModelScope.launch {
            if (!repo.deleteBook(id)) _message.value = "至少要保留一个账本"
        }
    }

    fun addAccount(
        name: String,
        type: AccountType,
        initialBalance: Long,
        onDone: () -> Unit
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repo.addAccount(name, type, initialBalance)
            onDone()
        }
    }

    fun updateAccount(account: Account, name: String, initialBalance: Long) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repo.updateAccount(account.copy(name = name.trim(), initialBalance = initialBalance))
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            if (!repo.deleteAccount(id)) _message.value = "这个账户下还有流水，不能删除"
        }
    }

    fun addCategory(name: String, kind: TxnKind, iconKey: String, colorIndex: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val exists = categories.value.any { it.name == name.trim() && it.kind == kind }
            if (exists) {
                _message.value = "已经有同名分类了"
                return@launch
            }
            repo.addCategory(name, kind, iconKey, colorIndex)
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            if (!repo.deleteCategory(id)) _message.value = "这个分类下还有流水，不能删除"
        }
    }

    fun createPendingToConfirmed(txnId: Long) {
        viewModelScope.launch { repo.confirmPending(txnId) }
    }

    fun discardPending(txnId: Long) {
        viewModelScope.launch { repo.discardPending(txnId) }
    }

    suspend fun parseBill(context: Context, uri: Uri, password: String): ParsedBill =
        BillImporter.load(context, uri, password)

    fun commitImport(rows: List<BillRow>, accountId: Long, onResult: (ImportCommit) -> Unit) {
        viewModelScope.launch {
            val bookId = validBookId.value
            if (bookId <= 0 || accountId <= 0) return@launch
            val commit = repo.commitImport(bookId, accountId, rows)
            onResult(commit)
        }
    }

    fun isNotificationAccessEnabled(context: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

    private fun buildChart(list: List<TxnUi>): ChartData {
        fun slices(kind: TxnKind): Pair<Long, List<CategorySlice>> {
            val items = list.filter { it.txn.kind == kind }
            val total = items.sumOf { it.txn.amount }
            val grouped = items.groupBy { it.category }.map { (category, rows) ->
                val amount = rows.sumOf { it.txn.amount }
                CategorySlice(
                    category = category,
                    amount = amount,
                    fraction = if (total > 0) amount.toFloat() / total else 0f,
                    colorIndex = category?.colorIndex ?: 14
                )
            }.sortedByDescending { it.amount }
            return total to grouped
        }

        val (expenseTotal, expenseSlices) = slices(TxnKind.EXPENSE)
        val (incomeTotal, incomeSlices) = slices(TxnKind.INCOME)
        return ChartData(
            expenseTotal = expenseTotal,
            incomeTotal = incomeTotal,
            expense = expenseSlices,
            income = incomeSlices
        )
    }

    private fun buildMonths(list: List<Txn>, end: YearMonth): List<MonthBar> =
        (5 downTo 0).map { offset ->
            val ym = end.minusMonths(offset.toLong())
            val start = Dates.monthStart(ym)
            val endMillis = Dates.monthEndExclusive(ym)
            val inMonth = list.filter { it.time >= start && it.time < endMillis }
            MonthBar(
                label = "${ym.monthValue}月",
                expense = inMonth.filter { it.kind == TxnKind.EXPENSE }.sumOf { it.amount },
                income = inMonth.filter { it.kind == TxnKind.INCOME }.sumOf { it.amount }
            )
        }
}
