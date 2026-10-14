package com.nmiet.app.domain.model

enum class Board(val displayName: String, val code: String) {
    ODISHA_STATE("Odisha State Board (BSE)", "ODISHA_STATE"),
    CBSE("Central Board of Secondary Education (CBSE)", "CBSE"),
    CISCE_ICSE("Indian Certificate of Secondary Education (ICSE)", "CISCE_ICSE")
}

data class LearnerProfile(
    val studentId: String = "student_pilot_01",
    val name: String = "Soumya Ranjan",
    val email: String = "student@nmiet.edu.in",
    val avatarUrl: String = "",
    val classGrade: Int = 9,
    val board: Board = Board.ODISHA_STATE,
    val preferredLanguage: AppLanguage = AppLanguage.BILINGUAL_EN_OR,
    val isLoggedIn: Boolean = false,
    val authProvider: String = "GUEST",
    val sessionToken: String = "",
    val schoolName: String = "",
    val learningInterests: List<String> = emptyList(),
    val isProfileCompleted: Boolean = true
)
