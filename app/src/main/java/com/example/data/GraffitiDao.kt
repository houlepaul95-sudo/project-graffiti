package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GraffitiDao {
    @Query("SELECT * FROM saved_graffiti ORDER BY createdAt DESC")
    fun getAllSaved(): Flow<List<SavedGraffiti>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGraffiti(item: SavedGraffiti): Long

    @Query("DELETE FROM saved_graffiti WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM saved_graffiti")
    suspend fun getCount(): Int
}
