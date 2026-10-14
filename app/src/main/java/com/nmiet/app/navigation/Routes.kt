package com.nmiet.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String = "",
    val icon: ImageVector = Icons.Default.Person
) {
    data object Curriculum : Screen("curriculum", "Curriculum", Icons.Default.AutoStories)
    data object Tutor : Screen("tutor", "AI Tutor", Icons.Default.SmartToy)
    data object Mastery : Screen("mastery", "Mastery", Icons.Default.Insights)
    data object NearbyShare : Screen("mesh", "P2P Share", Icons.Default.CellTower)

    // Replace Settings with Profile root and sub-routes
    data object Profile : Screen("profile", "Profile", Icons.Outlined.Person)
    data object AppSettings : Screen("profile/app_settings", "App Settings", Icons.Default.Tune)
    data object ConnectivityPermissions : Screen("profile/connectivity_permissions", "Connectivity & Permissions", Icons.Default.Bluetooth)
    data object OfflineContent : Screen("profile/offline_content", "Offline Content", Icons.Default.Storage)
    data object PrivacySecurity : Screen("profile/privacy_security", "Privacy & Security", Icons.Default.Security)
    data object AboutRelaynet : Screen("profile/about", "About Relaynet", Icons.Default.Info)

    companion object {
        val AITutor = Tutor
        val MeshShare = NearbyShare
        val Settings = AppSettings
    }
}

object Routes {
    const val CURRICULUM = "curriculum"
    const val TUTOR = "tutor"
    const val MESH_SHARE = "mesh"
    const val NEARBY_SHARE = "nearby_share"
    const val MASTERY = "mastery"
    const val SETTINGS = "settings"

    const val PROFILE = "profile"
    const val APP_SETTINGS = "profile/app_settings"
    const val CONNECTIVITY_PERMISSIONS = "profile/connectivity_permissions"
    const val OFFLINE_CONTENT = "profile/offline_content"
    const val PRIVACY_SECURITY = "profile/privacy_security"
    const val ABOUT_RELAYNET = "profile/about"

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
