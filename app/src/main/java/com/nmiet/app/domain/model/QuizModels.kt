package com.nmiet.app.domain.model

data class QuizQuestion(
    val id: String,
    val questionOdia: String,
    val questionEnglish: String,
    val optionsOdia: List<String>,
    val optionsEnglish: List<String>,
    val correctIndex: Int,
    val explanationOdia: String,
    val explanationEnglish: String,
    val conceptTag: String,
    val misconceptionHintOdia: String,
    val misconceptionHintEnglish: String
)

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
    val subject: String,
    val masteryScore: Int, // 0 to 100
    val lastPracticedDate: Long,
    val attemptsCount: Int,
    val recommendedNextAction: String
) {
    val tierLabel: String
        get() = when {
            masteryScore >= 85 -> "Strong Mastery (ଉତ୍କୃଷ୍ଟ ଦକ୍ଷତା)"
            masteryScore >= 70 -> "Good Progress (ଉତ୍ତମ ଅଗ୍ରଗତି)"
            masteryScore >= 40 -> "Developing (ବିକାଶଶୀଳ)"
            else -> "Needs Foundation Practice (ମୂଳଧାରଣା ଅଭ୍ୟାସ ଆବଶ୍ୟକ)"
        }
}
