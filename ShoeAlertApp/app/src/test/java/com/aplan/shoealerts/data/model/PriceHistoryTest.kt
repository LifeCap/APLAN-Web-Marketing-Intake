package com.aplan.shoealerts.data.model

import org.junit.Assert.*
import org.junit.Test

class PriceHistoryTest {

    @Test
    fun `price history entry defaults to current time`() {
        val before = System.currentTimeMillis()
        val entry = PriceHistory(dealId = 1L, price = 12.99)
        val after = System.currentTimeMillis()
        assertTrue(entry.recordedAt in before..after)
    }

    @Test
    fun `multiple entries for same deal track price changes`() {
        val history = listOf(
            PriceHistory(dealId = 1L, price = 19.99, recordedAt = 1000L),
            PriceHistory(dealId = 1L, price = 14.99, recordedAt = 2000L),
            PriceHistory(dealId = 1L, price = 9.99,  recordedAt = 3000L)
        )
        val lowest = history.minOf { it.price }
        val latest = history.maxBy { it.recordedAt }.price
        assertEquals(9.99, lowest, 0.001)
        assertEquals(9.99, latest, 0.001)
    }

    @Test
    fun `price drop is detectable from history`() {
        val oldPrice = 20.0
        val newPrice = 12.0
        val dropped = newPrice < oldPrice
        assertTrue(dropped)
        val savings = oldPrice - newPrice
        assertEquals(8.0, savings, 0.001)
    }
}
