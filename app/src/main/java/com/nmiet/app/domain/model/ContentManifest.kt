package com.nmiet.app.domain.model

import org.json.JSONArray
import org.json.JSONObject

data class ContentManifestItem(
    val contentId: String,
    val title: String,
    val mediaType: ContentMediaType,
    val language: String,
    val version: Int,
    val sizeBytes: Long,
    val sha256Checksum: String,
    val publisherId: String = "relaynet_verified_curriculum"
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("contentId", contentId)
            put("title", title)
            put("mediaType", mediaType.name)
            put("language", language)
            put("version", version)
            put("sizeBytes", sizeBytes)
            put("sha256Checksum", sha256Checksum)
            put("publisherId", publisherId)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): ContentManifestItem {
            val typeStr = json.optString("mediaType", ContentMediaType.LEARNING_PACK_ZIP.name)
            val type = try {
                ContentMediaType.valueOf(typeStr)
            } catch (_: Exception) {
                ContentMediaType.LEARNING_PACK_ZIP
            }

            val rawId = json.optString("contentId", "").trim()
            if (rawId.isBlank()) {
                throw IllegalArgumentException("contentId cannot be empty or missing")
            }

            return ContentManifestItem(
                contentId = rawId,
                title = json.optString("title", "Untitled Content"),
                mediaType = type,
                language = json.optString("language", "or+en"),
                version = json.optInt("version", 1).coerceAtLeast(1),
                sizeBytes = json.optLong("sizeBytes", 0L).coerceAtLeast(0L),
                sha256Checksum = json.optString("sha256Checksum", ""),
                publisherId = json.optString("publisherId", "relaynet_curriculum")
            )
        }
    }
}

data class PeerContentManifest(
    val protocolVersion: Int = 1,
    val deviceName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val items: List<ContentManifestItem> = emptyList()
) {
    /**
     * Normalized manifest with duplicates merged by contentId, retaining the highest version.
     */
    val deduplicatedItems: List<ContentManifestItem> by lazy {
        items.groupBy { it.contentId }
            .values
            .map { list -> list.maxByOrNull { it.version } ?: list.first() }
    }

    fun serialize(): ByteArray {
        val root = JSONObject().apply {
            put("type", "RELAYNET_MANIFEST_EXCHANGE")
            put("protocolVersion", protocolVersion)
            put("deviceName", deviceName)
            put("timestamp", timestamp)
            val arr = JSONArray()
            deduplicatedItems.forEach { arr.put(it.toJson()) }
            put("items", arr)
        }
        return root.toString().toByteArray(Charsets.UTF_8)
    }

    companion object {
        fun deserialize(bytes: ByteArray): PeerContentManifest? {
            if (bytes.isEmpty()) return null
            return try {
                val str = String(bytes, Charsets.UTF_8)
                val json = JSONObject(str)
                if (json.optString("type") != "RELAYNET_MANIFEST_EXCHANGE") return null

                val version = json.optInt("protocolVersion", 1)
                val deviceName = json.optString("deviceName", "Nearby Learner").ifBlank { "Nearby Learner" }
                val timestamp = json.optLong("timestamp", System.currentTimeMillis())
                val arr = json.optJSONArray("items") ?: JSONArray()

                val itemsList = mutableListOf<ContentManifestItem>()
                for (i in 0 until arr.length()) {
                    val itemObj = arr.optJSONObject(i) ?: continue
                    try {
                        itemsList.add(ContentManifestItem.fromJson(itemObj))
                    } catch (_: Exception) {
                        // Skip malformed individual items rather than dropping the entire manifest
                    }
                }

                // Merge duplicates retaining highest version
                val deduplicated = itemsList.groupBy { it.contentId }
                    .values
                    .map { list -> list.maxByOrNull { it.version } ?: list.first() }

                PeerContentManifest(
                    protocolVersion = version,
                    deviceName = deviceName,
                    timestamp = timestamp,
                    items = deduplicated
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
