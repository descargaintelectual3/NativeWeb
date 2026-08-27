package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DevelopmentActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DevelopmentActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DevelopmentActivityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DevelopmentActivityEntity>)

    @Update
    suspend fun update(entry: DevelopmentActivityEntity)

    @Query("SELECT * FROM development_activity_logs ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<DevelopmentActivityEntity>>

    @Query("SELECT * FROM development_activity_logs ORDER BY timestamp DESC")
    suspend fun getAllActivitiesList(): List<DevelopmentActivityEntity>

    @Query("SELECT * FROM development_activity_logs WHERE category = :category ORDER BY timestamp DESC")
    fun getActivitiesByCategory(category: String): Flow<List<DevelopmentActivityEntity>>

    @Query("SELECT * FROM development_activity_logs WHERE id = :id LIMIT 1")
    suspend fun getActivityById(id: Long): DevelopmentActivityEntity?

    @Query("DELETE FROM development_activity_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM development_activity_logs")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM development_activity_logs")
    suspend fun getCount(): Int
}
