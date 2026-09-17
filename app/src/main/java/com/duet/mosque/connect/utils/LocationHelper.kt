package com.duet.mosque.connect.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import kotlin.math.abs

/**
 * =========================================================================================
 * HARDWARE / SYSTEM: LOCATION HELPER (DUET Mosque Connect)
 * =========================================================================================
 * Manages GPS device location fetching via Google Play Services [FusedLocationProviderClient].
 *
 * Capabilities:
 *  1. Runtime Permission Verification (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`).
 *  2. High-Accuracy One-Shot Location Request (with fallback to last known location).
 *  3. Continuous GPS tracking for live Qibla bearing recalculations.
 *  4. Reverse-Geocoding: Converts raw coordinates into clean human-readable names (e.g. "Gazipur, Bangladesh").
 *  5. Graceful Fallback: Defaults to DUET Central Campus (23.9999° N, 90.4201° E) if GPS is disabled.
 *
 * Kotlin Concepts Explained for Beginners:
 *  - `data class UserLocationInfo(...)`: Holds latitude, longitude, accuracy, and formatted address.
 *  - `@Suppress("MissingPermission")`: Suppresses IDE lint warnings after we manually verify permissions
 *    with `hasLocationPermission()`.
 *  - Lambda callbacks: `onLocationReceived: (UserLocationInfo) -> Unit` allows passing the result
 *    asynchronously once GPS hardware responds.
 * =========================================================================================
 */

/**
 * Holds GPS and Geocoded location state.
 */
data class UserLocationInfo(
    val latitude: Double = 23.9999, // DUET, Gazipur fallback
    val longitude: Double = 90.4201,
    val altitude: Double = 15.0,
    val accuracyMeters: Float = 0f,
    val locationName: String = "DUET Campus, Gazipur",
    val isGpsActive: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var locationCallback: LocationCallback? = null
    private var cancellationTokenSource: CancellationTokenSource? = null

    /**
     * Checks if the user has granted either FINE or COARSE location runtime permissions.
     */
    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /**
     * Requests a fresh high-accuracy GPS fix from Google Play Services.
     */
    @Suppress("MissingPermission")
    fun requestFreshLocation(onLocationReceived: (UserLocationInfo) -> Unit) {
        if (!hasLocationPermission()) {
            onLocationReceived(
                UserLocationInfo(
                    latitude = 23.9999,
                    longitude = 90.4201,
                    locationName = "DUET Campus, Gazipur (Default)",
                    isGpsActive = false
                )
            )
            return
        }

        cancellationTokenSource?.cancel()
        val tokenSource = CancellationTokenSource()
        cancellationTokenSource = tokenSource

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                tokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val locationName = resolveLocationName(location.latitude, location.longitude)
                    onLocationReceived(
                        UserLocationInfo(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            altitude = location.altitude,
                            accuracyMeters = location.accuracy,
                            locationName = locationName,
                            isGpsActive = true,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Fallback to lastLocation if current location returned null
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                        if (lastLoc != null) {
                            val locName = resolveLocationName(lastLoc.latitude, lastLoc.longitude)
                            onLocationReceived(
                                UserLocationInfo(
                                    latitude = lastLoc.latitude,
                                    longitude = lastLoc.longitude,
                                    altitude = lastLoc.altitude,
                                    accuracyMeters = lastLoc.accuracy,
                                    locationName = locName,
                                    isGpsActive = true,
                                    lastUpdated = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            }.addOnFailureListener {
                // If high accuracy fails, attempt last known location
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                    if (lastLoc != null) {
                        val locName = resolveLocationName(lastLoc.latitude, lastLoc.longitude)
                        onLocationReceived(
                            UserLocationInfo(
                                latitude = lastLoc.latitude,
                                longitude = lastLoc.longitude,
                                altitude = lastLoc.altitude,
                                accuracyMeters = lastLoc.accuracy,
                                locationName = locName,
                                isGpsActive = true,
                                lastUpdated = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        } catch (_: SecurityException) {
        }
    }

    /**
     * Starts continuous GPS location updates while on the Qibla screen.
     */
    @Suppress("MissingPermission")
    fun startContinuousLocationUpdates(onLocationReceived: (UserLocationInfo) -> Unit) {
        if (!hasLocationPermission()) return

        stopContinuousLocationUpdates()

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .setMinUpdateDistanceMeters(2f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val locationName = resolveLocationName(location.latitude, location.longitude)
                onLocationReceived(
                    UserLocationInfo(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitude = location.altitude,
                        accuracyMeters = location.accuracy,
                        locationName = locationName,
                        isGpsActive = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback as LocationCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
        }
    }

    /**
     * Stops continuous location updates to preserve device battery.
     */
    fun stopContinuousLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
        cancellationTokenSource?.cancel()
        cancellationTokenSource = null
    }

    /**
     * Converts latitude & longitude coordinates into a human-readable city / locality string.
     */
    private fun resolveLocationName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                formatAddress(addresses?.firstOrNull(), lat, lon)
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                formatAddress(addresses?.firstOrNull(), lat, lon)
            }
        } catch (_: Exception) {
            formatCoordinates(lat, lon)
        }
    }

    private fun formatAddress(address: Address?, lat: Double, lon: Double): String {
        if (address == null) return formatCoordinates(lat, lon)
        val locality = address.locality ?: address.subAdminArea ?: address.adminArea
        val country = address.countryName
        return when {
            locality != null && country != null -> "$locality, $country"
            locality != null -> locality
            country != null -> "$country (${formatCoordinates(lat, lon)})"
            else -> formatCoordinates(lat, lon)
        }
    }

    private fun formatCoordinates(lat: Double, lon: Double): String {
        val latDir = if (lat >= 0) "N" else "S"
        val lonDir = if (lon >= 0) "E" else "W"
        return String.format(Locale.US, "%.4f° %s, %.4f° %s", abs(lat), latDir, abs(lon), lonDir)
    }
}
