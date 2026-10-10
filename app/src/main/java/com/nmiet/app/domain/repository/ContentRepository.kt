package com.nmiet.app.domain.repository

import com.nmiet.app.domain.model.ContentItem
import com.nmiet.app.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    fun getAllContentFlow(): Flow<List<ContentItem>>
    fun getContentByIdFlow(id: String): Flow<ContentItem?>
    fun getSavedOfflineContentFlow(): Flow<List<ContentItem>>
    fun getSyncStatusFlow(): Flow<SyncStatus>

    suspend fun refreshContentFromRemote(): Result<Int>
    suspend fun toggleSavedOffline(id: String)
    suspend fun saveIncomingPeerContent(items: List<ContentItem>, peerName: String): Int
    suspend fun clearAllCache()
    suspend fun resetToInitialDemoData()
    fun setSimulatedNetworkOnline(isOnline: Boolean)
}
