package az.shia.azan.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import az.shia.azan.R
import az.shia.azan.data.PrayerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Azan səslərini oxutmaq üçün player
 */
class AzanPlayer(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPrayerType = MutableStateFlow<PrayerType?>(null)
    val currentPrayerType: StateFlow<PrayerType?> = _currentPrayerType.asStateFlow()
    
    /**
     * Azan səsini oxut
     */
    fun playAzan(prayerType: PrayerType, onComplete: (() -> Unit)? = null) {
        // Əgər artıq oxuyursa, əvvəlcə dayandır
        stop()
        
        try {
            val audioResId = getAzanAudioResource(prayerType)
            
            mediaPlayer = MediaPlayer().apply {
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
                
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPrayerType.value = null
                    release()
                    mediaPlayer = null
                    onComplete?.invoke()
                }
                
                setOnErrorListener { mp, what, extra ->
                    _isPlaying.value = false
                    _currentPrayerType.value = null
                    release()
                    mediaPlayer = null
                    true
                }
                
                prepare()
                start()
                
                _isPlaying.value = true
                _currentPrayerType.value = prayerType
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
            _currentPrayerType.value = null
        }
    }
    
    /**
     * Azanı dayandır
     */
    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
        _isPlaying.value = false
        _currentPrayerType.value = null
    }
    
    /**
     * Pauza et / davam et
     */
    fun togglePause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            } else {
                it.start()
                _isPlaying.value = true
            }
        }
    }
    
    /**
     * Səs səviyyəsini dəyiş (0.0 - 1.0)
     */
    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }
    
    /**
     * Player-i təmizlə
     */
    fun release() {
        stop()
    }
    
    /**
     * Namaz növünə görə audio resource-u seç
     */
    private fun getAzanAudioResource(prayerType: PrayerType): Int {
        return when (prayerType) {
            PrayerType.FAJR -> R.raw.azan_fajr  // Sübh azanı (əlavə tekstlə)
            else -> R.raw.azan_default           // Digər namaz vaxtları
        }
    }
}
