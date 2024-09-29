package com.qimi.app.qplayer.core.ui

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Composable
fun Player(
    modifier: Modifier = Modifier,
    state: PlayerState = rememberPlayerState(),
    playerController: @Composable BoxScope.(PlayerState) -> Unit = {}
) {
    val context: Context = LocalContext.current
    var ticks: Boolean by remember { mutableStateOf(false) }
    var isShowPlayerController: Boolean by remember { mutableStateOf(false) }
    LaunchedEffect(ticks) {
        isShowPlayerController = true
        delay(5_000)
        isShowPlayerController = false
    }
    Box(modifier = modifier) {
        AndroidView(
            factory = { PlayerView(context) },
            modifier = Modifier.fillMaxSize(),
            update = {
                it.player = state.player
                it.useController = false
            }
        )
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = null,
                    indication = null
                ) {
                    ticks = !ticks
                }
        )
        AnimatedVisibility(
            visible = isShowPlayerController,
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

    val isShowPlayButton: Boolean by derivedStateOf {
        availableCommands.contains(Player.COMMAND_PLAY_PAUSE)
    }

    val isShowPlayState: Boolean by derivedStateOf {
        !isPlayWhenReady
                || playbackState == Player.STATE_IDLE
                || playbackState == Player.STATE_ENDED
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

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun seekTo(percentage: Float) {
        val duration = player.duration
        if (duration != C.TIME_UNSET) {
            player.seekTo((percentage * duration).toLong())
        }
    }
    
}
