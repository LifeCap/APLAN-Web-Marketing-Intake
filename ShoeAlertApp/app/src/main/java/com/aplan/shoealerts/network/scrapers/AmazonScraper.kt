package com.aplan.shoealerts.network.scrapers

import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.model.SearchPreference
import com.aplan.shoealerts.data.model.ShoppingSource
import okhttp3.OkHttpClient
import java.net.URLEncoder

class AmazonScraper(client: OkHttpClient) : BaseScraper(client) {

    override suspend fun search(preference: SearchPreference): List<Deal> {
        val query = buildQuery(preference)
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://www.amazon.com/s?k=$encodedQuery&rh=p_36%3A0-1500"  // under $15

        val doc = fetchDocument(url, "https://www.amazon.com") ?: return emptyList()
        val deals = mutableListOf<Deal>()

        // Amazon search result cards
        val items = doc.select("div[data-component-type=s-search-result]")
        for (item in items.take(20)) {
            try {
                val title = item.select("h2 span").text().takeIf { it.isNotBlank() } ?: continue
                val priceWhole = item.select("span.a-price-whole").firstOrNull()?.text() ?: continue
                val priceFraction = item.select("span.a-price-fraction").firstOrNull()?.text() ?: "00"
                val priceStr = "$priceWhole.$priceFraction"
                val price = parsePrice(priceStr) ?: continue

                if (price > preference.maxPriceThreshold) continue

                val asin = item.attr("data-asin").takeIf { it.isNotBlank() } ?: continue
                val productUrl = "https://www.amazon.com/dp/$asin"
                val imageUrl = item.select("img.s-image").firstOrNull()?.attr("src")

                val originalPriceText = item.select("span.a-text-price span").firstOrNull()?.text()
                val originalPrice = originalPriceText?.let { parsePrice(it) }

                // Filter: must be enclosed toe + rubber sole style
                if (!isRelevantShoe(title, preference)) continue

                deals.add(
                    Deal(
                        title = title,
                        price = price,
                        originalPrice = originalPrice,
                        imageUrl = imageUrl,
                        productUrl = productUrl,
                        source = ShoppingSource.AMAZON,
                        size = preference.shoeSize,
                        brand = null,
                        condition = "new",
                        searchPreferenceId = preference.id
                    )
                )
            } catch (_: Exception) { /* skip malformed items */ }
        }
        return deals
    }

    private fun buildQuery(pref: SearchPreference): String {
        val parts = mutableListOf<String>()
        parts.add("enclosed toe rubber sole shoes")
        if (pref.shoeSize.isNotBlank()) parts.add("size ${pref.shoeSize}")
        if (pref.brand.isNotBlank()) parts.add(pref.brand)
        if (pref.gender != "unisex") parts.add(pref.gender)
        if (pref.style.displayName != "Any Style") parts.add(pref.style.displayName)
        return parts.joinToString(" ")
    }

    private fun isRelevantShoe(title: String, pref: SearchPreference): Boolean {
        val lower = title.lowercase()
        val enclosedTerms = listOf("shoe", "sneaker", "loafer", "oxford", "moccasin",
            "slip-on", "slip on", "boat shoe", "closed toe", "enclosed")
        val hasEnclosed = enclosedTerms.any { lower.contains(it) }
        if (!hasEnclosed) return false
        if (pref.brand.isNotBlank() && !lower.contains(pref.brand.lowercase())) return false
        return true
    }
}
