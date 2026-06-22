package com.aplan.shoealerts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aplan.shoealerts.data.model.AlertLog
import com.aplan.shoealerts.data.repository.DealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    repository: DealRepository
) : ViewModel() {

    val alerts: StateFlow<List<AlertLog>> = repository.getRecentAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
