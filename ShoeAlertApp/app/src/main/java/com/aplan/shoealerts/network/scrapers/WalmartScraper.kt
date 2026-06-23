package com.aplan.shoealerts.network.scrapers

import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.model.SearchPreference
import com.aplan.shoealerts.data.model.ShoppingSource
import com.aplan.shoealerts.network.ScraperRateLimiter
import okhttp3.OkHttpClient
import java.net.URLEncoder

class WalmartScraper(client: OkHttpClient, rateLimiter: ScraperRateLimiter) : BaseScraper(client, rateLimiter) {

    override suspend fun search(preference: SearchPreference): List<Deal> {
        val query = buildQuery(preference)
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://www.walmart.com/search?q=$encodedQuery" +
            "&max_price=${preference.maxPriceThreshold.toInt()}&facet=category%3AShoes"

        val doc = fetchDocument(url, "https://www.walmart.com") ?: return emptyList()
        val deals = mutableListOf<Deal>()

        // Walmart search result items
        val items = doc.select("div[data-item-id], article.sans-serif")
        for (item in items.take(20)) {
            try {
                val title = item.select("span.f6, [itemprop=name]").firstOrNull()?.text()
                    ?.takeIf { it.isNotBlank() } ?: continue

                val priceText = item.select("div[data-automation-id=product-price] span").firstOrNull()?.text()
                    ?: item.select(".price-main .price-characteristic").firstOrNull()?.text()
                    ?: continue
                val price = parsePrice(priceText) ?: continue
                if (price > preference.maxPriceThreshold) continue

                val href = item.select("a[href*=/ip/]").attr("href").takeIf { it.isNotBlank() }
                    ?: continue
                val productUrl = if (href.startsWith("http")) href else "https://www.walmart.com$href"

                val imageUrl = item.select("img").firstOrNull()
                    ?.let { it.attr("data-src").ifBlank { it.attr("src") } }

                if (!isRelevantShoe(title, preference)) continue

                deals.add(
                    Deal(
                        title = title,
                        price = price,
                        originalPrice = null,
                        imageUrl = imageUrl,
                        productUrl = productUrl,
                        source = ShoppingSource.WALMART,
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
        val parts = mutableListOf("enclosed toe rubber sole shoes")
        if (pref.shoeSize.isNotBlank()) parts.add("size ${pref.shoeSize}")
        if (pref.brand.isNotBlank()) parts.add(pref.brand)
        return parts.joinToString(" ")
    }

    private fun isRelevantShoe(title: String, pref: SearchPreference): Boolean {
        val lower = title.lowercase()
        val terms = listOf("shoe", "sneaker", "loafer", "flat", "moccasin",
            "oxford", "slip on", "slip-on", "casual shoe")
        return terms.any { lower.contains(it) }
    }
}
