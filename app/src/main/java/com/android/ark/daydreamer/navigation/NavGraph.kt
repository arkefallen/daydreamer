

package com.android.ark.daydreamer.navigation

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.android.ark.daydreamer.model.GalleryImage
import com.android.ark.daydreamer.model.Mood
import com.android.ark.daydreamer.presentation.components.DisplayAlertDialog
import com.android.ark.daydreamer.presentation.components.rememberGalleryState
import com.android.ark.daydreamer.presentation.screens.auth.AuthenticationScreen
import com.android.ark.daydreamer.presentation.screens.auth.AuthenticationViewmodel
import com.android.ark.daydreamer.presentation.screens.home.HomeScreen
import com.android.ark.daydreamer.presentation.screens.home.HomeViewmodel
import com.android.ark.daydreamer.presentation.screens.write.WriteScreen
import com.android.ark.daydreamer.presentation.screens.write.WriteViewmodel
import com.android.ark.daydreamer.utils.Constants
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
            },
            navigateToWriteWithArgs = { diaryId ->
                navController.navigate(Screen.Write.createRoute(diaryId = diaryId))
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
            onSuccessfulFirebaseLogin = { tokenId ->
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
            onFailedFirebaseLogin = { exception ->
                messageBarState.addError(exception)
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
    navigateToWriteWithArgs: (String) -> Unit,
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
            onSignOutClicked = { signOutDialogOpened = true },
            navigateToWriteWithArgs = navigateToWriteWithArgs,
            viewmodel = viewmodel
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

@OptIn(ExperimentalFoundationApi::class)
fun NavGraphBuilder.writeRoute(onBackPressed: () -> Unit) {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = "diaryId") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val writeViewmodel: WriteViewmodel = viewModel()
        val uiState = writeViewmodel.uiState
        val pagerState = rememberPagerState(pageCount = { Mood.entries.size })
        val context = LocalContext.current
        var dialogOpened by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        val galleryState = writeViewmodel.galleryState

        WriteScreen(
            onBackPressed = onBackPressed,
            onDeleteClick = {
                writeViewmodel.deleteDiary(
                    onSuccess = {
                        onBackPressed()
                        Toast.makeText(context, "Succesfully removed a diary!", Toast.LENGTH_SHORT)
                            .show()
                    },
                    onError = { error ->
                        dialogOpened = true
                        errorMessage = error
                    }
                )
            },
            pagerState = pagerState,
            writeViewmodel = writeViewmodel,
            uiState = uiState,
            onSaveClicked = {
                writeViewmodel.upsertDiary(
                    diary = it,
                    onSuccess = {
                        onBackPressed()
                        if (uiState.selectedDiaryId != null) {
                            Toast.makeText(context, "Succesfully updated diary!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Succesfully added diary!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { error ->
                        dialogOpened = true
                        errorMessage = error
                    }
                )
            },
            onUpdatedDateTime = {
                writeViewmodel.updateDateTime(it)
            },
            onImageSelected = {
                val type = context.contentResolver.getType(it)?.split("/")?.last() ?: "jpg"
                writeViewmodel.addImage(image = it, imageType = type)
            },
            galleryState = galleryState,
        )
        DisplayAlertDialog(
            title = "Error",
            message = errorMessage,
            dialogOpened = dialogOpened,
            onDialogClosed = { },
            onYesClicked = { dialogOpened = false }
        )
    }
}