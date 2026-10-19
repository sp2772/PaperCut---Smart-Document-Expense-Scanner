package com.example.new_app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.new_app.data.db.ExpenseEntity
import com.example.new_app.ui.components.ExpenseCard
import com.example.new_app.ui.viewmodel.ExpenseUiState
import com.example.new_app.ui.viewmodel.ExpenseViewModel
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    uiState: ExpenseUiState,
    onRequestScan: () -> Unit
) {
    val context = LocalContext.current
    var selectedExpenseForDetail by remember { mutableStateOf<ExpenseEntity?>(null) }
    var isExportDialogVisible by remember { mutableStateOf(false) }
    var isAddCategoryDialogVisible by remember { mutableStateOf(false) }
    var isCurrencyPickerModalVisible by remember { mutableStateOf(false) }
    var isBudgetGoalDialogVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show export/scan snackbar messages with optional Open File action
    LaunchedEffect(uiState.exportMessage) {
        uiState.exportMessage?.let { msg ->
            val exportedFile = uiState.exportedFile
            val hasFile = exportedFile != null && exportedFile.exists()

            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = if (hasFile) "OPEN" else null,
                duration = SnackbarDuration.Long
            )

            if (result == SnackbarResult.ActionPerformed && exportedFile != null) {
                openExportedFile(context, exportedFile)
            }
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "App Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "PaperCut",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    // Searchable Currency Picker Trigger
                    Surface(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isCurrencyPickerModalVisible = true },
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${uiState.selectedCurrency.flagEmoji} ${uiState.selectedCurrency.code}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Monthly Budget Goal Editor
                    IconButton(onClick = { isBudgetGoalDialogVisible = true }) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Budget Target",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Export Report Action
                    IconButton(onClick = { isExportDialogVisible = true }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export Report",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onRequestScan,
                icon = {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = "Scan Receipt"
                    )
                },
                text = { Text("Scan Receipt") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Sleek Centered Dark Total Expenses & Budget Target Card
                SummaryHeaderCard(
                    totalAmount = uiState.totalAmount,
                    currencySymbol = uiState.selectedCurrency.symbol,
                    expenseCount = uiState.totalRecordCount,
                    budgetGoal = uiState.monthlyBudgetGoal
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Compact Fixed Height Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Search vendor, category, or notes...",
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.onSearchQueryChanged("") },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Colored Category Filter Chips + Add Custom Category Chip
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add Custom Category Chip
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { isAddCategoryDialogVisible = true },
                            label = { Text("+ Add Category", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                labelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    items(uiState.categories) { category ->
                        val isSelected = uiState.selectedCategoryFilter == category.name
                        val categoryColor = parseHexColor(category.colorHex)

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onCategoryFilterSelected(category.name) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(categoryColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(category.name)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = categoryColor.copy(alpha = 0.3f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Expenses List or Centered Empty State
                if (uiState.expenses.isEmpty()) {
                    EmptyExpensesState()
                } else {
                    val categoryColorMap = uiState.categories.associate { it.name to it.colorHex }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(
                            items = uiState.expenses,
                            key = { it.id }
                        ) { expense ->
                            val colorHex = categoryColorMap[expense.category] ?: "#00A896"

                            ExpenseCard(
                                expense = expense,
                                categoryColorHex = colorHex,
                                onClick = { selectedExpenseForDetail = expense },
                                onDeleteClick = { viewModel.deleteExpense(expense) }
                            )
                        }

                        // Pagination Bar at Bottom of List
                        item {
                            PaginationBar(
                                currentPage = uiState.currentPage,
                                totalPages = uiState.totalPages,
                                totalRecords = uiState.totalRecordCount,
                                pageSize = uiState.pageSize,
                                currentRecordCount = uiState.expenses.size,
                                onPrevious = { viewModel.previousPage() },
                                onNext = { viewModel.nextPage() }
                            )
                        }
                    }
                }
            }

            // Progress Overlay during scanning/exporting
            if (uiState.isScanning || uiState.isExporting) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ElevatedCard {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = if (uiState.isScanning) "Scanning document..." else "Generating report...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }

    // Full Searchable Currency Modal
    if (isCurrencyPickerModalVisible) {
        CurrencyPickerModal(
            onDismiss = { isCurrencyPickerModalVisible = false },
            onCurrencySelected = { item ->
                viewModel.onCurrencySelected(item)
                isCurrencyPickerModalVisible = false
            }
        )
    }

    // Monthly Budget Goal Editor
    if (isBudgetGoalDialogVisible) {
        BudgetGoalDialog(
            currentGoal = uiState.monthlyBudgetGoal,
            currencySymbol = uiState.selectedCurrency.symbol,
            onDismiss = { isBudgetGoalDialogVisible = false },
            onSaveGoal = { goal ->
                viewModel.updateMonthlyBudgetGoal(goal)
                isBudgetGoalDialogVisible = false
            }
        )
    }

    // Scanned Pending Expense Dialog
    uiState.scannedPendingExpense?.let { pending ->
        ScanResultDialog(
            pendingExpense = pending,
            categories = uiState.categories,
            selectedCurrency = uiState.selectedCurrency,
            onSave = { vendor, total, category, currencySymbol, notes, supplierContact ->
                viewModel.savePendingExpense(vendor, total, System.currentTimeMillis(), category, currencySymbol, notes, supplierContact)
            },
            onDiscard = { viewModel.discardPendingExpense() }
        )
    }

    // Add Custom Category Dialog
    if (isAddCategoryDialogVisible) {
        AddCategoryDialog(
            onDismiss = { isAddCategoryDialogVisible = false },
            onAddCategory = { name, colorHex ->
                viewModel.addCustomCategory(name, colorHex)
            }
        )
    }

    // Expense Detail Dialog
    selectedExpenseForDetail?.let { detailExpense ->
        ExpenseDetailDialog(
            expense = detailExpense,
            onDismiss = { selectedExpenseForDetail = null },
            onDelete = { viewModel.deleteExpense(it) }
        )
    }

    // Export Dialog
    if (isExportDialogVisible) {
        ExportDialog(
            onDismiss = { isExportDialogVisible = false },
            onExportCsv = {
                viewModel.exportReportCsv()
                isExportDialogVisible = false
            },
            onExportPdf = {
                viewModel.exportReportPdf()
                isExportDialogVisible = false
            }
        )
    }
}

@Composable
fun SummaryHeaderCard(totalAmount: Double, currencySymbol: String, expenseCount: Int, budgetGoal: Double) {
    val progress = (totalAmount / budgetGoal).coerceIn(0.0, 1.0).toFloat()
    val isOverBudget = totalAmount > budgetGoal
    val gaugeColor = if (isOverBudget) Color(0xFFE76F51) else Color(0xFF00A896)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF121212) // Sleek Dark Background
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Perfectly Centered Text
        ) {
            Text(
                text = "Total Expenses",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Formatted Total Amount
            Text(
                text = "$currencySymbol${String.format(Locale.US, "%.2f", totalAmount)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = gaugeColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Pluralization
            val receiptLabel = if (expenseCount == 1) "1 receipt recorded" else "$expenseCount receipts recorded"
            Text(
                text = receiptLabel,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Monthly Budget Progress Gauge Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = gaugeColor,
                    trackColor = Color.White.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                val percentStr = String.format(Locale.US, "%.1f", (totalAmount / budgetGoal) * 100)
                Text(
                    text = "Target: $currencySymbol${String.format(Locale.US, "%.0f", budgetGoal)} ($percentStr% Spent)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverBudget) Color(0xFFE76F51) else Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    totalRecords: Int,
    pageSize: Int,
    currentRecordCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val startRecord = if (totalRecords == 0) 0 else ((currentPage - 1) * pageSize) + 1
    val endRecord = startRecord + currentRecordCount - 1

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = currentPage > 1,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous Page",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Prev")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Page $currentPage of $totalPages",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Showing $startRecord-$endRecord of $totalRecords",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            OutlinedButton(
                onClick = onNext,
                enabled = currentPage < totalPages,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next Page",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyExpensesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DocumentScanner,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Expenses Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap 'Scan Receipt' below to capture your first invoice or receipt.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun openExportedFile(context: android.content.Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val mimeType = if (file.extension.lowercase(Locale.ROOT) == "pdf") "application/pdf" else "text/csv"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Open Report"))
    } catch (_: Exception) {
        // Fallback or share intent
    }
}
