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
    val sourceTag: String = "[Cloud AI]",      // "[Cloud AI] RelayNet Tutor", "[Cloud AI] Smart Visuals", or "[Offline Grounded]"
    val contentType: ContentType = when {
        !generatedDiagramBase64.isNullOrBlank() -> ContentType.GENERATED_DIAGRAM
        !userUploadedImageUri.isNullOrBlank() -> ContentType.PHOTO_QUERY
        else -> ContentType.TEXT
    },
    val timestamp: Long = System.currentTimeMillis(),
    val imageBitmap: android.graphics.Bitmap? = null
) {
    val isFromUser: Boolean get() = sender == MessageSender.USER
}

fun decodeBase64ToBitmap(b64: String): android.graphics.Bitmap? {
    return try {
        val cleanB64 = b64.substringAfter("base64,")
        val decodedBytes = android.util.Base64.decode(cleanB64, android.util.Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (_: Exception) {
        null
    }
}
