package com.nmiet.app.domain.model

enum class ContentOrigin {
    CLOUD_DOWNLOADED,    // Downloaded directly via cloud gateway
    P2P_RECEIVED,        // Received from peer via Bluetooth RFCOMM
    INTERNET_DOWNLOADED, // Backward compatibility alias
    P2P_SENT             // Shared outward to peers
}

data class KeyTerm(
    val termOdia: String,
    val termEnglish: String,
    val definitionOdia: String,
    val definitionEnglish: String
)

data class LessonSection(
    val titleOdia: String,
    val titleEnglish: String,
    val contentOdia: String,
    val contentEnglish: String,
    val keyTerms: List<KeyTerm> = emptyList()
)

data class LessonData(
    val summaryOdia: String,
    val summaryEnglish: String,
    val equationOrFormula: String? = null,
    val sections: List<LessonSection> = emptyList()
)

data class LearningPack(
    val packId: String,
    val titleOdia: String,
    val titleEnglish: String,
    val board: Board,
    val classGrade: Int,
    val subject: String,
    val chapterNumber: Int = 4,
    val chapterOdia: String = "",
    val chapterEnglish: String = "",
    val topicOdia: String = "",
    val topicEnglish: String = "",
    val version: Int = 1,
    val lesson: LessonData,
    val quizQuestions: List<QuizQuestion>,
    val sizeBytes: Long = 124000,
    val checksumSha256: String,
    val isDownloaded: Boolean = true,
    val isReceivedViaMesh: Boolean = false,
    val peerSenderName: String? = null,
    val sourceAttribution: String = "Odisha State Bureau of Textbook Preparation & NCERT Curriculum",
    val origin: ContentOrigin = ContentOrigin.INTERNET_DOWNLOADED,
    val receivedTimestamp: Long = System.currentTimeMillis()
)
