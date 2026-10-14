package com.nmiet.app.ai.llm

import java.io.File

/**
 * Metadata and specifications for an on-device Large Language Model.
 */
data class LocalModelInfo(
    val modelId: String,
    val displayName: String,
    val architecture: String,
    val variant: String,
    val format: String,
    val approxSizeBytes: Long,
    val expectedSha256: String,
    val pinnedRevision: String,
    val sourceUrl: String,
    val license: String,
    val description: String,
    val academicGuidance: String,
    val minRamGb: Int = 2,
    val isInstalled: Boolean = false,
    val installedFile: File? = null
)

/**
 * Registry of certified edge models for RelayNet offline peer learning.
 */
object LocalModelRegistry {

    /**
     * Primary candidate: Qwen3 0.6B INT4 (Non-thinking variant).
     *
     * Exact artifact metadata verified from Hugging Face Git LFS:
     * - Repository: litert-community/Qwen3-0.6B-int4
     * - Pinned Revision: 6aa2daf8aba4aa456797fb8040b36a3948bcfda7
     * - Filename: qwen3_0.6b_nothink_q4_block32_ekv1280.litertlm
     * - Size: 347,251,840 bytes (331.16 MiB)
     * - Git LFS OID / SHA-256: 2df6821ec12702dafd33915e7a1a1adc7c4b053f3672fd9555dfaf3a114c4139
     * - License: Apache 2.0
     */
    val QWEN3_0_6B_NOTHINK = LocalModelInfo(
        modelId = "qwen3_0.6b_nothink_int4",
        displayName = "Qwen3 0.6B (INT4 No-Think)",
        architecture = "Qwen3 Dense Transformer",
        variant = "INT4 Blockwise (Group 32, Pre-closed reasoning turn)",
        format = ".litertlm",
        approxSizeBytes = 347_251_840L,
        expectedSha256 = "2df6821ec12702dafd33915e7a1a1adc7c4b053f3672fd9555dfaf3a114c4139",
        pinnedRevision = "6aa2daf8aba4aa456797fb8040b36a3948bcfda7",
        sourceUrl = "https://huggingface.co/litert-community/Qwen3-0.6B-int4/resolve/6aa2daf8aba4aa456797fb8040b36a3948bcfda7/qwen3_0.6b_nothink_q4_block32_ekv1280.litertlm",
        license = "Apache 2.0",
        description = "Lightweight on-device language model packaged for Google LiteRT-LM. Provides direct, low-latency conversational responses without chain-of-thought overhead.",
        academicGuidance = "Development Smoke Test Variant: Optimized for low latency on edge mobile CPUs (~331 MB). Not intended for complex multi-step reasoning, advanced mathematical derivations, or comprehensive academic curriculum mastery.",
        minRamGb = 2
    )

    /**
     * All registered models available for on-device deployment.
     */
    val availableModels: List<LocalModelInfo> = listOf(QWEN3_0_6B_NOTHINK)

    fun getModelById(id: String): LocalModelInfo? = availableModels.find { it.modelId == id }
}
