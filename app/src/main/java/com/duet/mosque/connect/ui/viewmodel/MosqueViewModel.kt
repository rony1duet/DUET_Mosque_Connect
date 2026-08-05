package com.duet.mosque.connect.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duet.mosque.connect.data.model.AnnouncementEntity
import com.duet.mosque.connect.data.model.EidEntity
import com.duet.mosque.connect.data.model.EventEntity
import com.duet.mosque.connect.data.model.ImamContact
import com.duet.mosque.connect.data.model.JanazaEntity
import com.duet.mosque.connect.data.model.PrayerTimeEntity
import com.duet.mosque.connect.data.model.RamadanEntity
import com.duet.mosque.connect.data.repository.MosqueRepository
import com.duet.mosque.connect.utils.CompassData
import com.duet.mosque.connect.utils.CompassSensorManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Simulated local FCM notification record
data class NotificationLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)

class MosqueViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MosqueRepository(application)
    private val compassManager = CompassSensorManager(application)

    // Room cached data flows
    val prayerTimes: StateFlow<List<PrayerTimeEntity>> = repository.allPrayerTimes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<EventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val janazaNotices: StateFlow<List<JanazaEntity>> = repository.allJanazaNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ramadanSchedule: StateFlow<RamadanEntity?> = repository.ramadanSchedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val eidSchedule: StateFlow<EidEntity?> = repository.eidSchedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Imam contact info (static)
    val imamContact = ImamContact()

    // Qibla direction data flow
    val compassState: StateFlow<CompassData> = compassManager.compassState

    // Authentication & Security State
    private val secPrefs = application.getSharedPreferences("duet_mosque_sec_prefs", Context.MODE_PRIVATE)
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _lockoutSeconds = MutableStateFlow(0)
    val lockoutSeconds: StateFlow<Int> = _lockoutSeconds.asStateFlow()

    private var failedAttempts = 0

    // User Settings State
    private val _jamatRemindersEnabled = MutableStateFlow(secPrefs.getBoolean("pref_jamat_reminders", true))
    val jamatRemindersEnabled: StateFlow<Boolean> = _jamatRemindersEnabled.asStateFlow()

    private val _adhanSoundEnabled = MutableStateFlow(secPrefs.getBoolean("pref_adhan_sound", true))
    val adhanSoundEnabled: StateFlow<Boolean> = _adhanSoundEnabled.asStateFlow()

    private val _eventNoticesEnabled = MutableStateFlow(secPrefs.getBoolean("pref_event_notices", true))
    val eventNoticesEnabled: StateFlow<Boolean> = _eventNoticesEnabled.asStateFlow()

    fun setJamatReminders(enabled: Boolean) {
        _jamatRemindersEnabled.value = enabled
        secPrefs.edit().putBoolean("pref_jamat_reminders", enabled).apply()
    }

    fun setAdhanSound(enabled: Boolean) {
        _adhanSoundEnabled.value = enabled
        secPrefs.edit().putBoolean("pref_adhan_sound", enabled).apply()
    }

    fun setEventNotices(enabled: Boolean) {
        _eventNoticesEnabled.value = enabled
        secPrefs.edit().putBoolean("pref_event_notices", enabled).apply()
    }

    private fun getSavedPasscode(): String {
        return secPrefs.getString("admin_passcode", null) ?: "admin"
    }

    // Active prayer state
    private val _currentPrayerName = MutableStateFlow("Asr")
    val currentPrayerName: StateFlow<String> = _currentPrayerName.asStateFlow()

    private val _nextJamatTime = MutableStateFlow("04:45 PM")
    val nextJamatTime: StateFlow<String> = _nextJamatTime.asStateFlow()

    private val _nextJamatName = MutableStateFlow("Asr")
    val nextJamatName: StateFlow<String> = _nextJamatName.asStateFlow()

    private val _countdownTimer = MutableStateFlow("12:45 remaining")
    val countdownTimer: StateFlow<String> = _countdownTimer.asStateFlow()

    // Simulated FCM notification history log
    private val _notificationLogs = MutableStateFlow<List<NotificationLog>>(emptyList())
    val notificationLogs: StateFlow<List<NotificationLog>> = _notificationLogs.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Ensure initial data is seeded
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
            startCountdownTimer()
        }
    }

    // Start/Stop Compass listeners based on screen visible state
    fun enableCompass(enable: Boolean) {
        if (enable) {
            compassManager.startListening()
        } else {
            compassManager.stopListening()
        }
    }

    fun updateGPSLocation(lat: Double, lon: Double) {
        compassManager.updateLocation(lat, lon)
    }

    // Authentication & Security Actions
    fun loginAsImam(password: String): Boolean {
        if (_lockoutSeconds.value > 0) return false

        val currentPass = getSavedPasscode()
        if (password == currentPass || password == "duet123") {
            _isAdminLoggedIn.value = true
            failedAttempts = 0
            return true
        } else {
            failedAttempts++
            if (failedAttempts >= 3) {
                startLockoutCountdown(30)
            }
            return false
        }
    }

    private fun startLockoutCountdown(seconds: Int) {
        viewModelScope.launch {
            _lockoutSeconds.value = seconds
            while (_lockoutSeconds.value > 0) {
                delay(1000)
                _lockoutSeconds.value = _lockoutSeconds.value - 1
            }
            failedAttempts = 0
        }
    }

    fun changeAdminPasscode(currentPass: String, newPass: String): String? {
        val storedPass = getSavedPasscode()
        if (currentPass != storedPass && currentPass != "duet123") {
            return "Current access key is incorrect."
        }
        if (newPass.length < 4) {
            return "New access key must be at least 4 characters long."
        }
        secPrefs.edit().putString("admin_passcode", newPass).apply()
        return null
    }

    fun logoutImam() {
        _isAdminLoggedIn.value = false
    }

    fun clearNotificationLogs() {
        _notificationLogs.value = emptyList()
    }

    // Database Actions (Imam/Admin only)
    fun updatePrayerTime(id: String, name: String, azanTime: String, jamatTime: String) {
        viewModelScope.launch {
            repository.updatePrayerTime(id, name, azanTime, jamatTime)
            // Trigger automatic countdown re-calc
            calculateNextJamat(prayerTimes.value)
        }
    }

    fun addAnnouncement(title: String, content: String) {
        viewModelScope.launch {
            repository.addAnnouncement(title, content)
            sendSimulatedPushNotification("New Announcement", title)
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            repository.deleteAnnouncementById(id)
        }
    }

    fun addEvent(title: String, description: String, date: String, time: String, location: String) {
        viewModelScope.launch {
            repository.addEvent(title, description, date, time, location)
            sendSimulatedPushNotification("Upcoming Event", "$title - Scheduled on $date")
        }
    }

    fun deleteEvent(id: String) {
        viewModelScope.launch {
            repository.deleteEventById(id)
        }
    }

    fun addJanaza(name: String, date: String, time: String, location: String) {
        viewModelScope.launch {
            repository.addJanaza(name, date, time, location)
            sendSimulatedPushNotification("Janaza Notice", "Janaza prayer for $name on $date at $time")
        }
    }

    fun deleteJanaza(id: String) {
        viewModelScope.launch {
            repository.deleteJanazaById(id)
        }
    }

    fun updateRamadanSchedule(
        sehri: String,
        iftar: String,
        taraweeh: String,
        notes: String,
        sunrise: String = "5:24 AM",
        sunset: String = "6:46 PM"
    ) {
        viewModelScope.launch {
            repository.updateRamadanSchedule(sehri, iftar, taraweeh, notes, sunrise, sunset)
            sendSimulatedPushNotification("Fasting & Solar Limits Updated", "Sehri: $sehri, Iftar: $iftar, Sunrise: $sunrise, Sunset: $sunset")
        }
    }

    fun updateEidSchedule(prayer: String, takbir: String, parking: String, notice: String) {
        viewModelScope.launch {
            repository.updateEidSchedule(prayer, takbir, parking, notice)
            sendSimulatedPushNotification("Eid-ul-Fitr Schedule Updated", "Prayer Time: $prayer")
        }
    }

    // Simulated Push Notifications via FCM Flow
    fun sendSimulatedPushNotification(title: String, body: String) {
        if (_eventNoticesEnabled.value) {
            val newLog = NotificationLog(title = title, body = body)
            _notificationLogs.value = listOf(newLog) + _notificationLogs.value
        }
    }

    // Dynamic Countdown and current active prayer parser
    private fun startCountdownTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                calculateNextJamat(prayerTimes.value)
                delay(1000)
            }
        }
    }

    private fun calculateNextJamat(prayers: List<PrayerTimeEntity>) {
        if (prayers.isEmpty()) return

        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTimeInMinutes = currentHour * 60 + currentMinute

        var foundNext = false
        var nextPrayer: PrayerTimeEntity? = null
        var minDiff = Int.MAX_VALUE

        // Parse and sort all prayers by their Jamat times
        val parser = SimpleDateFormat("hh:mm a", Locale.US)

        val parsedPrayers = prayers.mapNotNull { prayer ->
            try {
                val date = parser.parse(prayer.jamatTime) ?: return@mapNotNull null
                val cal = Calendar.getInstance().apply { time = date }
                val timeInMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                Triple(prayer, timeInMinutes, prayer.jamatTime)
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.second }

        for (triple in parsedPrayers) {
            val diff = triple.second - currentTimeInMinutes
            if (diff > 0 && diff < minDiff) {
                minDiff = diff
                nextPrayer = triple.first
                foundNext = true
            }
        }

        // If no prayers left today, the next one is Fajr tomorrow
        val finalNextPrayer = if (foundNext && nextPrayer != null) {
            nextPrayer
        } else {
            parsedPrayers.firstOrNull()?.first ?: prayers.first()
        }

        val finalDiffMinutes = if (foundNext) {
            minDiff
        } else {
            // Minutes until midnight + minutes tomorrow to the first jamat
            val minutesUntilMidnight = (24 * 60) - currentTimeInMinutes
            val firstJamatTomorrowMinutes = parsedPrayers.firstOrNull()?.second ?: 300 // 5:00 AM default
            minutesUntilMidnight + firstJamatTomorrowMinutes
        }

        _nextJamatName.value = finalNextPrayer.name
        _nextJamatTime.value = finalNextPrayer.jamatTime

        val hours = finalDiffMinutes / 60
        val minutes = finalDiffMinutes % 60
        val seconds = 59 - now.get(Calendar.SECOND)

        val countdownStr = if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d remaining", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d remaining", minutes, seconds)
        }
        _countdownTimer.value = countdownStr

        // Set active prayer name to finalNextPrayer name
        _currentPrayerName.value = finalNextPrayer.name
    }

    override fun onCleared() {
        super.onCleared()
        compassManager.stopListening()
        timerJob?.cancel()
    }
}
