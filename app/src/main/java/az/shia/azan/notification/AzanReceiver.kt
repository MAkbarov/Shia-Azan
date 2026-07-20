package az.shia.azan.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import az.shia.azan.data.PrayerType
import az.shia.azan.service.AzanForegroundService
import az.shia.azan.utils.TimeFormatter
import java.util.Calendar

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
        
        try {
            val prayerType = PrayerType.valueOf(prayerTypeName)
            
            // Bildiriş göstər
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showPrayerNotification(prayerType, prayerName, prayerTime)
            
            Log.d(TAG, "Prayer notification shown: $prayerName at $prayerTime")
        } catch (e: Exception) {
            Log.e(TAG, "Error handling prayer time", e)
        }
    }
    
    /**
     * Azan oxutma düyməsinə basıldıqda
     * Foreground Service ilə arxa fonda oxuyur
     */
    private fun handlePlayAzan(context: Context, intent: Intent) {
        val prayerTypeName = intent.getStringExtra(EXTRA_PRAYER_TYPE) ?: return
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Namaz"
        
        try {
            // Foreground Service başlat - battery optimization-dan yan keçir
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
