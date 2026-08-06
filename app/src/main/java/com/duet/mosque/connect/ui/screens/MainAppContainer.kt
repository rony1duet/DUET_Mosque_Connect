package com.duet.mosque.connect.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.duet.mosque.connect.data.model.NewsEntity
import com.duet.mosque.connect.data.model.EidEntity
import com.duet.mosque.connect.data.model.EventEntity
import com.duet.mosque.connect.data.model.JanazaEntity
import com.duet.mosque.connect.data.model.ScheduleEntity
import com.duet.mosque.connect.data.model.RamadanEntity
import com.duet.mosque.connect.ui.theme.CreamAccent
import com.duet.mosque.connect.ui.theme.EmeraldGreen
import com.duet.mosque.connect.ui.theme.EmeraldGreenDark
import com.duet.mosque.connect.ui.theme.EmeraldGreenLight
import com.duet.mosque.connect.ui.theme.GoldAccent
import com.duet.mosque.connect.ui.theme.GoldAccentLight
import com.duet.mosque.connect.ui.theme.NoticeRed
import com.duet.mosque.connect.ui.theme.NoticeRedLight
import com.duet.mosque.connect.ui.theme.SurfaceDark
import com.duet.mosque.connect.ui.theme.TextDark
import com.duet.mosque.connect.ui.theme.TextLight
import com.duet.mosque.connect.ui.viewmodel.MosqueViewModel
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class TabScreen(val route: String, val title: String) {
    object Home : TabScreen("home", "Home")
    object Prayer : TabScreen("prayer", "Prayer")
    object Qibla : TabScreen("qibla", "Qibla")
    object Events : TabScreen("events", "Updates")
    object Settings : TabScreen("settings", "Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: MosqueViewModel) {
    var currentTab by remember { mutableStateOf<TabScreen>(TabScreen.Home) }
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val context = LocalContext.current

    // Observe compass orientation only on the Qibla screen to optimize battery/sensors
    DisposableEffect(currentTab) {
        val isQiblaScreen = currentTab == TabScreen.Qibla
        viewModel.enableCompass(isQiblaScreen)
        onDispose {
            if (isQiblaScreen) {
                viewModel.enableCompass(false)
            }
        }
    }

    // GPS location & permissions updates
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val startupPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        viewModel.updateGPSLocation(location.latitude, location.longitude)
                    }
                }
            } catch (_: SecurityException) {}
        }
    }

    LaunchedEffect(Unit) {
        com.duet.mosque.connect.utils.NotificationHelper.createNotificationChannel(context)

        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        if (!fineLocationGranted || !notificationGranted) {
            val permissionsToRequest = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }.toTypedArray()
            startupPermissionLauncher.launch(permissionsToRequest)
        } else {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        viewModel.updateGPSLocation(location.latitude, location.longitude)
                    }
                }
            } catch (_: SecurityException) {}
        }
    }

    LaunchedEffect(currentTab) {
        if (currentTab == TabScreen.Qibla) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        viewModel.updateGPSLocation(location.latitude, location.longitude)
                    }
                }
            } else {
                startupPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                val items = listOf(
                    TabScreen.Home to Icons.Default.Home,
                    TabScreen.Prayer to Icons.Default.Schedule,
                    TabScreen.Qibla to Icons.Default.Explore,
                    TabScreen.Events to Icons.Default.Event,
                    TabScreen.Settings to Icons.Default.Settings
                )

                items.forEach { (tab, icon) ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = { Icon(icon, contentDescription = tab.title, modifier = Modifier.size(24.dp)) },
                        label = { Text(tab.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TextLight,
                            selectedTextColor = EmeraldGreen,
                            indicatorColor = EmeraldGreen,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                TabScreen.Home -> HomeScreen(viewModel, onNavigateToTab = { currentTab = it })
                TabScreen.Prayer -> ScheduleScreen(viewModel)
                TabScreen.Qibla -> QiblaCompassScreen(viewModel)
                TabScreen.Events -> EventsAndNoticesScreen(viewModel)
                TabScreen.Settings -> SettingsAndImamScreen(viewModel)
            }
        }
    }
}

// 1. HOME SCREEN
@Composable
fun HomeScreen(viewModel: MosqueViewModel, onNavigateToTab: (TabScreen) -> Unit) {
    val countdown by viewModel.countdownTimer.collectAsState()
    val nextJamatName by viewModel.nextJamatName.collectAsState()
    val nextJamatTime by viewModel.nextJamatTime.collectAsState()
    val currentPrayerName by viewModel.currentPrayerName.collectAsState()
    val prayers by viewModel.schedules.collectAsState()
    val notices by viewModel.news.collectAsState()
    val compassState by viewModel.compassState.collectAsState()
    val ramadan by viewModel.ramadanSchedule.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    var showRamadanEditDialog by remember { mutableStateOf(false) }

    val currentDate = remember {
        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US)
        formatter.format(Date())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-Fidelity Mosque Curved Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(EmeraldGreen, EmeraldGreenDark)
                        )
                    )
                    .padding(top = 28.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
            ) {
                // Background decorative glowing circles
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 30.dp, y = (-40).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = (-30).dp, y = 30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title and location
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DUET Central Mosque",
                                color = TextLight,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Gazipur, Bangladesh",
                                color = TextLight.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Elegant geometric emblem representation
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Next Jamat Countdown Banner
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldAccent)
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Next Jamat",
                            color = EmeraldGreenDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$nextJamatName Prayer",
                        color = TextLight,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-1).sp
                    )

                    Text(
                        text = countdown.substringBefore(" "),
                        color = TextLight,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Jamat at $nextJamatTime",
                            color = TextLight,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 2. DAILY PRAYERS HORIZONTAL ROW CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Prayer Times",
                            color = EmeraldGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = currentDate,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5-Column Prayer Grid Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val displayPrayers = prayers.filter { it.id != "jummah" }
                        displayPrayers.forEach { prayer ->
                            val isActive = prayer.name.equals(currentPrayerName, ignoreCase = true) || prayer.name.equals(nextJamatName, ignoreCase = true)
                            val cardBg = if (isActive) GoldAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.background
                            val borderCol = if (isActive) GoldAccent else Color.Transparent
                            val borderWidth = if (isActive) 2.dp else 1.dp
                            val textCol = if (isActive) EmeraldGreen else MaterialTheme.colorScheme.onSurface

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cardBg)
                                    .border(
                                        borderWidth,
                                        if (isActive) borderCol else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(vertical = 12.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = prayer.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (isActive) EmeraldGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = prayer.jamatTime,
                                    fontSize = 10.sp,
                                    fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                                    color = textCol,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fasting & Solar Limits Card
        ramadan?.let { r ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Fasting & Solar Limits",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                                Text(
                                    text = "DUET Central Mosque Daily Schedule",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            if (isAdminLoggedIn) {
                                IconButton(
                                    onClick = { showRamadanEditDialog = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Fasting & Solar Limits",
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Sehri
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 10.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Sehri",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = r.sehriTime,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            // Iftar
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 10.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Iftar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = r.iftarTime,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            // Sunrise
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 10.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Sunrise",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = r.sunriseTime,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            // Sunset
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 10.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Sunset",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = r.sunsetTime,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. LATEST NOTICE CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NoticeRed)
                        )
                        Text(
                            text = "Latest Notice",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val latestNotice = notices.firstOrNull()?.content ?: "No notices published yet."
                    Text(
                        text = latestNotice,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background)
                                .clickable { onNavigateToTab(TabScreen.Events) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Extra padding at the end
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showRamadanEditDialog) {
        FastingSolarDialog(
            currentRamadan = ramadan,
            onDismiss = { showRamadanEditDialog = false },
            onConfirmRamadan = { sehri, iftar, sunrise, sunset ->
                viewModel.updateRamadanSchedule(
                    sehri = sehri,
                    iftar = iftar,
                    notes = ramadan?.notes ?: "DUET Mosque Schedule",
                    sunrise = sunrise,
                    sunset = sunset
                )
                showRamadanEditDialog = false
            }
        )
    }
}

// 2. PRAYER TIMES SCREEN
@Composable
fun ScheduleScreen(viewModel: MosqueViewModel) {
    val context = LocalContext.current
    val prayers by viewModel.schedules.collectAsState()
    val countdown by viewModel.countdownTimer.collectAsState()
    val nextJamatName by viewModel.nextJamatName.collectAsState()
    val currentPrayerName by viewModel.currentPrayerName.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    var editingPrayer by remember { mutableStateOf<ScheduleEntity?>(null) }

    fun parseTimeString(timeStr: String): Pair<Int, Int> {
        return try {
            val parser = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
            val date = parser.parse(timeStr)
            if (date != null) {
                val cal = java.util.Calendar.getInstance().apply { time = date }
                Pair(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
            } else {
                Pair(12, 0)
            }
        } catch (e: Exception) {
            Pair(12, 0)
        }
    }

    fun formatTimeString(hourOfDay: Int, minute: Int): String {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
            set(java.util.Calendar.MINUTE, minute)
        }
        val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
        return formatter.format(cal.time)
    }

    fun showTimePicker(initialTime: String, onTimeSelected: (String) -> Unit) {
        val (hour, minute) = parseTimeString(initialTime)
        android.app.TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                onTimeSelected(formatTimeString(selectedHour, selectedMinute))
            },
            hour,
            minute,
            false
        ).show()
    }

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

        // Time Banner Card
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

        // Prayer List - Fit single page without scrolling
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            prayers.forEach { prayer ->
                val isCurrent = prayer.name.equals(currentPrayerName, ignoreCase = true) || prayer.name.equals(nextJamatName, ignoreCase = true)
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

    // Edit Prayer Times Dialog
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

                    // Azan Time Selector
                    TimePickerClickableField(
                        label = "Azan Time",
                        value = azanInput,
                        onTimeSelected = { azanInput = it }
                    )

                    // Jamat Time Selector
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

// 3. QIBLA SCREEN
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(GoldAccent)
            )
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

@Composable
fun QiblaCompassScreen(viewModel: MosqueViewModel) {
    val compassState by viewModel.compassState.collectAsState()
    val context = LocalContext.current

    val isAligned = compassState.hasCompassSensor && (compassState.relativeAngle < 5f || compassState.relativeAngle > 355f)

    var lastAzimuth by remember { mutableStateOf(compassState.azimuth) }
    var continuousAzimuth by remember { mutableStateOf(compassState.azimuth) }
    LaunchedEffect(compassState.azimuth) {
        val current = compassState.azimuth
        var delta = current - lastAzimuth
        while (delta < -180f) delta += 360f
        while (delta > 180f) delta -= 360f
        continuousAzimuth += delta
        lastAzimuth = current
    }
    val animatedNorthAngle by animateFloatAsState(targetValue = -continuousAzimuth)

    var lastQibla by remember { mutableStateOf(compassState.relativeAngle) }
    var continuousQibla by remember { mutableStateOf(compassState.relativeAngle) }
    LaunchedEffect(compassState.relativeAngle) {
        val current = compassState.relativeAngle
        var delta = current - lastQibla
        while (delta < -180f) delta += 360f
        while (delta > 180f) delta -= 360f
        continuousQibla += delta
        lastQibla = current
    }
    val animatedQiblaAngle by animateFloatAsState(targetValue = continuousQibla)

    var wasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !wasAligned) {
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
                                50,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(50)
                    }
                }
            } catch (e: Exception) {
            }
        }
        wasAligned = isAligned
    }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
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

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isAligned) EmeraldGreen.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isAligned) "Perfectly Aligned with Qibla" else "Align your phone to find the Qibla",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAligned) EmeraldGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        if (!compassState.hasCompassSensor) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Magnetic Sensor Missing",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoticeRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your device lacks a physical compass sensor. Displaying static angle bearing reference relative to North instead.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isAligned) {
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        EmeraldGreen.copy(alpha = pulseAlpha),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2 - 16f

                    drawCircle(
                        color = if (isAligned) GoldAccent else EmeraldGreen.copy(alpha = 0.4f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = if (isAligned) 4.dp.toPx() else 2.dp.toPx())
                    )

                    drawCircle(
                        color = EmeraldGreen.copy(alpha = 0.1f),
                        radius = radius - 12.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    for (angle in 0 until 360 step 30) {
                        val isMajor = angle % 90 == 0
                        val tickLength = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
                        val tickWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                        val tickColor = if (isMajor) EmeraldGreen.copy(alpha = 0.6f) else EmeraldGreen.copy(alpha = 0.25f)

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

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedNorthAngle),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "N",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = NoticeRed,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 22.dp)
                        )
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

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.minDimension / 2 - 16f

                        val northPointer = Path().apply {
                            moveTo(center.x, center.y - radius + 40.dp.toPx())
                            lineTo(center.x - 5.dp.toPx(), center.y - 45.dp.toPx())
                            lineTo(center.x + 5.dp.toPx(), center.y - 45.dp.toPx())
                            close()
                        }
                        drawPath(northPointer, color = Color.Gray.copy(alpha = 0.35f))

                        val southPointer = Path().apply {
                            moveTo(center.x, center.y + radius - 40.dp.toPx())
                            lineTo(center.x - 5.dp.toPx(), center.y + 45.dp.toPx())
                            lineTo(center.x + 5.dp.toPx(), center.y + 45.dp.toPx())
                            close()
                        }
                        drawPath(southPointer, color = Color.Gray.copy(alpha = 0.15f))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedQiblaAngle),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.minDimension / 2 - 16f

                        val outerNeedle = Path().apply {
                            moveTo(center.x, center.y - radius + 15.dp.toPx())
                            lineTo(center.x - 13.dp.toPx(), center.y - radius + 38.dp.toPx())
                            lineTo(center.x - 4.dp.toPx(), center.y - radius + 33.dp.toPx())
                            lineTo(center.x - 3.dp.toPx(), center.y - 24.dp.toPx())
                            lineTo(center.x + 3.dp.toPx(), center.y - 24.dp.toPx())
                            lineTo(center.x + 4.dp.toPx(), center.y - radius + 33.dp.toPx())
                            lineTo(center.x + 13.dp.toPx(), center.y - radius + 38.dp.toPx())
                            close()
                        }
                        drawPath(outerNeedle, color = GoldAccent)

                        val innerNeedle = Path().apply {
                            moveTo(center.x, center.y - radius + 21.dp.toPx())
                            lineTo(center.x - 8.dp.toPx(), center.y - radius + 35.dp.toPx())
                            lineTo(center.x - 1.5.dp.toPx(), center.y - radius + 31.dp.toPx())
                            lineTo(center.x - 1.5.dp.toPx(), center.y - 24.dp.toPx())
                            lineTo(center.x + 1.5.dp.toPx(), center.y - 24.dp.toPx())
                            lineTo(center.x + 1.5.dp.toPx(), center.y - radius + 31.dp.toPx())
                            lineTo(center.x + 8.dp.toPx(), center.y - radius + 35.dp.toPx())
                            close()
                        }
                        drawPath(innerNeedle, color = EmeraldGreen)

                        drawCircle(
                            color = GoldAccent,
                            radius = 2.5.dp.toPx(),
                            center = Offset(center.x, center.y - radius + 48.dp.toPx())
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .border(
                            width = if (isAligned) 2.dp else 1.dp,
                            color = if (isAligned) GoldAccent else EmeraldGreen.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    KaabaIcon()
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%.0f° W", compassState.bearingToKaaba),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                    Text(
                        text = "Qibla Angle",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%,.0f km", compassState.distanceToKaabaKm),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                    Text(
                        text = "Distance",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%.0f°", compassState.azimuth),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                    Text(
                        text = "Your Heading",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// 4. EVENTS SCREEN (Announcements, Events, Janaza, Ramadan & Eid Sub-Tabs)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsAndNoticesScreen(viewModel: MosqueViewModel) {
    val eid by viewModel.eidSchedule.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val isEidEnabled = eid?.isEnabled == true

    var activeTab by remember { mutableIntStateOf(0) }

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
            Box(modifier = Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.Center) {
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
            Box(modifier = Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.Center) {
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
            Box(modifier = Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.Center) {
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
            Box(modifier = Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.Center) {
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
                                        Text(text = "Eid Prayer Time", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                        Text(text = e.prayerTime, fontSize = 20.sp, fontWeight = FontWeight.Black, color = EmeraldGreen)
                                    }
                                    Text(
                                        text = e.takbirReminder,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.weight(1f).padding(start = 16.dp),
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

// 5. SETTINGS AND IMAM PANEL
@Composable
fun SettingsAndImamScreen(viewModel: MosqueViewModel) {
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val lockoutSeconds by viewModel.lockoutSeconds.collectAsState()
    val notificationLogs by viewModel.notificationLogs.collectAsState()

    val jamatReminders by viewModel.jamatRemindersEnabled.collectAsState()
    val adhanSound by viewModel.adhanSoundEnabled.collectAsState()
    val eventNotices by viewModel.eventNoticesEnabled.collectAsState()

    var showLoginDialog by remember { mutableStateOf(false) }
    var showChangeKeyDialog by remember { mutableStateOf(false) }
    var showAdminLogsPage by remember { mutableStateOf(false) }

    if (showAdminLogsPage && isAdminLoggedIn) {
        AdminNotificationLogsScreen(
            viewModel = viewModel,
            onBack = { showAdminLogsPage = false }
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Settings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                    Text(
                        text = "App preferences, notification controls & Imam portal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Notification Preferences Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Notification Preferences",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Jamat Reminders Toggle
                        SettingToggleRow(
                            title = "Jamat Prayer Alerts",
                            description = "Receive push audio & vibration reminder before Jamat",
                            checked = jamatReminders,
                            onCheckedChange = { viewModel.setJamatReminders(it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Adhan Audio Sound Toggle
                        SettingToggleRow(
                            title = "Adhan Audio Alert",
                            description = "Trigger full Adhan audio chime at Azan times",
                            checked = adhanSound,
                            onCheckedChange = { viewModel.setAdhanSound(it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Event Notices Toggle
                        SettingToggleRow(
                            title = "Mosque Updates & Janaza Alerts",
                            description = "Receive broadcast push alerts for new announcements & Janaza",
                            checked = eventNotices,
                            onCheckedChange = { viewModel.setEventNotices(it) }
                        )
                    }
                }
            }

            // Role & Access Security Card (Redesigned for seamless Dark & White theme compatibility)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isAdminLoggedIn) EmeraldGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAdminLoggedIn) Icons.Default.Security else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Admin Portal & Security",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }

                            // Status Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isAdminLoggedIn) EmeraldGreen.copy(alpha = 0.15f) else NoticeRed.copy(alpha = 0.1f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (isAdminLoggedIn) "ADMIN ACTIVE" else "STUDENT GUEST",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdminLoggedIn) EmeraldGreen else NoticeRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isAdminLoggedIn) {
                                "Authenticated as Imam. You hold access to modify Jamat schedules, post announcements, broadcast alerts, and manage Ramadan/Eid timings."
                            } else {
                                "Standard Student View. Imam credentials are required to edit Jamat schedules or publish updates."
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (isAdminLoggedIn) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showChangeKeyDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, EmeraldGreen)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Key,
                                                contentDescription = null,
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Change Key", fontSize = 11.sp, color = EmeraldGreen)
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.logoutImam() },
                                        colors = ButtonDefaults.buttonColors(containerColor = NoticeRed),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Logout", fontSize = 11.sp, color = Color.White)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showAdminLogsPage = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, EmeraldGreen)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = null,
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "View Push Broadcast Logs",
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldGreen,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = GoldAccent.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = "${notificationLogs.size} logs",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = EmeraldGreenDark,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { showLoginDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = TextLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Imam Login", fontSize = 13.sp, color = TextLight)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Login Dialog with Rate Limiting & Password Toggle
    if (showLoginDialog) {
        var passwordInput by remember { mutableStateOf("") }
        var isPasswordVisible by remember { mutableStateOf(false) }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.loginAsImam(passwordInput)) {
                            showLoginDialog = false
                        } else {
                            isError = true
                        }
                    },
                    enabled = lockoutSeconds == 0,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Unlock Panel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) {
                    Text("Cancel", color = EmeraldGreen)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Imam Authentication", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter secure access key to modify jamat schedules or post announcements.",
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            isError = false
                        },
                        label = { Text("Access Key") },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = EmeraldGreen
                                )
                            }
                        },
                        isError = isError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            focusedLabelColor = EmeraldGreen
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (lockoutSeconds > 0) {
                        Text(
                            text = "Too many failed attempts. Locked for $lockoutSeconds seconds.",
                            color = NoticeRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (isError) {
                        Text(
                            text = "Invalid passcode. Please check credentials.",
                            color = NoticeRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Change Access Key Dialog
    if (showChangeKeyDialog) {
        var currentKeyInput by remember { mutableStateOf("") }
        var newKeyInput by remember { mutableStateOf("") }
        var confirmKeyInput by remember { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var successMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangeKeyDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (newKeyInput != confirmKeyInput) {
                            errorMessage = "New keys do not match."
                            return@Button
                        }
                        val result = viewModel.changeAdminPasscode(currentKeyInput, newKeyInput)
                        if (result == null) {
                            errorMessage = null
                            successMessage = "Access key updated successfully!"
                            showChangeKeyDialog = false
                        } else {
                            errorMessage = result
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Save Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeKeyDialog = false }) {
                    Text("Cancel", color = EmeraldGreen)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Access Key", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currentKeyInput,
                        onValueChange = { currentKeyInput = it; errorMessage = null },
                        label = { Text("Current Access Key") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newKeyInput,
                        onValueChange = { newKeyInput = it; errorMessage = null },
                        label = { Text("New Access Key (min 4 chars)") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmKeyInput,
                        onValueChange = { confirmKeyInput = it; errorMessage = null },
                        label = { Text("Confirm New Key") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = EmeraldGreen
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen),
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorMessage?.let { err ->
                        Text(text = err, color = NoticeRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// Dedicated Admin-Only Notification Logs Screen
@Composable
fun AdminNotificationLogsScreen(
    viewModel: MosqueViewModel,
    onBack: () -> Unit
) {
    val notificationLogs by viewModel.notificationLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onBack() }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = EmeraldGreen.copy(alpha = 0.12f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Push Broadcast Logs",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                    Text(
                        text = "Admin View • FCM & Broadcast History",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if (notificationLogs.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearNotificationLogs() },
                    colors = ButtonDefaults.textButtonColors(contentColor = NoticeRed)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = NoticeRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear Logs",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Channel Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GoldAccent,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = EmeraldGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "FCM Notification Engine",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                        Text(
                            text = "Jamat Updates & System Alerts Log",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextLight.copy(alpha = 0.8f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GoldAccent.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "${notificationLogs.size} SENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (notificationLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldGreen.copy(alpha = 0.12f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Broadcast Logs Yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "When Jamat schedules are updated or announcements and Janaza notices are published, system push logs will appear here.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notificationLogs) { log ->
                    val icon = when {
                        log.title.contains("Jamat", ignoreCase = true) || log.title.contains("Schedule", ignoreCase = true) -> Icons.Default.Schedule
                        log.title.contains("Janaza", ignoreCase = true) -> Icons.Default.Warning
                        log.title.contains("Event", ignoreCase = true) -> Icons.Default.Event
                        else -> Icons.Default.Notifications
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.18f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = EmeraldGreen.copy(alpha = 0.12f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = log.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                }

                                val formattedTime = remember(log.timestamp) {
                                    val formatter = SimpleDateFormat("hh:mm:ss a", Locale.US)
                                    formatter.format(Date(log.timestamp))
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldGreen.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = formattedTime,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = log.body,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GoldAccent.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "FCM STATUS: DELIVERED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldGreenDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "System Alert ID #${log.timestamp.toString().takeLast(6)}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimePickerClickableField(
    label: String,
    value: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    fun parseTimeString(timeStr: String): Pair<Int, Int> {
        return try {
            val parser = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
            val date = parser.parse(timeStr)
            if (date != null) {
                val cal = java.util.Calendar.getInstance().apply { time = date }
                Pair(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
            } else {
                Pair(12, 0)
            }
        } catch (e: Exception) {
            Pair(12, 0)
        }
    }

    fun formatTimeString(hourOfDay: Int, minute: Int): String {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
            set(java.util.Calendar.MINUTE, minute)
        }
        val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
        return formatter.format(cal.time)
    }

    fun launchPicker() {
        val (hour, minute) = parseTimeString(value)
        android.app.TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                onTimeSelected(formatTimeString(selectedHour, selectedMinute))
            },
            hour,
            minute,
            false
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
        val cal = java.util.Calendar.getInstance()
        return try {
            val parser = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US)
            val date = parser.parse(dateStr)
            if (date != null) {
                cal.time = date
            }
            Triple(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH))
        } catch (e: Exception) {
            Triple(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH))
        }
    }

    fun formatDateString(year: Int, month: Int, dayOfMonth: Int): String {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.MONTH, month)
            set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        val formatter = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US)
        return formatter.format(cal.time)
    }

    fun launchPicker() {
        val (year, month, day) = parseDateString(value)
        android.app.DatePickerDialog(
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

// 6. UPDATE DIALOGS FOR IMAM EDIT & ADD
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen, focusedLabelColor = EmeraldGreen),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    label = { Text("Message Content") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen, focusedLabelColor = EmeraldGreen),
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
        val formatter = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US)
        formatter.format(java.util.Date())
    }
    var titleInput by remember { mutableStateOf(initialTitle) }
    var descInput by remember { mutableStateOf(initialDescription) }
    var dateInput by remember { mutableStateOf(if (initialDate.isBlank() || initialDate.equals("Today", ignoreCase = true)) todayFormatted else initialDate) }
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen, focusedLabelColor = EmeraldGreen),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen, focusedLabelColor = EmeraldGreen),
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen, focusedLabelColor = EmeraldGreen),
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
        val formatter = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US)
        formatter.format(java.util.Date())
    }
    var nameInput by remember { mutableStateOf(initialName) }
    var dateInput by remember { mutableStateOf(if (initialDate.isBlank() || initialDate.equals("Today", ignoreCase = true)) todayFormatted else initialDate) }
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NoticeRed, focusedLabelColor = NoticeRed),
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NoticeRed, focusedLabelColor = NoticeRed),
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen, focusedLabelColor = EmeraldGreen),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = parkingInput,
                    onValueChange = { parkingInput = it },
                    label = { Text("Parking Info") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen, focusedLabelColor = EmeraldGreen),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = noticeInput,
                    onValueChange = { noticeInput = it },
                    label = { Text("Special Notice") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldGreen, focusedLabelColor = EmeraldGreen),
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


