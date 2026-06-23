package com.aplan.shoealerts.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

data class AppSettings(
    val defaultPriceThreshold: Double = 15.0,
    val defaultSmsNumber: String = "",
    val useDarkTheme: Boolean = false,
    val useDynamicColor: Boolean = true,
    val showOnlyUnderThreshold: Boolean = false,
    val defaultSearchIntervalHours: Int = 4,
    val lastSearchTimestamp: Long = 0L
)

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val PRICE_THRESHOLD = doublePreferencesKey("price_threshold")
        val SMS_NUMBER = stringPreferencesKey("sms_number")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val SHOW_UNDER_THRESHOLD = booleanPreferencesKey("show_under_threshold")
        val SEARCH_INTERVAL = intPreferencesKey("search_interval_hours")
        val LAST_SEARCH_TS = longPreferencesKey("last_search_ts")
    }

    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            AppSettings(
                defaultPriceThreshold = prefs[Keys.PRICE_THRESHOLD] ?: 15.0,
                defaultSmsNumber = prefs[Keys.SMS_NUMBER] ?: "",
                useDarkTheme = prefs[Keys.DARK_THEME] ?: false,
                useDynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
                showOnlyUnderThreshold = prefs[Keys.SHOW_UNDER_THRESHOLD] ?: false,
                defaultSearchIntervalHours = prefs[Keys.SEARCH_INTERVAL] ?: 4,
                lastSearchTimestamp = prefs[Keys.LAST_SEARCH_TS] ?: 0L
            )
        }

    suspend fun setPriceThreshold(value: Double) {
        context.dataStore.edit { it[Keys.PRICE_THRESHOLD] = value }
    }

    suspend fun setSmsNumber(number: String) {
        context.dataStore.edit { it[Keys.SMS_NUMBER] = number }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setShowOnlyUnderThreshold(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_UNDER_THRESHOLD] = enabled }
    }

    suspend fun setSearchInterval(hours: Int) {
        context.dataStore.edit { it[Keys.SEARCH_INTERVAL] = hours }
    }

    suspend fun recordSearchTimestamp() {
        context.dataStore.edit { it[Keys.LAST_SEARCH_TS] = System.currentTimeMillis() }
    }
}
