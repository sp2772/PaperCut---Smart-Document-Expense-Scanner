package com.example.new_app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.new_app.data.CurrencyItem
import com.example.new_app.data.WorldCurrencies
import com.example.new_app.data.db.CategoryEntity
import com.example.new_app.data.db.ExpenseEntity
import java.io.File
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultDialog(
    pendingExpense: ExpenseEntity,
    categories: List<CategoryEntity>,
    selectedCurrency: CurrencyItem,
    onSave: (vendor: String, total: Double, category: String, currencySymbol: String, notes: String, supplierContact: String?) -> Unit,
    onDiscard: () -> Unit
) {
    var vendorName by remember { mutableStateOf(pendingExpense.vendorName) }
    var totalAmountStr by remember { mutableStateOf(max(0.0, pendingExpense.totalAmount).toString()) }
    var chosenCurrency by remember { mutableStateOf(selectedCurrency) }
    var selectedCategory by remember { mutableStateOf(pendingExpense.category) }
    var notes by remember { mutableStateOf("") }
    var supplierContact by remember { mutableStateOf("") }

    var isCurrencyPickerModalVisible by remember { mutableStateOf(false) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val categoryNames = if (categories.isNotEmpty()) categories.map { it.name } else listOf("Food & Dining", "Groceries", "Travel & Transport", "Utilities & Bills", "Office & Supplies", "Miscellaneous")

    AlertDialog(
        onDismissRequest = onDiscard,
        title = {
            Text(
                text = "Scanned Receipt Details",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Receipt Image Preview
                pendingExpense.imageUri?.let { imagePath ->
                    val file = File(imagePath)
                    if (file.exists()) {
                        Image(
                            painter = rememberAsyncImagePainter(file),
                            contentDescription = "Scanned Receipt Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Vendor Name Field
                OutlinedTextField(
                    value = vendorName,
                    onValueChange = { vendorName = it },
                    label = { Text("Vendor / Merchant") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Amount & Searchable Currency Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Currency Picker Trigger Button
                    Surface(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isCurrencyPickerModalVisible = true },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${chosenCurrency.flagEmoji} ${chosenCurrency.code}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Currency"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Total Amount Input
                    OutlinedTextField(
                        value = totalAmountStr,
                        onValueChange = { input ->
                            val cleanInput = input.filter { it.isDigit() || it == '.' }
                            totalAmountStr = cleanInput
                        },
                        label = { Text("Total Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Dropdown Picker
                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false }
                    ) {
                        categoryNames.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    isCategoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Supplier / Contact Field
                OutlinedTextField(
                    value = supplierContact,
                    onValueChange = { supplierContact = it },
                    label = { Text("Supplier Phone or Contact (Optional)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Contact Phone",
                            modifier = Modifier.width(18.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rawAmount = totalAmountStr.toDoubleOrNull() ?: 0.0
                    val safeAmount = max(0.0, rawAmount)
                    onSave(vendorName, safeAmount, selectedCategory, chosenCurrency.symbol, notes, supplierContact.ifBlank { null })
                }
            ) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text("Discard")
            }
        }
    )

    // Full Searchable Currency Modal
    if (isCurrencyPickerModalVisible) {
        CurrencyPickerModal(
            onDismiss = { isCurrencyPickerModalVisible = false },
            onCurrencySelected = { item ->
                chosenCurrency = item
                isCurrencyPickerModalVisible = false
            }
        )
    }
}
