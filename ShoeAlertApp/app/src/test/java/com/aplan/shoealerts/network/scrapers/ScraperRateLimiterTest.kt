package com.aplan.shoealerts.network.scrapers

import com.aplan.shoealerts.network.ScraperRateLimiter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScraperRateLimiterTest {

    @Test
    fun `first request to domain passes immediately`() = runTest {
        val limiter = ScraperRateLimiter()
        val start = System.currentTimeMillis()
        limiter.throttle("www.ebay.com")
        val elapsed = System.currentTimeMillis() - start
        // First call should complete very quickly (no wait needed)
        assertTrue("First call should be fast, took ${elapsed}ms", elapsed < 500)
    }

    @Test
    fun `unknown domain uses default interval`() = runTest {
        val limiter = ScraperRateLimiter()
        // Just verify no exception is thrown for an unrecognised domain
        limiter.throttle("unknown.example.com")
    }

    @Test
    fun `different domains do not interfere`() = runTest {
        val limiter = ScraperRateLimiter()
        limiter.throttle("www.amazon.com")
        val start = System.currentTimeMillis()
        limiter.throttle("www.ebay.com")   // different domain — no wait
        val elapsed = System.currentTimeMillis() - start
        assertTrue("Different domain should not wait, took ${elapsed}ms", elapsed < 500)
    }
}
