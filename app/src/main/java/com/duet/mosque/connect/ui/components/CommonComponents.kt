package com.duet.mosque.connect.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duet.mosque.connect.ui.theme.EmeraldGreen
import com.duet.mosque.connect.ui.theme.GoldAccent
import com.duet.mosque.connect.ui.theme.TextLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * =========================================================================================
 * REUSABLE UI COMPONENTS (DUET Mosque Connect)
 * =========================================================================================
 * In Jetpack Compose, UI is built using modular "@Composable" functions.
 * Rather than duplicating dialog launchers, date formatters, and switches in multiple screens,
 * we extract them here as clean, reusable building blocks.
 *
 * Kotlin Concepts Used:
 *  1. `@Composable`: Tells the Kotlin compiler that this function transforms data into UI.
 *  2. `(String) -> Unit`: A higher-order function (callback lambda). 'Unit' is Kotlin's 'void'.
 *  3. `LocalContext.current`: Provides access to the Android Context needed to show platform dialogs.
 *  4. `Modifier`: The standard Compose mechanism to set sizing, padding, background, borders, and click actions.
 * =========================================================================================
 */

/**
 * Kaaba Graphic Icon
 * A custom visual representation of the Holy Kaaba in Makkah with the golden Kiswah band and door.
 */
@Composable
fun KaabaIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(6.dp))
            .border(1.5.dp, GoldAccent, shape = RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            // Gold Kiswah Top Band
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(GoldAccent)
            )
            // Gold Kaaba Door (Bab ar-Rahmah)
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(14.dp)
                    .background(GoldAccent, shape = RoundedCornerShape(topStart = 1.5.dp, topEnd = 1.5.dp))
            )
            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}

/**
 * TimePickerClickableField
 * Displays a formatted time card (e.g. "04:30 AM") and opens the native Android TimePickerDialog when tapped.
 *
 * @param label The descriptor above the time (e.g. "Fajr Azan", "Iftar Time").
 * @param value The currently selected time string.
 * @param onTimeSelected Callback invoked when the user selects a new time in the picker.
 */
@Composable
fun TimePickerClickableField(
    label: String,
    value: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Helper: Parses standard 12-hour "hh:mm a" string into hour & minute integers
    fun parseTimeString(timeStr: String): Pair<Int, Int> {
        return try {
            val parser = SimpleDateFormat("hh:mm a", Locale.US)
            val date = parser.parse(timeStr)
            if (date != null) {
                val cal = Calendar.getInstance().apply { time = date }
                Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            } else {
                Pair(12, 0)
            }
        } catch (_: Exception) {
            Pair(12, 0)
        }
    }

    // Helper: Formats 24h hour and minute back to clean 12h "hh:mm a" string
    fun formatTimeString(hourOfDay: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
        val formatter = SimpleDateFormat("hh:mm a", Locale.US)
        return formatter.format(cal.time)
    }

    // Launch Android platform TimePickerDialog
    fun launchPicker() {
        val (hour, minute) = parseTimeString(value)
        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                onTimeSelected(formatTimeString(selectedHour, selectedMinute))
            },
            hour,
            minute,
            false // 12-hour AM/PM format
        ).show()
    }

    OutlinedCard(
        onClick = { launchPicker() },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (value.isNotBlank()) value else "Select Time",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )
            }
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Pick Time",
                tint = EmeraldGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * DatePickerClickableField
 * Displays a formatted date card (e.g. "Sep 17, 2026") and opens Android DatePickerDialog when tapped.
 */
@Composable
fun DatePickerClickableField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = EmeraldGreen
) {
    val context = LocalContext.current

    fun parseDateString(dateStr: String): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        return try {
            val parser = SimpleDateFormat("MMM d, yyyy", Locale.US)
            val date = parser.parse(dateStr)
            if (date != null) {
                cal.time = date
            }
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        } catch (_: Exception) {
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
    }

    fun formatDateString(year: Int, month: Int, dayOfMonth: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.US)
        return formatter.format(cal.time)
    }

    fun launchPicker() {
        val (year, month, day) = parseDateString(value)
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                onDateSelected(formatDateString(selectedYear, selectedMonth, selectedDay))
            },
            year,
            month,
            day
        ).show()
    }

    OutlinedCard(
        onClick = { launchPicker() },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (value.isNotBlank()) value else "Select Date",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = "Pick Date",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * SettingToggleRow
 * A reusable settings row with title, description, and an animated Material3 Switch toggle.
 */
@Composable
fun SettingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                lineHeight = 14.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextLight,
                checkedTrackColor = EmeraldGreen,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

/**
 * SecurityFeatureItem
 * A clean visual bullet list item used in the Admin/Imam Security guide.
 */
@Composable
fun SecurityFeatureItem(title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(EmeraldGreen)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
            Text(
                text = description,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
