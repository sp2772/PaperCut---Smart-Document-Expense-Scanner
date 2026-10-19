package com.example.new_app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vendorName: String,
    val totalAmount: Double,
    val currency: String = "$",
    val date: Long, // Epoch timestamp in millis
    val category: String, // e.g., "Food & Dining", "Groceries", "Travel", "Utilities", "Office", "Miscellaneous"
    val imageUri: String? = null,
    val rawOcrText: String? = null,
    val notes: String? = null,
    val supplierPhoneOrContact: String? = null, // Tag supplier / friend / contact number
    val createdAt: Long = System.currentTimeMillis()
)

data class CategorySummary(
    val category: String,
    val total: Double
)
