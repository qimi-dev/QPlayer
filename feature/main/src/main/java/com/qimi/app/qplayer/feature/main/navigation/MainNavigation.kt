package com.qimi.app.qplayer.feature.main.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.qimi.app.qplayer.feature.main.MainRoute

const val MAIN_ROUTE = "main"

fun NavController.navigateToMain() = navigate(MAIN_ROUTE)

fun NavGraphBuilder.mainScreen() {
    composable(
        route = MAIN_ROUTE
    ) {
        MainRoute()
    }
}

