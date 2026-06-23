package com.aplan.shoealerts.data.model

import org.junit.Assert.*
import org.junit.Test

class SearchPreferenceTest {

    @Test
    fun `enabledSourcesList parses all five sources correctly`() {
        val pref = SearchPreference(
            name = "Test",
            shoeSize = "10",
            enabledSources = "AMAZON,POSHMARK,SHEIN,EBAY,WALMART"
        )
        val sources = pref.enabledSourcesList()
        assertEquals(5, sources.size)
        assertTrue(sources.contains(ShoppingSource.AMAZON))
        assertTrue(sources.contains(ShoppingSource.POSHMARK))
        assertTrue(sources.contains(ShoppingSource.SHEIN))
        assertTrue(sources.contains(ShoppingSource.EBAY))
        assertTrue(sources.contains(ShoppingSource.WALMART))
    }

    @Test
    fun `enabledSourcesList handles subset of sources`() {
        val pref = SearchPreference(
            name = "Test",
            shoeSize = "9",
            enabledSources = "AMAZON,EBAY"
        )
        val sources = pref.enabledSourcesList()
        assertEquals(2, sources.size)
        assertTrue(sources.contains(ShoppingSource.AMAZON))
        assertTrue(sources.contains(ShoppingSource.EBAY))
        assertFalse(sources.contains(ShoppingSource.POSHMARK))
    }

    @Test
    fun `enabledSourcesList ignores invalid source names`() {
        val pref = SearchPreference(
            name = "Test",
            shoeSize = "8",
            enabledSources = "AMAZON,INVALID_STORE,EBAY"
        )
        val sources = pref.enabledSourcesList()
        assertEquals(2, sources.size)
    }

    @Test
    fun `default maxPriceThreshold is 15 dollars`() {
        val pref = SearchPreference(name = "Test", shoeSize = "10")
        assertEquals(15.0, pref.maxPriceThreshold, 0.001)
    }

    @Test
    fun `default searchIntervalHours is 4`() {
        val pref = SearchPreference(name = "Test", shoeSize = "10")
        assertEquals(4, pref.searchIntervalHours)
    }
}
