package com.duet.mosque.connect.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duet.mosque.connect.ui.components.KaabaIcon
import com.duet.mosque.connect.ui.theme.EmeraldGreen
import com.duet.mosque.connect.ui.theme.EmeraldGreenDark
import com.duet.mosque.connect.ui.theme.GoldAccent
import com.duet.mosque.connect.ui.theme.GoldAccentLight
import com.duet.mosque.connect.ui.theme.NoticeRed
import com.duet.mosque.connect.ui.viewmodel.MosqueViewModel
import java.util.Locale

/**
 * =========================================================================================
 * 3. QIBLA COMPASS SCREEN (DUET Mosque Connect)
 * =========================================================================================
 * An interactive high-precision digital compass that points Muslims directly towards the Holy
 * Kaaba (Makkah Al-Mukarramah).
 *
 * Core Capabilities:
 *  1. Rotating Compass Dial: Rotates relative to True North using real-time geomagnetic sensor data.
 *  2. Sculpted Qibla Needle: Points towards the calculated Great-Circle forward azimuth (Kaaba bearing).
 *  3. Angle Unwrapping Math: Eliminates jarring needle snapping when rotating past 0°/360°.
 *  4. Spirit Level Detection: Senses device tilt (pitch & roll) and guides the user to hold the phone flat.
 *  5. Haptic Feedback: Triggers a gentle pulse vibration when perfectly aligned within +/- 3.5°.
 *  6. Calibration Assistant: Offers a guide for figure-8 calibration if the sensor status is uncalibrated.
 *
 * Kotlin Concepts Explained for Beginners:
 *  - `animateFloatAsState`: Smoothly interpolates angle changes so needle rotation looks fluid and natural.
 *  - `LaunchedEffect(key)`: Runs a background coroutine whenever the key variable (e.g. `isAligned`) changes.
 *  - `Canvas { ... }`: Low-level 2D graphics API in Compose to draw custom rings, needle paths, and ticks.
 * =========================================================================================
 */

private fun getCompassDirectionLabel(degrees: Float): String {
    val normalized = (degrees % 360f + 360f) % 360f
    val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
    val index = ((normalized + 11.25f) / 22.5f).toInt() % 16
    return directions[index]
}

@Composable
fun QiblaCompassScreen(viewModel: MosqueViewModel) {
    val compassState by viewModel.compassState.collectAsState()
    val context = LocalContext.current

    // Alignment is true when relative angle from top of phone is within +/- 3.5 degrees of Kaaba
    val isAligned = compassState.hasCompassSensor &&
            (compassState.relativeAngle <= 3.5f || compassState.relativeAngle >= 356.5f)

    // Unwrapped continuous azimuth calculation to prevent 359° -> 0° snapping
    var lastAzimuth by remember { mutableFloatStateOf(compassState.azimuth) }
    var continuousAzimuth by remember { mutableFloatStateOf(compassState.azimuth) }
    LaunchedEffect(compassState.azimuth) {
        val current = compassState.azimuth
        var delta = current - lastAzimuth
        while (delta < -180f) delta += 360f
        while (delta > 180f) delta -= 360f
        continuousAzimuth += delta
        lastAzimuth = current
    }
    val animatedNorthAngle by animateFloatAsState(
        targetValue = -continuousAzimuth,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "northDialRotation"
    )

    // Unwrapped continuous Qibla needle angle calculation
    var lastQibla by remember { mutableFloatStateOf(compassState.relativeAngle) }
    var continuousQibla by remember { mutableFloatStateOf(compassState.relativeAngle) }
    LaunchedEffect(compassState.relativeAngle) {
        val current = compassState.relativeAngle
        var delta = current - lastQibla
        while (delta < -180f) delta += 360f
        while (delta > 180f) delta -= 360f
        continuousQibla += delta
        lastQibla = current
    }
    val animatedQiblaAngle by animateFloatAsState(
        targetValue = continuousQibla,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "qiblaNeedleRotation"
    )

    // Haptic Vibration when user aligns directly with Qibla
    var lastVibrationTime by remember { mutableStateOf(0L) }
    var wasAligned by remember { mutableStateOf(false) }

    LaunchedEffect(isAligned) {
        val now = System.currentTimeMillis()
        if (isAligned && (!wasAligned || (now - lastVibrationTime > 2000L))) {
            lastVibrationTime = now
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =
                        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                70,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(70)
                    }
                }
            } catch (_: Exception) {
            }
        }
        wasAligned = isAligned
    }

    // Alignment Glowing Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    var showCalibrateDialog by remember { mutableStateOf(false) }
    var isRefreshingLocation by remember { mutableStateOf(false) }

    // Calibration modal instruction dialog
    if (showCalibrateDialog) {
        AlertDialog(
            onDismissRequest = { showCalibrateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compass Calibration", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "To ensure high precision Qibla detection:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "1. Hold your device flat in your palm.\n2. Wave your phone in a figure-8 motion (♾️) 3 to 5 times.\n3. Keep away from magnetic phone cases, iron metal, or electronic appliances.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCalibrateDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Got It", color = Color.White)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Title & Direction Guidance Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Qibla Finder",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldGreen
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Dynamic guidance pill text
            val guidanceText = remember(isAligned, compassState.relativeAngle, compassState.hasCompassSensor, compassState.isLevel) {
                when {
                    !compassState.hasCompassSensor -> "Compass sensor unavailable on this device"
                    !compassState.isLevel -> "Hold phone flat horizontally for precision"
                    isAligned -> "Perfectly Aligned with Kaaba"
                    compassState.relativeAngle <= 180f -> "Turn Right by ${String.format(Locale.US, "%.0f°", compassState.relativeAngle)}"
                    else -> "Turn Left by ${String.format(Locale.US, "%.0f°", 360f - compassState.relativeAngle)}"
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        when {
                            isAligned -> EmeraldGreen.copy(alpha = 0.16f)
                            !compassState.isLevel -> GoldAccent.copy(alpha = 0.18f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = when {
                            isAligned -> EmeraldGreen.copy(alpha = 0.5f)
                            !compassState.isLevel -> GoldAccent.copy(alpha = 0.5f)
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    text = guidanceText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isAligned -> EmeraldGreen
                        !compassState.isLevel -> Color(0xFFD97706)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    }
                )
            }
        }

        // Compass Dial or Fallback Warning Card
        if (!compassState.hasCompassSensor) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = null,
                        tint = NoticeRed,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Magnetic Compass Sensor Missing",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoticeRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your device does not have a physical magnetic sensor. The Qibla bearing from your location is approximately ${String.format(Locale.US, "%.1f°", compassState.bearingToKaaba)} from True North.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Interactive 3D Compass Dial
            Box(
                modifier = Modifier
                    .size(290.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background aligned glowing pulse
                if (isAligned) {
                    Box(
                        modifier = Modifier
                            .size(270.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        EmeraldGreen.copy(alpha = pulseAlpha),
                                        GoldAccent.copy(alpha = pulseAlpha * 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Dial Outer Frame & Degree Ticks
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2 - 14f

                    // Outer circle ring
                    drawCircle(
                        color = if (isAligned) GoldAccent else EmeraldGreen.copy(alpha = 0.35f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = if (isAligned) 3.5.dp.toPx() else 2.dp.toPx())
                    )

                    // Inner decorative circle
                    drawCircle(
                        color = EmeraldGreen.copy(alpha = 0.1f),
                        radius = radius - 14.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Circular ticks every 10 degrees
                    for (angle in 0 until 360 step 10) {
                        val isMajor = angle % 30 == 0
                        val isCardinal = angle % 90 == 0
                        val tickLength = if (isCardinal) 12.dp.toPx() else if (isMajor) 8.dp.toPx() else 4.dp.toPx()
                        val tickWidth = if (isCardinal) 2.5.dp.toPx() else if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()
                        val tickColor = if (isCardinal) EmeraldGreen.copy(alpha = 0.8f) else if (isMajor) EmeraldGreen.copy(alpha = 0.4f) else EmeraldGreen.copy(alpha = 0.2f)

                        rotate(degrees = angle.toFloat(), pivot = center) {
                            drawLine(
                                color = tickColor,
                                start = Offset(center.x, center.y - radius),
                                end = Offset(center.x, center.y - radius + tickLength),
                                strokeWidth = tickWidth
                            )
                        }
                    }
                }

                // Fixed Top Sight Indicator (12 O'Clock Reticle)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2 - 14f

                    val sightMarker = Path().apply {
                        moveTo(center.x, center.y - radius - 6.dp.toPx())
                        lineTo(center.x - 6.dp.toPx(), center.y - radius - 16.dp.toPx())
                        lineTo(center.x + 6.dp.toPx(), center.y - radius - 16.dp.toPx())
                        close()
                    }
                    drawPath(
                        sightMarker,
                        color = if (isAligned) GoldAccent else EmeraldGreen
                    )
                }

                // Rotating Compass Rose (N, E, S, W + Kaaba Rim Marker)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedNorthAngle),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // North
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "N",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = NoticeRed
                            )
                        }
                        // East
                        Text(
                            text = "E",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 22.dp)
                                .rotate(90f)
                        )
                        // South
                        Text(
                            text = "S",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 22.dp)
                                .rotate(180f)
                        )
                        // West
                        Text(
                            text = "W",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 22.dp)
                                .rotate(-90f)
                        )
                    }

                    // North & South direction pointers on dial
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.minDimension / 2 - 14f

                        val northPointer = Path().apply {
                            moveTo(center.x, center.y - radius + 42.dp.toPx())
                            lineTo(center.x - 5.dp.toPx(), center.y - 48.dp.toPx())
                            lineTo(center.x + 5.dp.toPx(), center.y - 48.dp.toPx())
                            close()
                        }
                        drawPath(northPointer, color = NoticeRed.copy(alpha = 0.4f))

                        val southPointer = Path().apply {
                            moveTo(center.x, center.y + radius - 42.dp.toPx())
                            lineTo(center.x - 5.dp.toPx(), center.y + 48.dp.toPx())
                            lineTo(center.x + 5.dp.toPx(), center.y + 48.dp.toPx())
                            close()
                        }
                        drawPath(southPointer, color = Color.Gray.copy(alpha = 0.18f))

                        // Kaaba Marker On Rotating Dial Rim At Bearing Angle
                        rotate(degrees = compassState.bearingToKaaba, pivot = center) {
                            drawCircle(
                                color = GoldAccent,
                                radius = 4.dp.toPx(),
                                center = Offset(center.x, center.y - radius + 12.dp.toPx())
                            )
                        }
                    }
                }

                // Sculpted Qibla Aim Needle (Points directly to Kaaba relative to top of phone)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedQiblaAngle),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.minDimension / 2 - 14f

                        // Outer gold arrow
                        val outerNeedle = Path().apply {
                            moveTo(center.x, center.y - radius + 15.dp.toPx())
                            lineTo(center.x - 13.dp.toPx(), center.y - radius + 38.dp.toPx())
                            lineTo(center.x - 4.dp.toPx(), center.y - radius + 33.dp.toPx())
                            lineTo(center.x - 3.dp.toPx(), center.y - 26.dp.toPx())
                            lineTo(center.x + 3.dp.toPx(), center.y - 26.dp.toPx())
                            lineTo(center.x + 4.dp.toPx(), center.y - radius + 33.dp.toPx())
                            lineTo(center.x + 13.dp.toPx(), center.y - radius + 38.dp.toPx())
                            close()
                        }
                        drawPath(
                            outerNeedle,
                            color = if (isAligned) GoldAccent else GoldAccent.copy(alpha = 0.85f)
                        )

                        // Inner emerald arrow core
                        val innerNeedle = Path().apply {
                            moveTo(center.x, center.y - radius + 21.dp.toPx())
                            lineTo(center.x - 8.dp.toPx(), center.y - radius + 35.dp.toPx())
                            lineTo(center.x - 1.5.dp.toPx(), center.y - radius + 31.dp.toPx())
                            lineTo(center.x - 1.5.dp.toPx(), center.y - 26.dp.toPx())
                            lineTo(center.x + 1.5.dp.toPx(), center.y - 26.dp.toPx())
                            lineTo(center.x + 1.5.dp.toPx(), center.y - radius + 31.dp.toPx())
                            lineTo(center.x + 8.dp.toPx(), center.y - radius + 35.dp.toPx())
                            close()
                        }
                        drawPath(
                            innerNeedle,
                            color = if (isAligned) EmeraldGreenDark else EmeraldGreen
                        )

                        // Center alignment jewel dot
                        drawCircle(
                            color = if (isAligned) GoldAccentLight else GoldAccent,
                            radius = 3.dp.toPx(),
                            center = Offset(center.x, center.y - radius + 48.dp.toPx())
                        )
                    }
                }

                // Center Kaaba Hub & Spirit Level Bubble Indicator
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .border(
                            width = if (isAligned) 2.5.dp else 1.5.dp,
                            color = if (isAligned) GoldAccent else EmeraldGreen.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Level spirit bubble moving based on phone pitch and roll
                    val bubbleOffsetX = (compassState.roll * 0.8f).coerceIn(-18f, 18f)
                    val bubbleOffsetY = (compassState.pitch * 0.8f).coerceIn(-18f, 18f)

                    Box(
                        modifier = Modifier
                            .offset(x = bubbleOffsetX.dp, y = bubbleOffsetY.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (compassState.isLevel) EmeraldGreen.copy(alpha = 0.25f)
                                else NoticeRed.copy(alpha = 0.25f)
                            )
                    )

                    KaabaIcon()
                }
            }
        }

        // Location & Sensor Calibration Action Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = compassState.locationName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = String.format(Locale.US, "Lat: %.4f°, Lon: %.4f°", compassState.userLatitude, compassState.userLongitude),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Calibration Guide Button
                    IconButton(
                        onClick = { showCalibrateDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Calibration Info",
                            tint = if (compassState.isCalibrated) EmeraldGreen else NoticeRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Refresh GPS Coordinates Button
                    IconButton(
                        onClick = {
                            isRefreshingLocation = true
                            viewModel.refreshGPSLocation()
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isRefreshingLocation = false
                            }, 1000)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Location",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Compass Metrics Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Qibla Bearing
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%.1f° %s", compassState.bearingToKaaba, getCompassDirectionLabel(compassState.bearingToKaaba)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                    Text(
                        text = "Qibla Direction",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }

                // Kaaba Distance in Kilometers
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%,.0f km", compassState.distanceToKaabaKm),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                    Text(
                        text = "Kaaba Distance",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }

                // Current Device Azimuth Heading (True North)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%.0f° %s", compassState.azimuth, getCompassDirectionLabel(compassState.azimuth)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAligned) GoldAccent else EmeraldGreen
                    )
                    Text(
                        text = "Your Heading",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }

                // Geomagnetic Declination
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%+.1f°", compassState.declination),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "Declination",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}
