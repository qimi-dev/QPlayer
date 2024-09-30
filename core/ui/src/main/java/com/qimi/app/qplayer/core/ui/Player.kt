package com.qimi.app.qplayer.core.ui

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Player(
    modifier: Modifier = Modifier,
    state: PlayerState = rememberPlayerState(),
    playerController: @Composable BoxScope.(PlayerState) -> Unit = {}
) {
    val context: Context = LocalContext.current
    LaunchedEffect(state.isControllerAvailable) {
        if (state.isControllerAvailable) {
            delay(5_000)
            state.hideController()
        }
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
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
                .combinedClickable(
                    interactionSource = null,
                    indication = null,
                    onClick = {
                        if (state.isControllerAvailable) {
                            state.hideController()
                        } else {
                            state.showController()
                        }
                    },
                    onDoubleClick = {
                        if (state.isPlayButtonAvailable) {
                            if (state.isPlaying) state.play() else state.pause()
                        }
                    }
                )
        )
        AnimatedVisibility(
            visible = state.isControllerAvailable,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            playerController(state)
        }
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

    var isControllerAvailable: Boolean by mutableStateOf(true)
        private set

    val isPlayButtonAvailable: Boolean by derivedStateOf {
        availableCommands.contains(Player.COMMAND_PLAY_PAUSE)
    }

    val isPlaying: Boolean by derivedStateOf {
        !isPlayWhenReady
                || playbackState == Player.STATE_IDLE
                || playbackState == Player.STATE_ENDED
                || playbackSuppressionReason != Player.PLAYBACK_SUPPRESSION_REASON_NONE
    }

    val isBuffering: Boolean by derivedStateOf {
        playbackState == Player.STATE_BUFFERING
    }

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

        })
    }

    fun play() {
        if (playbackState == Player.STATE_IDLE
            && player.isCommandAvailable(Player.COMMAND_PREPARE)) {
            player.prepare()
        } else if (playbackState == Player.STATE_ENDED
            && player.isCommandAvailable(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)) {
            player.seekToDefaultPosition()
        }
        if (player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE)) {
            player.play()
        }
    }

    fun play(url: String) {
        player.setMediaItem(MediaItem.fromUri(url))
        play()
    }

    fun pause() {
        if (player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE)) {
            player.pause()
        }
    }

    fun seekTo(percentage: Float) {
        val duration = player.duration
        if (duration != C.TIME_UNSET && player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
            player.seekTo((percentage * duration).toLong())
        }
    }

    fun showController() {
        isControllerAvailable = true
    }

    fun hideController() {
        isControllerAvailable = false
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
