package az.shia.azan.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import az.shia.azan.data.DailyPrayerTimes
import az.shia.azan.data.PrayerTime
import az.shia.azan.utils.TimeFormatter
import java.util.Calendar

/**
 * Namaz vaxtları üçün alarm qurma
 */
class AlarmScheduler(private val context: Context) {
    
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    companion object {
        private const val TAG = "AlarmScheduler"
        private const val BASE_REQUEST_CODE = 10000
    }
    
    /**
     * Bir günün bütün namaz vaxtları üçün alarm qur
     */
    fun schedulePrayerAlarms(prayerTimes: DailyPrayerTimes) {
        // Əvvəlki alarmları ləğv et
        cancelAllAlarms()
        
        val prayers = prayerTimes.getAllPrayers()
        val currentTime = Calendar.getInstance()
        
        prayers.forEachIndexed { index, prayer ->
            // Günəş vaxtı üçün alarm qurma (namaz deyil)
            if (prayer.type != az.shia.azan.data.PrayerType.SUNRISE) {
                if (prayer.time.timeInMillis > currentTime.timeInMillis) {
                    scheduleAlarm(prayer, index)
                    Log.d(TAG, "Alarm scheduled for ${prayer.name} at ${TimeFormatter.formatTime(prayer.time)}")
                }
            }
        }
    }
    
    /**
     * Tək namaz üçün alarm qur
     */
    private fun scheduleAlarm(prayer: PrayerTime, index: Int) {
        val intent = Intent(context, AzanReceiver::class.java).apply {
            action = AzanReceiver.ACTION_PRAYER_TIME
            putExtra(AzanReceiver.EXTRA_PRAYER_TYPE, prayer.type.name)
            putExtra(AzanReceiver.EXTRA_PRAYER_NAME, prayer.name)
            putExtra(AzanReceiver.EXTRA_PRAYER_TIME, TimeFormatter.formatTime(prayer.time))
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            BASE_REQUEST_CODE + index,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        try {
            // Dəqiq alarm qur (Android 12+ üçün icazə lazımdır)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        prayer.time.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // Dəqiq alarm icazəsi yoxdursa, təxmini alarm qur
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        prayer.time.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    prayer.time.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception scheduling alarm", e)
        }
    }
    
    /**
     * Bütün alarmları ləğv et
     */
    fun cancelAllAlarms() {
        for (i in 0 until 6) { // 6 namaz vaxtı
            val intent = Intent(context, AzanReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                BASE_REQUEST_CODE + i,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
        
        Log.d(TAG, "All alarms cancelled")
    }
    
    /**
     * Xatırlatma alarmı qur (namaz vaxtından əvvəl)
     */
    fun scheduleReminderAlarm(prayer: PrayerTime, minutesBefore: Int) {
        val reminderTime = prayer.time.clone() as Calendar
        reminderTime.add(Calendar.MINUTE, -minutesBefore)
        
        val currentTime = Calendar.getInstance()
        if (reminderTime.timeInMillis <= currentTime.timeInMillis) {
            return // Keçmiş vaxtda xatırlatma qurma
        }
        
        val intent = Intent(context, AzanReceiver::class.java).apply {
            action = AzanReceiver.ACTION_PRAYER_TIME
            putExtra(AzanReceiver.EXTRA_PRAYER_TYPE, prayer.type.name)
            putExtra(AzanReceiver.EXTRA_PRAYER_NAME, "${prayer.name} (Xatırlatma)")
            putExtra(AzanReceiver.EXTRA_PRAYER_TIME, "$minutesBefore dəqiqə sonra")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            BASE_REQUEST_CODE + 100 + prayer.type.ordinal,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime.timeInMillis,
                pendingIntent
            )
            
            Log.d(TAG, "Reminder alarm scheduled for ${prayer.name}, $minutesBefore minutes before")
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception scheduling reminder alarm", e)
        }
    }
}
