package com.nmiet.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Curriculum : Screen("curriculum", "Curriculum", Icons.Default.AutoStories)
    data object AITutor : Screen("tutor", "AI Tutor", Icons.Default.SmartToy)
    data object Mastery : Screen("mastery", "Mastery", Icons.Default.Insights)
    data object MeshShare : Screen("mesh", "P2P Share", Icons.Default.CellTower)
    data object Settings : Screen("settings", "Settings", Icons.Default.Tune)
}

object Routes {
    const val CURRICULUM = "curriculum"
    const val TUTOR = "tutor"
    const val MESH_SHARE = "mesh"
    const val MASTERY = "mastery"
    const val SETTINGS = "settings"
    const val LESSON_READER = "lesson/{packId}?section={section}"
    const val QUIZ_RUNNER = "quiz/{packId}?topicTag={topicTag}"
    const val NOTES_VIEW = "notes?topicId={topicId}"
    const val CUSTOM_QUIZ = "custom_quiz?topicId={topicId}"

    fun lessonReader(packId: String, section: String? = null): String =
        if (!section.isNullOrBlank()) "lesson/$packId?section=$section" else "lesson/$packId"

    fun quizRunner(packId: String, topicTag: String? = null): String =
        if (!topicTag.isNullOrBlank()) "quiz/$packId?topicTag=$topicTag" else "quiz/$packId"

    fun notesView(topicId: String = "relaynet_class9_science_photosynthesis_v1"): String =
        "notes?topicId=$topicId"

    fun customQuiz(topicId: String = "relaynet_class9_science_photosynthesis_v1"): String =
        "custom_quiz?topicId=$topicId"
}
