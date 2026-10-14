package com.nmiet.app.domain.model

enum class DownloadStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    COMPLETED,
    FAILED
}

enum class ContentMediaType {
    PDF_TEXTBOOK,
    VIDEO_LESSON,
    LEARNING_PACK_ZIP,
    REVISION_NOTES,
    IMAGE_DIAGRAM,
    JSON_RESOURCE
}

data class ContentItem(
    val id: String,
    val title: String,
    val description: String,
    val mediaType: ContentMediaType,
    val remoteUrl: String,
    val localFilePath: String? = null,
    val sizeBytes: Long,
    val sha256Checksum: String = "",
    val classGrade: Int = 9,
    val subject: String = "Science",
    val board: Board = Board.ODISHA_STATE,
    val downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
    val downloadProgressPercent: Int = 0,
    val version: Int = 1,
    val language: String = "English",
    val errorMessage: String? = null
) {
    val isImage: Boolean get() = mediaType == ContentMediaType.IMAGE_DIAGRAM
    val isText: Boolean get() = mediaType == ContentMediaType.REVISION_NOTES || mediaType == ContentMediaType.JSON_RESOURCE
    val isZip: Boolean get() = mediaType == ContentMediaType.LEARNING_PACK_ZIP

    val formattedSize: String get() {
        if (sizeBytes <= 0) return "Unknown size"
        if (sizeBytes < 1024) return "$sizeBytes B"
        val kb = sizeBytes / 1024.0
        if (kb < 1024) return "%.1f KB".format(kb)
        val mb = kb / 1024.0
        return "%.2f MB".format(mb)
    }
}

