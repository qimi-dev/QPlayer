package com.qimi.app.qplayer.core.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn

@Composable
fun Player(
    modifier: Modifier = Modifier,
    state: PlayerState = rememberPlayerState(),
    playerController: @Composable BoxScope.(PlayerState) -> Unit = {}
) {
    val context: Context = LocalContext.current
    val corePlayer: ExoPlayer = remember { ExoPlayer.Builder(context).build() }
    LaunchedEffect(state.playerEvents.size) {
        state.playerEvents.forEach { playerEvent ->
            when (playerEvent) {
                is PlayerEvent.Prepare -> {
                    corePlayer.setMediaItem(MediaItem.fromUri(playerEvent.url))
                    corePlayer.prepare()
                }
                is PlayerEvent.Play -> corePlayer.play()
                else -> Unit
            }
        }
    }
    Box(modifier = modifier) {
        AndroidView(
            factory = { PlayerView(context) },
            modifier = Modifier.fillMaxSize(),
            update = {
                it.player = corePlayer
                it.useController = false
            }
        )
        playerController(state)
    }
}

@Composable
fun rememberPlayerState(): PlayerState {
    return remember { PlayerState() }
}

@Stable
class PlayerState {

    internal val playerEvents: MutableList<PlayerEvent> = mutableStateListOf()

    fun prepare(url: String) {
        playerEvents.add(PlayerEvent.Prepare(url))
    }

    fun play() {
        playerEvents.add(PlayerEvent.Play)
    }

    fun play(url: String) {
        prepare(url)
        play()
    }

}

sealed interface PlayerEvent {
    data object Initial : PlayerEvent
    data class Prepare(val url: String) : PlayerEvent
    data object Play : PlayerEvent
}
