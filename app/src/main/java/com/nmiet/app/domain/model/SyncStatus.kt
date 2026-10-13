package com.nmiet.app.domain.model

data class SyncStatus(
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: Long? = null,
    val totalCachedCount: Int = 0,
    val totalSavedOfflineCount: Int = 0,
    val lastSyncMessage: String? = null
)
