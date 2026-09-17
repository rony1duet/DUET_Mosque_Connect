package com.duet.mosque.connect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duet.mosque.connect.data.model.EidEntity
import com.duet.mosque.connect.data.model.EventEntity
import com.duet.mosque.connect.data.model.JanazaEntity
import com.duet.mosque.connect.data.model.NewsEntity
import com.duet.mosque.connect.ui.dialogs.EidDialog
import com.duet.mosque.connect.ui.dialogs.EventDialog
import com.duet.mosque.connect.ui.dialogs.JanazaDialog
import com.duet.mosque.connect.ui.dialogs.NewsDialog
import com.duet.mosque.connect.ui.theme.EmeraldGreen
import com.duet.mosque.connect.ui.theme.GoldAccent
import com.duet.mosque.connect.ui.theme.NoticeRed
import com.duet.mosque.connect.ui.theme.TextLight
import com.duet.mosque.connect.ui.viewmodel.MosqueViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * =========================================================================================
 * 4. UPDATES & NOTICES SCREEN (DUET Mosque Connect)
 * =========================================================================================
 * A multi-tab information hub for the campus mosque community containing 4 sub-tabs:
 *  1. News: General administrative notices and announcements.
 *  2. Events: Islamic lectures, Halaqas, and special campus gatherings.
 *  3. Janaza: Funeral announcements with urgent Red badges and timings.
 *  4. Eid: Eid ul-Fitr / Eid ul-Adha prayer times, parking guidance, and notices (toggleable).
 *
 * Kotlin Concepts Explained for Beginners:
 *  - Dynamic Tab List: `remember(isEidEnabled, isAdminLoggedIn) { ... }` re-evaluates the tab list
 *    only when Eid visibility or admin login status changes.
 *  - `items(list) { item -> ... }`: LazyColumn builder that dynamically produces cards for each database row.
 * =========================================================================================
 */
@Composable
fun EventsAndNoticesScreen(viewModel: MosqueViewModel) {
    val eid by viewModel.eidSchedule.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val isEidEnabled = eid?.isEnabled == true

    var activeTab by remember { mutableIntStateOf(0) }

    // Dynamically show Eid tab if enabled by Imam, or if Admin is logged in
    val tabNames = remember(isEidEnabled, isAdminLoggedIn) {
        if (isEidEnabled || isAdminLoggedIn) {
            listOf("News", "Events", "Janaza", "Eid")
        } else {
            listOf("News", "Events", "Janaza")
        }
    }

    LaunchedEffect(tabNames.size) {
        if (activeTab >= tabNames.size) {
            activeTab = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        Text(
            text = "Updates",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldGreen,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = "DUET Mosque Connect",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Sub-Tab Switcher Pill Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabNames.forEachIndexed { index, name ->
                val isSelected = activeTab == index
                val selectedColor = if (name == "Janaza") NoticeRed else EmeraldGreen
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) selectedColor else Color.Transparent)
                        .clickable { activeTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
        }

        // Active Sub-Tab Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val selectedTabName = tabNames.getOrNull(activeTab) ?: "News"
            when (selectedTabName) {
                "News" -> NewsTab(viewModel)
                "Events" -> EventsTab(viewModel)
                "Janaza" -> JanazaTab(viewModel)
                "Eid" -> EidTab(viewModel)
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 1. NEWS TAB
// -----------------------------------------------------------------------------------------
@Composable
fun NewsTab(viewModel: MosqueViewModel) {
    val newsList by viewModel.news.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingNotice by remember { mutableStateOf<NewsEntity?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isAdminLoggedIn) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = TextLight)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add New Notice / News", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (newsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No announcements published.",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(newsList) { notice ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notice.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isAdminLoggedIn) {
                                    Row {
                                        IconButton(
                                            onClick = { editingNotice = notice },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { viewModel.deleteNews(notice.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = NoticeRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = notice.content,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val formattedDate = remember(notice.timestamp) {
                                val formatter = SimpleDateFormat("MMM d, yyyy - hh:mm a", Locale.US)
                                formatter.format(Date(notice.timestamp))
                            }
                            Text(
                                text = formattedDate,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        NewsDialog(
            dialogTitle = "Add News",
            initialTitle = "",
            initialContent = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { title, content ->
                viewModel.addNews(title, content)
                showAddDialog = false
            }
        )
    }

    editingNotice?.let { notice ->
        NewsDialog(
            dialogTitle = "Edit News",
            initialTitle = notice.title,
            initialContent = notice.content,
            onDismiss = { editingNotice = null },
            onConfirm = { title, content ->
                viewModel.deleteNews(notice.id)
                viewModel.addNews(title, content)
                editingNotice = null
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// 2. EVENTS TAB
// -----------------------------------------------------------------------------------------
@Composable
fun EventsTab(viewModel: MosqueViewModel) {
    val events by viewModel.events.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<EventEntity?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isAdminLoggedIn) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = TextLight)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add New Event", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No events scheduled.",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = event.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isAdminLoggedIn) {
                                    Row {
                                        IconButton(
                                            onClick = { editingEvent = event },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { viewModel.deleteEvent(event.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = NoticeRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = event.description,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Date: ${event.date}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "Time: ${event.time}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                Text(
                                    text = "Venue: ${event.location}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen,
                                    modifier = Modifier.align(Alignment.Bottom)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        EventDialog(
            dialogTitle = "Add New Event",
            initialTitle = "",
            initialDescription = "",
            initialDate = "Today",
            initialTime = "05:00 PM",
            initialLocation = "DUET Central Mosque",
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, date, time, loc ->
                viewModel.addEvent(title, desc, date, time, loc)
                showAddDialog = false
            }
        )
    }

    editingEvent?.let { event ->
        EventDialog(
            dialogTitle = "Edit Event",
            initialTitle = event.title,
            initialDescription = event.description,
            initialDate = event.date,
            initialTime = event.time,
            initialLocation = event.location,
            onDismiss = { editingEvent = null },
            onConfirm = { title, desc, date, time, loc ->
                viewModel.deleteEvent(event.id)
                viewModel.addEvent(title, desc, date, time, loc)
                editingEvent = null
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// 3. JANAZA TAB
// -----------------------------------------------------------------------------------------
@Composable
fun JanazaTab(viewModel: MosqueViewModel) {
    val janazas by viewModel.janazaNotices.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingJanaza by remember { mutableStateOf<JanazaEntity?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isAdminLoggedIn) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NoticeRed)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = TextLight)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Janaza Notice", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (janazas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Janaza notices published.",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(janazas) { janaza ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Janaza Notice: ${janaza.name}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NoticeRed,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isAdminLoggedIn) {
                                    Row {
                                        IconButton(
                                            onClick = { editingJanaza = janaza },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { viewModel.deleteJanaza(janaza.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = NoticeRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Date: ${janaza.date}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "Time: ${janaza.time}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                }
                                Text(
                                    text = "Venue: ${janaza.location}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.align(Alignment.Bottom)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        JanazaDialog(
            dialogTitle = "Add Janaza Notice",
            initialName = "",
            initialDate = "Today",
            initialTime = "02:00 PM",
            initialLocation = "DUET Central Mosque Premises",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, date, time, loc ->
                viewModel.addJanaza(name, date, time, loc)
                showAddDialog = false
            }
        )
    }

    editingJanaza?.let { janaza ->
        JanazaDialog(
            dialogTitle = "Edit Janaza Notice",
            initialName = janaza.name,
            initialDate = janaza.date,
            initialTime = janaza.time,
            initialLocation = janaza.location,
            onDismiss = { editingJanaza = null },
            onConfirm = { name, date, time, loc ->
                viewModel.deleteJanaza(janaza.id)
                viewModel.addJanaza(name, date, time, loc)
                editingJanaza = null
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// 4. EID TAB
// -----------------------------------------------------------------------------------------
@Composable
fun EidTab(viewModel: MosqueViewModel) {
    val eid by viewModel.eidSchedule.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isAdminLoggedIn) {
            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = TextLight)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Eid Schedule", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (eid == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Eid schedule published.",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                eid?.let { e ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Eid Schedule",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isAdminLoggedIn) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (e.isEnabled) EmeraldGreen.copy(alpha = 0.12f) else NoticeRed.copy(alpha = 0.12f),
                                                modifier = Modifier.padding(end = 6.dp)
                                            ) {
                                                Text(
                                                    text = if (e.isEnabled) "Visible" else "Hidden",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (e.isEnabled) EmeraldGreen else NoticeRed,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GoldAccent.copy(alpha = 0.15f))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Eid Prayer Time",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                        Text(
                                            text = e.prayerTime,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = EmeraldGreen
                                        )
                                    }
                                    Text(
                                        text = e.takbirReminder,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 16.dp),
                                        textAlign = TextAlign.End
                                    )
                                }

                                if (e.parkingInfo.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Parking Info",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = e.parkingInfo,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }

                                if (e.specialNotice.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Special Notice",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NoticeRed
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = e.specialNotice,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EidDialog(
            currentEid = eid,
            onDismiss = { showEditDialog = false },
            onConfirmEid = { prayer, takbir, parking, notice, isEnabled ->
                viewModel.updateEidSchedule(prayer, takbir, parking, notice, isEnabled)
                showEditDialog = false
            }
        )
    }
}
