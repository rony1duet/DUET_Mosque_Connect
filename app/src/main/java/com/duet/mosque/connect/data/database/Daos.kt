package com.duet.mosque.connect.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.duet.mosque.connect.data.model.EidEntity
import com.duet.mosque.connect.data.model.EventEntity
import com.duet.mosque.connect.data.model.JanazaEntity
import com.duet.mosque.connect.data.model.NewsEntity
import com.duet.mosque.connect.data.model.RamadanEntity
import com.duet.mosque.connect.data.model.ScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * =========================================================================================
 * DATA ACCESS OBJECTS (DAOs) (DUET Mosque Connect)
 * =========================================================================================
 * In Room, a DAO (`@Dao`) defines the SQLite queries and database operations for each entity.
 *
 * Kotlin & Room Concepts Explained for Beginners:
 *  1. `interface`: A contract defining function signatures without implementations.
 *     Room automatically generates the underlying SQL implementation code during compilation via KSP.
 *  2. `suspend fun`: A Coroutine function that can perform long-running work (like inserting or
 *     deleting database records) asynchronously on a background thread without freezing the UI.
 *  3. `Flow<List<T>>`: A reactive stream from Kotlin Coroutines. When a query returns a Flow,
 *     Room monitors the database table. Whenever any row is updated, added, or deleted, Room
 *     automatically emits the new list to all active UI screens!
 *  4. `OnConflictStrategy.REPLACE`: If a record with the same PrimaryKey already exists, overwrite it.
 * =========================================================================================
 */

/**
 * ScheduleDao
 * Handles CRUD database operations for daily prayer timings (Fajr, Dhuhr, Asr, Maghrib, Isha, Jummah).
 */
@Dao
interface ScheduleDao {
    @Query("SELECT * FROM Schedule")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)
}

/**
 * NewsDao
 * Handles CRUD database operations for mosque announcements and news notices.
 */
@Dao
interface NewsDao {
    @Query("SELECT * FROM News ORDER BY timestamp DESC")
    fun getAllNews(): Flow<List<NewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: NewsEntity)

    @Delete
    suspend fun deleteNews(news: NewsEntity)

    @Query("DELETE FROM News WHERE id = :id")
    suspend fun deleteNewsById(id: String)
}

/**
 * EventDao
 * Handles CRUD database operations for campus Islamic events and gatherings.
 */
@Dao
interface EventDao {
    @Query("SELECT * FROM Events ORDER BY date ASC, time ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM Events WHERE id = :id")
    suspend fun deleteEventById(id: String)
}

/**
 * JanazaDao
 * Handles CRUD database operations for funeral (Janaza) announcements.
 */
@Dao
interface JanazaDao {
    @Query("SELECT * FROM Janaza ORDER BY timestamp DESC")
    fun getAllJanazaNotices(): Flow<List<JanazaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJanaza(janaza: JanazaEntity)

    @Delete
    suspend fun deleteJanaza(janaza: JanazaEntity)

    @Query("DELETE FROM Janaza WHERE id = :id")
    suspend fun deleteJanazaById(id: String)
}

/**
 * RamadanDao
 * Handles reading and updating the single Ramadan / daily solar limits record (id = 1).
 */
@Dao
interface RamadanDao {
    @Query("SELECT * FROM Ramadan WHERE id = 1 LIMIT 1")
    fun getRamadanSchedule(): Flow<RamadanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRamadanSchedule(ramadan: RamadanEntity)
}

/**
 * EidDao
 * Handles reading and updating the single Eid prayer schedule record (id = 1).
 */
@Dao
interface EidDao {
    @Query("SELECT * FROM Eid WHERE id = 1 LIMIT 1")
    fun getEidSchedule(): Flow<EidEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEidSchedule(eid: EidEntity)
}
