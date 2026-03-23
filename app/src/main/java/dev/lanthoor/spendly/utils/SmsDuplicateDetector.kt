package dev.lanthoor.spendly.utils

class SmsDuplicateDetector {
    companion object {
        const val SEMANTIC_WINDOW_MS = 10 * 60 * 1000L
    }

    private val strictSeen = mutableSetOf<String>()
    private val strictBodyTimestampSeen = mutableSetOf<String>()
    private val strictBodyTimestampSeenWithoutSender = mutableSetOf<String>()
    private val strictBodySenderTimes = mutableMapOf<String, MutableList<Long>>()
    private val semanticSeen = mutableMapOf<String, MutableList<Long>>()

    fun preload(fingerprints: Iterable<SmsFingerprint>) {
        fingerprints.forEach { markSeen(it) }
    }

    fun findDuplicateReason(fingerprint: SmsFingerprint): String? {
        if (strictSeen.contains(fingerprint.strictKey)) {
            return "strict"
        }

        if (!fingerprint.hasSender && strictBodyTimestampSeen.contains(fingerprint.strictBodyTimestampKey)) {
            return "strict"
        }

        if (fingerprint.hasSender &&
            strictBodyTimestampSeenWithoutSender.contains(fingerprint.strictBodyTimestampKey)
        ) {
            return "strict"
        }

        if (!fingerprint.hasSender) {
            return null
        }

        val exactMessageDriftDuplicate = strictBodySenderTimes[fingerprint.strictBodySenderKey]
            ?.any { existingTimestamp ->
                val minTime = fingerprint.timestamp - SEMANTIC_WINDOW_MS
                val maxTime = fingerprint.timestamp + SEMANTIC_WINDOW_MS
                existingTimestamp in minTime..maxTime
            } == true
        if (exactMessageDriftDuplicate) {
            return "strict"
        }

        val semanticSenderKey = fingerprint.semanticSenderKey
        if (semanticSenderKey != null) {
            val candidates = semanticSeen[semanticSenderKey]
            if (candidates != null && fingerprint.hasSender) {
                val minTime = fingerprint.timestamp - SEMANTIC_WINDOW_MS
                val maxTime = fingerprint.timestamp + SEMANTIC_WINDOW_MS
                val hasNearTimestamp = candidates.any { existing ->
                    existing in minTime..maxTime
                }
                if (hasNearTimestamp) {
                    return "semantic"
                }
            }
        }

        return null
    }

    fun markSeen(fingerprint: SmsFingerprint) {
        strictSeen.add(fingerprint.strictKey)
        strictBodyTimestampSeen.add(fingerprint.strictBodyTimestampKey)
        if (!fingerprint.hasSender) {
            strictBodyTimestampSeenWithoutSender.add(fingerprint.strictBodyTimestampKey)
        }
        strictBodySenderTimes
            .getOrPut(fingerprint.strictBodySenderKey) { mutableListOf() }
            .add(fingerprint.timestamp)

        val semanticSenderKey = fingerprint.semanticSenderKey
        if (semanticSenderKey != null && fingerprint.hasSender) {
            val list = semanticSeen.getOrPut(semanticSenderKey) { mutableListOf() }
            list.add(fingerprint.timestamp)
        }
    }
}
