package com.aplan.shoealerts.data.database

import androidx.room.*
import com.aplan.shoealerts.data.model.SearchPreference
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchPreferenceDao {

    @Query("SELECT * FROM search_preferences ORDER BY createdAt DESC")
    fun getAllPreferences(): Flow<List<SearchPreference>>

    @Query("SELECT * FROM search_preferences WHERE isActive = 1")
    suspend fun getActivePreferences(): List<SearchPreference>

    @Query("SELECT * FROM search_preferences WHERE id = :id")
    suspend fun getPreferenceById(id: Long): SearchPreference?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: SearchPreference): Long

    @Update
    suspend fun updatePreference(preference: SearchPreference)

    @Query("UPDATE search_preferences SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("DELETE FROM search_preferences WHERE id = :id")
    suspend fun deletePreference(id: Long)
}
