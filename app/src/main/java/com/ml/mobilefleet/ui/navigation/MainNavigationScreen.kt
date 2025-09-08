package com.ml.mobilefleet.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ml.mobilefleet.navigation.FleetDestinations
import com.ml.mobilefleet.ui.trip.CompleteTripScreen
import com.ml.mobilefleet.ui.trip.StartTripScreen
import com.ml.mobilefleet.ui.trip.TripViewModel
import com.ml.mobilefleet.ui.trip.TripViewModelFactory
import com.ml.mobilefleet.ui.history.TripHistoryScreen
import com.ml.mobilefleet.ui.settings.SettingsScreen
import com.ml.mobilefleet.ui.auth.AuthViewModel

/**
 * Main navigation screen with bottom navigation bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = FleetDestinations.START_TRIP,
    authViewModel: AuthViewModel? = null,
    onAuthenticationSuccess: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Shared ViewModel across screens with AuthRepository
    val tripViewModel: TripViewModel = viewModel(
        factory = TripViewModelFactory(context)
    )
    val currentTrip by tripViewModel.currentTrip.collectAsStateWithLifecycle()

    // Initialize user data when authenticated
    LaunchedEffect(authViewModel) {
        if (authViewModel != null) {
            tripViewModel.initializeUserData()
        }
    }

    // Get current destination for bottom navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route ?: startDestination

    // Handle app lifecycle for state persistence
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Refresh trip status when app comes to foreground
                    tripViewModel.refreshTripStatus()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Determine initial destination based on trip status
    LaunchedEffect(currentTrip) {
        if (currentTrip != null && currentDestination == FleetDestinations.START_TRIP) {
            // If there's an active trip and we're on start trip screen, navigate to complete trip
            navController.navigate(FleetDestinations.COMPLETE_TRIP) {
                popUpTo(FleetDestinations.START_TRIP) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            SmartBottomNavigationBar(
                currentDestination = currentDestination,
                currentTrip = currentTrip,
                onNavigate = { destination ->
                    // Smart navigation logic
                    when (destination) {
                        FleetDestinations.START_TRIP -> {
                            if (currentTrip == null) {
                                navController.navigate(destination) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        FleetDestinations.COMPLETE_TRIP -> {
                            if (currentTrip != null) {
                                navController.navigate(destination) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        else -> {
                            // History and Settings are always accessible
                            navController.navigate(destination) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(FleetDestinations.START_TRIP) {
                StartTripScreen(
                    viewModel = tripViewModel,
                    onTripStarted = {
                        navController.navigate(FleetDestinations.COMPLETE_TRIP) {
                            // Clear the back stack to prevent going back to start trip
                            popUpTo(FleetDestinations.START_TRIP) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            
            composable(FleetDestinations.COMPLETE_TRIP) {
                CompleteTripScreen(
                    viewModel = tripViewModel,
                    onTripCompleted = {
                        // Reset trip state and navigate back to start trip
                        tripViewModel.resetTripState()
                        navController.navigate(FleetDestinations.START_TRIP) {
                            // Clear the back stack
                            popUpTo(FleetDestinations.COMPLETE_TRIP) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            
            composable(FleetDestinations.TRIP_HISTORY) {
                TripHistoryScreen(
                    viewModel = tripViewModel
                )
            }
            
            composable(FleetDestinations.SETTINGS) {
                SettingsScreen(
                    authViewModel = authViewModel
                )
            }
        }
    }
}

/**
 * Helper function to determine if navigation should be allowed
 */
private fun isNavigationAllowed(
    destination: String,
    currentTrip: Any?
): Boolean {
    return when (destination) {
        FleetDestinations.START_TRIP -> currentTrip == null
        FleetDestinations.COMPLETE_TRIP -> currentTrip != null
        FleetDestinations.TRIP_HISTORY -> true
        FleetDestinations.SETTINGS -> true
        else -> true
    }
}
