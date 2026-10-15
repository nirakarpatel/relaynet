package com.nmiet.app.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

class GeminiKeyRotator(private var apiKeys: List<String> = DEFAULT_KEYS) {
    private val currentIndex = AtomicInteger(0)
    private val mutex = Mutex()

    fun hasValidKeys(): Boolean {
        return apiKeys.any { it.isNotBlank() && !it.startsWith("YOUR_") }
    }

    fun getValidKeys(): List<String> {
        return apiKeys.filter { it.isNotBlank() && !it.startsWith("YOUR_") }
    }

    fun getActiveKey(): String {
        val valid = getValidKeys()
        if (valid.isEmpty()) return ""
        return valid[currentIndex.get() % valid.size]
    }

    fun getActiveKeyIndex(): Int {
        val valid = getValidKeys()
        if (valid.isEmpty()) return 1
        return (currentIndex.get() % valid.size) + 1
    }

    fun updateKeys(newKeys: List<String>) {
        val cleaned = newKeys.map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("YOUR_") }
        if (cleaned.isNotEmpty()) {
            apiKeys = cleaned
            currentIndex.set(0)
            Log.i("GeminiKeyRotator", "Updated Gemini keys with ${cleaned.size} active keys.")
        }
    }

    suspend fun rotateKey(failedKey: String? = null) = mutex.withLock {
        val valid = getValidKeys()
        if (valid.isEmpty()) return@withLock
        if (failedKey == null || failedKey == getActiveKey()) {
            val next = (currentIndex.incrementAndGet()) % valid.size
            Log.w("GeminiKeyRotator", "Switching to next Gemini API Key [Index: $next]")
        }
    }

    suspend fun <T> executeWithRetry(maxRetries: Int = 3, call: suspend (apiKey: String) -> T): Result<T> {
        val valid = getValidKeys()
        if (valid.isEmpty()) {
            return Result.failure(IllegalStateException("No valid Gemini API keys configured. Please add gemini.key1 in local.properties."))
        }

        var attempts = 0
        var lastException: Throwable? = null
        val retries = maxRetries.coerceAtMost(valid.size)

        while (attempts < retries) {
            val key = getActiveKey()
            try {
                val result = call(key)
                return Result.success(result)
            } catch (e: Exception) {
                lastException = e
                Log.e("GeminiKeyRotator", "Key [Index: ${getActiveKeyIndex()}] failed: ${e.message}. Rotating...")
                rotateKey(key)
                attempts++
            }
        }
        return Result.failure(lastException ?: Exception("All Gemini API keys exhausted."))
    }

    suspend fun queryAcademicTutor(
        prompt: String,
        languageMode: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val systemInstruction = """
            You are the RelayNet Academic Voice Tutor. You speak to students in $languageMode.
            - Answer in concise, conversational spoken sentences (max 2-3 short sentences).
            - Strictly answer school academic, science, and math questions only. Refuse non-study chatter politely.
            - Do not use markdown formatting, bullets, or asterisks since your response will be read aloud by TTS.
        """.trimIndent()

        executeWithRetry { key ->
            callGeminiApi(prompt = prompt, systemInstruction = systemInstruction, apiKey = key)
        }
    }

    private fun callGeminiApi(prompt: String, systemInstruction: String, apiKey: String): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 7000
        conn.readTimeout = 9000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

        val payload = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("maxOutputTokens", 200)
            })
        }

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(payload.toString())
            writer.flush()
        }

        val code = conn.responseCode
        if (code in 200..299) {
            val responseText = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val content = candidates.getJSONObject(0).optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val rawAnswer = parts.getJSONObject(0).optString("text", "")
                    return cleanSpokenText(rawAnswer)
                }
            }
            throw IllegalStateException("Empty content parts in Gemini response")
        } else {
            val errBody = conn.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            throw IllegalStateException("Gemini HTTP $code: $errBody")
        }
    }

    private fun cleanSpokenText(text: String): String {
        return text
            .replace("**", "")
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace("_", "")
            .trim()
    }

    companion object {
        val DEFAULT_KEYS: List<String> = run {
            val fromConfig = try {
                com.nmiet.app.BuildConfig.GEMINI_KEYS
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() && !it.startsWith("YOUR_") }
            } catch (_: Throwable) {
                emptyList()
            }

            if (fromConfig.isNotEmpty()) {
                fromConfig
            } else {
                emptyList()
            }
        }

        @Volatile
        private var instance: GeminiKeyRotator? = null

        fun getInstance(keys: List<String> = DEFAULT_KEYS): GeminiKeyRotator {
            return instance ?: synchronized(this) {
                instance ?: GeminiKeyRotator(keys).also { instance = it }
            }
        }
    }
}
