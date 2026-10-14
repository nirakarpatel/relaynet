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
    REVISION_NOTES
}

data class ContentItem(
    val id: String,
    val title: String,
    val description: String,
    val mediaType: ContentMediaType,
    val remoteUrl: String,
    val localFilePath: String? = null,
    val sizeBytes: Long,
    val sha256Checksum: String,
    val classGrade: Int = 9,
    val subject: String = "Science",
    val board: Board = Board.ODISHA_STATE,
    val downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
    val downloadProgressPercent: Int = 0,
    val version: Int = 1
)
