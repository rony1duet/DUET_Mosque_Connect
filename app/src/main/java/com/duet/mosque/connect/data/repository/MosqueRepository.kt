package com.duet.mosque.connect.data.repository

import android.content.Context
import android.util.Log
import com.duet.mosque.connect.data.database.AppDatabase
import com.duet.mosque.connect.data.model.AnnouncementEntity
import com.duet.mosque.connect.data.model.EidEntity
import com.duet.mosque.connect.data.model.EventEntity
import com.duet.mosque.connect.data.model.JanazaEntity
import com.duet.mosque.connect.data.model.PrayerTimeEntity
import com.duet.mosque.connect.data.model.RamadanEntity
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
    private val prayerTimeDao = database.prayerTimeDao()
    private val announcementDao = database.announcementDao()
    private val eventDao = database.eventDao()
    private val janazaDao = database.janazaDao()
    private val ramadanDao = database.ramadanDao()
    private val eidDao = database.eidDao()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private var firestore: FirebaseFirestore? = null

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
    val allPrayerTimes: Flow<List<PrayerTimeEntity>> = prayerTimeDao.getAllPrayerTimes()
    val allAnnouncements: Flow<List<AnnouncementEntity>> = announcementDao.getAllAnnouncements()
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()
    val allJanazaNotices: Flow<List<JanazaEntity>> = janazaDao.getAllJanazaNotices()
    val ramadanSchedule: Flow<RamadanEntity?> = ramadanDao.getRamadanSchedule()
    val eidSchedule: Flow<EidEntity?> = eidDao.getEidSchedule()

    private fun setupRealtimeListeners() {
        val fs = firestore ?: return

        // 1. Realtime Listener for Prayer Times
        try {
            fs.collection("prayer_times").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w("MosqueRepository", "Firestore prayer_times permission denied. Using local Room cache.")
                    } else {
                        Log.w("MosqueRepository", "Listening to prayer_times status: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val name = doc.getString("name") ?: ""
                        val azan = doc.getString("azanTime") ?: ""
                        val jamat = doc.getString("jamatTime") ?: ""
                        PrayerTimeEntity(id = doc.id, name = name, azanTime = azan, jamatTime = jamat)
                    }
                    repositoryScope.launch {
                        prayerTimeDao.insertPrayerTimes(list)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MosqueRepository", "Prayer times listener failed: ${e.message}")
        }

        // 2. Realtime Listener for Announcements
        try {
            fs.collection("announcements").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                repositoryScope.launch {
                    for (dc in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                val doc = dc.document
                                val title = doc.getString("title") ?: continue
                                val content = doc.getString("content") ?: ""
                                val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                announcementDao.insertAnnouncement(AnnouncementEntity(id = doc.id, title = title, content = content, timestamp = ts))
                            }
                            DocumentChange.Type.REMOVED -> {
                                announcementDao.deleteAnnouncementById(dc.document.id)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MosqueRepository", "Announcements listener failed: ${e.message}")
        }

        // 3. Realtime Listener for Islamic Events
        try {
            fs.collection("events").addSnapshotListener { snapshot, error ->
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
            fs.collection("janaza").addSnapshotListener { snapshot, error ->
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
            fs.collection("ramadan").document("main").addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) return@addSnapshotListener
                val sehri = doc.getString("sehriTime") ?: "04:30 AM"
                val iftar = doc.getString("iftarTime") ?: "06:45 PM"
                val taraweeh = doc.getString("taraweehTime") ?: "09:00 PM"
                val sunrise = doc.getString("sunriseTime") ?: "05:24 AM"
                val sunset = doc.getString("sunsetTime") ?: "06:46 PM"
                val notes = doc.getString("notes") ?: "Current Fasting & Solar Limits for DUET Central Mosque."
                repositoryScope.launch {
                    ramadanDao.insertRamadanSchedule(
                        RamadanEntity(
                            id = 1,
                            sehriTime = sehri,
                            iftarTime = iftar,
                            taraweehTime = taraweeh,
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
            fs.collection("eid").document("main").addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) return@addSnapshotListener
                val prayerTime = doc.getString("prayerTime") ?: "07:30 AM"
                val takbir = doc.getString("takbirReminder") ?: "Takbir recitations begin at 07:15 AM"
                val parking = doc.getString("parkingInfo") ?: ""
                val notice = doc.getString("specialNotice") ?: ""
                repositoryScope.launch {
                    eidDao.insertEidSchedule(
                        EidEntity(
                            id = 1,
                            prayerTime = prayerTime,
                            takbirReminder = takbir,
                            parkingInfo = parking,
                            specialNotice = notice
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MosqueRepository", "Eid listener failed: ${e.message}")
        }
    }

    fun checkAndSeedDatabase() {
        // Seeding logic removed to ensure fresh start without dummy data.
    }

    // Update Prayer Times
    suspend fun updatePrayerTime(id: String, name: String, azanTime: String, jamat: String) {
        val entity = PrayerTimeEntity(id, name, azanTime, jamat)
        prayerTimeDao.updatePrayerTime(entity)
        firestore?.let { fs ->
            try {
                val map = mapOf("name" to name, "azanTime" to azanTime, "jamatTime" to jamat)
                fs.collection("prayer_times").document(id).set(map, SetOptions.merge())
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore updatePrayerTime failed: ${e.message}")
            }
        }
    }

    // Add Announcement
    suspend fun addAnnouncement(title: String, content: String) {
        val entity = AnnouncementEntity(title = title, content = content)
        announcementDao.insertAnnouncement(entity)
        firestore?.let { fs ->
            try {
                val map = mapOf("title" to title, "content" to content, "timestamp" to entity.timestamp)
                fs.collection("announcements").document(entity.id).set(map)
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore addAnnouncement failed: ${e.message}")
            }
        }
    }

    // Delete Announcement
    suspend fun deleteAnnouncementById(id: String) {
        announcementDao.deleteAnnouncementById(id)
        firestore?.let { fs ->
            try {
                fs.collection("announcements").document(id).delete()
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore deleteAnnouncement failed: ${e.message}")
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
                fs.collection("events").document(entity.id).set(map)
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
                fs.collection("events").document(id).delete()
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
                fs.collection("janaza").document(entity.id).set(map)
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
                fs.collection("janaza").document(id).delete()
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore deleteJanaza failed: ${e.message}")
            }
        }
    }

    // Update Ramadan & Fasting/Solar Limits
    suspend fun updateRamadanSchedule(
        sehri: String,
        iftar: String,
        taraweeh: String,
        notes: String,
        sunrise: String = "5:24 AM",
        sunset: String = "6:46 PM"
    ) {
        val entity = RamadanEntity(
            id = 1,
            sehriTime = sehri,
            iftarTime = iftar,
            taraweehTime = taraweeh,
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
                    "taraweehTime" to taraweeh,
                    "sunriseTime" to sunrise,
                    "sunsetTime" to sunset,
                    "notes" to notes
                )
                fs.collection("ramadan").document("main").set(map, SetOptions.merge())
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore updateRamadanSchedule failed: ${e.message}")
            }
        }
    }

    // Update Eid
    suspend fun updateEidSchedule(prayer: String, takbir: String, parking: String, notice: String) {
        val entity = EidEntity(
            id = 1,
            prayerTime = prayer,
            takbirReminder = takbir,
            parkingInfo = parking,
            specialNotice = notice
        )
        eidDao.insertEidSchedule(entity)
        firestore?.let { fs ->
            try {
                val map = mapOf(
                    "prayerTime" to prayer,
                    "takbirReminder" to takbir,
                    "parkingInfo" to parking,
                    "specialNotice" to notice
                )
                fs.collection("eid").document("main").set(map, SetOptions.merge())
            } catch (e: Exception) {
                Log.e("MosqueRepository", "Firestore updateEidSchedule failed: ${e.message}")
            }
        }
    }
}
