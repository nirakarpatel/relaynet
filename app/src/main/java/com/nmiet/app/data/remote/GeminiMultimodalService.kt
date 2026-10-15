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

    // 1. TEXT / VOICE DOUBT SOLVING
    suspend fun solveTextQuery(
        query: String,
        language: AppLanguage,
        grade: String,
        board: String,
        studyLevel: StudyLevel = StudyLevel.INTERMEDIATE
    ): Result<String> = withContext(Dispatchers.IO) {
        keyRotator.executeWithRetry { key ->
            val model = getModel(key, buildSystemPrompt(language, grade, board, studyLevel))
            val response = model.generateContent(query)
            response.text ?: "Could not generate an explanation. Please try again."
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
