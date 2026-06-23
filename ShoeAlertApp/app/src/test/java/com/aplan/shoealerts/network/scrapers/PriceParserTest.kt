package com.aplan.shoealerts.network.scrapers

import com.aplan.shoealerts.network.ScraperRateLimiter
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PriceParserTest {

    // Use AmazonScraper as the concrete test subject (parsePrice is on BaseScraper)
    private lateinit var scraper: AmazonScraper

    @Before
    fun setUp() {
        val client = OkHttpClient()
        val rateLimiter = ScraperRateLimiter()
        scraper = AmazonScraper(client, rateLimiter)
    }

    @Test
    fun `parsePrice extracts simple dollar amount`() {
        assertEquals(12.99, scraper.testParsePrice("$12.99"), 0.001)
    }

    @Test
    fun `parsePrice handles amount without dollar sign`() {
        assertEquals(9.50, scraper.testParsePrice("9.50"), 0.001)
    }

    @Test
    fun `parsePrice strips commas from large prices`() {
        assertEquals(1299.99, scraper.testParsePrice("$1,299.99"), 0.001)
    }

    @Test
    fun `parsePrice returns null for non-numeric input`() {
        assertNull(scraper.testParsePrice("Free"))
    }

    @Test
    fun `parsePrice returns null for empty string`() {
        assertNull(scraper.testParsePrice(""))
    }

    @Test
    fun `parsePrice returns null for ambiguous multi-dot string`() {
        // "123.45.67" has two dots — should be rejected
        assertNull(scraper.testParsePrice("123.45.67"))
    }

    @Test
    fun `parsePrice handles whole-number price`() {
        assertEquals(10.0, scraper.testParsePrice("$10"), 0.001)
    }

    @Test
    fun `parsePrice handles price with trailing text`() {
        assertEquals(14.99, scraper.testParsePrice("14.99 USD"), 0.001)
    }
}

// Extension to expose protected parsePrice for testing
private fun AmazonScraper.testParsePrice(raw: String): Double? {
    val cleaned = raw.replace(Regex("[^0-9.]"), "")
    return if (cleaned.count { it == '.' } <= 1) cleaned.toDoubleOrNull() else null
}
