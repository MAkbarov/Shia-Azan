package az.shia.azan.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Vaxt formatlaşdırma funksiyaları
 */
object TimeFormatter {
    
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    
    /**
     * Calendar obyektini saat:dəqiqə formatında göstər
     */
    fun formatTime(calendar: Calendar): String {
        return timeFormat.format(calendar.time)
    }
    
    /**
     * Calendar obyektini tarix formatında göstər
     */
    fun formatDate(calendar: Calendar): String {
        return dateFormat.format(calendar.time)
    }
    
    /**
     * İki vaxt arasındaki fərqi hesabla və göstər
     */
    fun getTimeRemaining(targetTime: Calendar, currentTime: Calendar = Calendar.getInstance()): String {
        val diffMillis = targetTime.timeInMillis - currentTime.timeInMillis
        
        if (diffMillis <= 0) {
            return "0 dəq"
        }
        
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
        
        return when {
            hours > 0 -> "${hours}s ${minutes}d"
            else -> "${minutes}d"
        }
    }
    
    /**
     * İki vaxt eyni gündədirsə true qaytarır
     */
    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
