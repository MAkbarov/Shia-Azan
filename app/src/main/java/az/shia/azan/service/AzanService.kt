package az.shia.azan.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import az.shia.azan.audio.AzanPlayer
import az.shia.azan.data.PreferencesManager
import az.shia.azan.data.PrayerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Azan oxutmaq üçün background service. */
class AzanService : Service() {

    private lateinit var azanPlayer: AzanPlayer
    private lateinit var preferencesManager: PreferencesManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    companion object {
        const val ACTION_PLAY_AZAN = "az.shia.azan.ACTION_PLAY_AZAN"
        const val ACTION_STOP_AZAN = "az.shia.azan.ACTION_STOP_AZAN"
        const val EXTRA_PRAYER_TYPE = "prayer_type"
    }

    override fun onCreate() {
        super.onCreate()
        azanPlayer = AzanPlayer(applicationContext)
        preferencesManager = PreferencesManager(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_AZAN -> {
                val prayerTypeName = intent.getStringExtra(EXTRA_PRAYER_TYPE) ?: return START_NOT_STICKY
                serviceScope.launch {
                    try {
                        val prayerType = PrayerType.valueOf(prayerTypeName)
                        val settings = preferencesManager.settingsFlow.first()
                        azanPlayer.playAzan(
                            prayerType = prayerType,
                            sound = settings.selectedAzanSound,
                            volume = settings.azanVolume
                        ) { stopSelf() }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        stopSelf()
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
        serviceScope.cancel()
        azanPlayer.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
