package com.nmiet.app.domain.model

data class CurriculumNode(
    val id: String,
    val board: Board,
    val classGrade: Int,
    val subject: String,
    val chapterTitleOdia: String,
    val chapterTitleEnglish: String,
    val topicTitleOdia: String,
    val topicTitleEnglish: String,
    val learningObjectiveOdia: String,
    val learningObjectiveEnglish: String,
    val packId: String,
    val isAvailableOffline: Boolean = true
)
