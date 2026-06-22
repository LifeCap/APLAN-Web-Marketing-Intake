package com.aplan.shoealerts.network.scrapers

import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.model.SearchPreference
import com.aplan.shoealerts.data.model.ShoppingSource
import okhttp3.OkHttpClient
import java.net.URLEncoder

class EbayScraper(client: OkHttpClient) : BaseScraper(client) {

    override suspend fun search(preference: SearchPreference): List<Deal> {
        val query = buildQuery(preference)
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        // eBay search: category 63889 = Athletic Shoes, LH_BIN=1 = Buy It Now
        val maxPriceCents = (preference.maxPriceThreshold * 100).toLong()
        val url = "https://www.ebay.com/sch/i.html?_nkw=$encodedQuery" +
            "&_sacat=63889&LH_BIN=1&_udhi=${preference.maxPriceThreshold.toInt()}" +
            "&LH_ItemCondition=1000%7C1500%7C2000"  // new, like new, very good

        val doc = fetchDocument(url, "https://www.ebay.com") ?: return emptyList()
        val deals = mutableListOf<Deal>()

        val items = doc.select("li.s-item")
        for (item in items.drop(1).take(20)) {  // first item is a template
            try {
                val title = item.select("div.s-item__title span").text()
                    .replace("New Listing", "").trim()
                    .takeIf { it.isNotBlank() } ?: continue

                val priceText = item.select("span.s-item__price").firstOrNull()?.text() ?: continue
                val price = parsePrice(priceText) ?: continue
                if (price > preference.maxPriceThreshold) continue

                val productUrl = item.select("a.s-item__link").attr("href")
                    .takeIf { it.isNotBlank() } ?: continue

                val imageUrl = item.select("img").firstOrNull()
                    ?.let { it.attr("data-src").ifBlank { it.attr("src") } }

                val condition = item.select("span.SECONDARY_INFO").text().lowercase()

                if (!isRelevantShoe(title, preference)) continue

                deals.add(
                    Deal(
                        title = title,
                        price = price,
                        originalPrice = null,
                        imageUrl = imageUrl,
                        productUrl = productUrl,
                        source = ShoppingSource.EBAY,
                        size = preference.shoeSize,
                        brand = null,
                        condition = condition.ifBlank { "new" },
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
        val terms = listOf("shoe", "sneaker", "loafer", "oxford", "moccasin",
            "slip-on", "boat shoe", "flat", "closed toe")
        return terms.any { lower.contains(it) }
    }
}
