package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SavedGraffiti::class], version = 1, exportSchema = false)
abstract class GraffitiDatabase : RoomDatabase() {
    abstract fun graffitiDao(): GraffitiDao

    companion object {
        @Volatile
        private var INSTANCE: GraffitiDatabase? = null

        fun getDatabase(context: Context): GraffitiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GraffitiDatabase::class.java,
                    "graffiti_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
