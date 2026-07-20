package az.shia.azan.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import az.shia.azan.data.LocationData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * GPS və yer məlumatı üçün helper klass
 */
class LocationHelper(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    /**
     * Hazırkı yeri əldə et
     */
    suspend fun getCurrentLocation(): LocationData? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return try {
            val location = getCurrentGPSLocation()
            location?.let {
                getLocationData(it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * GPS-dən hazırkı koordinatları al
     */
    private suspend fun getCurrentGPSLocation(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            if (!hasLocationPermission()) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            
            val cancellationTokenSource = CancellationTokenSource()
            
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                continuation.resume(location)
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
                continuation.resume(null)
            }
            
            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            continuation.resume(null)
        }
    }
    
    /**
     * Koordinatlardan şəhər məlumatı al
     */
    private fun getLocationData(latitude: Double, longitude: Double): LocationData {
        val geocoder = Geocoder(context, Locale("az"))
        
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ üçün yeni API
                var locationData: LocationData? = null
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        locationData = LocationData(
                            latitude = latitude,
                            longitude = longitude,
                            cityName = address.locality ?: address.subAdminArea ?: "Naməlum Şəhər",
                            countryName = address.countryName ?: "Azərbaycan"
                        )
                    }
                }
                // Əgər məlumat alınmadısa, default məlumat qaytar
                locationData ?: LocationData(
                    latitude = latitude,
                    longitude = longitude,
                    cityName = "Hazırkı Yer",
                    countryName = "Azərbaycan"
                )
            } else {
                // Köhnə Android versiyaları üçün
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    LocationData(
                        latitude = latitude,
                        longitude = longitude,
                        cityName = address.locality ?: address.subAdminArea ?: "Naməlum Şəhər",
                        countryName = address.countryName ?: "Azərbaycan"
                    )
                } else {
                    LocationData(
                        latitude = latitude,
                        longitude = longitude,
                        cityName = "Hazırkı Yer",
                        countryName = "Azərbaycan"
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LocationData(
                latitude = latitude,
                longitude = longitude,
                cityName = "Hazırkı Yer",
                countryName = "Azərbaycan"
            )
        }
    }
    
    /**
     * Yer icazəsinin olub-olmadığını yoxla
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
