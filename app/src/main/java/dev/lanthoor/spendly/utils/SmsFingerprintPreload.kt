package dev.lanthoor.spendly.utils

import dev.lanthoor.spendly.domain.model.Expense
import dev.lanthoor.spendly.domain.model.Income

object SmsFingerprintPreload {
    fun fromExpenses(expenses: List<Expense>): List<SmsFingerprint> {
        return expenses.mapNotNull { expense ->
            val smsBody = expense.smsBody ?: return@mapNotNull null
            val smsTimestamp = expense.smsTimestamp ?: return@mapNotNull null
            val parsed = SmsParser.parseBankSms(smsBody, "", smsTimestamp)
            SmsFingerprintFactory.create(
                sender = null,
                body = smsBody,
                timestamp = smsTimestamp,
                parsed = parsed
            )
        }
    }

    fun fromIncome(incomeList: List<Income>): List<SmsFingerprint> {
        return incomeList.mapNotNull { income ->
            val smsBody = income.smsBody ?: return@mapNotNull null
            val smsTimestamp = income.smsTimestamp ?: return@mapNotNull null
            val parsed = SmsParser.parseBankSms(smsBody, "", smsTimestamp)
            SmsFingerprintFactory.create(
                sender = null,
                body = smsBody,
                timestamp = smsTimestamp,
                parsed = parsed
            )
        }
    }
}
