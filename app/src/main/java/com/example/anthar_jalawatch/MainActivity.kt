package com.example.anthar_jalawatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anthar_jalawatch.navigation.AppNavigation
import com.example.anthar_jalawatch.ui.screens.BorewellViewModel
import com.example.anthar_jalawatch.ui.theme.AntharJalaWatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AntharJalaWatchTheme {
                val borewellViewModel: BorewellViewModel = viewModel()
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(borewellViewModel = borewellViewModel)
                }
            }
        }
    }
}
