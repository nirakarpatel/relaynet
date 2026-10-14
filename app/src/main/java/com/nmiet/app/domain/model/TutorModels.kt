package com.nmiet.app.domain.model

import android.graphics.Bitmap

enum class TutorSender {
    USER, TUTOR
}

data class TutorMessage(
    val id: String,
    val sender: TutorSender,
    val textOdia: String,
    val textEnglish: String,
    val textHindi: String = "",
    val timestamp: Long,
    val isOfflineGrounded: Boolean = false,
    val sourceChapter: String? = null,
    val isStudyRelated: Boolean = true,
    val imageBitmap: Bitmap? = null,
    val sourceTags: List<String> = emptyList(),
    val isCloudAI: Boolean = false,
    val isNemotronReasoning: Boolean = false
) {
    fun getPrimaryText(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> textEnglish
        AppLanguage.ODIA_ONLY -> textOdia.ifBlank { textEnglish }
        AppLanguage.HINDI_ONLY -> if (textHindi.isNotBlank()) textHindi else textEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (textHindi.isNotBlank()) textHindi else textEnglish
        AppLanguage.BILINGUAL_EN_OR -> textOdia.ifBlank { textEnglish }
    }

    fun getSecondaryText(language: AppLanguage): String? = when (language) {
        AppLanguage.BILINGUAL_EN_HI -> if (textHindi.isNotBlank()) textEnglish else null
        AppLanguage.BILINGUAL_EN_OR -> if (textOdia.isNotBlank() && textOdia != textEnglish) textEnglish else null
        else -> null
    }
}

