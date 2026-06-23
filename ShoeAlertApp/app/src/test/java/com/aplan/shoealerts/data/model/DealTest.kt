package com.aplan.shoealerts.data.model

import org.junit.Assert.*
import org.junit.Test

class DealTest {

    private fun sampleDeal(price: Double = 12.99, source: ShoppingSource = ShoppingSource.AMAZON) = Deal(
        id = 1L,
        title = "Women's Rubber Sole Loafer",
        price = price,
        originalPrice = 24.99,
        imageUrl = null,
        productUrl = "https://amazon.com/dp/B123",
        source = source,
        size = "9",
        brand = null,
        condition = "new",
        searchPreferenceId = 1L
    )

    @Test
    fun `deal is under threshold when price is less than threshold`() {
        val deal = sampleDeal(price = 12.99)
        assertTrue(deal.price <= 15.0)
    }

    @Test
    fun `deal is not under threshold when price exceeds it`() {
        val deal = sampleDeal(price = 19.99)
        assertFalse(deal.price <= 15.0)
    }

    @Test
    fun `deal is exactly at threshold boundary`() {
        val deal = sampleDeal(price = 15.0)
        assertTrue(deal.price <= 15.0)
    }

    @Test
    fun `deal defaults to not favorited and not alerted`() {
        val deal = sampleDeal()
        assertFalse(deal.isFavorite)
        assertFalse(deal.isAlertSent)
    }

    @Test
    fun `discount calculation is correct when original price exists`() {
        val deal = sampleDeal(price = 12.99)
        val orig = deal.originalPrice ?: 0.0
        val savings = orig - deal.price
        assertEquals(12.0, savings, 0.01)
    }
}
