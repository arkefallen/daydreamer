

package com.android.ark.daydreamer.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.android.ark.daydreamer.model.Diary
import com.android.ark.daydreamer.presentation.components.DisplayAlertDialog
import com.android.ark.daydreamer.presentation.screens.auth.AuthenticationScreen
import com.android.ark.daydreamer.presentation.screens.auth.AuthenticationViewmodel
import com.android.ark.daydreamer.presentation.screens.home.HomeScreen
import com.android.ark.daydreamer.presentation.screens.home.HomeViewmodel
import com.android.ark.daydreamer.presentation.screens.write.WriteScreen
import com.android.ark.daydreamer.utils.Constants
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun SetupNavigationGraph(
    startDestination: String,
    navController: NavHostController,
) {
    NavHost(
        startDestination = startDestination,
        navController = navController
    ) {
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            }
        )
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.route)
            },
            navigateToAuth = {
                navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
            }
        )
        writeRoute(
            onBackPressed = { navController.popBackStack() }
        )
    }
}

fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit
) {
    composable(route = Screen.Authentication.route) {
        val authenticationViewmodel: AuthenticationViewmodel = viewModel()
        val isAuthenticated by authenticationViewmodel.authenticatedState
        val loadingState by authenticationViewmodel.loadingState
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        AuthenticationScreen(
            oneTapSignInState = oneTapState,
            loadingState = loadingState,
            onButtonClicked = {
                oneTapState.open()
                authenticationViewmodel.setLoading(true)
            },
            messageBarState = messageBarState,
            onTokenIdReceived = { tokenId ->
                authenticationViewmodel.signInToMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Successfully Authenticated!")
                        authenticationViewmodel.setLoading(false)
                    },
                    onError = { exception ->
                        messageBarState.addError(exception)
                        authenticationViewmodel.setLoading(false)
                    }
                )
            },
            onDialogDismissed = {
                messageBarState.addError(Exception(it))
            },
            navigateToHome = { navigateToHome() },
            isAuthenticated = isAuthenticated
        )
    }
}

fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToAuth: () -> Unit
) {
    composable(route = Screen.Home.route) {
        val viewmodel: HomeViewmodel = viewModel()
        val diaries by viewmodel.diaries
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()
        var signOutDialogOpened by remember { mutableStateOf(false) }
        HomeScreen(
            diaries = diaries,
            onMenuClick = {
                coroutineScope.launch {
                    drawerState.open()
                }
            },
            navigateToWrite = navigateToWrite,
            drawerState = drawerState,
            onSignOutClicked = { signOutDialogOpened = true }
        )

        DisplayAlertDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out from your account?",
            dialogOpened = signOutDialogOpened,
            onDialogClosed = { signOutDialogOpened = false },
            onYesClicked = {
                coroutineScope.launch(Dispatchers.IO) {
                    val user = App.create(Constants.APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        withContext(Dispatchers.Main) {
                            drawerState.close()
                            navigateToAuth()
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
fun NavGraphBuilder.writeRoute(onBackPressed: () -> Unit) {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = "diaryId") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val pagerState = rememberPagerState()

        WriteScreen(
            onBackPressed = onBackPressed,
            onDeleteClick = {},
            selectedDiary = Diary().apply {
                title = "Example Diary"
            },
            pagerState = pagerState
        )
    }
}