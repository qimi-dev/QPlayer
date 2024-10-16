package com.qimi.app.qplayer.navigation

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.qimi.app.qplayer.core.model.data.Movie
import com.qimi.app.qplayer.feature.main.navigation.MainRoute
import com.qimi.app.qplayer.feature.main.navigation.mainScreen
import com.qimi.app.qplayer.feature.main.navigation.navigateToMain
import com.qimi.app.qplayer.feature.preview.navigation.PreviewRoute
import com.qimi.app.qplayer.feature.preview.navigation.navigateToPreview
import com.qimi.app.qplayer.feature.preview.navigation.previewScreen
import com.qimi.app.qplayer.feature.search.navigation.SearchRoute
import com.qimi.app.qplayer.feature.search.navigation.navigateToSearch
import com.qimi.app.qplayer.feature.search.navigation.searchScreen
import com.qimi.app.qplayer.feature.settings.SettingsDestination
import com.qimi.app.qplayer.feature.settings.navigation.SettingsRoute
import com.qimi.app.qplayer.feature.settings.navigation.navigateToSettings
import com.qimi.app.qplayer.feature.settings.navigation.settingsScreen

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
            onSearchMovie = navController::navigateToSearch,
            onSettingsClick = navController::navigateToSettings,
            onPreviewMovie = navController::navigateToPreview
        )
        settingsScreen(
            onBackClick = navController::navigateUp
        )
        previewScreen(
            onBackClick = navController::navigateUp,
            onBackHomeClick = {
                navController.popBackStack( route = MainRoute, inclusive = false)
            }
        )
        searchScreen(
            onBackClick = navController::navigateUp,
            onPreviewMovie = navController::navigateToPreview
        )
    }
}

internal fun NavController.navigateToPreview(movie: Movie) {
    navigateToPreview(
        PreviewRoute(
            id = movie.id,
            name = movie.name,
            image = movie.image,
            movieClass = movie.movieClass,
            remark = movie.remark,
            content = movie.content,
            urls = movie.urls,
            score = movie.score
        )
    )
}
