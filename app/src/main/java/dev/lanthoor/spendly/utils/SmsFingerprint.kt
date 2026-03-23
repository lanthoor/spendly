package dev.lanthoor.spendly.utils

data class SmsFingerprint(
    val strictKey: String,
    val strictBodyTimestampKey: String,
    val strictBodySenderKey: String,
    val hasSender: Boolean,
    val semanticSenderKey: String?,
    val semanticAnySenderKey: String?,
    val timestamp: Long
)

object SmsFingerprintFactory {
    private val senderPrefixRegex = Regex("^[a-z]{2}-")
    private val collapseWhitespaceRegex = Regex("\\s+")

    fun create(
        sender: String?,
        body: String,
        timestamp: Long,
        parsed: ParsedTransaction?
    ): SmsFingerprint {
        val normalizedSender = normalizeSender(sender)
        val normalizedBody = normalizeBody(body)

        val strictKey = "s:$normalizedSender|b:$normalizedBody|t:$timestamp"
        val strictBodyTimestampKey = "b:$normalizedBody|t:$timestamp"
        val strictBodySenderKey = "s:$normalizedSender|b:$normalizedBody"
        val hasSender = normalizedSender.isNotBlank()

        val hasStrongSemanticFields = parsed != null &&
            (parsed.accountHint?.isNotBlank() == true || parsed.merchantName?.isNotBlank() == true)

        val semanticAnySenderKey = if (hasStrongSemanticFields) {
            val accountHint = normalizeToken(parsed?.accountHint)
            val merchant = normalizeToken(parsed?.merchantName ?: parsed?.description)
            "a:${parsed?.amount}|ty:${parsed?.transactionType?.name}|acc:$accountHint|m:$merchant"
        } else {
            null
        }

        val semanticSenderKey = if (hasSender && semanticAnySenderKey != null) {
            "s:$normalizedSender|$semanticAnySenderKey"
        } else {
            null
        }

        return SmsFingerprint(
            strictKey = strictKey,
            strictBodyTimestampKey = strictBodyTimestampKey,
            strictBodySenderKey = strictBodySenderKey,
            hasSender = hasSender,
            semanticSenderKey = semanticSenderKey,
            semanticAnySenderKey = semanticAnySenderKey,
            timestamp = timestamp
        )
    }

    fun normalizeSender(sender: String?): String {
        val lowered = sender?.trim()?.lowercase().orEmpty()
        return lowered.replace(senderPrefixRegex, "")
    }

    fun normalizeBody(body: String): String {
        return body.trim()
            .lowercase()
            .replace(collapseWhitespaceRegex, " ")
    }

    private fun normalizeToken(value: String?): String {
        if (value.isNullOrBlank()) return "-"
        return value.trim()
            .lowercase()
            .replace(collapseWhitespaceRegex, " ")
    }
}
