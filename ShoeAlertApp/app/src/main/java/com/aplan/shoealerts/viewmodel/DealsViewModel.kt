package com.aplan.shoealerts.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.model.SearchPreference
import com.aplan.shoealerts.data.repository.DealRepository
import com.aplan.shoealerts.worker.DealSearchWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DealsFilter { ALL, UNDER_THRESHOLD, FAVORITES, BY_SOURCE }
enum class DealsSortOrder { PRICE_ASC, PRICE_DESC, NEWEST, SOURCE }

data class DealsUiState(
    val deals: List<Deal> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val filter: DealsFilter = DealsFilter.ALL,
    val sortOrder: DealsSortOrder = DealsSortOrder.NEWEST,
    val searchQuery: String = "",
    val priceThreshold: Double = 15.0
)

@HiltViewModel
class DealsViewModel @Inject constructor(
    private val repository: DealRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DealsUiState())
    val uiState: StateFlow<DealsUiState> = _uiState.asStateFlow()

    init {
        // Load saved threshold from DataStore
        viewModelScope.launch {
            repository.appPreferences.settings.collect { settings ->
                _uiState.update { it.copy(priceThreshold = settings.defaultPriceThreshold) }
            }
        }
        observeDeals()
    }

    private fun observeDeals() {
        viewModelScope.launch {
            combine(
                repository.getAllDeals(),
                _uiState.map { Triple(it.filter, it.sortOrder, it.searchQuery) }.distinctUntilChanged(),
                _uiState.map { it.priceThreshold }.distinctUntilChanged()
            ) { deals, (filter, sort, query), threshold ->
                val queried = if (query.isBlank()) deals
                else deals.filter { it.title.contains(query, ignoreCase = true) ||
                        it.brand?.contains(query, ignoreCase = true) == true ||
                        it.source.displayName.contains(query, ignoreCase = true) }

                val filtered = when (filter) {
                    DealsFilter.ALL            -> queried
                    DealsFilter.UNDER_THRESHOLD -> queried.filter { it.price <= threshold }
                    DealsFilter.FAVORITES       -> queried.filter { it.isFavorite }
                    DealsFilter.BY_SOURCE       -> queried
                }

                when (sort) {
                    DealsSortOrder.PRICE_ASC  -> filtered.sortedBy { it.price }
                    DealsSortOrder.PRICE_DESC -> filtered.sortedByDescending { it.price }
                    DealsSortOrder.NEWEST     -> filtered.sortedByDescending { it.foundAt }
                    DealsSortOrder.SOURCE     -> filtered.sortedWith(
                        compareBy({ it.source.name }, { it.price })
                    )
                }
            }.collect { sorted ->
                _uiState.update { it.copy(deals = sorted) }
            }
        }
    }

    fun setFilter(filter: DealsFilter) = _uiState.update { it.copy(filter = filter) }
    fun setSortOrder(sort: DealsSortOrder) = _uiState.update { it.copy(sortOrder = sort) }
    fun setSearchQuery(q: String) = _uiState.update { it.copy(searchQuery = q) }
    fun clearSearch() = _uiState.update { it.copy(searchQuery = "") }

    fun setPriceThreshold(threshold: Double) {
        _uiState.update { it.copy(priceThreshold = threshold) }
        viewModelScope.launch { repository.appPreferences.setPriceThreshold(threshold) }
    }

    fun toggleFavorite(deal: Deal) {
        viewModelScope.launch { repository.toggleFavorite(deal) }
    }

    fun deleteDeal(id: Long) {
        viewModelScope.launch { repository.deleteDeal(id) }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        DealSearchWorker.scheduleImmediateSearch(context)
        // Optimistically clear refresh indicator after brief delay
        viewModelScope.launch {
            kotlinx.coroutines.delay(1_500)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
