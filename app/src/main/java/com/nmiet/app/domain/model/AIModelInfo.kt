package com.nmiet.app.domain.model

data class AIModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val format: String, // e.g. "GGUF Q4_K_M", "TFLite", "ONNX"
    val sizeBytes: Long,
    val minRamBytes: Long,
    val supportedLanguages: List<String>,
    val license: String,
    val version: String,
    val downloadUrl: String,
    val sha256Checksum: String,
    val isDownloaded: Boolean = false,
    val isActive: Boolean = false
)
