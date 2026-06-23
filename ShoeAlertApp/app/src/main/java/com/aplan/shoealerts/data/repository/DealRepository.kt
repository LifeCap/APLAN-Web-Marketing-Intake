package com.aplan.shoealerts.data.repository

import com.aplan.shoealerts.data.database.AlertLogDao
import com.aplan.shoealerts.data.database.DealDao
import com.aplan.shoealerts.data.database.PriceHistoryDao
import com.aplan.shoealerts.data.database.SearchPreferenceDao
import com.aplan.shoealerts.data.datastore.AppPreferences
import com.aplan.shoealerts.data.model.*
import com.aplan.shoealerts.network.scrapers.*
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DealRepository @Inject constructor(
    private val dealDao: DealDao,
    private val preferenceDao: SearchPreferenceDao,
    private val alertLogDao: AlertLogDao,
    private val priceHistoryDao: PriceHistoryDao,
    val appPreferences: AppPreferences,
    private val amazonScraper: AmazonScraper,
    private val poshmarkScraper: PoshmarkScraper,
    private val sheinScraper: SheinScraper,
    private val ebayScraper: EbayScraper,
    private val walmartScraper: WalmartScraper
) {
    // --- Deals ---
    fun getAllDeals(): Flow<List<Deal>> = dealDao.getAllDeals()
    fun getDealsUnderPrice(max: Double): Flow<List<Deal>> = dealDao.getDealsUnderPrice(max)
    fun getFavoriteDeals(): Flow<List<Deal>> = dealDao.getFavoriteDeals()
    fun getDealsByPreference(prefId: Long): Flow<List<Deal>> = dealDao.getDealsByPreference(prefId)

    suspend fun getDealById(id: Long): Deal? = dealDao.findById(id)

    suspend fun toggleFavorite(deal: Deal) {
        dealDao.setFavorite(deal.id, !deal.isFavorite)
    }

    suspend fun deleteDeal(id: Long) = dealDao.deleteDeal(id)

    suspend fun cleanupOldDeals() {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        dealDao.deleteOldDeals(cutoff)
    }

    // --- Price History ---
    fun getPriceHistory(dealId: Long): Flow<List<PriceHistory>> =
        priceHistoryDao.getHistoryForDeal(dealId)

    suspend fun lowestPrice(dealId: Long): Double? = priceHistoryDao.lowestPrice(dealId)

    // --- Preferences ---
    fun getAllPreferences(): Flow<List<SearchPreference>> = preferenceDao.getAllPreferences()

    suspend fun savePreference(pref: SearchPreference): Long =
        preferenceDao.insertPreference(pref)

    suspend fun updatePreference(pref: SearchPreference) =
        preferenceDao.updatePreference(pref)

    suspend fun deletePreference(id: Long) = preferenceDao.deletePreference(id)

    suspend fun togglePreferenceActive(id: Long, active: Boolean) =
        preferenceDao.setActive(id, active)

    // --- Scraping ---
    suspend fun runSearch(preference: SearchPreference): List<Deal> {
        val enabledSources = preference.enabledSourcesList()
        val newDeals = mutableListOf<Deal>()

        for (source in enabledSources) {
            val scraped = try {
                when (source) {
                    ShoppingSource.AMAZON   -> amazonScraper.search(preference)
                    ShoppingSource.POSHMARK -> poshmarkScraper.search(preference)
                    ShoppingSource.SHEIN    -> sheinScraper.search(preference)
                    ShoppingSource.EBAY     -> ebayScraper.search(preference)
                    ShoppingSource.WALMART  -> walmartScraper.search(preference)
                }
            } catch (_: Exception) { emptyList() }

            for (deal in scraped) {
                val existing = dealDao.findExistingDeal(deal.productUrl, preference.id)
                if (existing == null) {
                    val insertedId = dealDao.insertDeal(deal)
                    if (insertedId > 0) {
                        val saved = deal.copy(id = insertedId)
                        newDeals.add(saved)
                        priceHistoryDao.insert(PriceHistory(dealId = insertedId, price = deal.price))
                    }
                } else if (existing.price != deal.price) {
                    // Price changed — record history and re-flag for alert if it dropped
                    priceHistoryDao.insert(PriceHistory(dealId = existing.id, price = deal.price))
                    dealDao.updateDeal(
                        existing.copy(
                            price = deal.price,
                            isAlertSent = if (deal.price < existing.price) false else existing.isAlertSent
                        )
                    )
                    if (deal.price < existing.price) {
                        newDeals.add(existing.copy(price = deal.price))
                    }
                }
            }
        }

        appPreferences.recordSearchTimestamp()
        return newDeals
    }

    // --- Alert Logs ---
    fun getRecentAlerts(): Flow<List<AlertLog>> = alertLogDao.getRecentAlerts()

    suspend fun logAlert(log: AlertLog) {
        alertLogDao.insertAlert(log)
        dealDao.markAlertSent(log.dealId)
    }

    suspend fun cleanupOldAlerts() {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(60)
        alertLogDao.deleteOldAlerts(cutoff)
        priceHistoryDao.deleteOld(cutoff)
    }
}
