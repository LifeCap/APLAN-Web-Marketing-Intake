package com.aplan.shoealerts.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aplan.shoealerts.data.model.AlertLog
import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.model.PriceHistory
import com.aplan.shoealerts.data.model.SearchPreference

@Database(
    entities = [Deal::class, SearchPreference::class, AlertLog::class, PriceHistory::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dealDao(): DealDao
    abstract fun searchPreferenceDao(): SearchPreferenceDao
    abstract fun alertLogDao(): AlertLogDao
    abstract fun priceHistoryDao(): PriceHistoryDao

    companion object {
        const val DATABASE_NAME = "shoe_alerts_db"
    }
}
