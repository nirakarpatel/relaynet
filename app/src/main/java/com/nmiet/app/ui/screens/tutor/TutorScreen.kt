package com.nmiet.app.ui.screens.tutor

import androidx.compose.runtime.Composable

typealias TutorViewModel = AITutorViewModel

@Composable
fun TutorScreen(viewModel: TutorViewModel) {
    AITutorScreen(viewModel = viewModel)
}
