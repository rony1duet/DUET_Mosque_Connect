package com.duet.mosque.connect.utils

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * =========================================================================================
 * HARDWARE SENSORS: COMPASS & QIBLA MATH ENGINE (DUET Mosque Connect)
 * =========================================================================================
 * Interfaces directly with Android hardware sensors to compute live device orientation,
 * tilt angles (pitch/roll), and calculate the precise bearing towards the Holy Kaaba.
 *
 * Mathematical & Hardware Pipeline:
 *  1. [Sensor Acquisition]: Reads from TYPE_ROTATION_VECTOR (preferred) or falls back to
 *     TYPE_ACCELEROMETER + TYPE_MAGNETIC_FIELD.
 *  2. [Coordinate Remapping]: Remaps matrix coordinates based on the display's current rotation (Portrait/Landscape).
 *  3. [Declination Correction]: Uses Android's `GeomagneticField` model to convert Magnetic Azimuth
 *     into True Geographic North Azimuth based on the user's GPS coordinates.
 *  4. [Jitter Smoothing]: Applies circular low-pass filtering to remove hand vibrations and sensor noise.
 *  5. [Great-Circle Forward Azimuth]: Uses spherical trigonometry (Haversine & Forward Bearing)
 *     to calculate the direct heading to Kaaba Sanctuary in Makkah (Lat: 21.422487° N, Lon: 39.826206° E).
 *
 * Kotlin Concepts Explained for Beginners:
 *  - `class ... : SensorEventListener`: Implements the Android hardware sensor callback interface.
 *  - `FloatArray(9)`: Pre-allocated arrays in memory used for 3x3 rotation matrices to avoid garbage collection churn.
 *  - `StateFlow<CompassData>`: Emits updated calculations in real-time to the ViewModel and Compose UI.
 * =========================================================================================
 */

/**
 * Holds calculated compass and Qibla telemetry.
 */
data class CompassData(
    val azimuth: Float = 0f, // True Heading relative to True North (0° = North, 90° = East, etc.)
    val magneticAzimuth: Float = 0f, // Heading relative to Magnetic North
    val declination: Float = 0f, // Magnetic declination in degrees
    val bearingToKaaba: Float = 278.4f, // True Angle from True North to Kaaba (approx 278° for Bangladesh)
    val relativeAngle: Float = 0f, // Angle to rotate pointer from top of phone: (bearingToKaaba - azimuth + 360) % 360
    val distanceToKaabaKm: Double = 4820.0,
    val pitch: Float = 0f, // Pitch angle in degrees (-90 to 90)
    val roll: Float = 0f, // Roll angle in degrees (-180 to 180)
    val isLevel: Boolean = true, // Phone held reasonably flat (< 30° tilt)
    val hasCompassSensor: Boolean = true,
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val isCalibrated: Boolean = true,
    val userLatitude: Double = 23.9999,
    val userLongitude: Double = 90.4201,
    val locationName: String = "DUET Campus, Gazipur",
    val isGpsActive: Boolean = false
)

class CompassSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Hardware sensors
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val geomagneticVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val hasSensor = rotationVectorSensor != null ||
            geomagneticVectorSensor != null ||
            (accelerometer != null && magnetometer != null)

    private val _compassState = MutableStateFlow(
        CompassData(
            hasCompassSensor = hasSensor,
            userLatitude = 23.9999,
            userLongitude = 90.4201
        )
    )
    val compassState: StateFlow<CompassData> = _compassState.asStateFlow()

    // Sensor buffers & matrices (reused to avoid memory allocations)
    private val rotationMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false

    // Display rotation (default ROTATION_0 = Portrait)
    private var displayRotation: Int = Surface.ROTATION_0

    // User Location (Default: DUET Gazipur)
    private var currentLatitude = 23.9999
    private var currentLongitude = 90.4201
    private var currentAltitude = 15.0
    private var currentLocationName = "DUET Campus, Gazipur"
    private var isGpsActive = false
    private var magneticDeclination = 0f

    // Kaaba Sanctuary Coordinates (Makkah Al-Mukarramah, Saudi Arabia)
    private val kaabaLatitude = 21.422487
    private val kaabaLongitude = 39.826206

    // Filtered azimuth to eliminate micro-jitter
    private var smoothedAzimuth: Float = 0f
    private var isFirstReading: Boolean = true

    init {
        updateDeclination()
        updateCalculations()
    }

    fun setDisplayRotation(rotation: Int) {
        displayRotation = rotation
    }

    fun updateLocation(
        latitude: Double,
        longitude: Double,
        altitude: Double = 15.0,
        locationName: String = "",
        isGps: Boolean = true
    ) {
        currentLatitude = latitude
        currentLongitude = longitude
        currentAltitude = altitude
        if (locationName.isNotBlank()) {
            currentLocationName = locationName
        }
        isGpsActive = isGps

        updateDeclination()
        updateCalculations()
    }

    private fun updateDeclination() {
        try {
            val geomagneticField = GeomagneticField(
                currentLatitude.toFloat(),
                currentLongitude.toFloat(),
                currentAltitude.toFloat(),
                System.currentTimeMillis()
            )
            magneticDeclination = geomagneticField.declination
        } catch (_: Exception) {
            magneticDeclination = 0f
        }
    }

    /**
     * Registers hardware sensor listeners when entering the Qibla screen.
     */
    fun startListening() {
        isFirstReading = true
        when {
            rotationVectorSensor != null -> {
                sensorManager.registerListener(
                    this,
                    rotationVectorSensor,
                    SensorManager.SENSOR_DELAY_GAME
                )
            }
            geomagneticVectorSensor != null -> {
                sensorManager.registerListener(
                    this,
                    geomagneticVectorSensor,
                    SensorManager.SENSOR_DELAY_GAME
                )
            }
            else -> {
                if (accelerometer != null) {
                    sensorManager.registerListener(
                        this,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                }
                if (magnetometer != null) {
                    sensorManager.registerListener(
                        this,
                        magnetometer,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                }
            }
        }
    }

    /**
     * Unregisters hardware sensor listeners when exiting the Qibla screen to preserve battery.
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
        lastAccelerometerSet = false
        lastMagnetometerSet = false
        isFirstReading = true
    }

    override fun onSensorChanged(event: SensorEvent) {
        var matrixComputed = false

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR ||
            event.sensor.type == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR
        ) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            matrixComputed = true
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            lowPassFilter(event.values, lastAccelerometer, 0.2f)
            lastAccelerometerSet = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            lowPassFilter(event.values, lastMagnetometer, 0.2f)
            lastMagnetometerSet = true
        }

        if (!matrixComputed && lastAccelerometerSet && lastMagnetometerSet) {
            matrixComputed = SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                lastAccelerometer,
                lastMagnetometer
            )
        }

        if (matrixComputed) {
            // Remap coordinate system based on current screen rotation
            remapForDisplayRotation(rotationMatrix, remappedMatrix, displayRotation)
            SensorManager.getOrientation(remappedMatrix, orientationAngles)

            // Magnetic Azimuth in degrees (0° to 360°)
            var magAzimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            magAzimuthDegrees = (magAzimuthDegrees + 360f) % 360f

            // True North Azimuth = Magnetic Azimuth + Geomagnetic Declination
            val trueAzimuthDegrees = (magAzimuthDegrees + magneticDeclination + 360f) % 360f

            // Smooth azimuth using circular low-pass filter
            if (isFirstReading) {
                smoothedAzimuth = trueAzimuthDegrees
                isFirstReading = false
            } else {
                smoothedAzimuth = smoothCircularAngle(smoothedAzimuth, trueAzimuthDegrees, 0.25f)
            }

            // Pitch and roll tilt angles in degrees
            val pitchDegrees = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            val rollDegrees = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
            val isLevel = abs(pitchDegrees) < 30f && abs(rollDegrees) < 30f

            val isCalibrated = event.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH ||
                    event.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM

            val bearing = calculateBearing(currentLatitude, currentLongitude, kaabaLatitude, kaabaLongitude)
            val distance = calculateDistance(currentLatitude, currentLongitude, kaabaLatitude, kaabaLongitude)
            val relative = (bearing - smoothedAzimuth + 360f) % 360f

            _compassState.value = CompassData(
                azimuth = smoothedAzimuth,
                magneticAzimuth = magAzimuthDegrees,
                declination = magneticDeclination,
                bearingToKaaba = bearing,
                relativeAngle = relative,
                distanceToKaabaKm = distance,
                pitch = pitchDegrees,
                roll = rollDegrees,
                isLevel = isLevel,
                hasCompassSensor = true,
                accuracy = event.accuracy,
                isCalibrated = isCalibrated,
                userLatitude = currentLatitude,
                userLongitude = currentLongitude,
                locationName = currentLocationName,
                isGpsActive = isGpsActive
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val isCalibrated = accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE
        _compassState.value = _compassState.value.copy(
            accuracy = accuracy,
            isCalibrated = isCalibrated
        )
    }

    private fun remapForDisplayRotation(inR: FloatArray, outR: FloatArray, rotation: Int) {
        var axisX = SensorManager.AXIS_X
        var axisY = SensorManager.AXIS_Y

        when (rotation) {
            Surface.ROTATION_0 -> {
                axisX = SensorManager.AXIS_X
                axisY = SensorManager.AXIS_Y
            }
            Surface.ROTATION_90 -> {
                axisX = SensorManager.AXIS_Y
                axisY = SensorManager.AXIS_MINUS_X
            }
            Surface.ROTATION_180 -> {
                axisX = SensorManager.AXIS_MINUS_X
                axisY = SensorManager.AXIS_MINUS_Y
            }
            Surface.ROTATION_270 -> {
                axisX = SensorManager.AXIS_MINUS_Y
                axisY = SensorManager.AXIS_X
            }
        }
        SensorManager.remapCoordinateSystem(inR, axisX, axisY, outR)
    }

    private fun lowPassFilter(input: FloatArray, output: FloatArray, alpha: Float) {
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }

    private fun smoothCircularAngle(current: Float, target: Float, alpha: Float): Float {
        var diff = target - current
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        return (current + diff * alpha + 360f) % 360f
    }

    private fun updateCalculations() {
        val bearing = calculateBearing(currentLatitude, currentLongitude, kaabaLatitude, kaabaLongitude)
        val distance = calculateDistance(currentLatitude, currentLongitude, kaabaLatitude, kaabaLongitude)

        val currentData = _compassState.value
        val relative = (bearing - currentData.azimuth + 360f) % 360f

        _compassState.value = currentData.copy(
            bearingToKaaba = bearing,
            relativeAngle = relative,
            distanceToKaabaKm = distance,
            declination = magneticDeclination,
            userLatitude = currentLatitude,
            userLongitude = currentLongitude,
            locationName = currentLocationName,
            isGpsActive = isGpsActive
        )
    }

    /**
     * Great-circle distance calculation using Haversine formula.
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    /**
     * Great-Circle initial forward azimuth (bearing) from Point A to Point B.
     */
    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val dLonRad = Math.toRadians(lon2 - lon1)

        val y = sin(dLonRad) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLonRad)

        var bearing = Math.toDegrees(atan2(y, x)).toFloat()
        bearing = (bearing + 360f) % 360f
        return bearing
    }
}
