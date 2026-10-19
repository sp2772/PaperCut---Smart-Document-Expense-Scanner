package com.example.new_app.data.scanner

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DocumentScannerManager(private val context: Context) {

    /**
     * Creates an IntentSenderRequest to launch ML Kit Document Scanner using the provided Activity.
     */
    suspend fun createScannerIntentSenderRequest(activity: Activity): IntentSenderRequest = suspendCancellableCoroutine { continuation ->
        val options = GmsDocumentScannerOptions.Builder()
            .setPageLimit(1)
            .setGalleryImportAllowed(true)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .build()

        val scanner = GmsDocumentScanning.getClient(options)

        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                if (continuation.isActive) {
                    continuation.resume(IntentSenderRequest.Builder(intentSender).build())
                }
            }
            .addOnFailureListener { exception ->
                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }
    }

    /**
     * Copies temporary scanned image Uri to local app storage directory to ensure permanence.
     */
    suspend fun saveImagePermanently(sourceUri: Uri): String = withContext(Dispatchers.IO) {
        val storageDir = File(context.applicationContext.filesDir, "scanned_receipts")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val destFile = File(storageDir, "receipt_${System.currentTimeMillis()}.jpg")
        context.applicationContext.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        destFile.absolutePath
    }
}
