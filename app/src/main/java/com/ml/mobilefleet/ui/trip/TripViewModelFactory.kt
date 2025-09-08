package com.ml.mobilefleet.ui.trip

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ml.mobilefleet.data.repository.AuthRepository
import com.ml.mobilefleet.data.repository.FirebaseRepository

/**
 * Factory for creating TripViewModel with required dependencies
 */
class TripViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            return TripViewModel(
                repository = FirebaseRepository(),
                authRepository = AuthRepository(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
