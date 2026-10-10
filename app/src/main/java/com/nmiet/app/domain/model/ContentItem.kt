package com.nmiet.app.domain.model

data class ContentItem(
    val id: String,
    val title: String,
    val summary: String,
    val body: String,
    val category: String,
    val source: String,
    val publishedAt: Long,
    val contentHash: String,
    val isSavedOffline: Boolean = false,
    val isReceivedViaNearby: Boolean = false,
    val receivedFromPeerName: String? = null
)
