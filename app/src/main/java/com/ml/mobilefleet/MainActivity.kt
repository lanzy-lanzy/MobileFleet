package com.ml.mobilefleet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.ml.mobilefleet.navigation.FleetNavigation
import com.ml.mobilefleet.ui.theme.MobileFleetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileFleetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FleetNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}