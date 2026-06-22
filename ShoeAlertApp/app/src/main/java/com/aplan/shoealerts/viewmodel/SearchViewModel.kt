package com.aplan.shoealerts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aplan.shoealerts.data.model.*
import com.aplan.shoealerts.data.repository.DealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val preferences: List<SearchPreference> = emptyList(),
    val isSearching: Boolean = false,
    val lastSearchResult: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: DealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllPreferences().collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }
    }

    fun savePreference(pref: SearchPreference) {
        viewModelScope.launch {
            if (pref.id == 0L) {
                repository.savePreference(pref)
            } else {
                repository.updatePreference(pref)
            }
        }
    }

    fun deletePreference(id: Long) {
        viewModelScope.launch {
            repository.deletePreference(id)
        }
    }

    fun togglePreferenceActive(id: Long, active: Boolean) {
        viewModelScope.launch {
            repository.togglePreferenceActive(id, active)
        }
    }

    fun runManualSearch(pref: SearchPreference, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            try {
                val found = repository.runSearch(pref)
                val msg = "${found.size} new deal${if (found.size != 1) "s" else ""} found"
                _uiState.update { it.copy(isSearching = false, lastSearchResult = msg) }
                onComplete(found.size)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false, lastSearchResult = "Search failed: ${e.message}") }
                onComplete(0)
            }
        }
    }
}
