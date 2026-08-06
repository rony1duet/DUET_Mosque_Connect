package com.duet.mosque.connect.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.duet.mosque.connect.data.model.NewsEntity
import com.duet.mosque.connect.data.model.EidEntity
import com.duet.mosque.connect.data.model.EventEntity
import com.duet.mosque.connect.data.model.JanazaEntity
import com.duet.mosque.connect.data.model.ScheduleEntity
import com.duet.mosque.connect.data.model.RamadanEntity

@Database(
    entities = [
        ScheduleEntity::class,
        NewsEntity::class,
        EventEntity::class,
        JanazaEntity::class,
        RamadanEntity::class,
        EidEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao
    abstract fun newsDao(): NewsDao
    abstract fun eventDao(): EventDao
    abstract fun janazaDao(): JanazaDao
    abstract fun ramadanDao(): RamadanDao
    abstract fun eidDao(): EidDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "duet_mosque_connect_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
