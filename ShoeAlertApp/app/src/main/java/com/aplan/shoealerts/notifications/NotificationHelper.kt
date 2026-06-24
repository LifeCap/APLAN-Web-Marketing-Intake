package com.aplan.shoealerts.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.aplan.shoealerts.MainActivity
import com.aplan.shoealerts.R
import com.aplan.shoealerts.data.model.Deal
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init { createChannels() }

    private fun createChannels() {
        val dealsChannel = NotificationChannel(
            CHANNEL_NEW_DEALS, "New Deal Alerts", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifies when new shoe deals are found matching your search"
            enableVibration(true)
            setShowBadge(true)
        }
        val priceChannel = NotificationChannel(
            CHANNEL_PRICE_DROP, "Price Drop Alerts", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifies when a shoe price drops below your set threshold"
            enableVibration(true)
            setShowBadge(true)
        }
        manager.createNotificationChannels(listOf(dealsChannel, priceChannel))
    }

    // Deep-link PendingIntent — navigates directly to the Deal Detail screen
    private fun dealPendingIntent(deal: Deal, requestCode: Int): PendingIntent {
        val deepLinkUri = Uri.parse("$DEEP_LINK_SCHEME://deal/${deal.id}")
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri, context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Fallback PendingIntent — opens Deals list
    private fun homePendingIntent(requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_OPEN_DEALS, true)
        }
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun notifyNewDeal(deal: Deal) {
        val notification = NotificationCompat.Builder(context, CHANNEL_NEW_DEALS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Shoe Deal Found!")
            .setContentText("${deal.title} — \$${String.format("%.2f", deal.price)} on ${deal.source.displayName}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Found: ${deal.title}\n" +
                        "Price: \$${String.format("%.2f", deal.price)}\n" +
                        "Store: ${deal.source.displayName}\n\nTap to view deal"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(dealPendingIntent(deal, NOTIF_ID_NEW_DEAL_BASE + deal.id.toInt()))
            .build()

        manager.notify(NOTIF_ID_NEW_DEAL_BASE + deal.id.toInt(), notification)
    }

    fun notifyPriceDrop(deal: Deal, threshold: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_PRICE_DROP)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Price Drop! Under \$${String.format("%.0f", threshold)}")
            .setContentText("${deal.title} — \$${String.format("%.2f", deal.price)} on ${deal.source.displayName}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "${deal.title}\n" +
                        "Now: \$${String.format("%.2f", deal.price)}" +
                        (deal.originalPrice?.let { " (was \$${String.format("%.2f", it)})" } ?: "") +
                        "\nStore: ${deal.source.displayName}\n\nTap to view deal"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(dealPendingIntent(deal, NOTIF_ID_PRICE_DROP_BASE + deal.id.toInt()))
            .build()

        manager.notify(NOTIF_ID_PRICE_DROP_BASE + deal.id.toInt(), notification)
    }

    fun notifySearchSummary(newDealCount: Int, underThresholdCount: Int) {
        if (newDealCount == 0) return
        val body = buildString {
            append("$newDealCount new deal${if (newDealCount != 1) "s" else ""} found")
            if (underThresholdCount > 0) append(", $underThresholdCount under your price threshold!")
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_NEW_DEALS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("ShoeAlert Search Complete")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(homePendingIntent(NOTIF_ID_SUMMARY))
            .build()

        manager.notify(NOTIF_ID_SUMMARY, notification)
    }

    companion object {
        const val CHANNEL_NEW_DEALS = "shoe_alerts_new_deals"
        const val CHANNEL_PRICE_DROP = "shoe_alerts_price_drop"
        const val EXTRA_OPEN_DEALS = "open_deals"
        const val DEEP_LINK_SCHEME = "shoealert"
        private const val NOTIF_ID_NEW_DEAL_BASE = 1000
        private const val NOTIF_ID_PRICE_DROP_BASE = 5000
        private const val NOTIF_ID_SUMMARY = 9999
    }
}
