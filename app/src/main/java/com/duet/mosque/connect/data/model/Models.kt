package com.duet.mosque.connect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * =========================================================================================
 * DATA LAYER: ROOM DATABASE ENTITIES (DUET Mosque Connect)
 * =========================================================================================
 * In Android Room (SQLite ORM), `@Entity` classes represent database tables, and each instance
 * of the class represents a single row in that table.
 *
 * Kotlin Concepts Explained for Beginners:
 *  1. `data class`: A Kotlin class whose primary purpose is holding data. Kotlin automatically
 *     generates `equals()`, `hashCode()`, `toString()`, and `copy()` methods for data classes.
 *  2. `@Entity(tableName = "...")`: Room annotation defining the SQLite table name.
 *  3. `@PrimaryKey`: Specifies the unique identifier column for that table row.
 *  4. Default Arguments (e.g. `val timestamp: Long = System.currentTimeMillis()`):
 *     Kotlin allows specifying default parameter values so they don't have to be passed every time.
 * =========================================================================================
 */

/**
 * ScheduleEntity
 * Represents a daily prayer schedule row in the SQLite 'Schedule' table.
 *
 * @property id Unique prayer key (e.g. "fajr", "zuhr", "asr", "maghrib", "isha", "jummah")
 * @property name Human-readable prayer display name (e.g. "Fajr", "Dhuhr")
 * @property azanTime Azan start time in 12-hour format (e.g. "04:35 AM")
 * @property jamatTime Congregational prayer time in 12-hour format (e.g. "04:55 AM")
 */
@Entity(tableName = "Schedule")
data class ScheduleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val azanTime: String,
    val jamatTime: String
)

/**
 * NewsEntity
 * Represents a general announcement or news notice posted by the mosque administration.
 */
@Entity(tableName = "News")
data class NewsEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * EventEntity
 * Represents an upcoming Islamic event, lecture, or gathering on campus.
 */
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

/**
 * JanazaEntity
 * Represents a funeral / Janaza prayer announcement for a deceased community member.
 */
@Entity(tableName = "Janaza")
data class JanazaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String, // Name of the deceased
    val date: String,
    val time: String,
    val location: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * RamadanEntity
 * Stores current fasting and solar limits (Sehri, Iftar, Sunrise, Sunset).
 * Uses a single constant PrimaryKey (id = 1) since only one active limit record is needed.
 */
@Entity(tableName = "Ramadan")
data class RamadanEntity(
    @PrimaryKey val id: Int = 1,
    val sehriTime: String = "04:30 AM",
    val iftarTime: String = "06:45 PM",
    val sunriseTime: String = "05:24 AM",
    val sunsetTime: String = "06:46 PM",
    val notes: String = "Current Fasting & Solar Limits for DUET Central Mosque"
)

/**
 * EidEntity
 * Stores Eid prayer time, Takbir recitations schedule, parking info, and visibility toggle.
 * Uses a single constant PrimaryKey (id = 1).
 */
@Entity(tableName = "Eid")
data class EidEntity(
    @PrimaryKey val id: Int = 1,
    val prayerTime: String,
    val takbirReminder: String,
    val parkingInfo: String,
    val specialNotice: String,
    val isEnabled: Boolean = true
)
