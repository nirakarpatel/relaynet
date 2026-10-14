package com.nmiet.app.domain.model

data class StudyTask(
    val id: String,
    val topicId: String,
    val topicTitle: String,
    val subject: String,
    val scheduledDate: Long,
    val isCompleted: Boolean = false,
    val reason: String = "Scheduled revision based on mastery diagnosis",
    val estimatedMinutes: Int = 15
)

data class StudyPlan(
    val planId: String = "default_study_plan",
    val targetClass: Int = 9,
    val weeklyGoalHours: Int = 5,
    val tasks: List<StudyTask> = emptyList()
)
