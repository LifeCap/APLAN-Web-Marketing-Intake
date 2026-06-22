package com.aplan.shoealerts.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ShoppingSource(val displayName: String) {
    AMAZON("Amazon"),
    POSHMARK("Poshmark"),
    SHEIN("Shein"),
    EBAY("eBay"),
    WALMART("Walmart")
}

@Entity(tableName = "deals")
data class Deal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val price: Double,
    val originalPrice: Double?,
    val imageUrl: String?,
    val productUrl: String,
    val source: ShoppingSource,
    val size: String?,
    val brand: String?,
    val condition: String?,          // new, used, like new
    val isAlertSent: Boolean = false,
    val isFavorite: Boolean = false,
    val foundAt: Long = System.currentTimeMillis(),
    val searchPreferenceId: Long
)
