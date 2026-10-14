package com.nmiet.app.domain.model

import android.graphics.Bitmap

enum class QuizSourceType(val displayName: String, val odiaName: String) {
    WHOLE_CHAPTER("Whole Chapter", "ସମ୍ପୂର୍ଣ୍ଣ ଅଧ୍ୟାୟ"),
    WEAK_TOPIC("Targeted Weak Topic", "ଦୁର୍ବଳ ପ୍ରସଙ୍ଗ (Photolysis)"),
    TEXTBOOK_PHOTO("Textbook Photo", "ପାଠ୍ୟପୁସ୍ତକ ଫଟୋ")
}

enum class QuizDifficulty(val displayName: String, val odiaName: String) {
    FOUNDATION("Foundation", "ମୌଳିକ (Basic)"),
    INTERMEDIATE("Intermediate", "ମଧ୍ୟମ (Standard)"),
    EXAM_READY("Exam-Ready", "ପରୀକ୍ଷା ଉପଯୋଗୀ (Board)")
}

data class CustomQuizConfig(
    val sourceType: QuizSourceType = QuizSourceType.WHOLE_CHAPTER,
    val specificTopicTag: String = "PHOTOLYSIS_WATER",
    val questionCount: Int = 5,
    val difficulty: QuizDifficulty = QuizDifficulty.INTERMEDIATE,
    val customPhoto: Bitmap? = null
)

data class CustomQuizQuestion(
    val id: String,
    val questionOdia: String,
    val questionEnglish: String,
    val optionsOdia: List<String>,
    val optionsEnglish: List<String>,
    val correctIndex: Int,
    val explanationOdia: String,
    val explanationEnglish: String,
    val conceptTag: String,
    val difficulty: QuizDifficulty,
    val distractorExplanations: Map<Int, String> = emptyMap() // Map of wrong option index -> why it's incorrect
)
