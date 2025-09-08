package com.ml.mobilefleet.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ml.mobilefleet.data.repository.AuthRepository
import com.ml.mobilefleet.ui.auth.AuthViewModel
import com.ml.mobilefleet.ui.auth.AuthViewModelFactory
import com.ml.mobilefleet.ui.auth.LoginScreen

/**
 * Main app navigation that handles authentication flow
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Authentication ViewModel
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context)
    )
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    
    // Check for existing session on app start
    LaunchedEffect(Unit) {
        authViewModel.checkExistingSession()
    }
    
    if (authState.isAuthenticated && authState.currentDriver != null) {
        // User is authenticated, show main app
        MainNavigationScreen(
            modifier = modifier,
            authViewModel = authViewModel,
            onAuthenticationSuccess = {
                // This will be called when the main screen is first displayed
                // The TripViewModel will initialize user data automatically
            }
        )
    } else {
        // User is not authenticated, show login screen
        LoginScreen(
            viewModel = authViewModel,
            onLoginSuccess = {
                // Navigation will be handled automatically by state change
            },
            modifier = modifier.fillMaxSize()
        )
    }
}
