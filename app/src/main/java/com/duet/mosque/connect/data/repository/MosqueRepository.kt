package com.duet.mosque.connect.data.repository

import android.content.Context
import android.util.Log
import com.duet.mosque.connect.data.database.AppDatabase
import com.duet.mosque.connect.data.model.NewsEntity
import com.duet.mosque.connect.data.model.EidEntity
import com.duet.mosque.connect.data.model.EventEntity
import com.duet.mosque.connect.data.model.JanazaEntity
import com.duet.mosque.connect.data.model.ScheduleEntity
import com.duet.mosque.connect.data.model.RamadanEntity
import com.duet.mosque.connect.utils.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MosqueRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val scheduleDao = database.scheduleDao()
    private val newsDao = database.newsDao()
    private val eventDao = database.eventDao()
    private val janazaDao = database.janazaDao()
    private val ramadanDao = database.ramadanDao()
    private val eidDao = database.eidDao()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private var firestore: FirebaseFirestore? = null

    var onRemoteNotificationReceived: ((title: String, body: String, timestamp: Long) -> Unit)? = null
    private val processedNotificationIds = mutableSetOf<String>()
    private val appStartTime = System.currentTimeMillis() - 5000L

    init {
        try {
            FirebaseApp.initializeApp(context)
            firestore = FirebaseFirestore.getInstance()
            try {
                val auth = FirebaseAuth.getInstance()
                if (auth.currentUser == null) {
                    auth.signInAnonymously().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("MosqueRepository", "Anonymous Firebase Auth successful")
                        } else {
                            Log.w("MosqueRepository", "Anonymous Firebase Auth skipped/failed: ${task.exception?.message}")
                        }
                        setupRealtimeListeners()
                    }
                } else {
                    setupRealtimeListeners()
                }
            } catch (authEx: Exception) {
                Log.w("MosqueRepository", "Firebase Auth init skipped: ${authEx.message}")
                setupRealtimeListeners()
            }
        } catch (e: Exception) {
            Log.e("MosqueRepository", "Firebase Firestore initialization error: ${e.message}")
        }
    }

    // Flow getters (Room database serves as local offline cache)
    val allSchedules: Flow<List<ScheduleEntity>> = scheduleDao.getAllSchedules()
    val allNews: Flow<List<NewsEntity>> = newsDao.getAllNews()
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()
    val allJanazaNotices: Flow<List<JanazaEntity>> = janazaDao.getAllJanazaNotices()
    val ramadanSchedule: Flow<RamadanEntity?> = ramadanDao.getRamadanSchedule()
    val eidSchedule: Flow<EidEntity?> = eidDao.getEidSchedule()

    private fun setupRealtimeListeners() {
        val fs = firestore ?: return

        // 1. Realtime Listener for Schedules
        try {
            fs.collection("Schedule").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w("MosqueRepository", "Firestore Schedule permission denied. Using local Room cache.")
                    } else {
                        Log.w("MosqueRepository", "Listening to Schedule status: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val name = doc.getString("name") ?: ""
                        val azan = doc.getString("azanTime") ?: ""
                        val jamat = doc.getString("jamatTime") ?: ""
                        ScheduleEntity(id = doc.id, name = name, azanTime = azan, jamatTime = jamat)
                    }
                    repositoryScope.launch {
                        scheduleDao.insertSchedules(list)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MosqueRepository", "Schedule listener failed: ${e.message}")
        }

        // 2. Realtime Listener for News
        try {
            fs.collection("News").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                repositoryScope.launch {
                    for (dc in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                val doc = dc.document
                                val title = doc.getString("title") ?: continue
                                val content = doc.getString("content") ?: ""
                                val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                newsDao.insertNews(NewsEntity(id = doc.id, title = title, content = content, timestamp = ts))
                            }
                            DocumentChange.Type.REMOVED -> {
                                newsDao.deleteNewsById(dc.document.id)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MosqueRepository", "News listener failed: ${e.message}")
        }

        // 3. Realtime Listener for Events
        try {
            fs.collection("Events").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                repositoryScope.launch {
                    for (dc in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                val doc = dc.document
                                val title = doc.getString("title") ?: continue
                                val desc = doc.getString("description") ?: ""
                                val date = doc.getString("date") ?: ""
                                val time = doc.getString("time") ?: ""
                                val loc = doc.getString("location") ?: ""
                                val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                eventDao.insertEvent(EventEntity(id = doc.id, title = title, description = desc, date = date, time = time, location = loc, timestamp = ts))
                            }
                            DocumentChange.Type.REMOVED -> {
                                eventDao.deleteEventById(dc.document.id)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MosqueRepository", "Events listener failed: ${e.message}")
        }

        // 4. Realtime Listener for Janaza Notices
        try {
            fs.collection("Janaza").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                repositoryScope.launch {
                    for (dc in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                val doc = dc.document
                                val name = doc.getString("name") ?: continue
                                val date = doc.getString("date") ?: ""
                                val time = doc.getString("time") ?: ""
                                val loc = doc.getString("location") ?: ""
                                val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                janazaDao.insertJanaza(JanazaEntity(id = doc.id, name = name, date = date, time = time, location = loc, timestamp = ts))
                            }
                            DocumentChange.Type.REMOVED -> {
                                janazaDao.deleteJanazaById(dc.document.id)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MosqueRepository", "Janaza listener failed: ${e.message}")
        }

        // 5. Realtime Listener for Ramadan/Solar Limits
        try {
            fs.collection("Ramadan").document("main").addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) return@addSnapshotListener
                val sehri = doc.getString("sehriTime") ?: "04:30 AM"
                val iftar = doc.getString("iftarTime") ?: "06:45 PM"
                val sunrise = doc.getString("sunriseTime") ?: "05:24 AM"
                val sunset = doc.getString("sunsetTime") ?: "06:46 PM"
                val notes = doc.getString("notes") ?: "Current Fasting & Solar Limits for DUET Central Mosque."
                repositoryScope.launch {
                    ramadanDao.insertRamadanSchedule(
                        RamadanEntity(
                            id = 1,
                            sehriTime = sehri,
                            iftarTime = iftar,
                            sunriseTime = sunrise,
                            sunsetTime = sunset,
                            notes = notes
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MosqueRepository", "Ramadan listener failed: ${e.message}")
        }

        // 6. Realtime Listener for Eid Schedule
        try {
            fs.collection("Eid").document("main").addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) return@addSnapshotListener
                val prayerTime = doc.getString("prayerTime") ?: "07:30 AM"
                val takbir = doc.getString("takbirReminder") ?: "Takbir recitations begin at 07:15 AM"
                val parking = doc.getString("parkingInfo") ?: ""
                val notice = doc.getString("specialNotice") ?: ""
                val isEnabled = doc.getBoolean("isEnabled") ?: false
                repositoryScope.launch {
                    eidDao.insertEidSchedule(
                        EidEntity(
                            id = 1,
                            prayerTime = prayerTime,
                            takbirReminder = takbir,
                            parkingInfo = parking,
                            specialNotice = notice,
                            isEnabled = isEnabled
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MosqueRepository", "Eid listener failed: ${e.message}")
        }

        // 7. Realtime Listener for Broadcast Push Notifications across ALL installed devices
        try {
            fs.collection("PushNotifications").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                repositoryScope.launch {
                    for (dc in snapshot.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val doc = dc.document
                            val docId = doc.id
                            val title = doc.getString("title") ?: continue
                            val body = doc.getString("body") ?: ""
                            val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()

                            // Check if this push notification is new and not yet processed on this device
                            if (ts >= appStartTime && !processedNotificationIds.contains(docId)) {
                                processedNotificationIds.add(docId)

                                // Trigger System Status Bar Push Notification on THIS device!
                                NotificationHelper.triggerSystemNotification(
                                    context = context,
                                    title = title,
                                    body = body
                                )

                                onRemoteNotificationReceived?.invoke(title, body, ts)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MosqueRepository", "PushNotifications listener failed: ${e.message}")
        }
    }

    suspend fun checkAndSeedDatabase() {
        val defaultPrayers = listOf(
            ScheduleEntity("fajr", "Fajr", "04:35 AM", "04:55 AM"),
            ScheduleEntity("zuhr", "Dhuhr", "12:05 PM", "01:20 PM"),
            ScheduleEntity("asr", "Asr", "04:30 PM", "05:15 PM"),
            ScheduleEntity("maghrib", "Maghrib", "06:56 PM", "06:56 PM"),
            ScheduleEntity("isha", "Isha", "08:30 PM", "09:00 PM"),
            ScheduleEntity("jummah", "Jumma", "12:05 PM", "01:30 PM")
        )
        scheduleDao.insertSchedules(defaultPrayers)

        firestore?.let { fs ->
            try {
                defaultPrayers.forEach { prayer ->
                    val map = mapOf(
                        "name" to prayer.name,
                        "azanTime" to prayer.azanTime,
                        "jamatTime" to prayer.jamatTime
                    )
                    fs.collection("Schedule").document(prayer.id).set(map, SetOptions.merge())
                }
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Seeding prayer times to Firestore failed: ${e.message}")
            }
        }

        val defaultRamadan = RamadanEntity(
            sehriTime = "04:30 AM",
            iftarTime = "06:45 PM",
            sunriseTime = "05:24 AM",
            sunsetTime = "06:46 PM",
            notes = "Current Fasting & Solar Limits for DUET Central Mosque."
        )
        ramadanDao.insertRamadanSchedule(defaultRamadan)

        firestore?.let { fs ->
            try {
                val map = mapOf(
                    "sehriTime" to defaultRamadan.sehriTime,
                    "iftarTime" to defaultRamadan.iftarTime,
                    "sunriseTime" to defaultRamadan.sunriseTime,
                    "sunsetTime" to defaultRamadan.sunsetTime,
                    "notes" to defaultRamadan.notes
                )
                fs.collection("Ramadan").document("main").set(map, SetOptions.merge())
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Seeding ramadan to Firestore failed: ${e.message}")
            }
        }

        val defaultEid = EidEntity(
            prayerTime = "07:30 AM",
            takbirReminder = "Takbir recitations begin at 07:15 AM",
            parkingInfo = "Student parking at DUET Central Playground. Teacher parking near Administrative building.",
            specialNotice = "Bring your own prayer mat.",
            isEnabled = false
        )
        eidDao.insertEidSchedule(defaultEid)
        firestore?.let { fs ->
            try {
                val map = mapOf(
                    "prayerTime" to defaultEid.prayerTime,
                    "takbirReminder" to defaultEid.takbirReminder,
                    "parkingInfo" to defaultEid.parkingInfo,
                    "specialNotice" to defaultEid.specialNotice,
                    "isEnabled" to defaultEid.isEnabled
                )
                fs.collection("Eid").document("main").set(map, SetOptions.merge())
            } catch (_: Exception) {}
        }
    }

    // Publish cross-device push notification to Firestore
    suspend fun publishPushNotification(title: String, body: String) {
        firestore?.let { fs ->
            try {
                val doc = mapOf(
                    "title" to title,
                    "body" to body,
                    "timestamp" to System.currentTimeMillis()
                )
                fs.collection("PushNotifications").add(doc)
            } catch (e: Exception) {
                Log.e("MosqueRepository", "publishPushNotification failed: ${e.message}")
            }
        }
    }

    // Update Schedule
    suspend fun updateSchedule(id: String, name: String, azanTime: String, jamat: String) {
        val entity = ScheduleEntity(id, name, azanTime, jamat)
        scheduleDao.updateSchedule(entity)
        firestore?.let { fs ->
            try {
                val map = mapOf("name" to name, "azanTime" to azanTime, "jamatTime" to jamat)
                fs.collection("Schedule").document(id).set(map, SetOptions.merge())
                publishPushNotification("Jamat Schedule Updated", "$name Jamat time updated to $jamat (Azan: $azanTime)")
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore updateSchedule failed: ${e.message}")
            }
        }
    }

    // Add News
    suspend fun addNews(title: String, content: String) {
        val entity = NewsEntity(title = title, content = content)
        newsDao.insertNews(entity)
        firestore?.let { fs ->
            try {
                val map = mapOf("title" to title, "content" to content, "timestamp" to entity.timestamp)
                fs.collection("News").document(entity.id).set(map)
                publishPushNotification("New Announcement", title)
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore addNews failed: ${e.message}")
            }
        }
    }

    // Delete News
    suspend fun deleteNewsById(id: String) {
        newsDao.deleteNewsById(id)
        firestore?.let { fs ->
            try {
                fs.collection("News").document(id).delete()
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore deleteNews failed: ${e.message}")
            }
        }
    }

    // Add Event
    suspend fun addEvent(title: String, description: String, date: String, time: String, location: String) {
        val entity = EventEntity(title = title, description = description, date = date, time = time, location = location)
        eventDao.insertEvent(entity)
        firestore?.let { fs ->
            try {
                val map = mapOf(
                    "title" to title,
                    "description" to description,
                    "date" to date,
                    "time" to time,
                    "location" to location,
                    "timestamp" to entity.timestamp
                )
                fs.collection("Events").document(entity.id).set(map)
                publishPushNotification("Upcoming Event", "$title - Scheduled on $date")
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore addEvent failed: ${e.message}")
            }
        }
    }

    // Delete Event
    suspend fun deleteEventById(id: String) {
        eventDao.deleteEventById(id)
        firestore?.let { fs ->
            try {
                fs.collection("Events").document(id).delete()
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore deleteEvent failed: ${e.message}")
            }
        }
    }

    // Add Janaza
    suspend fun addJanaza(name: String, date: String, time: String, location: String) {
        val entity = JanazaEntity(name = name, date = date, time = time, location = location)
        janazaDao.insertJanaza(entity)
        firestore?.let { fs ->
            try {
                val map = mapOf(
                    "name" to name,
                    "date" to date,
                    "time" to time,
                    "location" to location,
                    "timestamp" to entity.timestamp
                )
                fs.collection("Janaza").document(entity.id).set(map)
                publishPushNotification("Janaza Notice", "Janaza prayer for $name on $date at $time")
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore addJanaza failed: ${e.message}")
            }
        }
    }

    // Delete Janaza
    suspend fun deleteJanazaById(id: String) {
        janazaDao.deleteJanazaById(id)
        firestore?.let { fs ->
            try {
                fs.collection("Janaza").document(id).delete()
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore deleteJanaza failed: ${e.message}")
            }
        }
    }

    // Update Ramadan & Fasting/Solar Limits
    suspend fun updateRamadanSchedule(
        sehri: String,
        iftar: String,
        notes: String,
        sunrise: String = "5:24 AM",
        sunset: String = "6:46 PM"
    ) {
        val entity = RamadanEntity(
            id = 1,
            sehriTime = sehri,
            iftarTime = iftar,
            sunriseTime = sunrise,
            sunsetTime = sunset,
            notes = notes
        )
        ramadanDao.insertRamadanSchedule(entity)
        firestore?.let { fs ->
            try {
                val map = mapOf(
                    "sehriTime" to sehri,
                    "iftarTime" to iftar,
                    "sunriseTime" to sunrise,
                    "sunsetTime" to sunset,
                    "notes" to notes
                )
                fs.collection("Ramadan").document("main").set(map, SetOptions.merge())
                publishPushNotification("Fasting & Solar Limits Updated", "Sehri: $sehri, Iftar: $iftar")
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore updateRamadanSchedule failed: ${e.message}")
            }
        }
    }

    // Update Eid
    suspend fun updateEidSchedule(prayer: String, takbir: String, parking: String, notice: String, isEnabled: Boolean = true) {
        val entity = EidEntity(
            id = 1,
            prayerTime = prayer,
            takbirReminder = takbir,
            parkingInfo = parking,
            specialNotice = notice,
            isEnabled = isEnabled
        )
        eidDao.insertEidSchedule(entity)
        firestore?.let { fs ->
            try {
                val map = mapOf(
                    "prayerTime" to prayer,
                    "takbirReminder" to takbir,
                    "parkingInfo" to parking,
                    "specialNotice" to notice,
                    "isEnabled" to isEnabled
                )
                fs.collection("Eid").document("main").set(map, SetOptions.merge())
                publishPushNotification("Eid Schedule Updated", "Prayer Time: $prayer")
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore updateEidSchedule failed: ${e.message}")
            }
        }
    }
}
