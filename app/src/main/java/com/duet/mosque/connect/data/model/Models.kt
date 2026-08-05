package com.duet.mosque.connect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// Room entity to store prayer times
@Entity(tableName = "prayer_times")
data class PrayerTimeEntity(
    @PrimaryKey val id: String, // "fajr", "zuhr", "asr", "maghrib", "isha", "jummah"
    val name: String,
    val startTime: String,
    val jamatTime: String
)

// Room entity for Announcements
@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Room entity for Islamic Events
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Room entity for Janaza Notices
@Entity(tableName = "janaza")
data class JanazaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String, // Name of the deceased
    val date: String,
    val time: String,
    val location: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Room entity for Ramadan info & Fasting/Solar limits
@Entity(tableName = "ramadan")
data class RamadanEntity(
    @PrimaryKey val id: Int = 1,
    val sehriTime: String = "0:00",
    val iftarTime: String = "0:00",
    val taraweehTime: String = "09:00 PM",
    val sunriseTime: String = "5:24 AM",
    val sunsetTime: String = "6:46 PM",
    val notes: String = "Current Fasting & Solar Limits for DUET Central Mosque"
)

// Room entity for Eid info
@Entity(tableName = "eid")
data class EidEntity(
    @PrimaryKey val id: Int = 1,
    val prayerTime: String,
    val takbirReminder: String,
    val parkingInfo: String,
    val specialNotice: String
)

// Simple data class for Imam's Contact Information
data class ImamContact(
    val name: String = "Hafez Mawlana Md. Abdul Muktadir",
    val phone: String = "+8801712345678",
    val email: String = "imam.mosque@duet.ac.bd",
    val officeHours: String = "Between Asr and Maghrib, Imam's Office, DUET Central Mosque"
)
