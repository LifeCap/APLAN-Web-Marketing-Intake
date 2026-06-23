package com.aplan.shoealerts.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.model.PriceHistory
import com.aplan.shoealerts.data.repository.DealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DealDetailUiState(
    val deal: Deal? = null,
    val priceHistory: List<PriceHistory> = emptyList(),
    val lowestPrice: Double? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DealDetailViewModel @Inject constructor(
    private val repository: DealRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val dealId: Long = checkNotNull(savedStateHandle["dealId"])

    private val _state = MutableStateFlow(DealDetailUiState())
    val state: StateFlow<DealDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val deal = repository.getDealById(dealId)
            val lowest = repository.lowestPrice(dealId)
            _state.update { it.copy(deal = deal, lowestPrice = lowest, isLoading = false) }
        }
        viewModelScope.launch {
            repository.getPriceHistory(dealId).collect { history ->
                _state.update { it.copy(priceHistory = history) }
            }
        }
    }

    fun toggleFavorite() {
        val deal = _state.value.deal ?: return
        viewModelScope.launch { repository.toggleFavorite(deal) }
        _state.update { it.copy(deal = it.deal?.copy(isFavorite = !it.deal.isFavorite)) }
    }
}
