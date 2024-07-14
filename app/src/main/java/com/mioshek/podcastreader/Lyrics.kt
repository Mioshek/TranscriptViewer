package com.mioshek.podcastreader

import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "Lyrics")
data class Lyrics(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val content: String,
    val lastSelectedPage: Int
)

@Dao
interface LyricsDao{
    @Upsert
    suspend fun upsert(lyrics: Lyrics)

    @Query("DELETE FROM Lyrics WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM Lyrics")
    fun getAll(): Flow<List<Lyrics>>

    @Query("SELECT * FROM Lyrics ORDER BY id DESC LIMIT 1")
    suspend fun getLast(): Lyrics

}


class LyricsRepository(private val lyricsDao: LyricsDao) {
    @WorkerThread
    suspend fun upsert(lyrics: Lyrics) = lyricsDao.upsert(lyrics)

    @WorkerThread
    suspend fun delete(id: Int) = lyricsDao.delete(id)

    fun getAll(): Flow<List<Lyrics>> = lyricsDao.getAll()

    suspend fun getLast(): Lyrics = lyricsDao.getLast()

}