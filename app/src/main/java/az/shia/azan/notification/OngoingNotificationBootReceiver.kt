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

        val action = intent.action
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Azan hər zaman işləsin: reboot, paket yenilənməsi, tarix/saat/timezone
                // dəyişməsində alarmları yenidən qur. Bu, daimi bildiriş statusundan
                // asılı deyil — azan seçilmiş namaz vaxtlarında mütləq oxunmalıdır.
                if (action != ACTION_PRAYER_STATE_CHANGED) {
                    runCatching { PrayerAlarmPlanner.reschedule(appContext) }
                }

                val settings = PreferencesManager(appContext).settingsFlow.first()
                if (settings.ongoingNotificationEnabled) {
                    if (action == Intent.ACTION_BOOT_COMPLETED ||
                        action == Intent.ACTION_MY_PACKAGE_REPLACED
                    ) {
                        OngoingNotificationService.startService(appContext)
                    } else {
                        OngoingNotificationService.refresh(appContext)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
