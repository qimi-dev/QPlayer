package com.qimi.app.qplayer.feature.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.qimi.app.qplayer.core.model.data.Movie
import kotlinx.serialization.Serializable
import com.qimi.app.qplayer.feature.search.SearchRoute

@Serializable
object SearchRoute

fun NavController.navigateToSearch() = navigate(SearchRoute)

fun NavGraphBuilder.searchScreen(
    onBackClick: () -> Unit,
    onPreviewMovie: (Movie) -> Unit
) {
    composable<SearchRoute> {
        SearchRoute(
            onBackClick = onBackClick,
            onPreviewMovie = onPreviewMovie
        )
    }
}