package com.duet.mosque.connect.data.repository

import android.content.Context
import com.duet.mosque.connect.data.database.AppDatabase
import com.duet.mosque.connect.data.model.AnnouncementEntity
import com.duet.mosque.connect.data.model.EidEntity
import com.duet.mosque.connect.data.model.EventEntity
import com.duet.mosque.connect.data.model.JanazaEntity
import com.duet.mosque.connect.data.model.PrayerTimeEntity
import com.duet.mosque.connect.data.model.RamadanEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach

class MosqueRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
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
                sehriTime = "0:00",
                iftarTime = "0:00",
                taraweehTime = "09:00 PM",
                sunriseTime = "5:24 AM",
                sunsetTime = "6:46 PM",
                notes = "Current Fasting & Solar Limits for DUET Central Mosque."
            )
        )

        val existingAnnouncements = allAnnouncements.firstOrNull()
        if (existingAnnouncements.isNullOrEmpty()) {
            // Seed Announcement
            announcementDao.insertAnnouncement(
                AnnouncementEntity(
                    title = "Central Mosque Digital Platform Launched",
                    content = "Assalamu Alaikum. Welcome to the official DUET Mosque Connect platform. Daily prayers and Jamat time updates will be synchronized here directly by the Imam."
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

            // Seed Janaza
            janazaDao.insertJanaza(
                JanazaEntity(
                    name = "Father of Dr. Aminul Islam (EEE Dept)",
                    date = "2026-07-21",
                    time = "02:00 PM",
                    location = "DUET Central Mosque Yard"
                )
            )

            // Seed Eid
            eidDao.insertEidSchedule(
                EidEntity(
                    prayerTime = "07:30 AM",
                    takbirReminder = "Takbir recitations begin at 07:15 AM",
                    parkingInfo = "Student parking at DUET Central Playground. Teacher parking near Administrative building.",
                    specialNotice = "Bring your own prayer mat. Wearing masks is recommended."
                )
            )
        }
    }

    // Update Prayer Times
    suspend fun updatePrayerTime(id: String, name: String, start: String, jamat: String) {
        prayerTimeDao.updatePrayerTime(PrayerTimeEntity(id, name, start, jamat))
    }

    // Add Announcement
    suspend fun addAnnouncement(title: String, content: String) {
        announcementDao.insertAnnouncement(AnnouncementEntity(title = title, content = content))
    }

    // Delete Announcement
    suspend fun deleteAnnouncementById(id: String) {
        announcementDao.deleteAnnouncementById(id)
    }

    // Add Event
    suspend fun addEvent(title: String, description: String, date: String, time: String, location: String) {
        eventDao.insertEvent(
            EventEntity(
                title = title,
                description = description,
                date = date,
                time = time,
                location = location
            )
        )
    }

    // Delete Event
    suspend fun deleteEventById(id: String) {
        eventDao.deleteEventById(id)
    }

    // Add Janaza
    suspend fun addJanaza(name: String, date: String, time: String, location: String) {
        janazaDao.insertJanaza(
            JanazaEntity(
                name = name,
                date = date,
                time = time,
                location = location
            )
        )
    }

    // Delete Janaza
    suspend fun deleteJanazaById(id: String) {
        janazaDao.deleteJanazaById(id)
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
        ramadanDao.insertRamadanSchedule(
            RamadanEntity(
                sehriTime = sehri,
                iftarTime = iftar,
                taraweehTime = taraweeh,
                sunriseTime = sunrise,
                sunsetTime = sunset,
                notes = notes
            )
        )
    }

    // Update Eid
    suspend fun updateEidSchedule(prayer: String, takbir: String, parking: String, notice: String) {
        eidDao.insertEidSchedule(
            EidEntity(
                prayerTime = prayer,
                takbirReminder = takbir,
                parkingInfo = parking,
                specialNotice = notice
            )
        )
    }
}
