package com.duet.mosque.connect.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.duet.mosque.connect.data.model.NewsEntity
import com.duet.mosque.connect.data.model.EidEntity
import com.duet.mosque.connect.data.model.EventEntity
import com.duet.mosque.connect.data.model.JanazaEntity
import com.duet.mosque.connect.data.model.ScheduleEntity
import com.duet.mosque.connect.data.model.RamadanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM Schedule")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)
}

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

@Dao
interface RamadanDao {
    @Query("SELECT * FROM Ramadan WHERE id = 1 LIMIT 1")
    fun getRamadanSchedule(): Flow<RamadanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRamadanSchedule(ramadan: RamadanEntity)
}

@Dao
interface EidDao {
    @Query("SELECT * FROM Eid WHERE id = 1 LIMIT 1")
    fun getEidSchedule(): Flow<EidEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEidSchedule(eid: EidEntity)
}
