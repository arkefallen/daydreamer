

package com.android.ark.daydreamer.navigation

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.android.ark.auth.AuthenticationScreen
import com.android.ark.auth.AuthenticationViewmodel
import com.android.ark.home.HomeScreen
import com.android.ark.home.HomeViewmodel
import com.android.ark.model.Mood
import com.android.ark.ui.components.DisplayAlertDialog
import com.android.ark.util.Constants
import com.android.ark.write.WriteScreen
import com.android.ark.write.WriteViewmodel
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
        val viewmodel: HomeViewmodel = hiltViewModel()
        val diaries by viewmodel.diaries.collectAsStateWithLifecycle()
        val deleteLoadingState by viewmodel.requestLoading.collectAsStateWithLifecycle()

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()
        var signOutDialogOpened by remember { mutableStateOf(false) }
        var deleteAllDiariesDialogOpened by remember { mutableStateOf(false) }
        var failedToDeleteAllDiariesDialogOpened by remember { mutableStateOf(false) }
        var failedToDeleteDiaryMessage by remember {
            mutableStateOf("")
        }

        val context = LocalContext.current

        if (deleteLoadingState) {
            Dialog(onDismissRequest = {}) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
        }

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
            viewmodel = viewmodel,
            onDeleteAllDiaries = {
                deleteAllDiariesDialogOpened = true
            }
        )

        if (failedToDeleteAllDiariesDialogOpened) {
            AlertDialog(
                onDismissRequest = { failedToDeleteAllDiariesDialogOpened = false },
                confirmButton = {
                    TextButton(onClick = { failedToDeleteAllDiariesDialogOpened = false }) {
                        Text(text = "Back")
                    }
                },
                title = {
                    Text(text = "Failed to Delete")
                },
                text = {
                    Text(text = failedToDeleteDiaryMessage)
                }
            )
        }

        DisplayAlertDialog(
            title = "Remove All Diaries",
            message = "Are you sure you want to permanently delete all your diaries?",
            dialogOpened = deleteAllDiariesDialogOpened,
            onDialogClosed = { deleteAllDiariesDialogOpened = false },
            onYesClicked = {
                viewmodel.deleteAllDiaries(
                    onSuccess = {
                        coroutineScope.launch { drawerState.close() }
                        Toast.makeText(context, "Successfully delete all diaries", Toast.LENGTH_SHORT).show()
                    },
                    onFailed = {
                        coroutineScope.launch { drawerState.close() }
                        failedToDeleteAllDiariesDialogOpened = true
                        failedToDeleteDiaryMessage = it.message.toString()
                    },
                    onLoading = {
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
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
        val writeViewmodel: WriteViewmodel = hiltViewModel()
        val uiState = writeViewmodel.uiState
        val pagerState = rememberPagerState(pageCount = { Mood.entries.size })
        val context = LocalContext.current
        var dialogOpened by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        val galleryState = writeViewmodel.galleryState

        WriteScreen(
            onBackPressed = {
                onBackPressed()
                galleryState.clearImagesToBeDeleted()
            },
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
            onImageRemoved = { galleryState.removeImage(it) }
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