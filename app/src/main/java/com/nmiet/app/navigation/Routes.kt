package com.nmiet.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Curriculum : Screen("curriculum", "Lessons", Icons.AutoMirrored.Filled.MenuBook)
    data object AITutor : Screen("tutor", "AI Tutor", Icons.Default.AutoAwesome)
    data object MeshShare : Screen("mesh", "P2P Share", Icons.Default.Share)
    data object Mastery : Screen("mastery", "Mastery", Icons.Default.Leaderboard)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

object Routes {
    const val CURRICULUM = "curriculum"
    const val TUTOR = "tutor"
    const val MESH_SHARE = "mesh"
    const val MASTERY = "mastery"
    const val SETTINGS = "settings"
    const val LESSON_READER = "lesson/{packId}"
    const val QUIZ_RUNNER = "quiz/{packId}"

    fun lessonReader(packId: String): String = "lesson/$packId"
    fun quizRunner(packId: String): String = "quiz/$packId"
}
