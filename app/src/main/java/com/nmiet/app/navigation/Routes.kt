package com.nmiet.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Feed : Screen("feed", "Campus Feed", Icons.Default.Article)
    data object Nearby : Screen("nearby", "Nearby Share", Icons.Default.Share)
    data object Library : Screen("library", "Offline Library", Icons.Default.Bookmark)
    data object Settings : Screen("settings", "Diagnostics", Icons.Default.Settings)
}

object Routes {
    const val FEED = "feed"
    const val NEARBY = "nearby"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val CONTENT_DETAIL = "content_detail/{itemId}"

    fun contentDetail(itemId: String): String = "content_detail/$itemId"
}
