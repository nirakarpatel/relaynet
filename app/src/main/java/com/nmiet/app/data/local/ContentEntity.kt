package com.nmiet.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nmiet.app.domain.model.ContentItem

@Entity(tableName = "contents")
data class ContentEntity(
    @PrimaryKey
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
) {
    fun toDomain(): ContentItem = ContentItem(
        id = id,
        title = title,
        summary = summary,
        body = body,
        category = category,
        source = source,
        publishedAt = publishedAt,
        contentHash = contentHash,
        isSavedOffline = isSavedOffline,
        isReceivedViaNearby = isReceivedViaNearby,
        receivedFromPeerName = receivedFromPeerName
    )

    companion object {
        fun fromDomain(item: ContentItem): ContentEntity = ContentEntity(
            id = item.id,
            title = item.title,
            summary = item.summary,
            body = item.body,
            category = item.category,
            source = item.source,
            publishedAt = item.publishedAt,
            contentHash = item.contentHash,
            isSavedOffline = item.isSavedOffline,
            isReceivedViaNearby = item.isReceivedViaNearby,
            receivedFromPeerName = item.receivedFromPeerName
        )
    }
}
