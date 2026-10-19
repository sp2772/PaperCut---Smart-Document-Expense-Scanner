package com.example.new_app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.new_app.data.CurrencyItem
import com.example.new_app.data.WorldCurrencies
import com.example.new_app.data.db.CategoryEntity
import com.example.new_app.data.db.CategorySummary
import com.example.new_app.data.db.ExpenseEntity
import com.example.new_app.data.repository.ExpenseRepository
import com.example.new_app.data.repository.ExpenseRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.ceil
import kotlin.math.max

data class ExpenseUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val totalAmount: Double = 0.0,
    val categorySummaries: List<CategorySummary> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCurrency: CurrencyItem = WorldCurrencies.FAVORITES.first(),
    val monthlyBudgetGoal: Double = 1000.0,
    val searchQuery: String = "",
    val selectedCategoryFilter: String? = null,
    val currentPage: Int = 1,
    val pageSize: Int = 20,
    val totalRecordCount: Int = 0,
    val totalPages: Int = 1,
    val scannedPendingExpense: ExpenseEntity? = null,
    val isScanning: Boolean = false,
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val exportedFile: File? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModel(
    application: Application,
    private val repository: ExpenseRepository
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    private val _currentPage = MutableStateFlow(1)
    private val _pageSize = MutableStateFlow(20)
    private val _scannedPendingExpense = MutableStateFlow<ExpenseEntity?>(null)
    private val _selectedCurrency = MutableStateFlow(WorldCurrencies.FAVORITES.first())
    private val _monthlyBudgetGoal = MutableStateFlow(1000.0)
    private val _isScanning = MutableStateFlow(false)
    private val _isExporting = MutableStateFlow(false)
    private val _exportMessage = MutableStateFlow<String?>(null)
    private val _exportedFile = MutableStateFlow<File?>(null)

    // Reactively query paginated expenses based on query, category filter, and current page
    private val _expensesFlow = combine(_searchQuery, _selectedCategoryFilter, _currentPage, _pageSize) { query, category, page, size ->
        Triple(query, category, Pair(page, size))
    }.flatMapLatest { (query, category, pageAndSize) ->
        val (page, size) = pageAndSize
        when {
            query.isNotBlank() -> repository.searchExpensesPaginated(query, page, size)
            category != null -> repository.getExpensesByCategoryPaginated(category, page, size)
            else -> repository.getExpensesPaginated(page, size)
        }
    }

    private data class CombinedData(
        val expenses: List<ExpenseEntity>,
        val totalAmount: Double,
        val count: Int,
        val summaries: List<CategorySummary>,
        val categories: List<CategoryEntity>
    )

    private data class FilterState(
        val query: String,
        val categoryFilter: String?,
        val page: Int,
        val pageSize: Int,
        val currency: CurrencyItem,
        val budgetGoal: Double,
        val pending: ExpenseEntity?,
        val isScanning: Boolean
    )

    private data class ExportState(
        val exporting: Boolean,
        val msg: String?,
        val file: File?
    )

    val uiState: StateFlow<ExpenseUiState> = combine(
        combine(
            _expensesFlow,
            repository.getTotalExpenseAmount(),
            repository.getExpenseCount(),
            repository.getCategorySummaries(),
            repository.getAllCategories()
        ) { expenses, total, count, summaries, categories ->
            CombinedData(expenses, total ?: 0.0, count, summaries, categories)
        },
        combine(_searchQuery, _selectedCategoryFilter, _currentPage, _pageSize, _selectedCurrency) { query, categoryFilter, page, pageSize, currency ->
            Pair(Pair(query, categoryFilter), Pair(page, Pair(pageSize, currency)))
        }.combine(combine(_monthlyBudgetGoal, _scannedPendingExpense, _isScanning) { goal, pending, isScanning ->
            Triple(goal, pending, isScanning)
        }) { (f1, f2), (goal, pending, isScanning) ->
            val (query, categoryFilter) = f1
            val (page, p2) = f2
            val (pageSize, currency) = p2
            FilterState(query, categoryFilter, page, pageSize, currency, goal, pending, isScanning)
        },
        combine(_isExporting, _exportMessage, _exportedFile) { exporting, msg, file ->
            ExportState(exporting, msg, file)
        }
    ) { data, filter, export ->
        val totalCount = data.count
        val calculatedPages = max(1, ceil(totalCount.toDouble() / filter.pageSize).toInt())

        ExpenseUiState(
            expenses = data.expenses,
            totalAmount = data.totalAmount,
            categorySummaries = data.summaries,
            categories = data.categories,
            selectedCurrency = filter.currency,
            monthlyBudgetGoal = filter.budgetGoal,
            searchQuery = filter.query,
            selectedCategoryFilter = filter.categoryFilter,
            currentPage = filter.page,
            pageSize = filter.pageSize,
            totalRecordCount = totalCount,
            totalPages = calculatedPages,
            scannedPendingExpense = filter.pending,
            isScanning = filter.isScanning,
            isExporting = export.exporting,
            exportMessage = export.msg,
            exportedFile = export.file
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseUiState()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        _currentPage.value = 1 // Reset to page 1 on search
    }

    fun onCategoryFilterSelected(category: String?) {
        _selectedCategoryFilter.value = if (_selectedCategoryFilter.value == category) null else category
        _currentPage.value = 1 // Reset to page 1 on category change
    }

    fun nextPage() {
        if (_currentPage.value < uiState.value.totalPages) {
            _currentPage.value += 1
        }
    }

    fun previousPage() {
        if (_currentPage.value > 1) {
            _currentPage.value -= 1
        }
    }

    fun onCurrencySelected(currencyItem: CurrencyItem) {
        _selectedCurrency.value = currencyItem
    }

    fun updateMonthlyBudgetGoal(goal: Double) {
        _monthlyBudgetGoal.value = max(1.0, goal)
    }

    fun addCustomCategory(name: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addCategory(CategoryEntity(name = name.trim(), colorHex = colorHex))
        }
    }

    fun onDocumentScanned(uri: Uri) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val (ocrResult, savedImagePath) = repository.processScannedDocument(uri)
                val pendingExpense = ExpenseEntity(
                    vendorName = ocrResult.vendorName,
                    totalAmount = max(0.0, ocrResult.totalAmount),
                    currency = _selectedCurrency.value.symbol,
                    date = ocrResult.dateMillis,
                    category = ocrResult.category,
                    imageUri = savedImagePath,
                    rawOcrText = ocrResult.rawText
                )
                _scannedPendingExpense.value = pendingExpense
            } catch (e: Exception) {
                _exportMessage.value = "Scanning failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun savePendingExpense(
        vendorName: String,
        totalAmount: Double,
        dateMillis: Long,
        category: String,
        currencySymbol: String,
        notes: String?,
        supplierContact: String?
    ) {
        viewModelScope.launch {
            val pending = _scannedPendingExpense.value
            val safeAmount = max(0.0, totalAmount)

            val expenseToSave = ExpenseEntity(
                vendorName = vendorName.ifBlank { "Unknown Merchant" },
                totalAmount = safeAmount,
                currency = currencySymbol,
                date = dateMillis,
                category = category,
                imageUri = pending?.imageUri,
                rawOcrText = pending?.rawOcrText,
                notes = notes,
                supplierPhoneOrContact = supplierContact
            )
            repository.saveExpense(expenseToSave)
            _scannedPendingExpense.value = null
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            val safeExpense = expense.copy(totalAmount = max(0.0, expense.totalAmount))
            repository.updateExpense(safeExpense)
        }
    }

    fun discardPendingExpense() {
        _scannedPendingExpense.value = null
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun exportReportCsv() {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val currentList = uiState.value.expenses
                val file = repository.exportCsv(currentList)
                _exportedFile.value = file
                _exportMessage.value = "CSV Report Saved To:\n${file.absolutePath}"
            } catch (e: Exception) {
                _exportMessage.value = "CSV Export failed: ${e.localizedMessage}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun exportReportPdf() {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val currentList = uiState.value.expenses
                val file = repository.exportPdf(currentList)
                _exportedFile.value = file
                _exportMessage.value = "PDF Report Saved To:\n${file.absolutePath}"
            } catch (e: Exception) {
                _exportMessage.value = "PDF Export failed: ${e.localizedMessage}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }
}

class ExpenseViewModelFactory(
    private val application: Application,
    private val repository: ExpenseRepository = ExpenseRepositoryImpl(application)
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            return ExpenseViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
