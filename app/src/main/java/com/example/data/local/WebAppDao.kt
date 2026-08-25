package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WebAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WebAppDao {

    @Query("SELECT * FROM web_apps ORDER BY lastOpened DESC")
    fun getAllWebApps(): Flow<List<WebAppEntity>>

    @Query("SELECT * FROM web_apps ORDER BY lastOpened DESC")
    suspend fun getAllWebAppsList(): List<WebAppEntity>

    @Query("SELECT * FROM web_apps WHERE id = :id LIMIT 1")
    suspend fun getWebAppById(id: Long): WebAppEntity?

    @Query("SELECT * FROM web_apps WHERE url = :url LIMIT 1")
    suspend fun getWebAppByUrl(url: String): WebAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebApp(webApp: WebAppEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWebApps(webApps: List<WebAppEntity>): List<Long>

    @Update
    suspend fun updateWebApp(webApp: WebAppEntity)

    @Delete
    suspend fun deleteWebApp(webApp: WebAppEntity)

    @Query("DELETE FROM web_apps WHERE id = :id")
    suspend fun deleteWebAppById(id: Long)

    @Query("DELETE FROM web_apps")
    suspend fun deleteAllWebApps()

    @Query("UPDATE web_apps SET openCount = openCount + 1, lastOpened = :timestamp WHERE id = :id")
    suspend fun recordAppOpen(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE web_apps SET isPinnedShortcut = :isPinned WHERE id = :id")
    suspend fun updatePinnedStatus(id: Long, isPinned: Boolean)

    @Query("SELECT COUNT(*) FROM web_apps")
    fun getWebAppsCount(): Flow<Int>
}
