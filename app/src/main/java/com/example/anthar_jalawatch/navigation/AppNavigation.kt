package com.example.anthar_jalawatch.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.anthar_jalawatch.ui.screens.AlertsScreen
import com.example.anthar_jalawatch.ui.screens.BorewellViewModel
import com.example.anthar_jalawatch.ui.screens.DashboardScreen
import com.example.anthar_jalawatch.ui.screens.LoginScreen
import com.example.anthar_jalawatch.ui.screens.LogBorewellScreen
import com.example.anthar_jalawatch.ui.screens.RechargeGuideScreen
import com.example.anthar_jalawatch.ui.screens.SignUpScreen
import com.example.anthar_jalawatch.ui.screens.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val DASHBOARD = "dashboard"
    const val LOG_BOREWELL = "log_borewell"
    const val RECHARGE = "recharge"
    const val ALERTS = "alerts"
}

@Composable
fun AppNavigation(
    borewellViewModel: BorewellViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(onSplashFinished = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(Routes.SIGNUP) },
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.SIGNUP) {
            SignUpScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        popUpTo(Routes.SIGNUP) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = borewellViewModel,
                onNavigateToLog = { navController.navigate(Routes.LOG_BOREWELL) },
                onNavigateToRecharge = { navController.navigate(Routes.RECHARGE) },
                onNavigateToAlerts = { navController.navigate(Routes.ALERTS) }
            )
        }
        composable(Routes.LOG_BOREWELL) {
            LogBorewellScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = borewellViewModel
            )
        }
        composable(Routes.RECHARGE) {
            RechargeGuideScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.ALERTS) {
            AlertsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = borewellViewModel
            )
        }
    }
}
