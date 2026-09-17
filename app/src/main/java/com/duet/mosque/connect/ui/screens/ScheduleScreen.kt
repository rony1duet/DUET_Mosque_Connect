package com.duet.mosque.connect.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duet.mosque.connect.data.model.ScheduleEntity
import com.duet.mosque.connect.ui.components.TimePickerClickableField
import com.duet.mosque.connect.ui.theme.EmeraldGreen
import com.duet.mosque.connect.ui.theme.GoldAccent
import com.duet.mosque.connect.ui.theme.TextLight
import com.duet.mosque.connect.ui.viewmodel.MosqueViewModel

/**
 * =========================================================================================
 * 2. PRAYER SCHEDULE SCREEN (DUET Mosque Connect)
 * =========================================================================================
 * Displays the complete prayer timetable for DUET Central Mosque:
 *  - Fajr, Dhuhr, Asr, Maghrib, Isha, and Jummah
 *  - Both Azan (call to prayer) and Jamat (congregational prayer) times.
 *  - Highlights whichever prayer is currently upcoming or active.
 *  - Allows the Mosque Imam/Admin to click the Edit icon and update times in realtime.
 *
 * Kotlin Concepts Explained for Beginners:
 *  - `viewModel.schedules.collectAsState()`: Subscribes to the Room database Flow mapped by ViewModel.
 *  - `editingPrayer?.let { prayer -> ... }`: Kotlin safe null-check idiom. The code block only
 *     executes when `editingPrayer` is not null (i.e. when admin clicked edit).
 *  - `verticalScroll(rememberScrollState())`: Adds smooth scrolling support for compact phone screens.
 * =========================================================================================
 */
@Composable
fun ScheduleScreen(viewModel: MosqueViewModel) {
    val prayers by viewModel.schedules.collectAsState()
    val countdown by viewModel.countdownTimer.collectAsState()
    val nextJamatName by viewModel.nextJamatName.collectAsState()
    val currentPrayerName by viewModel.currentPrayerName.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    var editingPrayer by remember { mutableStateOf<ScheduleEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = "Jamat Schedule",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldGreen,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = "DUET Central Mosque",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Hero Next Jamat Time Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Next Jamat: $nextJamatName",
                        color = GoldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = countdown,
                        color = TextLight,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = TextLight.copy(alpha = 0.2f),
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Full Timetable List for All 6 Prayers
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            prayers.forEach { prayer ->
                val isCurrent = prayer.name.equals(currentPrayerName, ignoreCase = true) ||
                        prayer.name.equals(nextJamatName, ignoreCase = true)
                val cardColor = if (isCurrent) GoldAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                val borderColor = if (isCurrent) GoldAccent else MaterialTheme.colorScheme.surfaceVariant

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = cardColor),
                    border = BorderStroke(if (isCurrent) 1.5.dp else 1.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = prayer.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Azan: ${prayer.azanTime}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Jamat: ${prayer.jamatTime}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }

                        // Edit Button visible only when logged in as Imam
                        if (isAdminLoggedIn) {
                            IconButton(
                                onClick = { editingPrayer = prayer },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Jamat Time",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Editing Prayer Times (Azan & Jamat)
    editingPrayer?.let { prayer ->
        var azanInput by remember { mutableStateOf(prayer.azanTime) }
        var jamatInput by remember { mutableStateOf(prayer.jamatTime) }

        AlertDialog(
            onDismissRequest = { editingPrayer = null },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSchedule(prayer.id, prayer.name, azanInput, jamatInput)
                        editingPrayer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Save Updates")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPrayer = null }) {
                    Text("Cancel", color = EmeraldGreen)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Update ${prayer.name} Schedule",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Tap any time block below to pick new hours & minutes:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    // Azan Time Picker
                    TimePickerClickableField(
                        label = "Azan Time",
                        value = azanInput,
                        onTimeSelected = { azanInput = it }
                    )

                    // Jamat Time Picker
                    TimePickerClickableField(
                        label = "Jamat Time",
                        value = jamatInput,
                        onTimeSelected = { jamatInput = it }
                    )
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
