package com.nmiet.app.data.remote

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.StudyLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiMultimodalService(
    private val keyRotator: GeminiKeyRotator = GeminiKeyRotator.getInstance() // Rotates 5 keys
) {
    private fun getModel(apiKey: String, systemInstruction: String): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            systemInstruction = content { text(systemInstruction) }
        )
    }

    private fun buildSystemPrompt(
        language: AppLanguage,
        grade: String,
        board: String,
        studyLevel: StudyLevel
    ): String {
        return """
            You are the RelayNet Academic Tutor for school students ($grade, $board, ${studyLevel.displayName} level).
            STRICT ACADEMIC POLICY:
            - Answer ONLY curriculum-related questions in Science, Mathematics, and secondary school subjects.
            - Refuse non-academic questions (games, entertainment, personal chat) politely.
            - If solving a scanned textbook image or problem:
              1. Transcribe the exact question clearly.
              2. Provide a clear, step-by-step conceptual derivation or explanation.
              3. Highlight the final formula, answer, or balanced chemical equation.
              4. Point out common student pitfalls or misconceptions.
            - Respond in the user's selected language: ${language.displayName}. If bilingual, show the native explanation followed by the English translation.
        """.trimIndent()
    }

    private fun buildVoicePrompt(
        language: AppLanguage,
        grade: String,
        board: String
    ): String {
        val langInstruction = when (language) {
            AppLanguage.ODIA_ONLY -> "Respond in clear, natural Odia (ଓଡ଼ିଆ)."
            AppLanguage.HINDI_ONLY -> "Respond in clear, conversational Hindi (हिंदी)."
            AppLanguage.BILINGUAL_EN_OR -> "Respond in Odia with English scientific keywords."
            AppLanguage.BILINGUAL_EN_HI -> "Respond in Hindi with English scientific keywords (Hinglish)."
            AppLanguage.ENGLISH_ONLY -> "Respond in clear, simple Indian English."
        }
        return """
            You are RelayNet Academic Voice Tutor for school students ($grade, $board).
            You answer curriculum questions across all school subjects: Science, Mathematics, Biology, Chemistry, Physics, History, Geography, and Social Studies.
            CRITICAL SPOKEN VOICE-TO-VOICE INSTRUCTIONS:
            - Give a direct, helpful, and conversational educational answer.
            - Keep your response concise: exactly 2 to 3 spoken sentences (max 45 words), suitable for Text-To-Speech (TTS).
            - Strictly NO markdown formatting: do not use asterisks (* or **), hashtags (#), bullet points, or backticks.
            - $langInstruction
            - Refuse non-study / non-academic queries politely and briefly.
        """.trimIndent()
    }

    private fun cleanSpokenText(text: String): String {
        return text
            .replace("**", "")
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace("•", "")
            .replace("_", "")
            .replace(Regex("(?m)^\\s*[-*]\\s+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    // 1. TEXT / VOICE DOUBT SOLVING
    suspend fun solveTextQuery(
        query: String,
        language: AppLanguage,
        grade: String,
        board: String,
        studyLevel: StudyLevel = StudyLevel.INTERMEDIATE
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!keyRotator.hasValidKeys()) {
            return@withContext Result.failure(IllegalStateException("No valid Gemini API key configured. Fallback to offline package content."))
        }
        keyRotator.executeWithRetry { key ->
            val model = getModel(key, buildVoicePrompt(language, grade, board))
            val response = model.generateContent(query)
            val raw = response.text ?: "Could not generate an explanation. Please try again."
            cleanSpokenText(raw)
        }
    }

    // 2. SCAN & SOLVE (IMAGE + OPTIONAL QUERY)
    suspend fun solveImageProblem(
        bitmap: Bitmap,
        caption: String?,
        language: AppLanguage,
        grade: String,
        board: String,
        studyLevel: StudyLevel = StudyLevel.INTERMEDIATE
    ): Result<String> = withContext(Dispatchers.IO) {
        keyRotator.executeWithRetry { key ->
            val model = getModel(key, buildSystemPrompt(language, grade, board, studyLevel))
            val inputContent = content {
                image(bitmap)
                text(caption ?: "Please solve this textbook question step-by-step and explain the core concept.")
            }
            val response = model.generateContent(inputContent)
            response.text ?: "Could not extract or solve the question from the image."
        }
    }
}
