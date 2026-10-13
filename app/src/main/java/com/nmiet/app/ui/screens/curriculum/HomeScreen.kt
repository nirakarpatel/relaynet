package com.nmiet.app.ui.screens.curriculum

import androidx.compose.runtime.Composable

/**
 * HomeScreen / LibraryScreen alias for CurriculumScreen featuring the
 * two prominent Material 3 content buckets: "Downloaded" vs "P2P Shared & Received".
 */
@Composable
fun HomeScreen(
    viewModel: CurriculumViewModel,
    onOpenLesson: (String) -> Unit,
    onStartQuiz: (String) -> Unit,
    onShareViaBluetooth: () -> Unit
) {
    CurriculumScreen(
        viewModel = viewModel,
        onOpenLesson = onOpenLesson,
        onStartQuiz = onStartQuiz,
        onShareViaBluetooth = onShareViaBluetooth
    )
}

@Composable
fun LibraryScreen(
    viewModel: CurriculumViewModel,
    onOpenLesson: (String) -> Unit,
    onStartQuiz: (String) -> Unit,
    onShareViaBluetooth: () -> Unit
) {
    CurriculumScreen(
        viewModel = viewModel,
        onOpenLesson = onOpenLesson,
        onStartQuiz = onStartQuiz,
        onShareViaBluetooth = onShareViaBluetooth
    )
}
