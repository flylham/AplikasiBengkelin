package com.bengkelin.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bengkelin.app.data.model.UserRole
import com.bengkelin.app.ui.auth.AuthViewModel
import com.bengkelin.app.ui.auth.LoginScreen
import com.bengkelin.app.ui.auth.RegisterScreen
import com.bengkelin.app.ui.estimator.CostEstimatorScreen
import com.bengkelin.app.ui.home.RoleHomeScreen
import com.bengkelin.app.ui.passport.VehiclePassportScreen
import com.bengkelin.app.ui.splash.SplashScreen
import com.bengkelin.app.ui.workshop.WorkshopListScreen

@Composable
fun BengkelinNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAuthSuccessful, uiState.currentUser) {
        if (uiState.isAuthSuccessful && uiState.currentUser != null) {
            val destination = when (uiState.currentUser?.userRole) {
                UserRole.PENGGUNA -> Screen.HomePengguna.route
                UserRole.PEMILIK_BENGKEL -> Screen.HomePemilikBengkel.route
                UserRole.ADMIN -> Screen.HomeAdmin.route
                null -> Screen.HomePengguna.route
            }
            navController.navigate(destination) {
                popUpTo(Screen.Login.route) { inclusive = true }
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (uiState.currentUser != null) {
                        val destination = when (uiState.currentUser?.userRole) {
                            UserRole.PENGGUNA -> Screen.HomePengguna.route
                            UserRole.PEMILIK_BENGKEL -> Screen.HomePemilikBengkel.route
                            UserRole.ADMIN -> Screen.HomeAdmin.route
                            null -> Screen.Login.route
                        }
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                uiState = uiState,
                onEvent = authViewModel::onEvent,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                uiState = uiState,
                onEvent = authViewModel::onEvent,
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.HomePengguna.route) {
            RoleHomeScreen(
                user = uiState.currentUser,
                onNavigateToWorkshopSearch = {
                    navController.navigate(Screen.WorkshopList.route)
                },
                onNavigateToEstimator = {
                    navController.navigate(Screen.CostEstimator.route)
                },
                onNavigateToPassport = {
                    navController.navigate(Screen.VehiclePassport.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CostEstimator.route) {
            CostEstimatorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToWorkshops = {
                    navController.navigate(Screen.WorkshopList.route)
                }
            )
        }

        composable(Screen.VehiclePassport.route) {
            VehiclePassportScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.WorkshopList.route) {
            WorkshopListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.HomePemilikBengkel.route) {
            RoleHomeScreen(
                user = uiState.currentUser,
                onNavigateToWorkshopSearch = {
                    navController.navigate(Screen.WorkshopList.route)
                },
                onNavigateToEstimator = {
                    navController.navigate(Screen.CostEstimator.route)
                },
                onNavigateToPassport = {
                    navController.navigate(Screen.VehiclePassport.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.HomeAdmin.route) {
            RoleHomeScreen(
                user = uiState.currentUser,
                onNavigateToWorkshopSearch = {
                    navController.navigate(Screen.WorkshopList.route)
                },
                onNavigateToEstimator = {
                    navController.navigate(Screen.CostEstimator.route)
                },
                onNavigateToPassport = {
                    navController.navigate(Screen.VehiclePassport.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
