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
    val urls: String,
    val score: String
)

fun NavController.navigateToPreview(route: PreviewRoute) = navigate(route)

fun NavGraphBuilder.previewScreen() {
    composable<PreviewRoute> {
        PreviewRoute()
    }
}