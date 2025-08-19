package com.ml.mobilefleet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ml.mobilefleet.ui.trip.CompleteTripScreen
import com.ml.mobilefleet.ui.trip.StartTripScreen
import com.ml.mobilefleet.ui.trip.TripViewModel

/**
 * Navigation routes for the Fleet app
 */
object FleetDestinations {
    const val START_TRIP = "start_trip"
    const val COMPLETE_TRIP = "complete_trip"
}

/**
 * Main navigation composable for the Fleet app
 */
@Composable
fun FleetNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = FleetDestinations.START_TRIP
) {
    // Shared ViewModel across screens
    val tripViewModel: TripViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
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
    }
}
