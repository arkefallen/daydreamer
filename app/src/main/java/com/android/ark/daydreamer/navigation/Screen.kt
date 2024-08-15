package com.android.ark.daydreamer.navigation

sealed class Screen(val route: String) {
    data object Authentication: Screen(route = "auth_screen")

    data object Home: Screen(route = "home")

    data object Write: Screen(route = "write?diaryId={diaryId}") {
        fun createRoute(diaryId: String) = "write?diaryId=$diaryId"
    }
}