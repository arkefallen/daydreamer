package com.android.ark.daydreamer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.android.ark.daydreamer.data.database.dao.ImagesToUploadDAO
import com.android.ark.daydreamer.domain.RetryUploadingImageToFirebaseUseCase
import com.android.ark.daydreamer.navigation.Screen
import com.android.ark.daydreamer.navigation.SetupNavigationGraph
import com.android.ark.daydreamer.ui.theme.DaydreamerAppTheme
import com.android.ark.daydreamer.utils.Constants.APP_ID
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalMaterial3Api
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imagesToUploadDAO: ImagesToUploadDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        FirebaseApp.initializeApp(this)
        setContent {
            DaydreamerAppTheme {
                val navController = rememberNavController()
                SetupNavigationGraph(
                    startDestination = getStartDestination(),
                    navController = navController
                )
            }
        }
        val sessionUploadUseCase = RetryUploadingImageToFirebaseUseCase()
        cleanupCheck(
            scope = lifecycleScope,
            imagesToUploadDAO = imagesToUploadDAO,
            retryUploadingImageToFirebaseUseCase = sessionUploadUseCase
        )
    }

    private fun getStartDestination(): String {
        val user = App.create(APP_ID).currentUser
        return if (user != null && user.loggedIn) Screen.Home.route
        else Screen.Authentication.route
    }

    private fun cleanupCheck(
        scope: CoroutineScope,
        imagesToUploadDAO: ImagesToUploadDAO,
        retryUploadingImageToFirebaseUseCase: RetryUploadingImageToFirebaseUseCase
    ) {
        scope.launch(Dispatchers.IO) {
            val result = imagesToUploadDAO.getAllImages()
            result.forEach { imageToUpload ->
                retryUploadingImageToFirebaseUseCase(
                    imageToUpload = imageToUpload,
                    onSuccess = {
                        scope.launch(Dispatchers.IO) {
                            imagesToUploadDAO.cleanupImage(imageToUpload.id)
                        }
                    }
                )
            }
        }
    }
}