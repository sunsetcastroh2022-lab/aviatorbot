package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.AviatorRoutes
import com.example.ui.AviatorViewModel
import com.example.ui.screens.AdminAuthScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.BotBrowserAndHudScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AviatorAoApp()
            }
        }
    }
}

@Composable
fun AviatorAoApp(
    viewModel: AviatorViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val reloadTrigger by viewModel.reloadTrigger.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = AviatorRoutes.USER_LOGIN
    ) {
        composable(route = AviatorRoutes.USER_LOGIN) {
            LoginScreen(
                users = uiState.allUsers,
                loginError = uiState.loginError,
                onClearError = viewModel::clearLoginError,
                onLoginSubmit = { username, password ->
                    viewModel.loginWithCredentials(
                        username = username,
                        password = password,
                        onNavigateToBrowser = {
                            navController.navigate(AviatorRoutes.BOT_BROWSER) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAdminDashboard = {
                            navController.navigate(AviatorRoutes.ADMIN_DASHBOARD) {
                                launchSingleTop = true
                            }
                        }
                    )
                },
                onOpenAdminPanel = {
                    viewModel.openAdminAuthScreen()
                    if (uiState.adminAuthUnlocked) {
                        navController.navigate(AviatorRoutes.ADMIN_DASHBOARD) {
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(AviatorRoutes.ADMIN_AUTH) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(route = AviatorRoutes.ADMIN_AUTH) {
            val now = System.currentTimeMillis()
            val activeCount = uiState.allUsers.count { it.isActive && it.expiresAtMillis > now }
            AdminAuthScreen(
                activeUsersCount = activeCount,
                totalActivitiesCount = uiState.subscriberActivities.size,
                authError = uiState.adminAuthError,
                onClearError = viewModel::clearAdminAuthError,
                onAuthenticateAdmin = { operatorId, masterKey ->
                    viewModel.authenticateAdmin(
                        operatorId = operatorId,
                        masterKey = masterKey,
                        onSuccess = {
                            navController.navigate(AviatorRoutes.ADMIN_DASHBOARD) {
                                popUpTo(AviatorRoutes.ADMIN_AUTH) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                },
                onNavigateBack = {
                    viewModel.navigateBackToLogin()
                    navController.popBackStack()
                }
            )
        }

        composable(route = AviatorRoutes.ADMIN_DASHBOARD) {
            AdminPanelScreen(
                operatorId = uiState.adminOperatorId,
                users = uiState.allUsers,
                activities = uiState.subscriberActivities,
                statusBanner = uiState.adminStatusBanner,
                onDismissBanner = viewModel::clearAdminBanner,
                onCreateAccount = viewModel::createNewAccount,
                onToggleAccountStatus = viewModel::toggleAccountStatus,
                onRenewAccount = viewModel::renewAccount,
                onDeleteAccount = viewModel::deleteAccount,
                onClearActivities = viewModel::clearSubscriberActivityLogs,
                onTestLoginAsUser = { account ->
                    viewModel.loginDirectlyFromAdmin(
                        account = account,
                        onNavigateToBrowser = {
                            navController.navigate(AviatorRoutes.BOT_BROWSER) {
                                launchSingleTop = true
                            }
                        }
                    )
                },
                onLogoutAdmin = {
                    viewModel.logoutAdmin {
                        navController.navigate(AviatorRoutes.USER_LOGIN) {
                            popUpTo(AviatorRoutes.USER_LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onNavigateBackToLogin = {
                    viewModel.navigateBackToLogin()
                    if (!navController.popBackStack()) {
                        navController.navigate(AviatorRoutes.USER_LOGIN) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(route = AviatorRoutes.BOT_BROWSER) {
            BotBrowserAndHudScreen(
                uiState = uiState,
                reloadTrigger = reloadTrigger,
                onSelectBookmaker = viewModel::selectBookmaker,
                onUrlChanged = viewModel::onWebViewUrlChanged,
                onLoadingChanged = viewModel::onWebViewLoadingChanged,
                onReloadClicked = viewModel::requestReloadWebView,
                onToggleSimulatedUnderlay = viewModel::toggleSimulatedUnderlay,
                onSetHudMode = viewModel::setHudMode,
                onToggleLiveFlight = viewModel::toggleLiveFlightTracker,
                onToggleAutoCycle = viewModel::toggleAutoCycleRadar,
                onRecordVela = viewModel::recordManualOrCapturedVela,
                onUndoLastVela = viewModel::undoLastVela,
                onResetVelas = viewModel::resetBookmakerHistory,
                onSetTotalStakeKz = viewModel::setTotalStakeKz,
                onOpenAdminPanel = {
                    if (uiState.adminAuthUnlocked) {
                        navController.navigate(AviatorRoutes.ADMIN_DASHBOARD) {
                            launchSingleTop = true
                        }
                    } else {
                        viewModel.openAdminAuthScreen()
                        navController.navigate(AviatorRoutes.ADMIN_AUTH) {
                            launchSingleTop = true
                        }
                    }
                },
                onLogout = {
                    viewModel.navigateBackToLogin()
                    navController.navigate(AviatorRoutes.USER_LOGIN) {
                        popUpTo(AviatorRoutes.USER_LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
