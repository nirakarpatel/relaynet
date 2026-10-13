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
    const val LESSON_READER = "lesson/{packId}"
    const val QUIZ_RUNNER = "quiz/{packId}"

    fun lessonReader(packId: String): String = "lesson/$packId"
    fun quizRunner(packId: String): String = "quiz/$packId"
}
