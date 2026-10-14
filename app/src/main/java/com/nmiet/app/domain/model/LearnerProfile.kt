package com.nmiet.app.domain.model

enum class Board(val displayName: String, val code: String) {
    ODISHA_STATE("Odisha State Board (BSE)", "ODISHA_STATE"),
    CBSE("Central Board of Secondary Education (CBSE)", "CBSE"),
    CISCE_ICSE("Indian Certificate of Secondary Education (ICSE)", "CISCE_ICSE")
}

enum class AppLanguage(val displayName: String, val code: String) {
    ODIA("ଓଡ଼ିଆ (Odia)", "or"),
    ENGLISH("English", "en"),
    BILINGUAL("Bilingual (ଓଡ଼ିଆ + English)", "bi"),
    HINDI("हिन्दी (Hindi)", "hi"),
    BENGALI("বাংলা (Bengali)", "bn"),
    TAMIL("தமிழ் (Tamil)", "ta"),
    TELUGU("తెలుగు (Telugu)", "te")
}

data class LearnerProfile(
    val studentId: String = "student_pilot_01",
    val name: String = "Soumya Ranjan",
    val classGrade: Int = 9,
    val board: Board = Board.ODISHA_STATE,
    val preferredLanguage: AppLanguage = AppLanguage.BILINGUAL
)
