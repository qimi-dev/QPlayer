package com.qimi.app.qplayer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qimi.app.qplayer.feature.main.navigation.MainRoute
import com.qimi.app.qplayer.feature.main.navigation.mainScreen
import com.qimi.app.qplayer.feature.preview.navigation.PreviewRoute
import com.qimi.app.qplayer.feature.preview.navigation.navigateToPreview
import com.qimi.app.qplayer.feature.preview.navigation.previewScreen

@Composable
fun QPlayerNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = MainRoute,
        modifier = modifier
    ) {
        mainScreen(
            onMovieClick = {
                navController.navigateToPreview(
                    PreviewRoute(
                        id = it.id,
                        name = it.name,
                        image = it.image,
                        movieClass = it.movieClass,
                        remark = it.remark,
                        content = it.content,
                        urls = it.urls
                    )
                )
            }
        )
        previewScreen()
    }
}