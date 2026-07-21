package az.shia.azan.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import az.shia.azan.R
import az.shia.azan.data.AzanSound
import az.shia.azan.data.PrayerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

/** Azan səslərini seçilmiş resurs və səs səviyyəsi ilə oxudan player. */
class AzanPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var currentVolume = 1f

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPrayerType = MutableStateFlow<PrayerType?>(null)
    val currentPrayerType: StateFlow<PrayerType?> = _currentPrayerType.asStateFlow()

    fun playAzan(
        prayerType: PrayerType,
        sound: AzanSound = AzanSound.DEFAULT,
        volume: Float = currentVolume,
        onComplete: (() -> Unit)? = null
    ) {
        stop()
        currentVolume = volume.coerceIn(0f, 1f)

        try {
            val audioResId = getAzanAudioResource(prayerType, sound)
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(
                    context,
                    android.net.Uri.parse("android.resource://${context.packageName}/$audioResId")
                )
                setOnCompletionListener { completedPlayer ->
                    releaseEnhancer()
                    completedPlayer.release()
                    if (mediaPlayer === completedPlayer) mediaPlayer = null
                    _isPlaying.value = false
                    _currentPrayerType.value = null
                    onComplete?.invoke()
                }
                setOnErrorListener { failedPlayer, _, _ ->
                    releaseEnhancer()
                    failedPlayer.release()
                    if (mediaPlayer === failedPlayer) mediaPlayer = null
                    _isPlaying.value = false
                    _currentPrayerType.value = null
                    true
                }
                prepare()
                setVolume(currentVolume, currentVolume)
            }

            mediaPlayer = player
            configureLoudnessEnhancer(player, currentVolume)
            player.start()
            _isPlaying.value = true
            _currentPrayerType.value = prayerType
        } catch (exception: Exception) {
            exception.printStackTrace()
            releaseEnhancer()
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying.value = false
            _currentPrayerType.value = null
        }
    }

    fun stop() {
        releaseEnhancer()
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) player.stop()
            } catch (_: IllegalStateException) {
                // Player artıq tamamlanıbsa yalnız release kifayətdir.
            }
            player.release()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentPrayerType.value = null
    }

    fun togglePause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
            } else {
                player.start()
                _isPlaying.value = true
            }
        }
    }

    /** Slider həm cari, həm də növbəti playback üçün real volume-a tətbiq edilir. */
    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(currentVolume, currentVolume)
        loudnessEnhancer?.setTargetGain((MAX_GAIN_MB * currentVolume).roundToInt())
    }

    fun release() = stop()

    private fun configureLoudnessEnhancer(player: MediaPlayer, volume: Float) {
        releaseEnhancer()
        try {
            loudnessEnhancer = LoudnessEnhancer(player.audioSessionId).apply {
                setTargetGain((MAX_GAIN_MB * volume).roundToInt())
                enabled = volume > 0f
            }
        } catch (exception: Exception) {
            // Bəzi istehsalçı audio mühərrikləri effekti dəstəkləmir; playback davam edir.
            exception.printStackTrace()
            loudnessEnhancer = null
        }
    }

    private fun releaseEnhancer() {
        try {
            loudnessEnhancer?.release()
        } catch (_: Exception) {
            // Artıq release edilmiş audio effect-i nəzərə alma.
        }
        loudnessEnhancer = null
    }

    private fun getAzanAudioResource(prayerType: PrayerType, sound: AzanSound): Int {
        val resourceName = sound.getResourceName(prayerType == PrayerType.FAJR)
        val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        return if (resourceId != 0) resourceId else R.raw.azan_default
    }

    private companion object {
        // Mənbə MP3 zəif olduğuna görə 100%-də yumşaq +6 dB gücləndirmə.
        const val MAX_GAIN_MB = 600f
    }
}
