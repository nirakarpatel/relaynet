package com.nmiet.app.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class GeminiImageService(private val keyRotator: GeminiKeyRotator = GeminiKeyRotator.getInstance()) {

    suspend fun generateEducationalDiagram(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        keyRotator.executeWithRetry { apiKey ->
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:predict?key=$apiKey"
            val connection = URL(endpoint).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 40000

            val enhancedPrompt = "A clear, textbook scientific diagram of $prompt. Clean white background, high contrast, labeled chemical bonds and atoms, educational diagram style, high quality."

            val jsonPayload = JSONObject().apply {
                put("instances", JSONArray().apply {
                    put(JSONObject().apply {
                        put("prompt", enhancedPrompt)
                    })
                })
                put("parameters", JSONObject().apply {
                    put("sampleCount", 1)
                    put("aspectRatio", "1:1")
                    put("outputOptions", JSONObject().apply {
                        put("mimeType", "image/jpeg")
                    })
                })
            }

            connection.outputStream.use { os ->
                os.write(jsonPayload.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val responseString = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val root = JSONObject(responseString)
                val predictions = root.getJSONArray("predictions")
                val b64Bytes = predictions.getJSONObject(0).getString("bytesBase64Encoded")
                b64Bytes
            } else {
                val errorString = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                throw Exception("Image generation failed (HTTP $responseCode): $errorString")
            }
        }
    }
}
