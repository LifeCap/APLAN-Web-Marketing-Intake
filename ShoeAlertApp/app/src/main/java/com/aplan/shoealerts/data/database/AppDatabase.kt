package com.aplan.shoealerts.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.aplan.shoealerts.data.model.AlertLog
import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.model.SearchPreference

@Database(
    entities = [Deal::class, SearchPreference::class, AlertLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dealDao(): DealDao
    abstract fun searchPreferenceDao(): SearchPreferenceDao
    abstract fun alertLogDao(): AlertLogDao

    companion object {
        const val DATABASE_NAME = "shoe_alerts_db"
    }
}
