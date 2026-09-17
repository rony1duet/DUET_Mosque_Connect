package com.duet.mosque.connect.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duet.mosque.connect.data.model.EidEntity
import com.duet.mosque.connect.data.model.RamadanEntity
import com.duet.mosque.connect.ui.components.DatePickerClickableField
import com.duet.mosque.connect.ui.components.TimePickerClickableField
import com.duet.mosque.connect.ui.theme.EmeraldGreen
import com.duet.mosque.connect.ui.theme.NoticeRed
import com.duet.mosque.connect.ui.theme.TextLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * =========================================================================================
 * ADMIN / IMAM EDIT & CREATION DIALOGS (DUET Mosque Connect)
 * =========================================================================================
 * These popup dialogs allow authenticated Imams/Admins to add or modify:
 *  - Announcements / News notices (NewsDialog)
 *  - Islamic events & lectures (EventDialog)
 *  - Janaza funeral announcements (JanazaDialog)
 *  - Eid prayer details & visibility toggles (EidDialog)
 *  - Daily Ramadan & Solar limits (FastingSolarDialog)
 *
 * Kotlin Concepts Explained for Beginners:
 *  1. `var titleInput by remember { mutableStateOf(initialTitle) }`:
 *     - `remember`: Keeps the variable in memory across screen redraws (recompositions).
 *     - `mutableStateOf`: Makes the variable observable. When changed, only the parts of UI
 *        reading it will automatically redraw.
 *     - `by` delegate: Allows direct reading and writing (`titleInput = "..."`) without `.value`.
 *
 *  2. `onConfirm: (String, String) -> Unit`:
 *     - A lambda parameter passed by the parent screen. When the user taps "Save", the dialog
 *       invokes this function to send the form data back to the ViewModel.
 *
 *  3. `AlertDialog`: Material Design 3 modal dialog with standard title, text body, and action buttons.
 * =========================================================================================
 */

/**
 * News / Announcement Dialog
 */
@Composable
fun NewsDialog(
    dialogTitle: String,
    initialTitle: String,
    initialContent: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var titleInput by remember { mutableStateOf(initialTitle) }
    var contentInput by remember { mutableStateOf(initialContent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(dialogTitle, fontWeight = FontWeight.Bold, color = EmeraldGreen)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Notice Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        focusedLabelColor = EmeraldGreen
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    label = { Text("Message Content") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        focusedLabelColor = EmeraldGreen
                    ),
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleInput.isNotBlank() && contentInput.isNotBlank()) {
                        onConfirm(titleInput, contentInput)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = EmeraldGreen)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Islamic Event Dialog
 */
@Composable
fun EventDialog(
    dialogTitle: String,
    initialTitle: String,
    initialDescription: String,
    initialDate: String,
    initialTime: String,
    initialLocation: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    val todayFormatted = remember {
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.US)
        formatter.format(Date())
    }
    var titleInput by remember { mutableStateOf(initialTitle) }
    var descInput by remember { mutableStateOf(initialDescription) }
    var dateInput by remember {
        mutableStateOf(if (initialDate.isBlank() || initialDate.equals("Today", ignoreCase = true)) todayFormatted else initialDate)
    }
    var timeInput by remember { mutableStateOf(initialTime) }
    var locationInput by remember { mutableStateOf(initialLocation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(dialogTitle, fontWeight = FontWeight.Bold, color = EmeraldGreen)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Event Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        focusedLabelColor = EmeraldGreen
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        focusedLabelColor = EmeraldGreen
                    ),
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePickerClickableField(
                        label = "Date",
                        value = dateInput,
                        onDateSelected = { dateInput = it },
                        modifier = Modifier.weight(1f),
                        accentColor = EmeraldGreen
                    )
                    TimePickerClickableField(
                        label = "Event Time",
                        value = if (timeInput.isBlank()) "05:00 PM" else timeInput,
                        onTimeSelected = { timeInput = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it },
                    label = { Text("Venue / Location") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        focusedLabelColor = EmeraldGreen
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleInput.isNotBlank() && descInput.isNotBlank()) {
                        onConfirm(titleInput, descInput, dateInput, timeInput, locationInput)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = EmeraldGreen)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Janaza Notice Dialog
 */
@Composable
fun JanazaDialog(
    dialogTitle: String,
    initialName: String,
    initialDate: String,
    initialTime: String,
    initialLocation: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    val todayFormatted = remember {
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.US)
        formatter.format(Date())
    }
    var nameInput by remember { mutableStateOf(initialName) }
    var dateInput by remember {
        mutableStateOf(if (initialDate.isBlank() || initialDate.equals("Today", ignoreCase = true)) todayFormatted else initialDate)
    }
    var timeInput by remember { mutableStateOf(initialTime) }
    var locationInput by remember { mutableStateOf(initialLocation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(dialogTitle, fontWeight = FontWeight.Bold, color = NoticeRed)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name of Deceased") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoticeRed,
                        focusedLabelColor = NoticeRed
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePickerClickableField(
                        label = "Date",
                        value = dateInput,
                        onDateSelected = { dateInput = it },
                        modifier = Modifier.weight(1f),
                        accentColor = NoticeRed
                    )
                    TimePickerClickableField(
                        label = "Janaza Time",
                        value = if (timeInput.isBlank()) "02:00 PM" else timeInput,
                        onTimeSelected = { timeInput = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it },
                    label = { Text("Janaza Location") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NoticeRed,
                        focusedLabelColor = NoticeRed
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        onConfirm(nameInput, dateInput, timeInput, locationInput)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NoticeRed)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NoticeRed)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Eid Schedule Dialog
 */
@Composable
fun EidDialog(
    currentEid: EidEntity?,
    onDismiss: () -> Unit,
    onConfirmEid: (String, String, String, String, Boolean) -> Unit
) {
    var prayerInput by remember { mutableStateOf(currentEid?.prayerTime ?: "07:30 AM") }
    var takbirInput by remember { mutableStateOf(currentEid?.takbirReminder ?: "Takbir begins at 07:15 AM") }
    var parkingInput by remember { mutableStateOf(currentEid?.parkingInfo ?: "Parking near central playground") }
    var noticeInput by remember { mutableStateOf(currentEid?.specialNotice ?: "Bring your own prayer mat.") }
    var isEnabledInput by remember { mutableStateOf(currentEid?.isEnabled == true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Eid Schedule", fontWeight = FontWeight.Bold, color = EmeraldGreen)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEnabledInput = !isEnabledInput },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEnabledInput) EmeraldGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, if (isEnabledInput) EmeraldGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (isEnabledInput) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = if (isEnabledInput) EmeraldGreen else NoticeRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isEnabledInput) "Visible to Students" else "Hidden from Students",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEnabledInput) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEnabledInput) "Eid sub tab is published" else "Eid sub tab is hidden",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Switch(
                            checked = isEnabledInput,
                            onCheckedChange = { isEnabledInput = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextLight,
                                checkedTrackColor = EmeraldGreen
                            )
                        )
                    }
                }

                TimePickerClickableField(
                    label = "Eid Prayer Time",
                    value = prayerInput,
                    onTimeSelected = { prayerInput = it }
                )
                OutlinedTextField(
                    value = takbirInput,
                    onValueChange = { takbirInput = it },
                    label = { Text("Takbir Notification") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        focusedLabelColor = EmeraldGreen
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = parkingInput,
                    onValueChange = { parkingInput = it },
                    label = { Text("Parking Info") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        focusedLabelColor = EmeraldGreen
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = noticeInput,
                    onValueChange = { noticeInput = it },
                    label = { Text("Special Notice") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        focusedLabelColor = EmeraldGreen
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmEid(prayerInput, takbirInput, parkingInput, noticeInput, isEnabledInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = EmeraldGreen)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Fasting & Solar Limits Dialog (Sehri, Iftar, Sunrise, Sunset)
 */
@Composable
fun FastingSolarDialog(
    currentRamadan: RamadanEntity?,
    onDismiss: () -> Unit,
    onConfirmRamadan: (String, String, String, String) -> Unit
) {
    var sehriInput by remember { mutableStateOf(currentRamadan?.sehriTime ?: "04:30 AM") }
    var iftarInput by remember { mutableStateOf(currentRamadan?.iftarTime ?: "06:45 PM") }
    var sunriseInput by remember { mutableStateOf(currentRamadan?.sunriseTime ?: "05:24 AM") }
    var sunsetInput by remember { mutableStateOf(currentRamadan?.sunsetTime ?: "06:46 PM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Fasting & Solar Limits", fontWeight = FontWeight.Bold, color = EmeraldGreen)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TimePickerClickableField(
                    label = "Sehri Time",
                    value = sehriInput,
                    onTimeSelected = { sehriInput = it }
                )
                TimePickerClickableField(
                    label = "Iftar Time",
                    value = iftarInput,
                    onTimeSelected = { iftarInput = it }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimePickerClickableField(
                        label = "Sunrise",
                        value = sunriseInput,
                        onTimeSelected = { sunriseInput = it },
                        modifier = Modifier.weight(1f)
                    )
                    TimePickerClickableField(
                        label = "Sunset",
                        value = sunsetInput,
                        onTimeSelected = { sunsetInput = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmRamadan(sehriInput, iftarInput, sunriseInput, sunsetInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = EmeraldGreen)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
