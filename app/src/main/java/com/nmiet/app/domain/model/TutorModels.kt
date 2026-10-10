package com.nmiet.app.domain.model

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
    val sourceChapter: String? = null
)
