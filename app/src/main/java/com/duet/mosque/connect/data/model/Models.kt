package com.duet.mosque.connect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// Room entity to store prayer times
@Entity(tableName = "Schedule")
data class ScheduleEntity(
    @PrimaryKey val id: String, // "fajr", "zuhr", "asr", "maghrib", "isha", "jummah"
    val name: String,
    val azanTime: String,
    val jamatTime: String
)

// Room entity for Announcements
@Entity(tableName = "News")
data class NewsEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Room entity for Islamic Events
@Entity(tableName = "Events")
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
@Entity(tableName = "Janaza")
data class JanazaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String, // Name of the deceased
    val date: String,
    val time: String,
    val location: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Room entity for Ramadan info & Fasting/Solar limits
@Entity(tableName = "Ramadan")
data class RamadanEntity(
    @PrimaryKey val id: Int = 1,
    val sehriTime: String = "04:30 AM",
    val iftarTime: String = "06:45 PM",
    val sunriseTime: String = "05:24 AM",
    val sunsetTime: String = "06:46 PM",
    val notes: String = "Current Fasting & Solar Limits for DUET Central Mosque"
)

// Room entity for Eid info
@Entity(tableName = "Eid")
data class EidEntity(
    @PrimaryKey val id: Int = 1,
    val prayerTime: String,
    val takbirReminder: String,
    val parkingInfo: String,
    val specialNotice: String,
    val isEnabled: Boolean = true
)

// Simple data class for Imam's Contact Information
data class ImamContact(
    val name: String = "Hafez Mawlana Md. Abdul Muktadir",
    val phone: String = "+880 1700-000000",
    val email: String = "imam.mosque@duet.ac.bd",
    val officeHours: String = "Between Asr and Maghrib, Imam's Office, DUET Central Mosque"
)
