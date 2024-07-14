package com.mioshek.podcastreader

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Lyrics::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract val lyricsDao: LyricsDao

    companion object {
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lyrics_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        fun closeDb(){
            INSTANCE?.close()
        }
    }
}