package com.nmiet.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.nmiet.app.ui.screens.curriculum.CurriculumScreen
import com.nmiet.app.ui.screens.curriculum.CurriculumViewModel
import com.nmiet.app.ui.screens.lesson.LessonReaderScreen
import com.nmiet.app.ui.screens.mastery.MasteryScreen
import com.nmiet.app.ui.screens.mastery.MasteryViewModel
import com.nmiet.app.ui.screens.nearbyshare.NearbyShareScreen
import com.nmiet.app.ui.screens.nearbyshare.NearbyShareViewModel
import com.nmiet.app.ui.screens.quiz.QuizRunnerScreen
import com.nmiet.app.ui.screens.quiz.QuizViewModel
import com.nmiet.app.ui.screens.settings.SettingsScreen
import com.nmiet.app.ui.screens.settings.SettingsViewModel
import com.nmiet.app.ui.screens.tutor.AITutorScreen
import com.nmiet.app.ui.screens.tutor.AITutorViewModel

@Composable
fun AppNavHost(
    repository: ContentRepository,
    curriculumViewModel: CurriculumViewModel,
    aiTutorViewModel: AITutorViewModel,
    nearbyShareViewModel: NearbyShareViewModel,
    masteryViewModel: MasteryViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Curriculum,
        Screen.AITutor,
        Screen.Mastery,
        Screen.MeshShare,
        Screen.Settings
    )

    val isFullScreen = currentRoute?.startsWith("lesson") == true || currentRoute?.startsWith("quiz") == true

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = !isFullScreen,
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
                            icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
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
            startDestination = Routes.CURRICULUM,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.CURRICULUM) {
                CurriculumScreen(
                    viewModel = curriculumViewModel,
                    onOpenLesson = { packId ->
                        navController.navigate(Routes.lessonReader(packId))
                    },
                    onStartQuiz = { packId ->
                        navController.navigate(Routes.quizRunner(packId))
                    },
                    onShareViaBluetooth = {
                        navController.navigate(Routes.MESH_SHARE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Routes.TUTOR) {
                AITutorScreen(viewModel = aiTutorViewModel)
            }

            composable(Routes.MASTERY) {
                MasteryScreen(
                    viewModel = masteryViewModel,
                    onPracticeTopic = { packId ->
                        navController.navigate(Routes.quizRunner(packId))
                    }
                )
            }

            composable(Routes.MESH_SHARE) {
                NearbyShareScreen(
                    viewModel = nearbyShareViewModel,
                    onNavigateToCurriculum = {
                        navController.navigate(Routes.CURRICULUM) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(viewModel = settingsViewModel)
            }

            composable(
                route = Routes.LESSON_READER,
                arguments = listOf(navArgument("packId") { type = NavType.StringType })
            ) { backStackEntry ->
                val packId = backStackEntry.arguments?.getString("packId") ?: ""
                LessonReaderScreen(
                    packId = packId,
                    repository = repository,
                    onBackClick = { navController.popBackStack() },
                    onStartQuiz = { targetPackId ->
                        navController.navigate(Routes.quizRunner(targetPackId))
                    }
                )
            }

            composable(
                route = Routes.QUIZ_RUNNER,
                arguments = listOf(navArgument("packId") { type = NavType.StringType })
            ) { backStackEntry ->
                val packId = backStackEntry.arguments?.getString("packId") ?: ""
                val quizViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    QuizViewModel(repository, packId)
                }
                QuizRunnerScreen(
                    viewModel = quizViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToMastery = {
                        navController.navigate(Routes.MASTERY) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
