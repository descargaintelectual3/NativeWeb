package com.example.data.repository

import com.example.data.local.WebAppDao
import com.example.data.model.WebAppEntity
import kotlinx.coroutines.flow.Flow

class WebAppRepository(private val webAppDao: WebAppDao) {

    val allWebApps: Flow<List<WebAppEntity>> = webAppDao.getAllWebApps()
    val webAppsCount: Flow<Int> = webAppDao.getWebAppsCount()

    suspend fun getWebAppById(id: Long): WebAppEntity? = webAppDao.getWebAppById(id)

    suspend fun getWebAppByUrl(url: String): WebAppEntity? = webAppDao.getWebAppByUrl(url)

    suspend fun insertWebApp(webApp: WebAppEntity): Long = webAppDao.insertWebApp(webApp)

    suspend fun insertAllWebApps(webApps: List<WebAppEntity>): List<Long> = webAppDao.insertAllWebApps(webApps)

    suspend fun updateWebApp(webApp: WebAppEntity) = webAppDao.updateWebApp(webApp)

    suspend fun deleteWebApp(webApp: WebAppEntity) = webAppDao.deleteWebApp(webApp)

    suspend fun deleteWebAppById(id: Long) = webAppDao.deleteWebAppById(id)

    suspend fun deleteAllWebApps() = webAppDao.deleteAllWebApps()

    suspend fun recordAppOpen(id: Long) = webAppDao.recordAppOpen(id)

    suspend fun updatePinnedStatus(id: Long, isPinned: Boolean) =
        webAppDao.updatePinnedStatus(id, isPinned)
}
