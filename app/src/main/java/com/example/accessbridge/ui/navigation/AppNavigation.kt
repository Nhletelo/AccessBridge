package com.example.accessbridge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.accessbridge.ui.auth.AuthScreen
import com.example.accessbridge.ui.detail.ResourceDetailScreen
import com.example.accessbridge.ui.help.HelpScreen
import com.example.accessbridge.ui.home.HomeScreen
import com.example.accessbridge.ui.learn.LearnScreen
import com.example.accessbridge.ui.onboarding.OnboardingScreen
import com.example.accessbridge.ui.services.ServicesScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding,
        modifier = modifier
    ) {
        composable<Screen.Onboarding> {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Onboarding) { inclusive = true }
                    }
                },
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth)
                }
            )
        }

        composable<Screen.Auth> {
            AuthScreen(
                onBack = { navController.popBackStack() },
                onAuthSuccess = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Auth) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Home> {
            HomeScreen(
                onNavigateToAuth = { navController.navigate(Screen.Auth) },
                onNavigateToLearn = { navController.navigate(Screen.Learn) },
                onNavigateToServices = { navController.navigate(Screen.Services) },
                onNavigateToHelp = { navController.navigate(Screen.Help) },
                onNavigateToDetail = { resourceId ->
                    navController.navigate(Screen.ResourceDetail(resourceId))
                }
            )
        }

        composable<Screen.Learn> {
            LearnScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { resourceId ->
                    navController.navigate(Screen.ResourceDetail(resourceId))
                }
            )
        }

        composable<Screen.Services> {
            ServicesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { resourceId ->
                    navController.navigate(Screen.ResourceDetail(resourceId))
                }
            )
        }

        composable<Screen.Help> {
            HelpScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { resourceId ->
                    navController.navigate(Screen.ResourceDetail(resourceId))
                }
            )
        }

        composable<Screen.ResourceDetail> { backStackEntry ->
            val detailRoute: Screen.ResourceDetail = backStackEntry.toRoute()
            ResourceDetailScreen(
                resourceId = detailRoute.resourceId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
