package az.shia.azan.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import az.shia.azan.MainActivity
import az.shia.azan.R
import az.shia.azan.data.PrayerType

/**
 * Bildiriş sistemi helper klassi
 */
class NotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "azan_notifications"
        const val CHANNEL_NAME = "Azan Bildirişləri"
        const val NOTIFICATION_ID = 1001
    }
    
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Bildiriş kanalını yarat (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Namaz vaxtı bildirişləri"
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    null
                )
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Namaz vaxtı bildirişi göstər
     */
    fun showPrayerNotification(prayerType: PrayerType, prayerName: String, time: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Azan oxutma action
        val playAzanIntent = Intent(context, AzanReceiver::class.java).apply {
            action = AzanReceiver.ACTION_PLAY_AZAN
            putExtra(AzanReceiver.EXTRA_PRAYER_TYPE, prayerType.name)
        }
        
        val playPendingIntent = PendingIntent.getBroadcast(
            context,
            prayerType.ordinal,
            playAzanIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🕌 $prayerName vaxtıdır")
            .setContentText("$prayerName namazı vaxtı: $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_play,
                "Azan oxut",
                playPendingIntent
            )
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        notificationManager.notify(NOTIFICATION_ID + prayerType.ordinal, notification)
    }
    
    /**
     * Növbəti namaz xatırlatması göstər
     */
    fun showUpcomingPrayerNotification(prayerName: String, timeRemaining: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏰ Xatırlatma")
            .setContentText("$prayerName namazına $timeRemaining qalıb")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * Bildirişi ləğv et
     */
    fun cancelNotification(notificationId: Int = NOTIFICATION_ID) {
        notificationManager.cancel(notificationId)
    }
    
    /**
     * Bütün bildirişləri ləğv et
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
