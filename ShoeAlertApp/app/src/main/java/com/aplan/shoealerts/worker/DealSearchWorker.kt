package com.aplan.shoealerts.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.aplan.shoealerts.data.model.AlertLog
import com.aplan.shoealerts.data.model.AlertType
import com.aplan.shoealerts.data.model.SearchPreference
import com.aplan.shoealerts.data.repository.DealRepository
import com.aplan.shoealerts.notifications.NotificationHelper
import com.aplan.shoealerts.notifications.SmsHelper
import com.aplan.shoealerts.widget.ShoeAlertWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class DealSearchWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: DealRepository,
    private val notificationHelper: NotificationHelper,
    private val smsHelper: SmsHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val activePreferences: List<SearchPreference> =
                repository.getAllPreferences().first().filter { it.isActive }

            var totalNew = 0
            var totalUnderThreshold = 0

            for (preference in activePreferences) {
                val newDeals = repository.runSearch(preference)
                totalNew += newDeals.size

                for (deal in newDeals) {
                    val underThreshold = deal.price <= preference.maxPriceThreshold
                    if (underThreshold) totalUnderThreshold++

                    // Push notification for new deal
                    if (preference.pushNotifications && preference.newDealAlerts) {
                        notificationHelper.notifyNewDeal(deal)
                    }

                    // Price drop notification (always send if under threshold)
                    if (underThreshold) {
                        if (preference.pushNotifications && preference.priceDropAlerts) {
                            notificationHelper.notifyPriceDrop(deal, preference.maxPriceThreshold)
                        }
                        if (preference.smsAlerts && preference.smsPhoneNumber.isNotBlank()) {
                            smsHelper.sendPriceDropSms(deal, preference.maxPriceThreshold, preference.smsPhoneNumber)
                        }
                    } else {
                        // New deal (not necessarily under threshold) — SMS if enabled
                        if (preference.smsAlerts && preference.smsPhoneNumber.isNotBlank() && preference.newDealAlerts) {
                            smsHelper.sendNewDealSms(deal, preference.smsPhoneNumber)
                        }
                    }

                    repository.logAlert(
                        AlertLog(
                            dealId = deal.id,
                            dealTitle = deal.title,
                            dealPrice = deal.price,
                            source = deal.source.displayName,
                            alertType = if (underThreshold) AlertType.PRICE_DROP else AlertType.NEW_DEAL,
                            wasSmsed = preference.smsAlerts,
                            wasPushSent = preference.pushNotifications
                        )
                    )
                }
            }

            // Summary notification
            if (totalNew > 0) {
                notificationHelper.notifySearchSummary(totalNew, totalUnderThreshold)
            }

            // Clean up old data
            repository.cleanupOldDeals()
            repository.cleanupOldAlerts()

            // Refresh home screen widget with latest deals
            ShoeAlertWidget().updateAll(applicationContext)

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "shoe_deal_search"

        fun schedulePeriodicWork(context: Context, intervalHours: Int) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<DealSearchWorker>(
                intervalHours.toLong(), TimeUnit.HOURS,
                15, TimeUnit.MINUTES  // flex period
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun scheduleImmediateSearch(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<DealSearchWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }

        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
