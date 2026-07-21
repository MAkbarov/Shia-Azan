package az.shia.azan.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import az.shia.azan.data.PreferencesManager
import az.shia.azan.service.OngoingNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Boot, tətbiq yenilənməsi, tarix, saat və timezone dəyişikliyində daimi
 * bildirişin hesabını yenidən qurur.
 */
class OngoingNotificationBootReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PRAYER_STATE_CHANGED =
            "az.shia.azan.action.PRAYER_STATE_CHANGED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val acceptedActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            ACTION_PRAYER_STATE_CHANGED
        )
        if (intent.action !in acceptedActions) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = PreferencesManager(context.applicationContext)
                    .settingsFlow
                    .first()
                if (settings.ongoingNotificationEnabled) {
                    if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
                        intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
                    ) {
                        OngoingNotificationService.startService(context.applicationContext)
                    } else {
                        OngoingNotificationService.refresh(context.applicationContext)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
