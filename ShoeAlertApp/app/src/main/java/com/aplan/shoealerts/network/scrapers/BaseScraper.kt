package com.aplan.shoealerts.network.scrapers

import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.model.SearchPreference
import com.aplan.shoealerts.network.ScraperRateLimiter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI
import java.util.concurrent.TimeUnit

abstract class BaseScraper(
    protected val client: OkHttpClient,
    protected val rateLimiter: ScraperRateLimiter
) {
    protected suspend fun fetchDocument(url: String, referer: String? = null): Document? {
        val domain = runCatching { URI(url).host }.getOrDefault("")
        rateLimiter.throttle(domain)
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Connection", "keep-alive")
                .apply { referer?.let { header("Referer", it) } }
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                Jsoup.parse(body)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    protected fun parsePrice(raw: String): Double? {
        val cleaned = raw.replace(Regex("[^0-9.]"), "")
        // Guard against strings like "123.45.67" which would parse as NaN
        return if (cleaned.count { it == '.' } <= 1) cleaned.toDoubleOrNull() else null
    }

    abstract suspend fun search(preference: SearchPreference): List<Deal>

    companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36"

        fun buildClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }
}
