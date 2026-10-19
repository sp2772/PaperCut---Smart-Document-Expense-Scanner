package com.example.new_app

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.new_app.data.repository.ExpenseRepositoryImpl
import com.example.new_app.ui.screens.HomeScreen
import com.example.new_app.ui.theme.PaperCutTheme
import com.example.new_app.ui.viewmodel.ExpenseViewModel
import com.example.new_app.ui.viewmodel.ExpenseViewModelFactory
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ExpenseViewModel
    private lateinit var repository: ExpenseRepositoryImpl
    private lateinit var scannerLauncher: ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>
    private lateinit var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = ExpenseRepositoryImpl(applicationContext)
        val factory = ExpenseViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[ExpenseViewModel::class.java]

        // Register launcher for ML Kit Document Scanner
        scannerLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val resultData = activityResult.data
                val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(resultData)
                val scannedImageUri = scanResult?.pages?.firstOrNull()?.imageUri

                if (scannedImageUri != null) {
                    viewModel.onDocumentScanned(scannedImageUri)
                } else {
                    pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            } else {
                // If GMS Document Scanner module download fails or is cancelled, fallback to photo picker
                try {
                    pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } catch (_: Exception) {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Register Photo Picker launcher
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                viewModel.onDocumentScanned(uri)
            }
        }

        setContent {
            PaperCutTheme {
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

                HomeScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onRequestScan = {
                        launchDocumentScanner()
                    }
                )
            }
        }
    }

    private fun launchDocumentScanner() {
        lifecycleScope.launch {
            try {
                val intentSenderRequest = repository.createScannerIntentSender(this@MainActivity)
                scannerLauncher.launch(intentSenderRequest)
            } catch (e: Exception) {
                // Fallback to gallery photo picker if Document Scanner launch fails
                try {
                    pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } catch (_: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to launch scanner: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
