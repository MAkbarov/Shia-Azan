package az.shia.azan.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import az.shia.azan.data.LocationData
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

/** GPS və yer məlumatı üçün helper klass. */
class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Keşlənmiş təxmini nəticə əvəzinə yeni, yüksək dəqiqlikli koordinat alır.
     * Reverse-geocoder yalnız şəhər adını tapır; namaz hesablamasında birbaşa
     * GPS-in qaytardığı latitude/longitude istifadə edilir.
     */
    suspend fun getCurrentLocation(): LocationData? {
        if (!hasFineLocationPermission()) return null

        return try {
            val location = getCurrentGPSLocation() ?: return null
            getLocationData(location.latitude, location.longitude)
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }

    /** GPS-dən maksimum 20 saniyə gözləyərək fresh və fine nəticə alır. */
    private suspend fun getCurrentGPSLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            if (!hasFineLocationPermission()) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val cancellationTokenSource = CancellationTokenSource()
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setMaxUpdateAgeMillis(0L)
                .setDurationMillis(20_000L)
                .build()

            try {
                fusedLocationClient.getCurrentLocation(
                    request,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                    if (continuation.isActive) continuation.resume(null)
                }
            } catch (exception: SecurityException) {
                exception.printStackTrace()
                if (continuation.isActive) continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }

    /** Koordinatları gözlənilən geocoder nəticəsi ilə oxunaqlı məkan adına çevirir. */
    private suspend fun getLocationData(latitude: Double, longitude: Double): LocationData {
        val address = withTimeoutOrNull(10_000L) {
            reverseGeocode(latitude, longitude)
        }

        return LocationData(
            latitude = latitude,
            longitude = longitude,
            cityName = address?.subLocality
                ?: address?.locality
                ?: address?.subAdminArea
                ?: address?.adminArea
                ?: "Hazırkı məkan",
            countryName = address?.countryName ?: "Azərbaycan",
            timeZone = TimeZone.getDefault().id
        )
    }

    private suspend fun reverseGeocode(latitude: Double, longitude: Double): Address? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale("az"))

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        if (continuation.isActive) {
                            continuation.resume(addresses.firstOrNull())
                        }
                    }
                }
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }

    fun hasFineLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun hasLocationPermission(): Boolean =
        hasFineLocationPermission() || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}
