package com.example.new_app.data.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.new_app.data.db.ExpenseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportExporter(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun getReportDirectory(): File {
        val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val dir = File(publicDocs, "PaperCut")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return if (dir.exists()) dir else File(context.applicationContext.filesDir, "PaperCut_Reports").apply { if (!exists()) mkdirs() }
    }

    suspend fun exportToCsv(expenses: List<ExpenseEntity>): File = withContext(Dispatchers.IO) {
        val reportsDir = getReportDirectory()
        val file = File(reportsDir, "papercut_expenses_${System.currentTimeMillis()}.csv")
        file.bufferedWriter().use { writer ->
            writer.write("ID,Date,Vendor,Amount,Currency,Category,Notes\n")
            for (expense in expenses) {
                val dateStr = dateFormat.format(Date(expense.date))
                val vendorEscaped = expense.vendorName.replace("\"", "\"\"")
                val notesEscaped = (expense.notes ?: "").replace("\"", "\"\"")
                writer.write("${expense.id},\"$dateStr\",\"$vendorEscaped\",${expense.totalAmount},\"${expense.currency}\",\"${expense.category}\",\"$notesEscaped\"\n")
            }
        }
        file
    }

    suspend fun exportToPdf(expenses: List<ExpenseEntity>, title: String = "Expense Summary Report"): File = withContext(Dispatchers.IO) {
        val reportsDir = getReportDirectory()
        val file = File(reportsDir, "papercut_report_${System.currentTimeMillis()}.pdf")
        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size @ 72 DPI
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(0, 168, 150)
            textSize = 22f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
        }

        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 50f

        // Draw Header
        canvas.drawText("PaperCut - Smart Expense Report", 40f, y, titlePaint)
        y += 25f
        canvas.drawText("Generated on: ${dateFormat.format(Date())} | Total Expenses: ${expenses.size}", 40f, y, textPaint)
        y += 15f

        val totalSum = expenses.sumOf { it.totalAmount }
        val sumPaint = Paint().apply {
            color = Color.rgb(46, 125, 50)
            textSize = 14f
            isFakeBoldText = true
        }
        val currencySymbol = expenses.firstOrNull()?.currency ?: "$"
        canvas.drawText("Grand Total: $currencySymbol${String.format(Locale.US, "%.2f", totalSum)}", 40f, y + 10f, sumPaint)
        y += 35f

        canvas.drawLine(40f, y, 550f, y, linePaint)
        y += 20f

        // Table Header
        canvas.drawText("Date", 40f, y, headerPaint)
        canvas.drawText("Vendor", 130f, y, headerPaint)
        canvas.drawText("Category", 310f, y, headerPaint)
        canvas.drawText("Amount", 480f, y, headerPaint)
        y += 10f
        canvas.drawLine(40f, y, 550f, y, linePaint)
        y += 20f

        // Table Rows
        for (expense in expenses.take(35)) {
            val dateStr = dateFormat.format(Date(expense.date))
            canvas.drawText(dateStr, 40f, y, textPaint)
            canvas.drawText(expense.vendorName.take(22), 130f, y, textPaint)
            canvas.drawText(expense.category.take(20), 310f, y, textPaint)
            canvas.drawText("${expense.currency}${String.format(Locale.US, "%.2f", expense.totalAmount)}", 480f, y, textPaint)

            y += 18f
            if (y > 780f) break
        }

        canvas.drawLine(40f, y, 550f, y, linePaint)
        pdfDocument.finishPage(page)

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        file
    }
}
