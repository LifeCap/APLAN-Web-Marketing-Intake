package com.aplan.shoealerts.network

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScraperRateLimiter @Inject constructor() {
    private val lastRequestMs = mutableMapOf<String, Long>()

    // Minimum milliseconds between requests to the same domain
    private val minIntervalMs = mapOf(
        "amazon.com"   to 3_500L,
        "poshmark.com" to 2_500L,
        "shein.com"    to 2_000L,
        "ebay.com"     to 2_000L,
        "walmart.com"  to 3_000L
    )

    suspend fun throttle(domain: String) {
        val interval = minIntervalMs.entries
            .firstOrNull { (key, _) -> domain.contains(key) }
            ?.value ?: 2_000L

        val last = lastRequestMs[domain] ?: 0L
        val elapsed = System.currentTimeMillis() - last
        if (elapsed < interval) {
            delay(interval - elapsed)
        }
        lastRequestMs[domain] = System.currentTimeMillis()
    }
}
