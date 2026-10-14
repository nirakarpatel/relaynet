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
    val timestamp: Long,
    val isOfflineGrounded: Boolean = false,
    val sourceChapter: String? = null,
    val isStudyRelated: Boolean = true,
    val imageBitmap: Bitmap? = null,
    val sourceTags: List<String> = emptyList(),
    val isCloudAI: Boolean = false
)
