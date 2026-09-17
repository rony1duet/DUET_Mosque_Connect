package com.duet.mosque.connect.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.duet.mosque.connect.data.model.EidEntity
import com.duet.mosque.connect.data.model.EventEntity
import com.duet.mosque.connect.data.model.JanazaEntity
import com.duet.mosque.connect.data.model.NewsEntity
import com.duet.mosque.connect.data.model.RamadanEntity
import com.duet.mosque.connect.data.model.ScheduleEntity

/**
 * =========================================================================================
 * ROOM DATABASE SINGLETON (DUET Mosque Connect)
 * =========================================================================================
 * The main database holder class that connects all entities and DAOs to the SQLite database.
 *
 * Kotlin & Android Concepts Explained for Beginners:
 *  1. `abstract class ... : RoomDatabase()`: Room builds the actual subclass implementation.
 *  2. `companion object`: In Kotlin, `companion object` holds static factory methods or constants
 *     belonging to the class itself rather than individual instances.
 *  3. `@Volatile`: Guarantees that writes to `INSTANCE` are immediately visible to all threads.
 *  4. `synchronized(this)`: Thread-safe locking pattern (Double-Checked Locking Singleton) to
 *     ensure only ONE database connection instance is opened across the whole app lifecycle.
 *  5. `fallbackToDestructiveMigration(dropAllTables = true)`: If schema changes during development,
 *     Room recreates tables safely to avoid app crashes.
 * =========================================================================================
 */
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

    // Abstract getters for DAOs
    abstract fun scheduleDao(): ScheduleDao
    abstract fun newsDao(): NewsDao
    abstract fun eventDao(): EventDao
    abstract fun janazaDao(): JanazaDao
    abstract fun ramadanDao(): RamadanDao
    abstract fun eidDao(): EidDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the thread-safe singleton instance of [AppDatabase].
         */
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
