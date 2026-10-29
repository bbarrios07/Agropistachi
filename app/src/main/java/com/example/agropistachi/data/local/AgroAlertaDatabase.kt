package com.example.agropistachi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TelemetryEntity::class,
        ActuatorEntity::class,
        AlertEntity::class,
        EventLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AgroAlertaDatabase : RoomDatabase() {

    abstract fun telemetryDao(): TelemetryDao
    abstract fun actuatorDao(): ActuatorDao
    abstract fun alertDao(): AlertDao
    abstract fun eventLogDao(): EventLogDao

    companion object {
        @Volatile
        private var INSTANCE: AgroAlertaDatabase? = null

        fun getInstance(context: Context): AgroAlertaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgroAlertaDatabase::class.java,
                    "agroalerta_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
