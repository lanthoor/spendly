package dev.lanthoor.spendly.utils

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import dev.lanthoor.spendly.data.local.SpendlyDatabase
import dev.lanthoor.spendly.data.local.entities.AccountEntity
import dev.lanthoor.spendly.data.local.entities.BudgetEntity
import dev.lanthoor.spendly.data.local.entities.CategoryEntity
import dev.lanthoor.spendly.data.local.entities.ExpenseEntity
import dev.lanthoor.spendly.data.local.entities.IncomeEntity
import dev.lanthoor.spendly.data.local.entities.ReceiptEntity
import dev.lanthoor.spendly.data.local.entities.RecurringTransactionEntity

/**
 * Test utilities for instrumented tests.
 * Provides helper functions for creating in-memory databases and test data builders.
 */

/**
 * Creates an in-memory Room database for testing.
 * Database is cleared between tests and allows queries on main thread for testing convenience.
 * Automatically seeds the default account ("My Account") on creation to satisfy foreign key constraints.
 *
 * @return SpendlyDatabase instance configured for testing
 */
fun createTestDatabase(): SpendlyDatabase {
    return Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        SpendlyDatabase::class.java
    )
        .allowMainThreadQueries() // For testing only - simplifies test code
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed default account for foreign key constraints
                // This runs synchronously before any tests execute
                db.execSQL(
                    """
                    INSERT INTO accounts (id, name, type, icon, color, is_custom, sort_order, created_at, modified_at)
                    VALUES (1, 'My Account', 'BANK', 'bank', ${0xFF2196F3.toInt()}, 0, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
                """.trimIndent()
                )
            }
        })
        .build()
}

/**
 * Test data builders for creating entity instances with sensible defaults.
 * All parameters have default values and can be overridden as needed.
 */
object TestDataBuilders {

    /**
     * Creates a test ExpenseEntity with default values.
     * Amount defaults to 12345L paise (₹123.45).
     */
    fun createTestExpenseEntity(
        id: Long = 0,
        amount: Long = 12345L,
        categoryId: Long? = null, // Null = Others
        date: Long = System.currentTimeMillis(),
        description: String = "Test Expense",
        accountId: Long = 1L, // Default account ID
        createdAt: Long = System.currentTimeMillis(),
        modifiedAt: Long = System.currentTimeMillis(),
        smsSourceId: Long? = null,
        smsBody: String? = null,
        smsConfidence: Float? = null,
        smsTimestamp: Long? = null
    ) = ExpenseEntity(
        id = id,
        amount = amount,
        categoryId = categoryId,
        date = date,
        description = description,
        accountId = accountId,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        smsSourceId = smsSourceId,
        smsBody = smsBody,
        smsConfidence = smsConfidence,
        smsTimestamp = smsTimestamp
    )

    /**
     * Creates a test CategoryEntity with default values.
     */
    fun createTestCategoryEntity(
        id: Long = 0,
        name: String = "Test Category",
        icon: String = "category",
        color: Int = 0xFF9E9E9E.toInt(), // Gray
        isCustom: Boolean = true,
        sortOrder: Int = 999
    ) = CategoryEntity(
        id = id,
        name = name,
        icon = icon,
        color = color,
        isCustom = isCustom,
        sortOrder = sortOrder
    )

    /**
     * Creates a test IncomeEntity with default values.
     * Amount defaults to 50000L paise (₹500.00).
     */
    fun createTestIncomeEntity(
        id: Long = 0,
        amount: Long = 50000L,
        categoryId: Long? = null,
        source: String = "SALARY",
        date: Long = System.currentTimeMillis(),
        description: String = "Test Income",
        accountId: Long = 1L, // Default account ID
        isRecurring: Boolean = false,
        linkedExpenseId: Long? = null,
        createdAt: Long = System.currentTimeMillis(),
        modifiedAt: Long = System.currentTimeMillis(),
        smsSourceId: Long? = null,
        smsBody: String? = null,
        smsConfidence: Float? = null,
        smsTimestamp: Long? = null
    ) = IncomeEntity(
        id = id,
        amount = amount,
        categoryId = categoryId,
        source = source,
        date = date,
        description = description,
        accountId = accountId,
        isRecurring = isRecurring,
        linkedExpenseId = linkedExpenseId,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        smsSourceId = smsSourceId,
        smsBody = smsBody,
        smsConfidence = smsConfidence,
        smsTimestamp = smsTimestamp
    )

    /**
     * Creates a test ReceiptEntity with default values.
     * File size defaults to 1MB (1,048,576 bytes).
     */
    fun createTestReceiptEntity(
        id: Long = 0,
        expenseId: Long,
        filePath: String = "/path/to/receipt.jpg",
        fileType: String = "JPG",
        fileSizeBytes: Long = 1_048_576L, // 1MB
        compressed: Boolean = true,
        createdAt: Long = System.currentTimeMillis()
    ) = ReceiptEntity(
        id = id,
        expenseId = expenseId,
        filePath = filePath,
        fileType = fileType,
        fileSizeBytes = fileSizeBytes,
        compressed = compressed,
        createdAt = createdAt
    )

    /**
     * Creates a test BudgetEntity with default values.
     * Amount defaults to 1000000L paise (₹10,000.00).
     */
    fun createTestBudgetEntity(
        id: Long = 0,
        categoryId: Long? = null, // Null = overall budget
        amount: Long = 1_000_000L,
        month: Int = 1,
        year: Int = 2025,
        notification75Sent: Boolean = false,
        notification100Sent: Boolean = false,
        createdAt: Long = System.currentTimeMillis(),
        modifiedAt: Long = System.currentTimeMillis()
    ) = BudgetEntity(
        id = id,
        categoryId = categoryId,
        amount = amount,
        month = month,
        year = year,
        notification75Sent = notification75Sent,
        notification100Sent = notification100Sent,
        createdAt = createdAt,
        modifiedAt = modifiedAt
    )

    /**
     * Creates a test RecurringTransactionEntity with default values.
     * Amount defaults to 20000L paise (₹200.00).
     */
    fun createTestRecurringTransactionEntity(
        id: Long = 0,
        transactionType: String = "EXPENSE",
        amount: Long = 20000L,
        categoryId: Long = 1L,
        accountId: Long = 1L, // Default account ID
        description: String = "Test Recurring Transaction",
        frequency: String = "MONTHLY",
        nextDate: Long = System.currentTimeMillis(),
        lastProcessed: Long? = null,
        createdAt: Long = System.currentTimeMillis(),
        modifiedAt: Long = System.currentTimeMillis()
    ) = RecurringTransactionEntity(
        id = id,
        transactionType = transactionType,
        amount = amount,
        categoryId = categoryId,
        accountId = accountId,
        description = description,
        frequency = frequency,
        nextDate = nextDate,
        lastProcessed = lastProcessed,
        createdAt = createdAt,
        modifiedAt = modifiedAt
    )

    /**
     * Creates a test AccountEntity with default values.
     */
    fun createTestAccountEntity(
        id: Long = 0,
        name: String = "Test Account",
        type: String = "BANK",
        icon: String = "bank",
        color: Int = 0xFF2196F3.toInt(), // Blue
        isCustom: Boolean = true,
        sortOrder: Int = 999,
        createdAt: Long = System.currentTimeMillis(),
        modifiedAt: Long = System.currentTimeMillis()
    ) = AccountEntity(
        id = id,
        name = name,
        type = type,
        icon = icon,
        color = color,
        isCustom = isCustom,
        sortOrder = sortOrder,
        createdAt = createdAt,
        modifiedAt = modifiedAt
    )
}

/**
 * Extension function to convert milliseconds to days for test date calculations.
 */
fun Long.daysAgo(days: Int): Long = this - (days * 24 * 60 * 60 * 1000L)

/**
 * Extension function to convert milliseconds to days in the future for test date calculations.
 */
fun Long.daysFromNow(days: Int): Long = this + (days * 24 * 60 * 60 * 1000L)
