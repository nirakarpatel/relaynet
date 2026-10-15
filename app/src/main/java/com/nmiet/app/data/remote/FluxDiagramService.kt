package com.nmiet.app.data.remote

import android.util.Log
import com.nmiet.app.BuildConfig
import com.nmiet.app.domain.model.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FluxDiagramService(
    private val apiKey: String = BuildConfig.NVIDIA_API_KEY
) {
    companion object {
        private const val TAG = "FluxDiagramService"
        private const val NIM_URL = "https://integrate.api.nvidia.com/v1/images/generations"
        private const val MODEL = "black-forest-labs/flux.2-klein-4b"

        private val DIAGRAM_INTENT_PATTERNS = listOf(
            "generate a image",
            "generate an image",
            "generate image",
            "draw",
            "sketch",
            "diagram",
            "bond diagram",
            "flowchart",
            "structure",
            "molecular",
            "formula chart",
            "visualize",
            "चित्र",
            "ଚିତ୍ର"
        )

        fun isDiagramIntent(query: String): Boolean {
            val q = query.lowercase().trim()
            return DIAGRAM_INTENT_PATTERNS.any { q.contains(it) }
        }
    }

    suspend fun generateDiagram(
        prompt: String,
        subjectContext: String = "Class 9 Science",
        language: AppLanguage = AppLanguage.BILINGUAL_EN_OR
    ): Result<String> = withContext(Dispatchers.IO) {
        val expandedPrompt = (
            "A crisp, highly structured educational school textbook diagram and scientific illustration of $prompt for $subjectContext. " +
            "Style: Indian school textbook illustration (NCERT / BSE Odisha style), clean white-to-light cream paper background, clear vector lines, labeled diagram. " +
            "Ensure exact process flows, arrow sequences, and appropriate chemical/mathematical formulas. " +
            "Text labeling and captions include English and ${language.displayName} side-by-side. 16:9 aspect ratio, high contrast, no blur."
        )

        // Try direct NVIDIA NIM API call if key is available
        if (apiKey.isNotBlank() && !apiKey.startsWith("YOUR_")) {
            try {
                val url = URL(NIM_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 10000
                conn.readTimeout = 30000
                conn.doOutput = true
                conn.setRequestProperty("Authorization", "Bearer $apiKey")
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

                val payload = JSONObject().apply {
                    put("model", MODEL)
                    put("prompt", expandedPrompt)
                    put("aspect_ratio", "16:9")
                    put("steps", 4)
                    put("response_format", "b64_json")
                }

                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8))
                    val res = reader.readText()
                    reader.close()
                    val json = JSONObject(res)
                    val dataArr = json.optJSONArray("data")
                    if (dataArr != null && dataArr.length() > 0) {
                        val b64 = dataArr.getJSONObject(0).optString("b64_json", "")
                        if (b64.isNotBlank()) {
                            return@withContext Result.success(b64)
                        }
                    }
                } else {
                    val err = conn.errorStream?.bufferedReader()?.readText() ?: ""
                    Log.w(TAG, "NVIDIA NIM response code ${conn.responseCode}: $err")
                }
            } catch (e: Exception) {
                Log.e(TAG, "NVIDIA NIM direct call failed: ${e.message}")
            }
        }

        // Try backend gateway fallback: /tutor/generate-visual
        val backendUrls = listOf(
            "http://10.0.2.2:8000/tutor/generate-visual",
            "http://127.0.0.1:8000/tutor/generate-visual"
        )
        for (endpoint in backendUrls) {
            try {
                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 4000
                conn.readTimeout = 20000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

                val payload = JSONObject().apply {
                    put("user_prompt", prompt)
                    put("subject_context", subjectContext)
                    put("language_mode", language.code)
                }

                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                if (conn.responseCode == 200) {
                    val res = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(res)
                    val b64 = json.optString("imageBase64", "")
                    if (b64.isNotBlank()) {
                        return@withContext Result.success(b64)
                    }
                }
            } catch (_: Exception) {}
        }

        Result.failure(Exception("Could not synthesize visual diagram. Please check your network connection."))
    }
}
