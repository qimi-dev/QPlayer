package com.qimi.app.qplayer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.qimi.app.qplayer.feature.main.navigation.MAIN_ROUTE
import com.qimi.app.qplayer.feature.main.navigation.mainScreen

@Composable
fun QPlayerNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = MAIN_ROUTE,
        modifier = modifier
    ) {
        mainScreen()
    }
}