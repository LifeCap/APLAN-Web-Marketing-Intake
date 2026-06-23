package com.aplan.shoealerts.data.repository

import com.aplan.shoealerts.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDealRepository {

    private val _deals = MutableStateFlow<List<Deal>>(emptyList())
    private val _prefs = MutableStateFlow<List<SearchPreference>>(emptyList())
    private val _alerts = MutableStateFlow<List<AlertLog>>(emptyList())

    fun getAllDeals(): Flow<List<Deal>> = _deals
    fun getFavoriteDeals(): Flow<List<Deal>> = _deals.map { it.filter { d -> d.isFavorite } }
    fun getDealsUnderPrice(max: Double): Flow<List<Deal>> = _deals.map { it.filter { d -> d.price <= max } }
    fun getAllPreferences(): Flow<List<SearchPreference>> = _prefs

    suspend fun insertDeal(deal: Deal): Deal {
        val saved = deal.copy(id = (_deals.value.maxOfOrNull { it.id } ?: 0) + 1)
        _deals.value = _deals.value + saved
        return saved
    }

    suspend fun toggleFavorite(deal: Deal) {
        _deals.value = _deals.value.map {
            if (it.id == deal.id) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    suspend fun deleteDeal(id: Long) {
        _deals.value = _deals.value.filter { it.id != id }
    }

    fun seedDeals(vararg deals: Deal) {
        _deals.value = deals.toList()
    }

    fun seedPreferences(vararg prefs: SearchPreference) {
        _prefs.value = prefs.toList()
    }
}
