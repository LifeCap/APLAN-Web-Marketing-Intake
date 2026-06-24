package com.aplan.shoealerts.data.database

import androidx.room.*
import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.model.ShoppingSource
import kotlinx.coroutines.flow.Flow

@Dao
interface DealDao {

    @Query("SELECT * FROM deals ORDER BY foundAt DESC")
    fun getAllDeals(): Flow<List<Deal>>

    @Query("SELECT * FROM deals WHERE searchPreferenceId = :prefId ORDER BY price ASC")
    fun getDealsByPreference(prefId: Long): Flow<List<Deal>>

    @Query("SELECT * FROM deals WHERE price <= :maxPrice ORDER BY price ASC")
    fun getDealsUnderPrice(maxPrice: Double): Flow<List<Deal>>

    @Query("SELECT * FROM deals WHERE isFavorite = 1 ORDER BY foundAt DESC")
    fun getFavoriteDeals(): Flow<List<Deal>>

    @Query("SELECT * FROM deals WHERE isAlertSent = 0 ORDER BY foundAt DESC")
    fun getUnalertedDeals(): Flow<List<Deal>>

    @Query("SELECT * FROM deals WHERE source = :source ORDER BY price ASC")
    fun getDealsBySource(source: ShoppingSource): Flow<List<Deal>>

    @Query("SELECT * FROM deals ORDER BY price ASC LIMIT :limit")
    suspend fun getCheapestDeals(limit: Int): List<Deal>

    @Query("SELECT * FROM deals WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): Deal?

    @Query("""
        SELECT * FROM deals
        WHERE productUrl = :url AND searchPreferenceId = :prefId
        LIMIT 1
    """)
    suspend fun findExistingDeal(url: String, prefId: Long): Deal?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeal(deal: Deal): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeals(deals: List<Deal>): List<Long>

    @Update
    suspend fun updateDeal(deal: Deal)

    @Query("UPDATE deals SET isAlertSent = 1 WHERE id = :id")
    suspend fun markAlertSent(id: Long)

    @Query("UPDATE deals SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("DELETE FROM deals WHERE foundAt < :olderThanMs AND isFavorite = 0")
    suspend fun deleteOldDeals(olderThanMs: Long)

    @Query("SELECT COUNT(*) FROM deals WHERE foundAt >= :since")
    suspend fun countNewDealsSince(since: Long): Int

    @Query("DELETE FROM deals WHERE id = :id")
    suspend fun deleteDeal(id: Long)
}
