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
    val preferredLanguage: AppLanguage = AppLanguage.BILINGUAL_EN_HI,
    val isLoggedIn: Boolean = false,
    val authProvider: String = "GUEST",
    val sessionToken: String = "",
    val schoolName: String = "",
    val learningInterests: List<String> = emptyList(),
    val isProfileCompleted: Boolean = true
) {
    // Secondary constructor for compatibility with string-based parameters (e.g. from UserPreferencesRepository)
    constructor(
        studentName: String,
        educationalBoard: String = "ODISHA_STATE",
        classGrade: String = "Class 9",
        languageMode: AppLanguage = AppLanguage.BILINGUAL_EN_HI,
        studentId: String = "student_pilot_01"
    ) : this(
        studentId = studentId,
        name = studentName,
        classGrade = classGrade.filter { it.isDigit() }.toIntOrNull() ?: 9,
        board = try { Board.valueOf(educationalBoard) } catch (_: Exception) { Board.ODISHA_STATE },
        preferredLanguage = languageMode
    )

    // Backwards-compatible accessors
    val studentName: String get() = name
    val educationalBoard: String get() = board.name
    val languageMode: AppLanguage get() = preferredLanguage
    val gradeInt: Int get() = classGrade

    fun withBoard(b: Board): LearnerProfile = copy(board = b)
    fun withBoard(b: String): LearnerProfile = copy(board = try { Board.valueOf(b) } catch (_: Exception) { Board.ODISHA_STATE })
    fun withGrade(g: String): LearnerProfile = copy(classGrade = g.filter { it.isDigit() }.toIntOrNull() ?: 9)
    fun withGrade(g: Int): LearnerProfile = copy(classGrade = g)
    fun withName(n: String): LearnerProfile = copy(name = n)
    fun withLanguage(l: AppLanguage): LearnerProfile = copy(preferredLanguage = l)
}
