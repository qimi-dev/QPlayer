package com.qimi.app.qplayer.core.ui

import android.content.Context
import android.media.session.PlaybackState
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

@Composable
fun Player(
    state: PlayerState,
    modifier: Modifier = Modifier,
    playerController: @Composable BoxScope.(PlayerState) -> Unit = {}
) {
    val context: Context = LocalContext.current
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.movie_loading))
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
        playerController(state)
    }
}

@Composable
fun rememberPlayerState(exoPlayer: ExoPlayer): PlayerState {
    return remember { PlayerState(exoPlayer) }
}

@Stable
class PlayerState(internal val player: ExoPlayer) {

    private var availableCommands: Player.Commands by mutableStateOf(player.availableCommands)

    private var playbackState: Int by mutableIntStateOf(player.playbackState)

    private var isPlayWhenReady: Boolean by mutableStateOf(player.playWhenReady)

    private var playbackSuppressionReason: Int by mutableIntStateOf(player.playbackSuppressionReason)

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

    @OptIn(UnstableApi::class)
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

    fun stop() = player.stop()

    fun seekTo(positionMs: Long) {
        if (player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
            player.seekTo(positionMs)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        if (player.isCommandAvailable(Player.COMMAND_SET_SPEED_AND_PITCH)) {
            player.setPlaybackSpeed(speed)
        }
    }

    @Composable
    fun produceContentPositionState(): State<Long> {
        return produceState(initialValue = 0, key1 = playbackState) {
            if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
                // 如果未加载或者已经播放完成，则退出
                return@produceState
            }
            while (true) {
                value = player.contentPosition
                delay(1_000)
            }
        }
    }

    @Composable
    fun produceContentDurationState(): State<Long> {
        return produceState(initialValue = 0, key1 = playbackState) {
            val contentDuration: Long = player.contentDuration
            if (contentDuration != C.TIME_UNSET) {
                value = player.contentDuration
            }
        }
    }

    @Composable
    fun produceContentBufferedPositionState(): State<Long> {
        return produceState(initialValue = 0, key1 = playbackState) {
            if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
                // 如果未加载或者已经播放完成，则退出
                return@produceState
            }
            while (true) {
                value = player.contentBufferedPosition
                delay(1_000)
            }
        }
    }

}


























