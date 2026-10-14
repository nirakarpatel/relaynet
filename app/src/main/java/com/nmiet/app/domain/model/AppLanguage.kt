package com.nmiet.app.domain.model

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val isBilingual: Boolean = false,
    val primaryCode: String,
    val secondaryCode: String? = null
) {
    ENGLISH_ONLY(
        code = "en",
        displayName = "English",
        isBilingual = false,
        primaryCode = "en"
    ),
    ODIA_ONLY(
        code = "or",
        displayName = "ଓଡ଼ିଆ (Odia)",
        isBilingual = false,
        primaryCode = "or"
    ),
    HINDI_ONLY(
        code = "hi",
        displayName = "हिंदी (Hindi)",
        isBilingual = false,
        primaryCode = "hi"
    ),
    BILINGUAL_EN_HI(
        code = "en_hi",
        displayName = "English + हिंदी",
        isBilingual = true,
        primaryCode = "en",
        secondaryCode = "hi"
    ),
    BILINGUAL_EN_OR(
        code = "en_or",
        displayName = "English + ଓଡ଼ିଆ",
        isBilingual = true,
        primaryCode = "en",
        secondaryCode = "or"
    );

    companion object {
        // Backwards compatibility aliases
        val ODIA = ODIA_ONLY
        val ENGLISH = ENGLISH_ONLY
        val BILINGUAL = BILINGUAL_EN_OR

        fun fromCode(code: String?): AppLanguage =
            entries.find { it.code == code }
                ?: when (code) {
                    "bi" -> BILINGUAL_EN_OR
                    "or" -> ODIA_ONLY
                    "en" -> ENGLISH_ONLY
                    "hi" -> HINDI_ONLY
                    "en_hi" -> BILINGUAL_EN_HI
                    "en_or" -> BILINGUAL_EN_OR
                    else -> BILINGUAL_EN_OR
                }
    }
}
