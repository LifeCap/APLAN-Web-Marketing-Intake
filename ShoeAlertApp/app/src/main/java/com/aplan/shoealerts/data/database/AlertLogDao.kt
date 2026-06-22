package com.aplan.shoealerts.data.database

import androidx.room.*
import com.aplan.shoealerts.data.model.AlertLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertLogDao {

    @Query("SELECT * FROM alert_logs ORDER BY sentAt DESC LIMIT 100")
    fun getRecentAlerts(): Flow<List<AlertLog>>

    @Insert
    suspend fun insertAlert(log: AlertLog)

    @Query("DELETE FROM alert_logs WHERE sentAt < :olderThanMs")
    suspend fun deleteOldAlerts(olderThanMs: Long)
}
