package az.shia.azan.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import az.shia.azan.data.PrayerType
import az.shia.azan.data.PreferencesManager
import az.shia.azan.service.AzanForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Namaz vaxtı alarmları üçün BroadcastReceiver
 */
class AzanReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_PRAYER_TIME = "az.shia.azan.ACTION_PRAYER_TIME"
        const val ACTION_PLAY_AZAN = "az.shia.azan.ACTION_PLAY_AZAN"
        const val EXTRA_PRAYER_TYPE = "prayer_type"
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_PRAYER_TIME = "prayer_time"
        
        private const val TAG = "AzanReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        
        when (intent.action) {
            ACTION_PRAYER_TIME -> {
                handlePrayerTime(context, intent)
            }
            ACTION_PLAY_AZAN -> {
                handlePlayAzan(context, intent)
            }
        }
    }
    
    /**
     * Namaz vaxtı gəldikdə bildiriş göstər
     */
    private fun handlePrayerTime(context: Context, intent: Intent) {
        val prayerTypeName = intent.getStringExtra(EXTRA_PRAYER_TYPE) ?: return
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
        val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: return

        val prayerType = try {
            PrayerType.valueOf(prayerTypeName)
        } catch (e: Exception) {
            Log.e(TAG, "Invalid prayer type: $prayerTypeName", e)
            return
        }

        // Daxil olmuş namaz köhnə "növbəti namaz" kimi qalmasın deyə daimi/status/
        // kilid ekranı bildirişini bu namazın bildiriş/azan statusundan asılı
        // olmayaraq hər zaman yenilə.
        context.sendBroadcast(
            Intent(context, OngoingNotificationBootReceiver::class.java).apply {
                action = OngoingNotificationBootReceiver.ACTION_PRAYER_STATE_CHANGED
            }
        )

        // Parametrlərdə bu namaz üçün bildiriş söndürülübsə, nə bildiriş, nə də azan.
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = PreferencesManager(appContext).settingsFlow.first()
                if (settings.isNotificationEnabled(prayerType)) {
                    NotificationHelper(appContext)
                        .showPrayerNotification(prayerType, prayerName, prayerTime)
                    startAzanService(appContext, prayerType.name, prayerName)
                    Log.d(TAG, "Prayer notification shown and Azan started: $prayerName at $prayerTime")
                } else {
                    Log.d(TAG, "Skipped disabled prayer: $prayerName")
                }
                // Bu namaz aktiv olsun-olmasın, növbəti namaz(lar) üçün alarmları
                // həmişə yenilə ki, azan silsiləsi kəsilməsin (gün keçidi daxil).
                runCatching { PrayerAlarmPlanner.reschedule(appContext) }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling prayer time", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
    
    /**
     * Azan oxutma düyməsinə basıldıqda
     * Foreground Service ilə arxa fonda oxuyur
     */
    private fun handlePlayAzan(context: Context, intent: Intent) {
        val prayerTypeName = intent.getStringExtra(EXTRA_PRAYER_TYPE) ?: return
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Namaz"
        // Bu yol istifadəçinin bildirişdəki "Azan oxut" düyməsidir — həmişə oxu.
        startAzanService(context, prayerTypeName, prayerName)
    }

    private fun startAzanService(context: Context, prayerTypeName: String, prayerName: String) {
        try {
            val serviceIntent = Intent(context, AzanForegroundService::class.java).apply {
                action = AzanForegroundService.ACTION_START_AZAN
                putExtra(AzanForegroundService.EXTRA_PRAYER_TYPE, prayerTypeName)
                putExtra(AzanForegroundService.EXTRA_PRAYER_NAME, prayerName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d(TAG, "Azan foreground service started for: $prayerName")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting azan foreground service", e)
        }
    }
}
