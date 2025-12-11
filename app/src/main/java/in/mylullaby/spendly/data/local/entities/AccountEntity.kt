package `in`.mylullaby.spendly.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Account entity for transaction tracking.
 *
 * Represents financial accounts where transactions are sourced from/go to.
 * Users can create custom accounts in addition to the predefined "My Account".
 *
 * Account types:
 * - BANK: Savings/checking bank accounts
 * - CARD: Credit/debit cards
 * - WALLET: Digital wallets (PayTM, GPay, PhonePe, etc.)
 * - CASH: Physical cash
 * - LOAN: Borrowed money accounts
 * - INVESTMENT: Investment/brokerage accounts
 *
 * @property id Unique identifier (auto-generated, predefined account uses ID 1)
 * @property name Account name (unique, e.g., "My Account", "HDFC Credit Card")
 * @property type Account type (BANK/CARD/WALLET/CASH/LOAN/INVESTMENT)
 * @property icon Phosphor Icon name (e.g., "bank", "creditcard", "wallet")
 * @property color ARGB color int for visual distinction
 * @property isCustom True for user-created accounts, false for predefined
 * @property sortOrder Display order in account lists
 * @property createdAt Timestamp when account was created (milliseconds since epoch)
 * @property modifiedAt Timestamp when account was last modified (milliseconds since epoch)
 */
@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["type"]),
        Index(value = ["sort_order"])
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "type")
    val type: String, // BANK, CARD, WALLET, CASH, LOAN, INVESTMENT

    @ColumnInfo(name = "icon")
    val icon: String, // Phosphor Icon name as String

    @ColumnInfo(name = "color")
    val color: Int, // ARGB color int

    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long
)
