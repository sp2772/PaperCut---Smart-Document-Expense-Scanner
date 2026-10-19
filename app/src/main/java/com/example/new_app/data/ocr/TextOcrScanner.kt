package com.example.new_app.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class OcrResult(
    val vendorName: String,
    val totalAmount: Double,
    val dateMillis: Long,
    val category: String,
    val rawText: String
)

class TextOcrScanner(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanDocumentUri(uri: Uri): OcrResult = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(context.applicationContext, uri)
            recognizer.process(image)
                .addOnSuccessListener { text ->
                    if (continuation.isActive) {
                        val parsed = parseReceiptText(text)
                        continuation.resume(parsed)
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(exception)
                    }
                }
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resumeWithException(e)
            }
        }
    }

    private fun parseReceiptText(mlText: Text): OcrResult {
        val rawText = mlText.text
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        val vendorName = extractVendor(lines)
        val totalAmount = extractTotalAmount(lines, rawText)
        val dateMillis = extractDate(rawText)
        val category = autoCategorize(rawText, vendorName)

        return OcrResult(
            vendorName = vendorName,
            totalAmount = totalAmount,
            dateMillis = dateMillis,
            category = category,
            rawText = rawText
        )
    }

    private fun extractVendor(lines: List<String>): String {
        // Exclude common header noise words
        val genericHeaderWords = listOf("welcome", "receipt", "tax invoice", "cash receipt", "store #", "tel:", "phone")
        for (line in lines.take(5)) {
            val lower = line.lowercase(Locale.ROOT)
            if (genericHeaderWords.none { lower.contains(it) } && line.length >= 3 && line.any { it.isLetter() }) {
                return line.take(30)
            }
        }
        return "Unknown Merchant"
    }

    private fun extractTotalAmount(lines: List<String>, rawText: String): Double {
        // Priority 1: Look for explicit lines containing TOTAL, AMOUNT, DUE
        val totalRegex = Pattern.compile("(?i)(?:total|amount|due|balance|paid)\\s*[:\\$]?\\s*([0-9]+[.,][0-9]{2})")
        for (line in lines.reversed()) {
            val matcher = totalRegex.matcher(line)
            if (matcher.find()) {
                val amountStr = matcher.group(1)?.replace(',', '.') ?: continue
                amountStr.toDoubleOrNull()?.let { return it }
            }
        }

        // Priority 2: Fallback to highest numeric currency match $XX.XX or XX.XX in raw text
        val currencyRegex = Pattern.compile("\\b([0-9]+\\.[0-9]{2})\\b")
        val matcher = currencyRegex.matcher(rawText)
        val amounts = mutableListOf<Double>()
        while (matcher.find()) {
            matcher.group(1)?.toDoubleOrNull()?.let { amounts.add(it) }
        }

        return amounts.maxOrNull() ?: 0.0
    }

    private fun extractDate(rawText: String): Long {
        // Matches MM/DD/YYYY, YYYY-MM-DD, DD.MM.YYYY, etc.
        val datePatterns = listOf(
            Pair(Pattern.compile("\\b(\\d{1,2})[/.-](\\d{1,2})[/.-](\\d{2,4})\\b"), "MM/dd/yyyy"),
            Pair(Pattern.compile("\\b(\\d{4})[/.-](\\d{1,2})[/.-](\\d{1,2})\\b"), "yyyy-MM-dd"),
            Pair(Pattern.compile("(?i)\\b(\\d{1,2})\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{4})\\b"), "dd MMM yyyy")
        )

        for ((pattern, format) in datePatterns) {
            val matcher = pattern.matcher(rawText)
            if (matcher.find()) {
                val dateStr = matcher.group(0)
                if (dateStr != null) {
                    try {
                        val sdf = SimpleDateFormat(format, Locale.US)
                        val date = sdf.parse(dateStr)
                        if (date != null && date.time <= System.currentTimeMillis()) {
                            return date.time
                        }
                    } catch (_: Exception) {
                        // ignore and continue
                    }
                }
            }
        }
        return System.currentTimeMillis()
    }

    private fun autoCategorize(rawText: String, vendor: String): String {
        val textLower = "$rawText $vendor".lowercase(Locale.ROOT)

        return when {
            listOf("coffee", "starbucks", "cafe", "restaurant", "burger", "pizza", "diner", "food", "kitchen", "baking", "eat").any { textLower.contains(it) } -> "Food & Dining"
            listOf("walmart", "target", "grocery", "supermarket", "market", "trader", "whole foods", "produce", "kroger").any { textLower.contains(it) } -> "Groceries"
            listOf("uber", "lyft", "flight", "airline", "transit", "metro", "bus", "taxi", "fuel", "gas", "shell", "chevron", "parking").any { textLower.contains(it) } -> "Travel & Transport"
            listOf("electricity", "electric", "water", "internet", "wifi", "telecom", "utility", "bill", "mobile", "verizon", "att").any { textLower.contains(it) } -> "Utilities & Bills"
            listOf("office", "staples", "paper", "amazon", "depot", "software", "printer", "apple", "microsoft", "supplies").any { textLower.contains(it) } -> "Office & Supplies"
            else -> "Miscellaneous"
        }
    }
}
