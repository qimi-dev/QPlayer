package com.qimi.app.qplayer.feature.preview

import android.content.Context
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.toRoute
import com.qimi.app.qplayer.core.data.repository.UserDataRepository
import com.qimi.app.qplayer.core.model.data.Movie
import com.qimi.app.qplayer.core.ui.PlayerState
import com.qimi.app.qplayer.feature.preview.navigation.PreviewRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
    userDataRepository: UserDataRepository
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

    private lateinit var exoPlayer: ExoPlayer

    private val playerState: MutableStateFlow<PlayerState?> = MutableStateFlow(null)

    private var volumeStateHandler: Job? = null

    private val volumeState: MutableStateFlow<VolumeState> =
        MutableStateFlow(VolumeState(false, 1f))

    private var brightnessStateHandler: Job? = null

    private val brightnessState: MutableStateFlow<BrightnessState> =
        MutableStateFlow(BrightnessState(false, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE))

    val playerUiState: StateFlow<PlayerUiState> =
        combine(playerState, volumeState, brightnessState) {
            playerState, volumeState, brightnessState ->
            PlayerUiState(
                title = movie.name,
                playerState = playerState,
                volumeState = volumeState,
                adjustVolume = ::adjustVolume,
                brightnessState = brightnessState,
                setBrightness = ::setBrightness,
                adjustBrightness = ::adjustBrightness
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = PlayerUiState(
                title = movie.name,
                playerState = null,
                volumeState = volumeState.value,
                adjustVolume = ::adjustVolume,
                brightnessState = brightnessState.value,
                setBrightness = ::setBrightness,
                adjustBrightness = ::adjustBrightness
            )
        )

    val contentUiState: StateFlow<ContentUiState> =
        MutableStateFlow(ContentUiState(movie.name, movie.score, movie.content, movieUrls))

    init {
        // 加载播放器并设置播放参数
        viewModelScope.launch {
            exoPlayer = ExoPlayer.Builder(context).build()
            playerState.value = PlayerState(exoPlayer)
        }
    }

    private fun adjustVolume(offsetPercent: Float) {
        val lastHandler: Job? = volumeStateHandler
        volumeStateHandler = viewModelScope.launch {
            lastHandler?.cancelAndJoin()
            val newVolume: Float = (exoPlayer.volume- offsetPercent).coerceIn(0f,1f)
            exoPlayer.volume = newVolume
            volumeState.value = VolumeState(true, newVolume)
            delay(500)
            volumeState.value = VolumeState(false, newVolume)
        }
    }

    private fun setBrightness(percent: Float) {
        brightnessState.update { it.copy(brightness = percent) }
    }

    private fun adjustBrightness(offsetPercent: Float) {
        val lastHandler: Job? = brightnessStateHandler
        brightnessStateHandler = viewModelScope.launch {
            lastHandler?.cancelAndJoin()
            val newBrightness: Float = (brightnessState.value.brightness - offsetPercent).coerceIn(0f,1f)
            brightnessState.value = BrightnessState(true, newBrightness)
            delay(500)
            brightnessState.value = BrightnessState(false, newBrightness)
        }
    }

    override fun onCleared() {
        exoPlayer.release()
    }

}

data class PlayerUiState(
    val title: String,
    val playerState: PlayerState?,
    val volumeState: VolumeState,
    val adjustVolume: (Float) -> Unit,
    val brightnessState: BrightnessState,
    val setBrightness: (Float) -> Unit,
    val adjustBrightness: (Float) -> Unit
)

data class ContentUiState(
    val name: String,
    val score: String,
    val description: String,
    val urls: List<Pair<String, String>>
)

data class VolumeState(
    val isShow: Boolean,
    val volume: Float
)

data class BrightnessState(
    val isShow: Boolean,
    val brightness: Float
)

