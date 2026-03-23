package dev.lanthoor.spendly.utils

import java.util.ArrayDeque

class SmsDuplicateDetector {
    enum class DuplicateReason {
        STRICT,
        SEMANTIC
    }

    companion object {
        const val SEMANTIC_WINDOW_MS = 10 * 60 * 1000L
    }

    private val strictSeen = mutableSetOf<String>()
    private val strictBodyTimestampSeen = mutableSetOf<String>()
    private val strictBodyTimestampSeenWithoutSender = mutableSetOf<String>()
    private val strictBodySenderTimes = mutableMapOf<String, ArrayDeque<Long>>()
    private val semanticSeen = mutableMapOf<String, ArrayDeque<Long>>()
    private val semanticUnknownSenderSeen = mutableMapOf<String, ArrayDeque<Long>>()

    fun preload(fingerprints: Iterable<SmsFingerprint>) {
        fingerprints.forEach { markSeen(it) }
    }

    fun findDuplicateReason(fingerprint: SmsFingerprint): DuplicateReason? {
        if (strictSeen.contains(fingerprint.strictKey)) {
            return DuplicateReason.STRICT
        }

        if (!fingerprint.hasSender && strictBodyTimestampSeen.contains(fingerprint.strictBodyTimestampKey)) {
            return DuplicateReason.STRICT
        }

        if (fingerprint.hasSender &&
            strictBodyTimestampSeenWithoutSender.contains(fingerprint.strictBodyTimestampKey)
        ) {
            return DuplicateReason.STRICT
        }

        if (!fingerprint.hasSender) {
            val semanticAnySenderKey = fingerprint.semanticAnySenderKey
            if (semanticAnySenderKey != null) {
                val candidates = semanticUnknownSenderSeen[semanticAnySenderKey]
                if (containsInWindow(candidates, fingerprint.timestamp)) {
                    return DuplicateReason.SEMANTIC
                }
            }
            return null
        }

        val exactMessageDriftDuplicate = containsInWindow(
            strictBodySenderTimes[fingerprint.strictBodySenderKey],
            fingerprint.timestamp
        )
        if (exactMessageDriftDuplicate) {
            return DuplicateReason.STRICT
        }

        val semanticSenderKey = fingerprint.semanticSenderKey
        if (semanticSenderKey != null) {
            val candidates = semanticSeen[semanticSenderKey]
            if (containsInWindow(candidates, fingerprint.timestamp)) {
                return DuplicateReason.SEMANTIC
            }
        }

        val semanticAnySenderKey = fingerprint.semanticAnySenderKey
        if (semanticAnySenderKey != null) {
            val anySenderCandidates = semanticUnknownSenderSeen[semanticAnySenderKey]
            if (containsInWindow(anySenderCandidates, fingerprint.timestamp)) {
                return DuplicateReason.SEMANTIC
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
        strictBodySenderTimes.addTimestamp(fingerprint.strictBodySenderKey, fingerprint.timestamp)

        val semanticSenderKey = fingerprint.semanticSenderKey
        if (semanticSenderKey != null) {
            semanticSeen.addTimestamp(semanticSenderKey, fingerprint.timestamp)
        }

        val semanticAnySenderKey = fingerprint.semanticAnySenderKey
        if (semanticAnySenderKey != null && !fingerprint.hasSender) {
            semanticUnknownSenderSeen.addTimestamp(semanticAnySenderKey, fingerprint.timestamp)
        }
    }

    private fun containsInWindow(candidates: ArrayDeque<Long>?, timestamp: Long): Boolean {
        if (candidates == null || candidates.isEmpty()) return false

        pruneForTimestamp(candidates, timestamp)
        val minTime = timestamp - SEMANTIC_WINDOW_MS
        val maxTime = timestamp + SEMANTIC_WINDOW_MS
        return candidates.any { existing -> existing in minTime..maxTime }
    }

    private fun pruneForTimestamp(candidates: ArrayDeque<Long>, timestamp: Long) {
        val threshold = timestamp + SEMANTIC_WINDOW_MS
        while (candidates.isNotEmpty() && candidates.first() > threshold) {
            candidates.removeFirst()
        }
    }

    private fun MutableMap<String, ArrayDeque<Long>>.addTimestamp(key: String, timestamp: Long) {
        val queue = getOrPut(key) { ArrayDeque() }
        queue.addLast(timestamp)
    }
}
