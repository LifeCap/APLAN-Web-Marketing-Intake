package com.aplan.shoealerts.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "price_history",
    foreignKeys = [
        ForeignKey(
            entity = Deal::class,
            parentColumns = ["id"],
            childColumns = ["dealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dealId")]
)
data class PriceHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dealId: Long,
    val price: Double,
    val recordedAt: Long = System.currentTimeMillis()
)
