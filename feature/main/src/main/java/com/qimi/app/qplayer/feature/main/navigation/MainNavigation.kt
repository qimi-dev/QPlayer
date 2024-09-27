package com.qimi.app.qplayer.feature.main.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.qimi.app.qplayer.core.model.data.Movie
import com.qimi.app.qplayer.feature.main.MainRoute
import kotlinx.serialization.Serializable

@Serializable
object MainRoute

fun NavController.navigateToMain() = navigate(MainRoute)

fun NavGraphBuilder.mainScreen(
    onSearchMovie: () -> Unit,
    onPreviewMovie: (Movie) -> Unit
) {
    composable<MainRoute> {
        MainRoute(
            onSearchMovie = onSearchMovie,
            onPreviewMovie = onPreviewMovie
        )
    }
}

