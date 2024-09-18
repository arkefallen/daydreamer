package com.android.ark.daydreamer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.android.ark.daydreamer.navigation.Screen
import com.android.ark.daydreamer.navigation.SetupNavigationGraph
import com.android.ark.domain.RetryRemovingImageFromFirebaseUseCase
import com.android.ark.domain.RetryUploadingImageToFirebaseUseCase
import com.android.ark.room.dao.ImagesToDeleteDAO
import com.android.ark.room.dao.ImagesToUploadDAO
import com.android.ark.ui.theme.DaydreamerAppTheme
import com.android.ark.util.Constants.APP_ID
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
    @Inject
    lateinit var imagesToDeleteDAO: ImagesToDeleteDAO

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
        val sessionRemoveUseCase = RetryRemovingImageFromFirebaseUseCase()
        cleanupCheck(
            scope = lifecycleScope,
            imagesToUploadDAO = imagesToUploadDAO,
            retryUploadingImageToFirebaseUseCase = sessionUploadUseCase,
            imagesToDeleteDAO = imagesToDeleteDAO,
            retryRemovingImageFromFirebaseUseCase = sessionRemoveUseCase
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
        retryUploadingImageToFirebaseUseCase: RetryUploadingImageToFirebaseUseCase,
        imagesToDeleteDAO: ImagesToDeleteDAO,
        retryRemovingImageFromFirebaseUseCase: RetryRemovingImageFromFirebaseUseCase
    ) {
        scope.launch(Dispatchers.IO) {
            val allImagesToUpload = imagesToUploadDAO.getAllImages()
            allImagesToUpload.forEach { imageToUpload ->
                retryUploadingImageToFirebaseUseCase(
                    imageToUpload = imageToUpload,
                    onSuccess = {
                        scope.launch(Dispatchers.IO) {
                            imagesToUploadDAO.cleanupImage(imageToUpload.id)
                        }
                    }
                )
            }
            val allImagesToDelete = imagesToDeleteDAO.getAllImages()
            allImagesToDelete.forEach { imageToDelete ->
                retryRemovingImageFromFirebaseUseCase(
                    imageToDelete = imageToDelete,
                    onSuccess = {
                        scope.launch(Dispatchers.IO) {
                            imagesToDeleteDAO.removeImageToDelete(imageToDelete.id)
                        }
                    }
                )
            }
        }
    }
}