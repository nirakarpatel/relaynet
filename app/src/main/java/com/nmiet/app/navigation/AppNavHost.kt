package com.nmiet.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nmiet.app.domain.repository.ContentRepository
import com.nmiet.app.ui.screens.contentdetail.ContentDetailScreen
import com.nmiet.app.ui.screens.home.HomeScreen
import com.nmiet.app.ui.screens.home.HomeViewModel
import com.nmiet.app.ui.screens.library.LibraryScreen
import com.nmiet.app.ui.screens.library.LibraryViewModel
import com.nmiet.app.ui.screens.nearbyshare.NearbyShareScreen
import com.nmiet.app.ui.screens.nearbyshare.NearbyShareViewModel
import com.nmiet.app.ui.screens.settings.SettingsScreen
import com.nmiet.app.ui.screens.settings.SettingsViewModel

@Composable
fun AppNavHost(
    repository: ContentRepository,
    homeViewModel: HomeViewModel,
    nearbyShareViewModel: NearbyShareViewModel,
    libraryViewModel: LibraryViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Feed,
        Screen.Nearby,
        Screen.Library,
        Screen.Settings
    )

    val isDetailScreen = currentRoute?.startsWith("content_detail") == true
    val syncStatus by repository.getSyncStatusFlow().collectAsState(initial = com.nmiet.app.domain.model.SyncStatus())

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = !isDetailScreen,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                if (screen == Screen.Library && syncStatus.totalSavedOfflineCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge {
                                                Text(syncStatus.totalSavedOfflineCount.toString())
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = screen.icon, contentDescription = screen.title)
                                    }
                                } else {
                                    Icon(imageVector = screen.icon, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.FEED,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.FEED) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onItemClick = { itemId ->
                        navController.navigate(Routes.contentDetail(itemId))
                    }
                )
            }

            composable(Routes.NEARBY) {
                NearbyShareScreen(
                    viewModel = nearbyShareViewModel,
                    onNavigateToLibrary = {
                        navController.navigate(Routes.LIBRARY) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Routes.LIBRARY) {
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onItemClick = { itemId ->
                        navController.navigate(Routes.contentDetail(itemId))
                    },
                    onNavigateToFeed = {
                        navController.navigate(Routes.FEED) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    viewModel = settingsViewModel
                )
            }

            composable(
                route = Routes.CONTENT_DETAIL,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                ContentDetailScreen(
                    itemId = itemId,
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToNearbyShare = {
                        navController.navigate(Routes.NEARBY) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
