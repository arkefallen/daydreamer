package com.android.ark.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.android.ark.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.MessageBarState
import com.stevdzasan.onetap.OneTapSignInState
import com.stevdzasan.onetap.OneTapSignInWithGoogle

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AuthenticationScreen(
    oneTapSignInState: OneTapSignInState,
    messageBarState: MessageBarState,
    loadingState: Boolean,
    onButtonClicked: () -> Unit,
    onSuccessfulFirebaseLogin: (String) -> Unit,
    onFailedFirebaseLogin: (Exception) -> Unit,
    onDialogDismissed: (String) -> Unit,
    isAuthenticated: Boolean,
    navigateToHome: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        ContentWithMessageBar(messageBarState = messageBarState) {
            AuthenticationContent(
                loadingState = loadingState,
                onButtonClicked = { onButtonClicked() }
            )
        }
    }

    OneTapSignInWithGoogle(
        state = oneTapSignInState,
        clientId = Constants.WEB_CLIENT_ID,
        onTokenIdReceived = { tokenId ->
            val credential = GoogleAuthProvider.getCredential(tokenId, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) onSuccessfulFirebaseLogin(tokenId)
                    else result.exception?.let { onFailedFirebaseLogin(it) }
                }
            onSuccessfulFirebaseLogin(tokenId)
        },
        onDialogDismissed = { message ->
            onDialogDismissed(message)
        }
    )

    LaunchedEffect(key1 = isAuthenticated) {
        if (isAuthenticated) navigateToHome()
    }
}