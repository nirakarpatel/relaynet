package com.nmiet.app.data.repository

import com.nmiet.app.data.local.ContentDao
import com.nmiet.app.data.local.ContentEntity
import com.nmiet.app.data.remote.FakeRemoteContentSource
import com.nmiet.app.data.remote.RemoteContentSource
import com.nmiet.app.domain.model.ContentItem
import com.nmiet.app.domain.model.SyncStatus
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class ContentRepositoryImpl(
    private val contentDao: ContentDao,
    private val remoteSource: RemoteContentSource = FakeRemoteContentSource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContentRepository {

    private val _syncStatus = MutableStateFlow(
        SyncStatus(
            isOnline = true,
            isSyncing = false,
            lastSyncTimestamp = null,
            totalCachedCount = 0,
            totalSavedOfflineCount = 0,
            lastSyncMessage = "Initialized. Ready to refresh."
        )
    )

    override fun getAllContentFlow(): Flow<List<ContentItem>> {
        return contentDao.getAllContents().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getContentByIdFlow(id: String): Flow<ContentItem?> {
        return contentDao.getContentById(id).map { it?.toDomain() }
    }

    override fun getSavedOfflineContentFlow(): Flow<List<ContentItem>> {
        return contentDao.getSavedOfflineContents().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSyncStatusFlow(): Flow<SyncStatus> = _syncStatus.asStateFlow()

    override fun setSimulatedNetworkOnline(isOnline: Boolean) {
        _syncStatus.update { current ->
            current.copy(
                isOnline = isOnline,
                lastSyncMessage = if (isOnline) "Network restored (Online)" else "Device is Offline (Using Cached Data)"
            )
        }
    }

    override suspend fun refreshContentFromRemote(): Result<Int> = withContext(ioDispatcher) {
        val currentStatus = _syncStatus.value
        if (!currentStatus.isOnline) {
            val cachedCount = contentDao.countAll()
            _syncStatus.update {
                it.copy(
                    isSyncing = false,
                    lastSyncMessage = "Device is offline. Serving $cachedCount cached items."
                )
            }
            return@withContext Result.failure(IllegalStateException("Device is currently offline."))
        }

        _syncStatus.update { it.copy(isSyncing = true, lastSyncMessage = "Fetching latest feeds...") }

        try {
            val remoteResult = remoteSource.fetchLatestContent()
            if (remoteResult.isSuccess) {
                val remoteItems = remoteResult.getOrThrow()
                var updatedCount = 0

                val entitiesToInsert = remoteItems.map { remoteItem ->
                    val existing = contentDao.getContentByIdOnce(remoteItem.id)
                    if (existing == null) {
                        updatedCount++
                        ContentEntity.fromDomain(remoteItem)
                    } else {
                        // Preserve local saved/peer attributes if already present
                        existing.copy(
                            title = remoteItem.title,
                            summary = remoteItem.summary,
                            body = remoteItem.body,
                            category = remoteItem.category,
                            source = remoteItem.source,
                            publishedAt = remoteItem.publishedAt,
                            contentHash = remoteItem.contentHash
                        )
                    }
                }

                contentDao.insertAll(entitiesToInsert)

                val totalCount = contentDao.countAll()
                val savedCount = contentDao.countSavedOffline()
                val now = System.currentTimeMillis()

                _syncStatus.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncTimestamp = now,
                        totalCachedCount = totalCount,
                        totalSavedOfflineCount = savedCount,
                        lastSyncMessage = "Refreshed successfully ($totalCount cached)."
                    )
                }

                Result.success(updatedCount)
            } else {
                val error = remoteResult.exceptionOrNull() ?: Exception("Remote fetch failed")
                _syncStatus.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncMessage = "Sync failed: ${error.localizedMessage}"
                    )
                }
                Result.failure(error)
            }
        } catch (e: Exception) {
            _syncStatus.update {
                it.copy(
                    isSyncing = false,
                    lastSyncMessage = "Sync failed: ${e.localizedMessage}"
                )
            }
            Result.failure(e)
        }
    }

    override suspend fun toggleSavedOffline(id: String) = withContext(ioDispatcher) {
        val existing = contentDao.getContentByIdOnce(id) ?: return@withContext
        val newSavedState = !existing.isSavedOffline
        contentDao.updateSavedOffline(id, newSavedState)

        val totalSaved = contentDao.countSavedOffline()
        _syncStatus.update { it.copy(totalSavedOfflineCount = totalSaved) }
    }

    override suspend fun saveIncomingPeerContent(items: List<ContentItem>, peerName: String): Int = withContext(ioDispatcher) {
        var newOrUpdatedCount = 0
        val entities = items.map { item ->
            val existing = contentDao.getContentByIdOnce(item.id)
            newOrUpdatedCount++
            ContentEntity(
                id = item.id,
                title = item.title,
                summary = item.summary,
                body = item.body,
                category = item.category,
                source = item.source,
                publishedAt = item.publishedAt,
                contentHash = item.contentHash,
                isSavedOffline = true, // Received items are immediately saved offline
                isReceivedViaNearby = true,
                receivedFromPeerName = peerName
            )
        }

        contentDao.insertAll(entities)

        val totalCount = contentDao.countAll()
        val savedCount = contentDao.countSavedOffline()
        _syncStatus.update {
            it.copy(
                totalCachedCount = totalCount,
                totalSavedOfflineCount = savedCount,
                lastSyncMessage = "Received ${items.size} item(s) from $peerName over Nearby Share."
            )
        }

        newOrUpdatedCount
    }

    override suspend fun clearAllCache() = withContext(ioDispatcher) {
        contentDao.clearAll()
        _syncStatus.update {
            it.copy(
                totalCachedCount = 0,
                totalSavedOfflineCount = 0,
                lastSyncTimestamp = null,
                lastSyncMessage = "Cache cleared."
            )
        }
    }

    override suspend fun resetToInitialDemoData() = withContext(ioDispatcher) {
        contentDao.clearAll()
        val initialItems = FakeRemoteContentSource.getInitialCatalog()
        contentDao.insertAll(initialItems.map { ContentEntity.fromDomain(it) })

        val totalCount = contentDao.countAll()
        val savedCount = contentDao.countSavedOffline()
        _syncStatus.update {
            it.copy(
                totalCachedCount = totalCount,
                totalSavedOfflineCount = savedCount,
                lastSyncTimestamp = System.currentTimeMillis(),
                lastSyncMessage = "Reset to default demo catalog ($totalCount items)."
            )
        }
    }
}
