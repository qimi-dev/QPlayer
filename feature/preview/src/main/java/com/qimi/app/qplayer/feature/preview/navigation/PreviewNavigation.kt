package com.qimi.app.qplayer.feature.preview.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.qimi.app.qplayer.core.model.data.Movie
import kotlinx.serialization.Serializable
import com.qimi.app.qplayer.feature.preview.PreviewRoute

@Serializable
data class PreviewRoute(
    val id: Int,
    val name: String,
    val image: String,
    val movieClass: String,
    val remark: String,
    val content: String,
    val urls: String
)

fun NavController.navigateToPreview(route: PreviewRoute) = navigate(route)

fun NavGraphBuilder.previewScreen() {
    composable<PreviewRoute> {
        val route: PreviewRoute = it.toRoute()
        PreviewRoute(
            movie = Movie(
                id = route.id,
                name = route.name,
                image = route.image,
                movieClass = route.movieClass,
                remark = route.remark,
                content = route.content,
                urls = route.urls
            )
        )
    }
}