package com.qimi.app.qplayer.feature.preview

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.toRoute
import com.qimi.app.qplayer.core.model.data.Movie
import com.qimi.app.qplayer.core.ui.PlayerState
import com.qimi.app.qplayer.feature.preview.navigation.PreviewRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val previewRoute: PreviewRoute = savedStateHandle.toRoute()

    private val movie: Movie = Movie(
        id = previewRoute.id,
        name = previewRoute.name,
        image = previewRoute.image,
        movieClass = previewRoute.movieClass,
        remark = previewRoute.remark,
        content = previewRoute.content,
        urls = previewRoute.urls,
        score = previewRoute.score
    )

    private val movieUrls: List<Pair<String, String>> = movie.urls
        .split("#")
        .map {
            val (first, second) = it.split("$")
            first to second
        }

    private val _previewUiState: MutableStateFlow<PreviewUiState> =
        MutableStateFlow(
            PreviewUiState(
                movie = movie,
                movieUrls = movieUrls,
                selectedIndex = 0
            )
        )

    val previewUiState: StateFlow<PreviewUiState> = _previewUiState.asStateFlow()

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    val playerState: PlayerState = PlayerState(exoPlayer)

    init {
        play(0)
    }

    fun play(index: Int) {
        playerState.play(movieUrls[index].second)
        _previewUiState.update {
            it.copy(selectedIndex = index)
        }
    }

    override fun onCleared() {
        exoPlayer.release()
    }

}

data class PreviewUiState(
    val movie: Movie,
    val movieUrls: List<Pair<String, String>>,
    val selectedIndex: Int
)

