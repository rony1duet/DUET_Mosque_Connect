package com.duet.mosque.connect.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt

data class CompassData(
    val azimuth: Float = 0f, // Direction device is pointing (0 = North, 90 = East, etc)
    val bearingToKaaba: Float = 0f, // Angle from North to Kaaba
    val relativeAngle: Float = 0f, // Angle to rotate compass dial (bearingToKaaba - azimuth)
    val distanceToKaabaKm: Double = 0.0,
    val hasCompassSensor: Boolean = true,
    val isCalibrated: Boolean = true
)

class CompassSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _compassState = MutableStateFlow(CompassData(hasCompassSensor = accelerometer != null && magnetometer != null))
    val compassState: StateFlow<CompassData> = _compassState

    // Sensor readings
    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // DUET, Gazipur Coordinates
    private var currentLatitude = 23.9999
    private var currentLongitude = 90.4201

    // Kaaba Coordinates
    private val kaabaLatitude = 21.4225
    private val kaabaLongitude = 39.8262

    init {
        updateCalculations()
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        currentLatitude = latitude
        currentLongitude = longitude
        updateCalculations()
    }

    fun startListening() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        lastAccelerometerSet = false
        lastMagnetometerSet = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            lastAccelerometerSet = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            lastMagnetometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                // Convert azimuth from radians to degrees
                // azimuth is orientationAngles[0], ranging from -PI to PI
                var azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                azimuthDegrees = (azimuthDegrees + 360) % 360

                // Check magnetometer calibration accuracy
                val isCalibrated = event.accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE

                val currentData = _compassState.value
                val relative = (currentData.bearingToKaaba - azimuthDegrees + 360) % 360

                _compassState.value = currentData.copy(
                    azimuth = azimuthDegrees,
                    relativeAngle = relative,
                    isCalibrated = isCalibrated
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val isCalibrated = accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE
            _compassState.value = _compassState.value.copy(isCalibrated = isCalibrated)
        }
    }

    private fun updateCalculations() {
        val bearing = calculateBearing(currentLatitude, currentLongitude, kaabaLatitude, kaabaLongitude)
        val distance = calculateDistance(currentLatitude, currentLongitude, kaabaLatitude, kaabaLongitude)

        val currentData = _compassState.value
        val relative = (bearing - currentData.azimuth + 360) % 360

        _compassState.value = currentData.copy(
            bearingToKaaba = bearing,
            relativeAngle = relative,
            distanceToKaabaKm = distance
        )
    }

    // Great-circle distance using Haversine formula
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

    // Direct bearing between two coordinates
    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val dLonRad = Math.toRadians(lon2 - lon1)

        val y = sin(dLonRad) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLonRad)

        var bearing = Math.toDegrees(atan2(y, x)).toFloat()
        bearing = (bearing + 360) % 360
        return bearing
    }
}
