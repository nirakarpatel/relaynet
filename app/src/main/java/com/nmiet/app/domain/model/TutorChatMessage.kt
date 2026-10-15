package com.nmiet.app.domain.model

import java.util.UUID

enum class MessageSender { USER, TUTOR }
enum class ContentType { TEXT, PHOTO_QUERY, GENERATED_DIAGRAM }

data class TutorChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String? = null,
    val userUploadedImageUri: String? = null, // Path/URI for scanned textbook photos
    val generatedDiagramBase64: String? = null, // Base64 data for FLUX diagrams
    val sourceTag: String = "[Cloud AI]",      // "[Cloud AI] Gemini 1.5", "[Cloud AI] FLUX.2", or "[Offline Grounded]"
    val contentType: ContentType = when {
        !generatedDiagramBase64.isNullOrBlank() -> ContentType.GENERATED_DIAGRAM
        !userUploadedImageUri.isNullOrBlank() -> ContentType.PHOTO_QUERY
        else -> ContentType.TEXT
    },
    val timestamp: Long = System.currentTimeMillis()
)
