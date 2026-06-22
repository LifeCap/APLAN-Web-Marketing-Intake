package com.aplan.shoealerts.network.scrapers

import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.model.SearchPreference
import com.aplan.shoealerts.data.model.ShoppingSource
import okhttp3.OkHttpClient
import java.net.URLEncoder

class PoshmarkScraper(client: OkHttpClient) : BaseScraper(client) {

    override suspend fun search(preference: SearchPreference): List<Deal> {
        val query = buildQuery(preference)
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        // Poshmark search with max price filter
        val maxCents = (preference.maxPriceThreshold * 100).toInt()
        val url = "https://poshmark.com/search?query=$encodedQuery&type=listings&department=Shoes&max_price=$maxCents"

        val doc = fetchDocument(url, "https://poshmark.com") ?: return emptyList()
        val deals = mutableListOf<Deal>()

        // Poshmark listing tiles
        val items = doc.select("div.tile")
        for (item in items.take(20)) {
            try {
                val title = item.select("a.tile__title").text().takeIf { it.isNotBlank() }
                    ?: item.select("[data-test=listing-title]").text().takeIf { it.isNotBlank() }
                    ?: continue

                val priceText = item.select("span.price").firstOrNull()?.text()
                    ?: item.select("[data-test=listing-price]").firstOrNull()?.text()
                    ?: continue
                val price = parsePrice(priceText) ?: continue

                if (price > preference.maxPriceThreshold) continue

                val href = item.select("a.tile__covershot").attr("href").takeIf { it.isNotBlank() }
                    ?: item.select("a[href*=/listing/]").attr("href").takeIf { it.isNotBlank() }
                    ?: continue
                val productUrl = if (href.startsWith("http")) href else "https://poshmark.com$href"

                val imageUrl = item.select("img").firstOrNull()?.attr("src")
                val size = item.select("span.size").text().takeIf { it.isNotBlank() }
                    ?: preference.shoeSize
                val brand = item.select("span.brand").text().takeIf { it.isNotBlank() }
                val condition = item.select("span.condition").text().lowercase()
                    .ifBlank { "used" }

                if (!isRelevantShoe(title, preference)) continue

                deals.add(
                    Deal(
                        title = title,
                        price = price,
                        originalPrice = null,
                        imageUrl = imageUrl,
                        productUrl = productUrl,
                        source = ShoppingSource.POSHMARK,
                        size = size,
                        brand = brand,
                        condition = condition,
                        searchPreferenceId = preference.id
                    )
                )
            } catch (_: Exception) { /* skip malformed items */ }
        }
        return deals
    }

    private fun buildQuery(pref: SearchPreference): String {
        val parts = mutableListOf("enclosed toe rubber sole")
        if (pref.style.displayName != "Any Style") parts.add(pref.style.displayName.lowercase())
        if (pref.brand.isNotBlank()) parts.add(pref.brand)
        return parts.joinToString(" ")
    }

    private fun isRelevantShoe(title: String, pref: SearchPreference): Boolean {
        val lower = title.lowercase()
        val terms = listOf("shoe", "sneaker", "loafer", "flat", "moccasin",
            "oxford", "slip on", "slip-on", "boat", "closed")
        return terms.any { lower.contains(it) }
    }
}
