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
        .filter { it.isNotEmpty() }
        .map {
            val (first, second) = it.split("$")
            first to second
        }

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val playerState: PlayerState = PlayerState(exoPlayer)

    private val _previewUiState: MutableStateFlow<PreviewUiState> =
        MutableStateFlow(
            PreviewUiState(
                name = movie.name,
                score = movie.score,
                description = movie.content,
                urls = movieUrls,
                selectedUrlIndex = 0,
                playerState = playerState,
                previewMode = PreviewMode.NON_FULLSCREEN
            )
        )

    val previewUiState: StateFlow<PreviewUiState> = _previewUiState.asStateFlow()

    init {
        play(0)
    }

    fun play(selectedUrlIndex: Int) {
        playerState.prepare(movieUrls[selectedUrlIndex].second)
        playerState.play()
        _previewUiState.update {
            it.copy(selectedUrlIndex = selectedUrlIndex)
        }
    }

    fun stop() {
        exoPlayer.stop()
    }

    fun enterFullScreen() {
        _previewUiState.update {
            it.copy(previewMode = PreviewMode.FULLSCREEN)
        }
    }

    fun exitFullScreen() {
        _previewUiState.update {
            it.copy(previewMode = PreviewMode.NON_FULLSCREEN)
        }
    }

    override fun onCleared() {
        exoPlayer.release()
    }

}

data class PreviewUiState(
    val name: String,
    val score: String,
    val description: String,
    val urls: List<Pair<String, String>>,
    val selectedUrlIndex: Int,
    val playerState: PlayerState,
    val previewMode: PreviewMode
)

enum class PreviewMode {
    NON_FULLSCREEN, FULLSCREEN
}

