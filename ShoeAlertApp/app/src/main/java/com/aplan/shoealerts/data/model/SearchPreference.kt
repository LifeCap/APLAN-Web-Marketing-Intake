package com.aplan.shoealerts.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ShoeStyle(val displayName: String) {
    SNEAKER("Sneaker"),
    LOAFER("Loafer"),
    OXFORD("Oxford"),
    SANDAL("Closed-Toe Sandal"),
    MOCCASIN("Moccasin"),
    BOAT_SHOE("Boat Shoe"),
    SLIP_ON("Slip-On"),
    ANY("Any Style")
}

enum class ShoeMaterial(val displayName: String) {
    RUBBER("Rubber Sole"),
    LEATHER("Leather"),
    CANVAS("Canvas"),
    SYNTHETIC("Synthetic"),
    ANY("Any Material")
}

@Entity(tableName = "search_preferences")
data class SearchPreference(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                              // user-given name for this search
    val shoeSize: String,                          // e.g. "10", "10.5", "W8"
    val style: ShoeStyle = ShoeStyle.ANY,
    val keywords: String = "enclosed toe rubber sole shoes",
    val maxPriceThreshold: Double = 15.0,          // alert when price drops below this
    val newDealAlerts: Boolean = true,
    val priceDropAlerts: Boolean = true,
    val smsAlerts: Boolean = false,
    val pushNotifications: Boolean = true,
    val smsPhoneNumber: String = "",
    val enabledSources: String = "AMAZON,POSHMARK,SHEIN,EBAY,WALMART",
    val searchIntervalHours: Int = 4,              // how often to check in background
    val isActive: Boolean = true,
    val gender: String = "unisex",                 // mens, womens, unisex
    val brand: String = "",                        // optional brand filter
    val condition: String = "any",                 // new, used, any
    val createdAt: Long = System.currentTimeMillis()
) {
    fun enabledSourcesList(): List<ShoppingSource> =
        enabledSources.split(",").mapNotNull { name ->
            runCatching { ShoppingSource.valueOf(name.trim()) }.getOrNull()
        }
}
