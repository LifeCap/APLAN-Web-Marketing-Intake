package com.aplan.shoealerts.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // Re-schedule the background search after device reboot
            DealSearchWorker.schedulePeriodicWork(context, intervalHours = 4)
        }
    }
}
