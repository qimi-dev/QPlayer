package com.qimi.app.qplayer.core.ui

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.getSystemService
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Player(
    modifier: Modifier = Modifier,
    state: PlayerState = rememberPlayerState(),
    playerController: @Composable BoxScope.(PlayerState) -> Unit = {}
) {
    val context: Context = LocalContext.current
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    var touchWidth: Int by remember { mutableIntStateOf(0) }
    var touchHeight: Int by remember { mutableIntStateOf(0) }
    var dragX: Float by remember { mutableFloatStateOf(0f) }
    var dragY: Float by remember { mutableFloatStateOf(0f) }
    val shouldShowController: Boolean by state.produceControllerState()
    Box(modifier = modifier) {
        AndroidView(
            factory = { PlayerView(context) },
            modifier = Modifier.fillMaxSize(),
            update = {
                it.player = state.player
                it.useController = false
            }
        )
        AnimatedVisibility(
            visible = state.isBuffering,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.height(120.dp),
                    contentScale = ContentScale.FillHeight
                )
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    touchWidth = it.width
                    touchHeight = it.height
                }
                .draggable2D(
                    onDragStarted = {
                        dragX = it.x
                        dragY = it.y
                    },
                    state = rememberDraggable2DState { delta ->
                        if (delta.x.absoluteValue < delta.y.absoluteValue) {
                            // 高度大于宽度，判断为亮度、声音调节
                            if (dragX < touchWidth / 2) {

                            } else {
                                state.adjustVolume(delta.y / touchHeight)
                            }
                        } else {
                            // 宽度大于高度，判断为进度调节

                        }
                    }
                )
                .combinedClickable(
                    interactionSource = null,
                    indication = null,
                    onClick = {
                        if (shouldShowController) {
                            state.hideController()
                        } else {
                            state.showController()
                        }
                    },
                    onDoubleClick = {
                        if (state.shouldShowPlayButton) {
                            if (state.isPlaying) state.pause() else state.play()
                        }
                    }
                )
        )
        playerController(state)
    }
}

@Composable
fun rememberPlayerState(): PlayerState {
    val context: Context = LocalContext.current
    return remember { PlayerState(ExoPlayer.Builder(context).build()) }
}

@Stable
class PlayerState(internal val player: ExoPlayer) {

    private var availableCommands: Player.Commands by mutableStateOf(player.availableCommands)

    private var playbackState: Int by mutableIntStateOf(player.playbackState)

    private var isPlayWhenReady: Boolean by mutableStateOf(player.playWhenReady)

    private var playbackSuppressionReason: Int by mutableIntStateOf(player.playbackSuppressionReason)

    private var showControllerSignal: Date? by mutableStateOf(null)

    private var shouldShowController: Boolean = true

    val shouldShowPlayButton: Boolean by derivedStateOf {
        availableCommands.contains(Player.COMMAND_PLAY_PAUSE)
    }

    val isPlaying: Boolean by derivedStateOf {
        isPlayWhenReady
                && playbackState != Player.STATE_IDLE
                && playbackState != Player.STATE_ENDED
                && playbackSuppressionReason == Player.PLAYBACK_SUPPRESSION_REASON_NONE
    }

    val isBuffering: Boolean by derivedStateOf {
        playbackState == Player.STATE_BUFFERING
    }

    private var showVolumeSignal: Date? by mutableStateOf(null)

    private var volume: Float = player.volume

    init {
        player.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                this@PlayerState.playbackState = playbackState
            }

            override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
                this@PlayerState.availableCommands = availableCommands
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                this@PlayerState.isPlayWhenReady = playWhenReady
            }

            override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
                this@PlayerState.playbackSuppressionReason = playbackSuppressionReason
            }

            override fun onVolumeChanged(volume: Float) {
                this@PlayerState.volume = volume
                showVolumeSignal = Date()
            }

        })
    }

    fun prepare(url: String) {
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
    }

    fun play() {
        if (!isPlaying) {
            Util.handlePlayButtonAction(player)
        }
    }

    fun pause() {
        if (isPlaying) {
            Util.handlePauseButtonAction(player)
        }
    }

    fun seekTo(percentage: Float) {
        val duration = player.duration
        if (duration != C.TIME_UNSET && player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
            player.seekTo((percentage * duration).toLong())
        }
    }

    fun showController() {
        shouldShowController = true
        showControllerSignal = Date()
    }

    fun hideController() {
        shouldShowController = false
        showControllerSignal = Date()
    }

    @Composable
    fun produceControllerState(): State<Boolean> {
        return produceState(
            initialValue = true,
            key1 = showControllerSignal
        ) {
            if (showControllerSignal == null) {
                return@produceState
            }
            if (shouldShowController) {
                value = true
                delay(5_000)
                shouldShowController = false
            }
            value = false
        }
    }

    fun adjustVolume(volume: Float) {
        if (!player.isCommandAvailable(Player.COMMAND_SET_VOLUME) or
            !player.isCommandAvailable(Player.COMMAND_GET_VOLUME)) {
            return
        }
        showVolumeSignal = Date()
        player.volume = (player.volume - volume).coerceIn(0f, 1f)
    }

    @Composable
    fun produceVolumeState(): State<VolumeState> {
        return produceState(
            initialValue = VolumeState(
                isShow = false,
                volume = volume
            ),
            key1 = showVolumeSignal
        ) {
            if (showVolumeSignal == null) {
                return@produceState
            }
            value = VolumeState(isShow = true, volume = volume)
            delay(500)
            value = VolumeState(isShow = false, volume = volume)
        }
    }

    fun getContentPercentage(): Float {
        if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
            return 0f
        }
        val duration: Long = player.duration
        if (duration == C.TIME_UNSET) {
            return 0f
        }
        return player.currentPosition * 1f / duration
    }

    fun getBufferedPercentage(): Float {
        return player.bufferedPercentage / 100f
    }

}

@Stable
data class VolumeState(
    val isShow: Boolean,
    val volume: Float
)
























