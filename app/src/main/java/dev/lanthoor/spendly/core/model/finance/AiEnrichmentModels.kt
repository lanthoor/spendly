package dev.lanthoor.spendly.core.model.finance

enum class AiEnrichmentStatus {
    PENDING,
    ENRICHED,
    FAILED
}

enum class CounterpartyRole {
    MERCHANT,
    SENDER,
    RECEIVER,
    BENEFICIARY,
    UNKNOWN
}

enum class CounterpartyType {
    PERSON,
    BUSINESS,
    BANK,
    UNKNOWN
}

enum class CounterpartyIdentifierType {
    VPA,
    ACCOUNT_LAST4,
    CARD_LAST4,
    UPI_REF,
    UTR,
    NONE
}

enum class PaymentRail {
    UPI,
    IMPS,
    NEFT,
    RTGS,
    CARD,
    SI,
    ATM,
    UNKNOWN
}
