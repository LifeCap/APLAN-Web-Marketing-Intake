package com.aplan.shoealerts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.model.SearchPreference
import com.aplan.shoealerts.data.repository.DealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DealsUiState(
    val deals: List<Deal> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val filter: DealsFilter = DealsFilter.ALL,
    val searchQuery: String = ""
)

enum class DealsFilter { ALL, UNDER_THRESHOLD, FAVORITES, BY_SOURCE }

@HiltViewModel
class DealsViewModel @Inject constructor(
    private val repository: DealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DealsUiState())
    val uiState: StateFlow<DealsUiState> = _uiState.asStateFlow()

    private val _priceThreshold = MutableStateFlow(15.0)
    val priceThreshold: StateFlow<Double> = _priceThreshold.asStateFlow()

    init {
        observeDeals()
    }

    private fun observeDeals() {
        viewModelScope.launch {
            repository.getAllDeals()
                .combine(_priceThreshold) { deals, threshold -> Pair(deals, threshold) }
                .combine(_uiState.map { it.filter }) { (deals, threshold), filter ->
                    Triple(deals, threshold, filter)
                }
                .collect { (deals, threshold, filter) ->
                    val filtered = when (filter) {
                        DealsFilter.ALL -> deals
                        DealsFilter.UNDER_THRESHOLD -> deals.filter { it.price <= threshold }
                        DealsFilter.FAVORITES -> deals.filter { it.isFavorite }
                        DealsFilter.BY_SOURCE -> deals.sortedBy { it.source.name }
                    }
                    _uiState.update { it.copy(deals = filtered) }
                }
        }
    }

    fun setFilter(filter: DealsFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun setPriceThreshold(threshold: Double) {
        _priceThreshold.value = threshold
    }

    fun toggleFavorite(deal: Deal) {
        viewModelScope.launch {
            repository.toggleFavorite(deal)
        }
    }

    fun deleteDeal(id: Long) {
        viewModelScope.launch {
            repository.deleteDeal(id)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Trigger immediate background search via the ViewModel context
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
