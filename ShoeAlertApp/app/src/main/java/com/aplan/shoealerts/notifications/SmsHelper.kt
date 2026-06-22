package com.aplan.shoealerts.notifications

import android.content.Context
import android.telephony.SmsManager
import android.os.Build
import com.aplan.shoealerts.data.model.Deal
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun sendNewDealSms(deal: Deal, phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) return false
        val message = "ShoeAlert: New deal found!\n" +
            "${deal.title}\n" +
            "Price: \$${String.format("%.2f", deal.price)}\n" +
            "Store: ${deal.source.displayName}\n" +
            deal.productUrl
        return sendSms(phoneNumber, message)
    }

    fun sendPriceDropSms(deal: Deal, threshold: Double, phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) return false
        val message = "ShoeAlert Price Drop!\n" +
            "${deal.title}\n" +
            "Now: \$${String.format("%.2f", deal.price)}" +
            (deal.originalPrice?.let { " (was \$${String.format("%.2f", it)})" } ?: "") +
            "\nUnder your \$${String.format("%.0f", threshold)} threshold!\n" +
            "Store: ${deal.source.displayName}\n" +
            deal.productUrl
        return sendSms(phoneNumber, message)
    }

    private fun sendSms(phoneNumber: String, message: String): Boolean {
        return try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            // Split message if too long for a single SMS
            val parts = smsManager.divideMessage(message)
            if (parts.size == 1) {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } else {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
