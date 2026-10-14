package com.nmiet.app.ui.screens.auth

import androidx.compose.runtime.Composable

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    LoginScreen(
        viewModel = viewModel,
        onNavigateToHome = onAuthSuccess,
        onNavigateToProfileCompletion = onAuthSuccess,
        onBackClick = onBackClick
    )
}
