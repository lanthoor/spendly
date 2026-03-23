package dev.lanthoor.spendly.utils

import dev.lanthoor.spendly.domain.model.Account
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class SmsAccountMatcherTest {

    @Test
    fun `accountHint match beats keyword-only match`() {
        val accounts = listOf(
            account(id = 1, name = "HDFC Savings", type = AccountType.BANK, sortOrder = 1),
            account(id = 2, name = "Scapia Card 1234", type = AccountType.CARD, sortOrder = 2)
        )
        val parsed = parsedTransaction(accountHint = "1234")

        val result = SmsAccountMatcher.resolveAccountId(
            accounts = accounts,
            parsed = parsed,
            sender = "HDFCBK",
            body = "INR 500 debited from A/c XX1234"
        )

        assertEquals(2L, result)
    }

    @Test
    fun `sender keyword match selects account when no hint`() {
        val accounts = listOf(
            account(id = 1, name = "HDFC Savings", type = AccountType.BANK, sortOrder = 1),
            account(id = 2, name = "ICICI Credit Card", type = AccountType.CARD, sortOrder = 2)
        )
        val parsed = parsedTransaction(accountHint = null)

        val result = SmsAccountMatcher.resolveAccountId(
            accounts = accounts,
            parsed = parsed,
            sender = "ICICIB",
            body = "Rs.200 debited"
        )

        assertEquals(2L, result)
    }

    @Test
    fun `type affinity prefers card account for card phrasing`() {
        val accounts = listOf(
            account(id = 1, name = "Main Bank", type = AccountType.BANK, sortOrder = 1),
            account(id = 2, name = "Travel Card", type = AccountType.CARD, sortOrder = 2)
        )
        val parsed = parsedTransaction(description = "credit card spent at store", accountHint = null)

        val result = SmsAccountMatcher.resolveAccountId(
            accounts = accounts,
            parsed = parsed,
            sender = "BANK",
            body = "INR 500 spent on card"
        )

        assertEquals(2L, result)
    }

    @Test
    fun `type affinity prefers wallet account for upi phrasing`() {
        val accounts = listOf(
            account(id = 1, name = "Main Bank", type = AccountType.BANK, sortOrder = 1),
            account(id = 2, name = "Paytm Wallet", type = AccountType.WALLET, sortOrder = 2)
        )
        val parsed = parsedTransaction(description = "payment to merchant@paytm", accountHint = null)

        val result = SmsAccountMatcher.resolveAccountId(
            accounts = accounts,
            parsed = parsed,
            sender = "PAYTM",
            body = "Rs.250 sent via UPI"
        )

        assertEquals(2L, result)
    }

    @Test
    fun `type affinity prefers bank account for bank account phrasing`() {
        val accounts = listOf(
            account(id = 1, name = "SBI Savings", type = AccountType.BANK, sortOrder = 1),
            account(id = 2, name = "General Card", type = AccountType.CARD, sortOrder = 2)
        )
        val parsed = parsedTransaction(description = "debited from account", accountHint = null)

        val result = SmsAccountMatcher.resolveAccountId(
            accounts = accounts,
            parsed = parsed,
            sender = "SBISMS",
            body = "Rs 100 debited from account"
        )

        assertEquals(1L, result)
    }

    @Test
    fun `tie break picks lowest sortOrder then lowest id`() {
        val accounts = listOf(
            account(id = 3, name = "Primary Account", type = AccountType.BANK, sortOrder = 2),
            account(id = 2, name = "Secondary Account", type = AccountType.BANK, sortOrder = 2),
            account(id = 4, name = "Tertiary Account", type = AccountType.BANK, sortOrder = 3)
        )
        val parsed = parsedTransaction(description = "debited from account", accountHint = null)

        val result = SmsAccountMatcher.resolveAccountId(
            accounts = accounts,
            parsed = parsed,
            sender = "BANK",
            body = "Rs 100 debited from account"
        )

        assertEquals(2L, result)
    }

    @Test
    fun `zero signal returns null for fallback`() {
        val accounts = listOf(
            account(id = 1, name = "Main Bank", type = AccountType.BANK, sortOrder = 1),
            account(id = 2, name = "Travel Card", type = AccountType.CARD, sortOrder = 2)
        )
        val parsed = parsedTransaction(description = "misc", accountHint = null, merchantName = null)

        val result = SmsAccountMatcher.resolveAccountId(
            accounts = accounts,
            parsed = parsed,
            sender = "ALERT",
            body = "hello world"
        )

        assertNull(result)
    }

    @Test
    fun `keyword matching is locale invariant`() {
        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            val accounts = listOf(
                account(id = 1, name = "ICICI Savings", type = AccountType.BANK, sortOrder = 1),
                account(id = 2, name = "HDFC Savings", type = AccountType.BANK, sortOrder = 2)
            )
            val parsed = parsedTransaction(accountHint = null)

            val result = SmsAccountMatcher.resolveAccountId(
                accounts = accounts,
                parsed = parsed,
                sender = "ICICIB",
                body = "Rs.200 debited"
            )

            assertEquals(1L, result)
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun `resolveDefaultAccountId prefers predefined default id`() {
        val accounts = listOf(
            account(id = 7, name = "Custom First", type = AccountType.BANK, sortOrder = 0),
            account(
                id = 1,
                name = "My Account",
                type = AccountType.BANK,
                sortOrder = 10
            )
        )

        val result = SmsAccountMatcher.resolveDefaultAccountId(accounts)

        assertEquals(1L, result)
    }

    @Test
    fun `resolveDefaultAccountId falls back to first when default missing`() {
        val accounts = listOf(
            account(id = 7, name = "Custom First", type = AccountType.BANK, sortOrder = 0),
            account(id = 9, name = "Custom Second", type = AccountType.CARD, sortOrder = 1)
        )

        val result = SmsAccountMatcher.resolveDefaultAccountId(accounts)

        assertEquals(7L, result)
    }

    private fun parsedTransaction(
        description: String = "transaction",
        accountHint: String? = null,
        merchantName: String? = null
    ): ParsedTransaction {
        return ParsedTransaction(
            amount = 10000L,
            transactionType = TransactionType.EXPENSE,
            date = 0L,
            description = description,
            accountHint = accountHint,
            merchantName = merchantName,
            confidence = 0.9f
        )
    }

    private fun account(id: Long, name: String, type: AccountType, sortOrder: Int): Account {
        return Account(
            id = id,
            name = name,
            type = type,
            icon = "icon",
            color = 0,
            isCustom = true,
            sortOrder = sortOrder,
            createdAt = 0L,
            modifiedAt = 0L
        )
    }
}
