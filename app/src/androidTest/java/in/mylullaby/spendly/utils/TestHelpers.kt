package `in`.mylullaby.spendly.utils

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import `in`.mylullaby.spendly.data.local.SpendlyDatabase
import `in`.mylullaby.spendly.data.local.entities.BudgetEntity
import `in`.mylullaby.spendly.data.local.entities.CategoryEntity
import `in`.mylullaby.spendly.data.local.entities.ExpenseEntity
import `in`.mylullaby.spendly.data.local.entities.IncomeEntity
import `in`.mylullaby.spendly.data.local.entities.ReceiptEntity
import `in`.mylullaby.spendly.data.local.entities.RecurringTransactionEntity
import `in`.mylullaby.spendly.data.local.entities.TagEntity
import `in`.mylullaby.spendly.data.local.entities.TransactionTagEntity

/**
 * Test utilities for instrumented tests.
 * Provides helper functions for creating in-memory databases and test data builders.
 */

/**
 * Creates an in-memory Room database for testing.
 * Database is cleared between tests and allows queries on main thread for testing convenience.
 *
 * @return SpendlyDatabase instance configured for testing
 */
fun createTestDatabase(): SpendlyDatabase {
    return Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        SpendlyDatabase::class.java
    )
        .allowMainThreadQueries() // For testing only - simplifies test code
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
        categoryId: Long? = null, // Null = uncategorized
        date: Long = System.currentTimeMillis(),
        description: String = "Test Expense",
        paymentMethod: String = "CASH",
        createdAt: Long = System.currentTimeMillis(),
        modifiedAt: Long = System.currentTimeMillis()
    ) = ExpenseEntity(
        id = id,
        amount = amount,
        categoryId = categoryId,
        date = date,
        description = description,
        paymentMethod = paymentMethod,
        createdAt = createdAt,
        modifiedAt = modifiedAt
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
        source: String = "SALARY",
        date: Long = System.currentTimeMillis(),
        description: String = "Test Income",
        isRecurring: Boolean = false,
        linkedExpenseId: Long? = null,
        createdAt: Long = System.currentTimeMillis(),
        modifiedAt: Long = System.currentTimeMillis()
    ) = IncomeEntity(
        id = id,
        amount = amount,
        source = source,
        date = date,
        description = description,
        isRecurring = isRecurring,
        linkedExpenseId = linkedExpenseId,
        createdAt = createdAt,
        modifiedAt = modifiedAt
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
        categoryId: Long,
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
        description = description,
        frequency = frequency,
        nextDate = nextDate,
        lastProcessed = lastProcessed,
        createdAt = createdAt,
        modifiedAt = modifiedAt
    )

    /**
     * Creates a test TagEntity with default values.
     */
    fun createTestTagEntity(
        id: Long = 0,
        name: String = "Test Tag",
        color: Int = 0xFF000000.toInt(), // Black
        createdAt: Long = System.currentTimeMillis()
    ) = TagEntity(
        id = id,
        name = name,
        color = color,
        createdAt = createdAt
    )

    /**
     * Creates a test TransactionTagEntity (junction table).
     */
    fun createTestTransactionTagEntity(
        transactionId: Long,
        tagId: Long,
        transactionType: String = "EXPENSE"
    ) = TransactionTagEntity(
        transactionId = transactionId,
        tagId = tagId,
        transactionType = transactionType
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
