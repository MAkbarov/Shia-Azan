package az.shia.azan.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import az.shia.azan.audio.AzanPlayer
import az.shia.azan.data.PrayerType

/**
 * Azan oxutmaq üçün background service
 */
class AzanService : Service() {
    
    private lateinit var azanPlayer: AzanPlayer
    
    companion object {
        const val ACTION_PLAY_AZAN = "az.shia.azan.ACTION_PLAY_AZAN"
        const val ACTION_STOP_AZAN = "az.shia.azan.ACTION_STOP_AZAN"
        const val EXTRA_PRAYER_TYPE = "prayer_type"
    }
    
    override fun onCreate() {
        super.onCreate()
        azanPlayer = AzanPlayer(applicationContext)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_AZAN -> {
                val prayerTypeName = intent.getStringExtra(EXTRA_PRAYER_TYPE)
                prayerTypeName?.let {
                    try {
                        val prayerType = PrayerType.valueOf(it)
                        azanPlayer.playAzan(prayerType) {
                            stopSelf()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            ACTION_STOP_AZAN -> {
                azanPlayer.stop()
                stopSelf()
            }
        }
        
        return START_NOT_STICKY
    }
    
    override fun onDestroy() {
        azanPlayer.release()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
