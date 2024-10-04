package com.qimi.app.qplayer.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ExpandedPlayerController(
    name: String,
    state: PlayerState,
    onBack: () -> Unit,
    onExitFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowController: Boolean by state.produceControllerState()
    val volumeState: VolumeState by state.produceVolumeState()
    PlayerController(
        modifier = modifier,
        topBar = {
            if (shouldShowController) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackButton(onBack = onBack)
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        content = {
            if (volumeState.isShow) {
                VolumeIndicator(
                    volume = volumeState.volume,
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.5f)
                )
            }
        },
        bottomBar = {
            if (shouldShowController) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(bottom = 8.dp)
                ) {
                    PlayProgressBar(
                        onSeekTo = {
                            state.showController()
                            state.seekTo(it)
                        },
                        onReceiveContentPercentage = state::getContentPercentage,
                        onReceiveBufferedPercentage = state::getBufferedPercentage,
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.shouldShowPlayButton) {
                            PlayButton(
                                onClick = {
                                    state.showController()
                                    if (state.isPlaying) state.pause() else state.play()
                                },
                                iconPainter = painterResource(
                                    if (state.isPlaying) R.drawable.ic_pause_24  else R.drawable.ic_play_arrow_24
                                ),
                                modifier = Modifier.wrapContentSize()
                            )
                        }
                        FullscreenButton(
                            onClick = {
                                state.showController()
                                onExitFullScreen()
                            },
                            iconPainter = painterResource(R.drawable.ic_fullscreen_24),
                            modifier = Modifier.wrapContentSize()
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun CompactPlayerController(
    state: PlayerState,
    onBack: () -> Unit,
    onBackHome: () -> Unit,
    onEnterFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowController: Boolean by state.produceControllerState()
    val volumeState: VolumeState by state.produceVolumeState()
    PlayerController(
        modifier = modifier,
        topBar = {
            if (shouldShowController) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackButton(onBack = onBack)
                    HomeButton(onBackHome = onBackHome)
                }
            }
        },
        content = {
            if (volumeState.isShow) {
                VolumeIndicator(
                    volume = volumeState.volume,
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.5f)
                )
            }
        },
        bottomBar = {
            if (shouldShowController) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.shouldShowPlayButton) {
                        PlayButton(
                            onClick = {
                                state.showController()
                                if (state.isPlaying) state.pause() else state.play()
                            },
                            iconPainter = painterResource(
                                if (state.isPlaying) R.drawable.ic_pause_24 else R.drawable.ic_play_arrow_24
                            ),
                            modifier = Modifier.wrapContentSize()
                        )
                    }
                    PlayProgressBar(
                        onSeekTo = {
                            state.showController()
                            state.seekTo(it)
                        },
                        onReceiveContentPercentage = state::getContentPercentage,
                        onReceiveBufferedPercentage = state::getBufferedPercentage,
                        modifier = Modifier.weight(1f)
                    )
                    FullscreenButton(
                        onClick = {
                            state.showController()
                            onEnterFullScreen()
                        },
                        iconPainter = painterResource(R.drawable.ic_fullscreen_24),
                        modifier = Modifier.wrapContentSize()
                    )
                }
            }
        }
    )
}

@Composable
internal fun PlayerController(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.CenterStart
            ) {
                topBar()
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                contentAlignment = Alignment.CenterStart
            ) {
                bottomBar()
            }
        }
    }
}

@Composable
internal fun BackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onBack,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = null
        )
    }
}

@Composable
internal fun HomeButton(
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onBackHome,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Outlined.Home,
            contentDescription = null
        )
    }
}

@Composable
internal fun PlayButton(
    onClick: () -> Unit,
    iconPainter: Painter,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = iconPainter,
            label = "PlayButtonIcon"
        ) { targetState ->
            Icon(
                painter = targetState,
                contentDescription = null
            )
        }
    }
}

@Composable
internal fun PlayProgressBar(
    onSeekTo: (Float) -> Unit,
    onReceiveContentPercentage: () -> Float,
    onReceiveBufferedPercentage: () -> Float,
    modifier: Modifier = Modifier
) {
    val percentages by produceState(0f to 0f) {
        while (true) {
            val contentPercentage = onReceiveContentPercentage()
            val bufferedPercentage = onReceiveBufferedPercentage()
            value = contentPercentage to bufferedPercentage
            delay(1000)
        }
    }
    var containerWidth: Int by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier
            .wrapContentHeight()
            .onSizeChanged {
                containerWidth = it.width
            }
            .pointerInput(Unit) {
                detectTapGestures { offset: Offset ->
                    // 获取当前触摸的位置，计算出视频跳转的位置
                    if (containerWidth > 0) {
                        onSeekTo(offset.x / containerWidth)
                    }
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        ProgressBarTrack(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth()
        )
        ProgressBarTrack(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.fillMaxWidth(percentages.second)
        )
        ProgressBarTrack(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(percentages.first)
        )
    }
}

@Composable
private fun ProgressBarTrack(
    color: Color,
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp
) {
    Surface(
        modifier = modifier.height(thickness),
        shape = CircleShape,
        color = color
    ) {}
}

@Composable
internal fun FullscreenButton(
    onClick: () -> Unit,
    iconPainter: Painter,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = null
        )
    }
}

@Composable
internal fun VolumeIndicator(
    volume: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = volume,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.3f),
        contentColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Icon(
                painter = painterResource(
                    if (animatedProgress == 0f) R.drawable.ic_volume_off_24 else R.drawable.ic_volume_24
                ),
                contentDescription = null
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.surface,
                trackColor = MaterialTheme.colorScheme.outline
            )
        }
    }
}
