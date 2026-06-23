package com.aplan.shoealerts.network.scrapers

import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.model.SearchPreference
import com.aplan.shoealerts.data.model.ShoppingSource
import com.aplan.shoealerts.network.ScraperRateLimiter
import okhttp3.OkHttpClient
import java.net.URLEncoder

class SheinScraper(client: OkHttpClient, rateLimiter: ScraperRateLimiter) : BaseScraper(client, rateLimiter) {

    override suspend fun search(preference: SearchPreference): List<Deal> {
        val query = buildQuery(preference)
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://us.shein.com/pdsearch/$encodedQuery/?ici=s`EditSearch`EditSearch`-`-`pagecomp0`1&src_identifier=fc%3DWomen%20Shoes%60sc%3DWomen%20Shoes%60tc%3D-%60oc%3D-%60ps%3Dpagecomp0%60jc%3DthemeType_5&src_module=topcat&src_tab_page_id=page_home"

        // Shein renders pages via JS - use their mobile API endpoint instead
        val apiUrl = "https://us.shein.com/api/productList/v2?keywords=$encodedQuery" +
            "&cat_id=1780&sort=9&limit=20&page=1&price_max=${preference.maxPriceThreshold.toInt()}"

        val doc = fetchDocument(apiUrl, "https://us.shein.com") ?: return emptyList()
        val deals = mutableListOf<Deal>()

        // Shein product cards (HTML fallback for search results)
        val items = doc.select("section.product-item, div.S-product-item")
        for (item in items.take(20)) {
            try {
                val title = item.select("p.goods-title-link, .product-item__name").text()
                    .takeIf { it.isNotBlank() } ?: continue

                val priceText = item.select(".product-item__price-new, .sale-price").firstOrNull()?.text()
                    ?: item.select("[class*=sale-price]").firstOrNull()?.text()
                    ?: continue
                val price = parsePrice(priceText) ?: continue
                if (price > preference.maxPriceThreshold) continue

                val href = item.select("a[href*=/p/]").attr("href").takeIf { it.isNotBlank() }
                    ?: item.select("a").attr("href").takeIf { it.contains("/p/") }
                    ?: continue
                val productUrl = if (href.startsWith("http")) href else "https://us.shein.com$href"

                val imageUrl = item.select("img").firstOrNull()
                    ?.let { it.attr("data-src").ifBlank { it.attr("src") } }

                val originalPriceText = item.select(".product-item__price-del, .del-price").firstOrNull()?.text()
                val originalPrice = originalPriceText?.let { parsePrice(it) }

                if (!isRelevantShoe(title, preference)) continue

                deals.add(
                    Deal(
                        title = title,
                        price = price,
                        originalPrice = originalPrice,
                        imageUrl = imageUrl,
                        productUrl = productUrl,
                        source = ShoppingSource.SHEIN,
                        size = preference.shoeSize,
                        brand = "Shein",
                        condition = "new",
                        searchPreferenceId = preference.id
                    )
                )
            } catch (_: Exception) { /* skip malformed items */ }
        }
        return deals
    }

    private fun buildQuery(pref: SearchPreference): String {
        val parts = mutableListOf("closed toe rubber sole shoes")
        if (pref.style.displayName != "Any Style") parts.add(pref.style.displayName.lowercase())
        return parts.joinToString(" ")
    }

    private fun isRelevantShoe(title: String, pref: SearchPreference): Boolean {
        val lower = title.lowercase()
        return lower.contains("shoe") || lower.contains("flat") ||
            lower.contains("sneaker") || lower.contains("loafer") ||
            lower.contains("mule") || lower.contains("slip")
    }
}
