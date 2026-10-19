package com.example.new_app.data.repository

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import com.example.new_app.data.db.CategoryDao
import com.example.new_app.data.db.CategoryEntity
import com.example.new_app.data.db.CategorySummary
import com.example.new_app.data.db.DocumentExpenseDatabase
import com.example.new_app.data.db.ExpenseDao
import com.example.new_app.data.db.ExpenseEntity
import com.example.new_app.data.export.ReportExporter
import com.example.new_app.data.ocr.OcrResult
import com.example.new_app.data.ocr.TextOcrScanner
import com.example.new_app.data.scanner.DocumentScannerManager
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    fun getExpensesPaginated(page: Int, pageSize: Int = 20): Flow<List<ExpenseEntity>>
    fun searchExpensesPaginated(query: String, page: Int, pageSize: Int = 20): Flow<List<ExpenseEntity>>
    fun getExpensesByCategoryPaginated(category: String, page: Int, pageSize: Int = 20): Flow<List<ExpenseEntity>>
    fun getExpenseCount(): Flow<Int>

    fun getCategorySummaries(): Flow<List<CategorySummary>>
    fun getTotalExpenseAmount(): Flow<Double?>
    fun getAllCategories(): Flow<List<CategoryEntity>>

    suspend fun getExpenseById(id: Long): ExpenseEntity?
    suspend fun saveExpense(expense: ExpenseEntity)
    suspend fun updateExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(expense: ExpenseEntity)
    suspend fun deleteExpenseById(id: Long)
    suspend fun addCategory(category: CategoryEntity)

    // Scanner & OCR Operations
    suspend fun createScannerIntentSender(activity: Activity): IntentSenderRequest
    suspend fun processScannedDocument(uri: Uri): Pair<OcrResult, String>

    // Report Export Operations
    suspend fun exportCsv(expenses: List<ExpenseEntity>): File
    suspend fun exportPdf(expenses: List<ExpenseEntity>): File
}

class ExpenseRepositoryImpl(context: Context) : ExpenseRepository {

    private val appContext = context.applicationContext
    private val db = DocumentExpenseDatabase.getDatabase(appContext)
    private val expenseDao: ExpenseDao = db.expenseDao()
    private val categoryDao: CategoryDao = db.categoryDao()
    private val ocrScanner = TextOcrScanner(appContext)
    private val scannerManager = DocumentScannerManager(appContext)
    private val reportExporter = ReportExporter(appContext)

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    override fun getExpensesPaginated(page: Int, pageSize: Int): Flow<List<ExpenseEntity>> {
        val offset = (page - 1) * pageSize
        return expenseDao.getExpensesPaginated(pageSize, offset)
    }

    override fun searchExpensesPaginated(query: String, page: Int, pageSize: Int): Flow<List<ExpenseEntity>> {
        val offset = (page - 1) * pageSize
        return expenseDao.searchExpensesPaginated(query, pageSize, offset)
    }

    override fun getExpensesByCategoryPaginated(category: String, page: Int, pageSize: Int): Flow<List<ExpenseEntity>> {
        val offset = (page - 1) * pageSize
        return expenseDao.getExpensesByCategoryPaginated(category, pageSize, offset)
    }

    override fun getExpenseCount(): Flow<Int> = expenseDao.getExpenseCount()

    override fun getCategorySummaries(): Flow<List<CategorySummary>> = expenseDao.getCategorySummaries()

    override fun getTotalExpenseAmount(): Flow<Double?> = expenseDao.getTotalExpenseAmount()

    override fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    override suspend fun getExpenseById(id: Long): ExpenseEntity? = expenseDao.getExpenseById(id)

    override suspend fun saveExpense(expense: ExpenseEntity) = expenseDao.insertExpense(expense)

    override suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.updateExpense(expense)

    override suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.deleteExpense(expense)

    override suspend fun deleteExpenseById(id: Long) = expenseDao.deleteExpenseById(id)

    override suspend fun addCategory(category: CategoryEntity) = categoryDao.insertCategory(category)

    override suspend fun createScannerIntentSender(activity: Activity): IntentSenderRequest = scannerManager.createScannerIntentSenderRequest(activity)

    override suspend fun processScannedDocument(uri: Uri): Pair<OcrResult, String> {
        val ocrResult = ocrScanner.scanDocumentUri(uri)
        val permanentImagePath = scannerManager.saveImagePermanently(uri)
        return Pair(ocrResult, permanentImagePath)
    }

    override suspend fun exportCsv(expenses: List<ExpenseEntity>): File = reportExporter.exportToCsv(expenses)

    override suspend fun exportPdf(expenses: List<ExpenseEntity>): File = reportExporter.exportToPdf(expenses)
}
