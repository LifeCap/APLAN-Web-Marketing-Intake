package com.aplan.shoealerts.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AlertType { PRICE_DROP, NEW_DEAL, SEARCH_COMPLETE }

@Entity(tableName = "alert_logs")
data class AlertLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dealId: Long,
    val dealTitle: String,
    val dealPrice: Double,
    val source: String,
    val alertType: AlertType,
    val wasSmsed: Boolean = false,
    val wasPushSent: Boolean = false,
    val sentAt: Long = System.currentTimeMillis()
)
