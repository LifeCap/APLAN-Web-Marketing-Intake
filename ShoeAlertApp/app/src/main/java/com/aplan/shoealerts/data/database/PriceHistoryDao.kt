package com.aplan.shoealerts.data.database

import androidx.room.*
import com.aplan.shoealerts.data.model.PriceHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {

    @Query("SELECT * FROM price_history WHERE dealId = :dealId ORDER BY recordedAt ASC")
    fun getHistoryForDeal(dealId: Long): Flow<List<PriceHistory>>

    @Query("SELECT * FROM price_history WHERE dealId = :dealId ORDER BY recordedAt ASC")
    suspend fun getHistoryForDealOnce(dealId: Long): List<PriceHistory>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: PriceHistory)

    @Query("SELECT MIN(price) FROM price_history WHERE dealId = :dealId")
    suspend fun lowestPrice(dealId: Long): Double?

    @Query("DELETE FROM price_history WHERE recordedAt < :olderThanMs")
    suspend fun deleteOld(olderThanMs: Long)
}
