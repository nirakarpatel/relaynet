package com.nmiet.app.domain.model

enum class Board(val displayName: String, val code: String) {
    ODISHA_STATE("Odisha State Board (BSE)", "ODISHA_STATE"),
    CBSE("Central Board of Secondary Education (CBSE)", "CBSE"),
    CISCE_ICSE("Indian Certificate of Secondary Education (ICSE)", "CISCE_ICSE")
}

data class LearnerProfile(
    val studentName: String = "Soumya Ranjan",
    val educationalBoard: String = "ODISHA_STATE", // "ODISHA_STATE", "CBSE", "CISCE_ICSE"
    val classGrade: String = "Class 9",            // "Class 6" through "Class 10"
    val languageMode: AppLanguage = AppLanguage.BILINGUAL_EN_HI,
    val studentId: String = "student_pilot_01"
) {
    // Secondary constructor for compatibility without default arguments to prevent ambiguity with parameterless constructor
    constructor(
        studentId: String,
        name: String,
        classGrade: Int,
        board: Board,
        preferredLanguage: AppLanguage
    ) : this(
        studentName = name,
        educationalBoard = board.name,
        classGrade = "Class $classGrade",
        languageMode = preferredLanguage,
        studentId = studentId
    )

    // Backwards-compatible accessors
    val name: String get() = studentName
    val board: Board get() = try { Board.valueOf(educationalBoard) } catch (_: Exception) { Board.ODISHA_STATE }
    val preferredLanguage: AppLanguage get() = languageMode
    val gradeInt: Int get() = classGrade.filter { it.isDigit() }.toIntOrNull() ?: 9

    fun withBoard(b: Board): LearnerProfile = copy(educationalBoard = b.name)
    fun withBoard(b: String): LearnerProfile = copy(educationalBoard = b)
    fun withGrade(g: String): LearnerProfile = copy(classGrade = g)
    fun withGrade(g: Int): LearnerProfile = copy(classGrade = "Class $g")
    fun withName(n: String): LearnerProfile = copy(studentName = n)
    fun withLanguage(l: AppLanguage): LearnerProfile = copy(languageMode = l)
}
