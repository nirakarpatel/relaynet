package com.nmiet.app.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nmiet.app.data.local.LanguageManager
import com.nmiet.app.data.remote.GeminiKeyRotator
import com.nmiet.app.domain.repository.ContentRepository
import com.nmiet.app.speech.VoiceAssistantManager
import com.nmiet.app.ui.components.VoiceAssistantModal
import com.nmiet.app.ui.components.VoiceAssistantOverlay
import com.nmiet.app.ui.screens.curriculum.CurriculumScreen
import com.nmiet.app.ui.screens.curriculum.CurriculumViewModel
import com.nmiet.app.ui.screens.customquiz.CustomQuizRunnerScreen
import com.nmiet.app.ui.screens.customquiz.CustomQuizViewModel
import com.nmiet.app.ui.screens.lesson.LessonReaderScreen
import com.nmiet.app.ui.screens.mastery.MasteryScreen
import com.nmiet.app.ui.screens.mastery.MasteryViewModel
import com.nmiet.app.ui.screens.nearbyshare.NearbyShareScreen
import com.nmiet.app.ui.screens.nearbyshare.NearbyShareViewModel
import com.nmiet.app.ui.screens.notes.NotesGeneratorViewModel
import com.nmiet.app.ui.screens.notes.NotesViewScreen
import com.nmiet.app.ui.screens.profile.AboutRelaynetScreen
import com.nmiet.app.ui.screens.profile.AppSettingsScreen
import com.nmiet.app.ui.screens.profile.ConnectivityPermissionsScreen
import com.nmiet.app.ui.screens.profile.OfflineContentScreen
import com.nmiet.app.ui.screens.profile.PrivacySecurityScreen
import com.nmiet.app.ui.screens.profile.ProfileScreen
import com.nmiet.app.ui.screens.quiz.QuizRunnerScreen
import com.nmiet.app.ui.screens.quiz.QuizViewModel
import com.nmiet.app.ui.screens.settings.SettingsViewModel
import com.nmiet.app.ui.screens.tutor.AITutorScreen
import com.nmiet.app.ui.screens.tutor.AITutorViewModel
import com.nmiet.app.ui.theme.PrimaryBlue

@Composable
fun AppNavHost(
    repository: ContentRepository,
    curriculumViewModel: CurriculumViewModel,
    aiTutorViewModel: AITutorViewModel,
    nearbyShareViewModel: NearbyShareViewModel,
    masteryViewModel: MasteryViewModel,
    settingsViewModel: SettingsViewModel,
    languageManager: LanguageManager? = null,
    geminiKeyRotator: GeminiKeyRotator? = null,
    voiceAssistantManager: VoiceAssistantManager? = null
) {
    val context = LocalContext.current
    val actualLanguageManager = languageManager ?: LanguageManager.getInstance(context.applicationContext)
    val actualKeyRotator = geminiKeyRotator ?: GeminiKeyRotator.getInstance()

    val voiceManager = voiceAssistantManager ?: remember {
        VoiceAssistantManager(
            context = context.applicationContext,
            repository = repository,
            languageManager = actualLanguageManager,
            geminiKeyRotator = actualKeyRotator
        )
    }

    var isVoiceAssistantOpen by rememberSaveable { mutableStateOf(false) }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isVoiceAssistantOpen = true
            voiceManager.startListening()
        }
    }

    val onMicClick = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            isVoiceAssistantOpen = true
            voiceManager.startListening()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tab 5: Profile replaces Settings
    val bottomNavItems = listOf(
        Screen.Curriculum,
        Screen.Tutor,
        Screen.Mastery,
        Screen.NearbyShare,
        Screen.Profile
    )

    val isFullScreen = currentRoute?.startsWith("lesson") == true ||
            currentRoute?.startsWith("quiz") == true ||
            currentRoute?.startsWith("notes") == true ||
            currentRoute?.startsWith("custom_quiz") == true ||
            currentRoute == Routes.LOGIN ||
            currentRoute == Routes.PROFILE_COMPLETION

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
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
                        // Profile tab is selected whenever route begins with "profile"
                        val isSelected = if (screen == Screen.Profile) {
                            currentRoute?.startsWith("profile") == true
                        } else {
                            currentRoute == screen.route
                        }

                        val icon = if (screen == Screen.Profile) {
                            if (isSelected) Icons.Default.Person else Icons.Outlined.Person
                        } else {
                            screen.icon
                        }

                        NavigationBarItem(
                            icon = { Icon(imageVector = icon, contentDescription = screen.title) },
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
        },
        floatingActionButton = {
            if (!isFullScreen && currentRoute != Screen.Tutor.route) {
                FloatingActionButton(
                    onClick = onMicClick,
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Assistant")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.CURRICULUM,
                modifier = Modifier.fillMaxSize()
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
                        },
                        onGenerateNotes = { packId ->
                            navController.navigate(Routes.notesView(packId))
                        }
                    )
                }

                composable(Routes.TUTOR) {
                    AITutorScreen(viewModel = aiTutorViewModel)
                }

                composable(Routes.MASTERY) {
                    MasteryScreen(
                        viewModel = masteryViewModel,
                        onReadTopic = { packId, section ->
                            navController.navigate(Routes.lessonReader(packId, section))
                        },
                        onRetakeTopicQuiz = { packId, topicTag ->
                            navController.navigate(Routes.quizRunner(packId, topicTag))
                        },
                        onPracticeTopic = { packId ->
                            navController.navigate(Routes.customQuiz(packId))
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

                // Profile Overview Root Screen
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        navController = navController,
                        settingsViewModel = settingsViewModel
                    )
                }

                // Profile Sub-destinations
                composable(Screen.AppSettings.route) {
                    AppSettingsScreen(
                        navController = navController,
                        settingsViewModel = settingsViewModel
                    )
                }

                composable(Screen.ConnectivityPermissions.route) {
                    ConnectivityPermissionsScreen(
                        navController = navController,
                        settingsViewModel = settingsViewModel
                    )
                }

                composable(Screen.OfflineContent.route) {
                    OfflineContentScreen(
                        navController = navController,
                        settingsViewModel = settingsViewModel
                    )
                }

                composable(Screen.PrivacySecurity.route) {
                    PrivacySecurityScreen(navController = navController)
                }

                composable(Screen.AboutRelaynet.route) {
                    AboutRelaynetScreen(navController = navController)
                }

                // Backward-compatibility: redirect legacy settings route to AppSettingsScreen
                composable(Routes.SETTINGS) {
                    AppSettingsScreen(
                        navController = navController,
                        settingsViewModel = settingsViewModel
                    )
                }

                composable(
                    route = Routes.LESSON_READER,
                    arguments = listOf(
                        navArgument("packId") { type = NavType.StringType },
                        navArgument("section") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val packId = backStackEntry.arguments?.getString("packId") ?: ""
                    val section = backStackEntry.arguments?.getString("section")
                    LessonReaderScreen(
                        packId = packId,
                        targetSection = section,
                        repository = repository,
                        onBackClick = { navController.popBackStack() },
                        onStartQuiz = { targetPackId ->
                            navController.navigate(Routes.quizRunner(targetPackId))
                        }
                    )
                }

                composable(
                    route = Routes.QUIZ_RUNNER,
                    arguments = listOf(
                        navArgument("packId") { type = NavType.StringType },
                        navArgument("topicTag") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val packId = backStackEntry.arguments?.getString("packId") ?: ""
                    val topicTag = backStackEntry.arguments?.getString("topicTag")
                    val quizViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                        QuizViewModel(repository, packId, topicTag, curriculumViewModel.languageManager)
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

                // MODULE 1: Automated Notes View / Generator Screen
                composable(
                    route = Routes.NOTES_VIEW,
                    arguments = listOf(
                        navArgument("topicId") {
                            type = NavType.StringType
                            defaultValue = "relaynet_class9_science_photosynthesis_v1"
                        }
                    )
                ) { backStackEntry ->
                    val topicId = backStackEntry.arguments?.getString("topicId") ?: "relaynet_class9_science_photosynthesis_v1"
                    val notesViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                        NotesGeneratorViewModel(repository)
                    }
                    NotesViewScreen(
                        viewModel = notesViewModel,
                        topicId = topicId,
                        onBackClick = { navController.popBackStack() },
                        onShareViaNearby = {
                            navController.navigate(Routes.MESH_SHARE)
                        }
                    )
                }

                // MODULE 2: Automated Custom Quiz Generator & Runner
                composable(
                    route = Routes.CUSTOM_QUIZ,
                    arguments = listOf(
                        navArgument("topicId") {
                            type = NavType.StringType
                            defaultValue = "relaynet_class9_science_photosynthesis_v1"
                        }
                    )
                ) {
                    val customQuizViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                        CustomQuizViewModel(repository)
                    }
                    CustomQuizRunnerScreen(
                        viewModel = customQuizViewModel,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToNotes = { targetTopic ->
                            navController.navigate(Routes.notesView(targetTopic))
                        },
                        onNavigateToMastery = {
                            navController.navigate(Routes.MASTERY) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                // Authentication & Profile Completion Screens
                composable(Routes.LOGIN) {
                    val authViewModel: com.nmiet.app.ui.screens.auth.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                        com.nmiet.app.ui.screens.auth.AuthViewModel(repository)
                    }
                    com.nmiet.app.ui.screens.auth.LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToHome = {
                            navController.navigate(Routes.CURRICULUM) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToProfileCompletion = {
                            navController.navigate(Routes.PROFILE_COMPLETION) {
                                launchSingleTop = true
                            }
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Routes.PROFILE_COMPLETION) {
                    val authViewModel: com.nmiet.app.ui.screens.auth.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                        com.nmiet.app.ui.screens.auth.AuthViewModel(repository)
                    }
                    com.nmiet.app.ui.screens.auth.ProfileCompletionScreen(
                        viewModel = authViewModel,
                        onProfileCompleted = {
                            navController.navigate(Routes.CURRICULUM) {
                                popUpTo(Routes.LOGIN) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }

        }

        // Real-Time Conversational Voice Assistant Overlay
        if (isVoiceAssistantOpen) {
            VoiceAssistantModal(
                manager = voiceManager,
                onDismiss = {
                    isVoiceAssistantOpen = false
                    voiceManager.dismiss()
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
