package com.duet.mosque.connect.data.repository

import android.content.Context
import com.duet.mosque.connect.data.database.AppDatabase
import com.duet.mosque.connect.data.model.AnnouncementEntity
import com.duet.mosque.connect.data.model.EidEntity
import com.duet.mosque.connect.data.model.EventEntity
import com.duet.mosque.connect.data.model.JanazaEntity
import com.duet.mosque.connect.data.model.PrayerTimeEntity
import com.duet.mosque.connect.data.model.RamadanEntity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MosqueRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val prayerTimeDao = database.prayerTimeDao()
    private val announcementDao = database.announcementDao()
    private val eventDao = database.eventDao()
    private val janazaDao = database.janazaDao()
    private val ramadanDao = database.ramadanDao()
    private val eidDao = database.eidDao()

    // Flow getters
    val allPrayerTimes: Flow<List<PrayerTimeEntity>> = prayerTimeDao.getAllPrayerTimes()
    val allAnnouncements: Flow<List<AnnouncementEntity>> = announcementDao.getAllAnnouncements()
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()
    val allJanazaNotices: Flow<List<JanazaEntity>> = janazaDao.getAllJanazaNotices()
    val ramadanSchedule: Flow<RamadanEntity?> = ramadanDao.getRamadanSchedule()
    val eidSchedule: Flow<EidEntity?> = eidDao.getEidSchedule()

    fun syncWithFirebase() {
        // Sync Prayer Times
        firebaseDatabase.getReference("prayer_times").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val prayers = mutableListOf<PrayerTimeEntity>()
                snapshot.children.forEach { child ->
                    child.getValue(PrayerTimeEntity::class.java)?.let { prayers.add(it) }
                }
                if (prayers.isNotEmpty()) {
                    scope.launch { prayerTimeDao.insertPrayerTimes(prayers) }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Sync Announcements
        firebaseDatabase.getReference("announcements").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcements = mutableListOf<AnnouncementEntity>()
                snapshot.children.forEach { child ->
                    child.getValue(AnnouncementEntity::class.java)?.let { announcements.add(it) }
                }
                scope.launch {
                    // Simple sync: clear and re-insert or diff. For simplicity, we'll replace all for now
                    // In a production app, we might want a more sophisticated sync.
                    announcementDao.insertAnnouncements(announcements)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Sync Events
        firebaseDatabase.getReference("events").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<EventEntity>()
                snapshot.children.forEach { child ->
                    child.getValue(EventEntity::class.java)?.let { events.add(it) }
                }
                scope.launch { eventDao.insertEvents(events) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Sync Janaza
        firebaseDatabase.getReference("janaza").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val janazas = mutableListOf<JanazaEntity>()
                snapshot.children.forEach { child ->
                    child.getValue(JanazaEntity::class.java)?.let { janazas.add(it) }
                }
                scope.launch { janazaDao.insertJanazas(janazas) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Sync Ramadan
        firebaseDatabase.getReference("ramadan").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(RamadanEntity::class.java)?.let {
                    scope.launch { ramadanDao.insertRamadanSchedule(it) }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Sync Eid
        firebaseDatabase.getReference("eid").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(EidEntity::class.java)?.let {
                    scope.launch { eidDao.insertEidSchedule(it) }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Suspending seeding method
    suspend fun checkAndSeedDatabase() {
        // Seed or update Prayer Times with exact specified DUET Mosque schedule
        val defaultPrayers = listOf(
            PrayerTimeEntity("fajr", "Fajr", "04:35 AM", "04:55 AM"),
            PrayerTimeEntity("zuhr", "Dhuhr", "12:05 PM", "01:20 PM"),
            PrayerTimeEntity("asr", "Asr", "04:30 PM", "05:15 PM"),
            PrayerTimeEntity("maghrib", "Maghrib", "06:56 PM", "06:56 PM"),
            PrayerTimeEntity("isha", "Isha", "08:30 PM", "09:00 PM"),
            PrayerTimeEntity("jummah", "Jumma", "12:05 PM", "01:30 PM")
        )
        prayerTimeDao.insertPrayerTimes(defaultPrayers)

        // Seed Fasting & Solar Limits
        ramadanDao.insertRamadanSchedule(
            RamadanEntity(
                sehriTime = "04:30 AM",
                iftarTime = "06:45 PM",
                taraweehTime = "09:00 PM",
                sunriseTime = "05:24 AM",
                sunsetTime = "06:46 PM",
                notes = "Current Fasting & Solar Limits for DUET Central Mosque."
            )
        )

        val existingAnnouncements = allAnnouncements.firstOrNull()
        if (existingAnnouncements.isNullOrEmpty()) {
            // Seed Announcement
            announcementDao.insertAnnouncement(
                AnnouncementEntity(
                    title = "Central Mosque Digital Platform Launched",
                    content = "Assalamu Alaikum. Welcome to the official DUET Mosque Connect platform. Daily prayers, Azan times, and Jamat time updates will be synchronized here directly by the Imam."
                )
            )

            // Seed Event
            eventDao.insertEvent(
                EventEntity(
                    title = "Weekly Quran Tafseer Session",
                    description = "Join us for our weekly Tafseer-ul-Quran lecture focusing on lessons for students.",
                    date = "Every Thursday",
                    time = "After Isha Prayer",
                    location = "DUET Central Mosque Main Hall"
                )
            )

            // Seed Eid
            eidDao.insertEidSchedule(
                EidEntity(
                    prayerTime = "07:30 AM",
                    takbirReminder = "Takbir recitations begin at 07:15 AM",
                    parkingInfo = "Student parking at DUET Central Playground. Teacher parking near Administrative building.",
                    specialNotice = "Bring your own prayer mat."
                )
            )
        }
    }

    // Update Prayer Times
    suspend fun updatePrayerTime(id: String, name: String, azanTime: String, jamat: String) {
        val prayer = PrayerTimeEntity(id, name, azanTime, jamat)
        prayerTimeDao.updatePrayerTime(prayer)
        firebaseDatabase.getReference("prayer_times").child(id).setValue(prayer)
    }

    // Add Announcement
    suspend fun addAnnouncement(title: String, content: String) {
        val announcement = AnnouncementEntity(title = title, content = content)
        announcementDao.insertAnnouncement(announcement)
        firebaseDatabase.getReference("announcements").child(announcement.id).setValue(announcement)
    }

    // Delete Announcement
    suspend fun deleteAnnouncementById(id: String) {
        announcementDao.deleteAnnouncementById(id)
        firebaseDatabase.getReference("announcements").child(id).removeValue()
    }

    // Add Event
    suspend fun addEvent(title: String, description: String, date: String, time: String, location: String) {
        val event = EventEntity(
            title = title,
            description = description,
            date = date,
            time = time,
            location = location
        )
        eventDao.insertEvent(event)
        firebaseDatabase.getReference("events").child(event.id).setValue(event)
    }

    // Delete Event
    suspend fun deleteEventById(id: String) {
        eventDao.deleteEventById(id)
        firebaseDatabase.getReference("events").child(id).removeValue()
    }

    // Add Janaza
    suspend fun addJanaza(name: String, date: String, time: String, location: String) {
        val janaza = JanazaEntity(
            name = name,
            date = date,
            time = time,
            location = location
        )
        janazaDao.insertJanaza(janaza)
        firebaseDatabase.getReference("janaza").child(janaza.id).setValue(janaza)
    }

    // Delete Janaza
    suspend fun deleteJanazaById(id: String) {
        janazaDao.deleteJanazaById(id)
        firebaseDatabase.getReference("janaza").child(id).removeValue()
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
        val ramadan = RamadanEntity(
            sehriTime = sehri,
            iftarTime = iftar,
            taraweehTime = taraweeh,
            sunriseTime = sunrise,
            sunsetTime = sunset,
            notes = notes
        )
        ramadanDao.insertRamadanSchedule(ramadan)
        firebaseDatabase.getReference("ramadan").setValue(ramadan)
    }

    // Update Eid
    suspend fun updateEidSchedule(prayer: String, takbir: String, parking: String, notice: String) {
        val eid = EidEntity(
            prayerTime = prayer,
            takbirReminder = takbir,
            parkingInfo = parking,
            specialNotice = notice
        )
        eidDao.insertEidSchedule(eid)
        firebaseDatabase.getReference("eid").setValue(eid)
    }
}
