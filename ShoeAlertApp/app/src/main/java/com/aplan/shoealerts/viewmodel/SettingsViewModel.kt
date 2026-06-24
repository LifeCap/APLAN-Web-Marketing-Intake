package com.aplan.shoealerts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aplan.shoealerts.data.datastore.AppPreferences
import com.aplan.shoealerts.data.datastore.AppSettings
import com.aplan.shoealerts.worker.DealSearchWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {

    val settings: StateFlow<AppSettings> = prefs.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setPriceThreshold(value: Double) =
        viewModelScope.launch { prefs.setPriceThreshold(value) }

    fun setSmsNumber(number: String) =
        viewModelScope.launch { prefs.setSmsNumber(number) }

    fun setDarkTheme(enabled: Boolean) =
        viewModelScope.launch { prefs.setDarkTheme(enabled) }

    fun setDynamicColor(enabled: Boolean) =
        viewModelScope.launch { prefs.setDynamicColor(enabled) }

    fun setShowOnlyUnderThreshold(enabled: Boolean) =
        viewModelScope.launch { prefs.setShowOnlyUnderThreshold(enabled) }

    fun setSearchInterval(hours: Int) =
        viewModelScope.launch { prefs.setSearchInterval(hours) }
}
