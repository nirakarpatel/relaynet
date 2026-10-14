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
    val termHindi: String = "",
    val definitionOdia: String,
    val definitionEnglish: String,
    val definitionHindi: String = ""
) {
    fun getTerm(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> termEnglish
        AppLanguage.ODIA_ONLY -> termOdia
        AppLanguage.HINDI_ONLY -> if (termHindi.isNotBlank()) termHindi else termEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (termHindi.isNotBlank()) "$termEnglish / $termHindi" else termEnglish
        AppLanguage.BILINGUAL_EN_OR -> "$termEnglish / $termOdia"
    }

    fun getDefinition(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> definitionEnglish
        AppLanguage.ODIA_ONLY -> definitionOdia
        AppLanguage.HINDI_ONLY -> if (definitionHindi.isNotBlank()) definitionHindi else definitionEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (definitionHindi.isNotBlank()) "$definitionEnglish\n$definitionHindi" else definitionEnglish
        AppLanguage.BILINGUAL_EN_OR -> "$definitionEnglish\n$definitionOdia"
    }
}

data class LessonSection(
    val titleOdia: String,
    val titleEnglish: String,
    val titleHindi: String = "",
    val contentOdia: String,
    val contentEnglish: String,
    val contentHindi: String = "",
    val keyTerms: List<KeyTerm> = emptyList()
) {
    fun getTitle(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> titleEnglish
        AppLanguage.ODIA_ONLY -> titleOdia
        AppLanguage.HINDI_ONLY -> if (titleHindi.isNotBlank()) titleHindi else titleEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (titleHindi.isNotBlank()) "$titleEnglish ($titleHindi)" else titleEnglish
        AppLanguage.BILINGUAL_EN_OR -> "$titleEnglish ($titleOdia)"
    }

    fun getContent(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> contentEnglish
        AppLanguage.ODIA_ONLY -> contentOdia
        AppLanguage.HINDI_ONLY -> if (contentHindi.isNotBlank()) contentHindi else contentEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (contentHindi.isNotBlank()) "$contentEnglish\n\n[हिंदी]:\n$contentHindi" else contentEnglish
        AppLanguage.BILINGUAL_EN_OR -> "$contentEnglish\n\n[ଓଡ଼ିଆ]:\n$contentOdia"
    }
}

data class LessonData(
    val summaryOdia: String,
    val summaryEnglish: String,
    val summaryHindi: String = "",
    val equationOrFormula: String? = null,
    val sections: List<LessonSection> = emptyList()
) {
    fun getSummary(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> summaryEnglish
        AppLanguage.ODIA_ONLY -> summaryOdia
        AppLanguage.HINDI_ONLY -> if (summaryHindi.isNotBlank()) summaryHindi else summaryEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (summaryHindi.isNotBlank()) "$summaryEnglish\n\n$summaryHindi" else summaryEnglish
        AppLanguage.BILINGUAL_EN_OR -> "$summaryEnglish\n\n$summaryOdia"
    }
}

data class LearningPack(
    val packId: String,
    val titleOdia: String,
    val titleEnglish: String,
    val titleHindi: String = "",
    val board: Board,
    val classGrade: Int,
    val subject: String,
    val chapterNumber: Int = 4,
    val chapterOdia: String = "",
    val chapterEnglish: String = "",
    val chapterHindi: String = "",
    val topicOdia: String = "",
    val topicEnglish: String = "",
    val topicHindi: String = "",
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
    val receivedTimestamp: Long = System.currentTimeMillis(),
    val diagramLocalPath: String? = null
) {
    fun getTitle(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> titleEnglish
        AppLanguage.ODIA_ONLY -> titleOdia
        AppLanguage.HINDI_ONLY -> if (titleHindi.isNotBlank()) titleHindi else titleEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (titleHindi.isNotBlank()) "$titleEnglish ($titleHindi)" else titleEnglish
        AppLanguage.BILINGUAL_EN_OR -> titleOdia
    }

    fun getChapter(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> chapterEnglish
        AppLanguage.ODIA_ONLY -> chapterOdia
        AppLanguage.HINDI_ONLY -> if (chapterHindi.isNotBlank()) chapterHindi else chapterEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (chapterHindi.isNotBlank()) "$chapterEnglish ($chapterHindi)" else chapterEnglish
        AppLanguage.BILINGUAL_EN_OR -> "$chapterEnglish ($chapterOdia)"
    }

    fun getTopic(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> topicEnglish
        AppLanguage.ODIA_ONLY -> topicOdia
        AppLanguage.HINDI_ONLY -> if (topicHindi.isNotBlank()) topicHindi else topicEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (topicHindi.isNotBlank()) "$topicEnglish ($topicHindi)" else topicEnglish
        AppLanguage.BILINGUAL_EN_OR -> "$topicEnglish ($topicOdia)"
    }
}
