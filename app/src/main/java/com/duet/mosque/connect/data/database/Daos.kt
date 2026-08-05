package com.duet.mosque.connect.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.duet.mosque.connect.data.model.AnnouncementEntity
import com.duet.mosque.connect.data.model.EidEntity
import com.duet.mosque.connect.data.model.EventEntity
import com.duet.mosque.connect.data.model.JanazaEntity
import com.duet.mosque.connect.data.model.PrayerTimeEntity
import com.duet.mosque.connect.data.model.RamadanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerTimeDao {
    @Query("SELECT * FROM prayer_times")
    fun getAllPrayerTimes(): Flow<List<PrayerTimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTimes(prayerTimes: List<PrayerTimeEntity>)

    @Update
    suspend fun updatePrayerTime(prayerTime: PrayerTimeEntity)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Delete
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncementById(id: String)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC, time ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: String)
}

@Dao
interface JanazaDao {
    @Query("SELECT * FROM janaza ORDER BY timestamp DESC")
    fun getAllJanazaNotices(): Flow<List<JanazaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJanaza(janaza: JanazaEntity)

    @Delete
    suspend fun deleteJanaza(janaza: JanazaEntity)

    @Query("DELETE FROM janaza WHERE id = :id")
    suspend fun deleteJanazaById(id: String)
}

@Dao
interface RamadanDao {
    @Query("SELECT * FROM ramadan WHERE id = 1 LIMIT 1")
    fun getRamadanSchedule(): Flow<RamadanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRamadanSchedule(ramadan: RamadanEntity)
}

@Dao
interface EidDao {
    @Query("SELECT * FROM eid WHERE id = 1 LIMIT 1")
    fun getEidSchedule(): Flow<EidEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEidSchedule(eid: EidEntity)
}
