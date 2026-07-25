package az.shia.azan

import android.app.Application
import az.shia.azan.notification.PrayerWatchdog
import az.shia.azan.update.UpdateScheduler

/** Tətbiq səviyyəli periodik işləri başladır. */
class AzanApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        UpdateScheduler.schedule(this)
        // Alarmların/vidcetin OEM tərəfindən öldürülməsinə qarşı watchdog.
        PrayerWatchdog.schedule(this)
    }
}
