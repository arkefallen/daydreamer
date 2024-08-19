package com.android.ark.daydreamer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.android.ark.daydreamer.navigation.Screen
import com.android.ark.daydreamer.navigation.SetupNavigationGraph
import com.android.ark.daydreamer.ui.theme.DaydreamerAppTheme
import com.android.ark.daydreamer.utils.Constants.APP_ID
import io.realm.kotlin.mongodb.App

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DaydreamerAppTheme {
                val navController = rememberNavController()
                SetupNavigationGraph(
                    startDestination = getStartDestination(),
                    navController = navController
                )
            }
        }
    }

    private fun getStartDestination(): String {
        val user = App.create(APP_ID).currentUser
        return if (user != null && user.loggedIn) Screen.Home.route
        else Screen.Authentication.route
    }
}