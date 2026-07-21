package az.shia.azan.update

import android.content.Context
import az.shia.azan.data.PreferencesManager
import kotlinx.coroutines.flow.first

internal suspend fun automaticUpdatesEnabled(context: Context): Boolean =
    runCatching {
        PreferencesManager(context.applicationContext)
            .settingsFlow
            .first()
            .automaticUpdatesEnabled
    }.getOrDefault(false)
