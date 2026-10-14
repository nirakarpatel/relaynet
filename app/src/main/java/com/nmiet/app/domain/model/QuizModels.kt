package com.nmiet.app.domain.model

data class QuizQuestion(
    val id: String,
    val questionOdia: String,
    val questionEnglish: String,
    val questionHindi: String = "",
    val optionsOdia: List<String>,
    val optionsEnglish: List<String>,
    val optionsHindi: List<String> = emptyList(),
    val correctIndex: Int,
    val explanationOdia: String,
    val explanationEnglish: String,
    val explanationHindi: String = "",
    val conceptTag: String,
    val misconceptionHintOdia: String,
    val misconceptionHintEnglish: String,
    val misconceptionHintHindi: String = ""
) {
    fun getQuestion(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> questionEnglish
        AppLanguage.ODIA_ONLY -> questionOdia
        AppLanguage.HINDI_ONLY -> if (questionHindi.isNotBlank()) questionHindi else questionEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (questionHindi.isNotBlank()) "$questionEnglish\n$questionHindi" else questionEnglish
        AppLanguage.BILINGUAL_EN_OR -> "$questionEnglish\n$questionOdia"
    }

    fun getOptions(language: AppLanguage): List<String> = when (language) {
        AppLanguage.ENGLISH_ONLY -> optionsEnglish
        AppLanguage.ODIA_ONLY -> optionsOdia
        AppLanguage.HINDI_ONLY -> if (optionsHindi.isNotEmpty()) optionsHindi else optionsEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (optionsHindi.isNotEmpty()) {
            optionsEnglish.mapIndexed { idx, optEn ->
                val optHi = optionsHindi.getOrNull(idx) ?: ""
                if (optHi.isNotBlank()) "$optEn ($optHi)" else optEn
            }
        } else optionsEnglish
        AppLanguage.BILINGUAL_EN_OR -> optionsOdia
    }

    fun getExplanation(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> explanationEnglish
        AppLanguage.ODIA_ONLY -> explanationOdia
        AppLanguage.HINDI_ONLY -> if (explanationHindi.isNotBlank()) explanationHindi else explanationEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (explanationHindi.isNotBlank()) "$explanationEnglish\n$explanationHindi" else explanationEnglish
        AppLanguage.BILINGUAL_EN_OR -> "$explanationEnglish\n$explanationOdia"
    }

    fun getMisconceptionHint(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> misconceptionHintEnglish
        AppLanguage.ODIA_ONLY -> misconceptionHintOdia
        AppLanguage.HINDI_ONLY -> if (misconceptionHintHindi.isNotBlank()) misconceptionHintHindi else misconceptionHintEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (misconceptionHintHindi.isNotBlank()) misconceptionHintHindi else misconceptionHintEnglish
        AppLanguage.BILINGUAL_EN_OR -> misconceptionHintOdia
    }
}

data class QuizAttempt(
    val id: String,
    val topicId: String,
    val topicName: String,
    val scorePercent: Int,
    val correctCount: Int,
    val totalCount: Int,
    val weakConcepts: List<String>,
    val timestamp: Long,
    val isSynced: Boolean = false
)

data class TopicMastery(
    val topicId: String,
    val topicTitleOdia: String,
    val topicTitleEnglish: String,
    val topicTitleHindi: String = "",
    val subject: String,
    val masteryScore: Int, // 0 to 100
    val lastPracticedDate: Long,
    val attemptsCount: Int,
    val recommendedNextAction: String
) {
    fun getTitle(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> topicTitleEnglish
        AppLanguage.ODIA_ONLY -> topicTitleOdia
        AppLanguage.HINDI_ONLY -> if (topicTitleHindi.isNotBlank()) topicTitleHindi else topicTitleEnglish
        AppLanguage.BILINGUAL_EN_HI -> if (topicTitleHindi.isNotBlank()) "$topicTitleEnglish ($topicTitleHindi)" else topicTitleEnglish
        AppLanguage.BILINGUAL_EN_OR -> "$topicTitleEnglish ($topicTitleOdia)"
    }

    val tierLabel: String
        get() = when {
            masteryScore >= 85 -> "Strong Mastery (ଉତ୍କୃଷ୍ଟ ଦକ୍ଷତା)"
            masteryScore >= 70 -> "Good Progress (ଉତ୍ତମ ଅଗ୍ରଗତି)"
            masteryScore >= 40 -> "Developing (ବିକାଶଶୀଳ)"
            else -> "Needs Foundation Practice (ମୂଳଧାରଣା ଅଭ୍ୟାସ ଆବଶ୍ୟକ)"
        }

    fun getLocalizedTier(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> when {
            masteryScore >= 85 -> "Strong Mastery (>85%)"
            masteryScore >= 70 -> "Good Progress (70–85%)"
            masteryScore >= 40 -> "Developing (40–70%)"
            else -> "Needs Foundation Practice (<40%)"
        }
        AppLanguage.ODIA_ONLY -> when {
            masteryScore >= 85 -> "ଉତ୍କୃଷ୍ଟ ଦକ୍ଷତା (>85%)"
            masteryScore >= 70 -> "ଉତ୍ତମ ଅଗ୍ରଗତି (70–85%)"
            masteryScore >= 40 -> "ବିକାଶଶୀଳ (40–70%)"
            else -> "ମୂଳଧାରଣା ଅଭ୍ୟାସ ଆବଶ୍ୟକ (<40%)"
        }
        AppLanguage.HINDI_ONLY -> when {
            masteryScore >= 85 -> "उत्कृष्ट प्रवीणता (>85%)"
            masteryScore >= 70 -> "अच्छी प्रगति (70–85%)"
            masteryScore >= 40 -> "विकासशील (40–70%)"
            else -> "मूल अभ्यास आवश्यक (<40%)"
        }
        AppLanguage.BILINGUAL_EN_HI -> when {
            masteryScore >= 85 -> "Strong Mastery • उत्कृष्ट प्रवीणता"
            masteryScore >= 70 -> "Good Progress • अच्छी प्रगति"
            masteryScore >= 40 -> "Developing • विकासशील"
            else -> "Needs Practice • मूल अभ्यास आवश्यक"
        }
        AppLanguage.BILINGUAL_EN_OR -> when {
            masteryScore >= 85 -> "Strong Mastery (ଉତ୍କୃଷ୍ଟ ଦକ୍ଷତା)"
            masteryScore >= 70 -> "Good Progress (ଉତ୍ତମ ଅଗ୍ରଗତି)"
            masteryScore >= 40 -> "Developing (ବିକାଶଶୀଳ)"
            else -> "Needs Foundation Practice (ମୂଳଧାରଣା ଅଭ୍ୟାସ ଆବଶ୍ୟକ)"
        }
    }
}
