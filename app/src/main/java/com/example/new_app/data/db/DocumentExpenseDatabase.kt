package com.example.new_app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ExpenseEntity::class, CategoryEntity::class], version = 3, exportSchema = false)
abstract class DocumentExpenseDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: DocumentExpenseDatabase? = null

        val DEFAULT_CATEGORIES = listOf(
            CategoryEntity("Food & Dining", "#00A896"),
            CategoryEntity("Groceries", "#2A9D8F"),
            CategoryEntity("Travel & Transport", "#E76F51"),
            CategoryEntity("Utilities & Bills", "#F4A261"),
            CategoryEntity("Office & Supplies", "#457B9D"),
            CategoryEntity("Miscellaneous", "#6C757D")
        )

        fun getDatabase(context: Context): DocumentExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DocumentExpenseDatabase::class.java,
                    "papercut_expense_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate default categories
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                database.categoryDao().insertCategories(DEFAULT_CATEGORIES)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
