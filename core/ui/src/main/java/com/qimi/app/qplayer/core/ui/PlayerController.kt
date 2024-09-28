package com.qimi.app.qplayer.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CompactPlayerController(
    state: PlayerState,
    onBack: () -> Unit,
    onEnterFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    PlayerController(
        modifier = modifier,
        topBar = {
            BackButton(
                onBack = onBack
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayButton(
                    state = state,
                    modifier = Modifier.wrapContentSize()
                )
                PlayProgressBar(
                    state = state,
                    modifier = Modifier.weight(1f)
                )
                FullscreenButton(
                    onClick = onEnterFullScreen,
                    modifier = Modifier.wrapContentSize()
                )
            }
        }
    )
}

@Composable
internal fun PlayerController(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
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
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = null
        )
    }
}

@Composable
internal fun PlayButton(
    state: PlayerState,
    modifier: Modifier = Modifier
) {
    if (state.isShowPlayButton) {
        IconButton(
            onClick = { if (state.isShowPlayState) state.play() else state.pause() },
            modifier = modifier
        ) {
            AnimatedContent(
                targetState = state.isShowPlayState,
                label = "isStartPlaying"
            ) { isShowPlayState ->
                Icon(
                    painter = painterResource(
                        if (isShowPlayState) R.drawable.ic_play_arrow_24 else R.drawable.ic_pause_24
                    ),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
internal fun PlayProgressBar(
    state: PlayerState,
    modifier: Modifier = Modifier
) {
    val percentages by produceState(0f to 0f, state) {
        while (true) {
            val contentPercentage = state.getContentPercentage()
            val bufferedPercentage = state.getBufferedPercentage()
            value = contentPercentage to bufferedPercentage
            delay(1000)
        }
    }
    var containerWidth: Int by remember { mutableIntStateOf(0) }
//    val localDensity: Density = LocalDensity.current
//    val thumbSize: Dp = remember { 16.dp }
//    val thumbMixOffset: Float by remember {
//        derivedStateOf { with(localDensity) { - (thumbSize.toPx() / 2) } }
//    }
//    val thumbMaxOffset: Float by remember {
//        derivedStateOf { with(localDensity) { containerWidth - (thumbSize.toPx() / 2) } }
//    }
//    var thumbOffset: Float by remember { mutableFloatStateOf(thumbMixOffset) }
    Box(
        modifier = modifier
            .heightIn(min = 24.dp)
            .onSizeChanged {
                containerWidth = it.width
            }
            .pointerInput(Unit) {
                detectTapGestures { offset: Offset ->
                    // 获取当前触摸的位置，计算出视频跳转的位置
                    if (containerWidth > 0) {
                        state.seekTo(offset.x / containerWidth)
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
//        ProgressBarThumb(
//            modifier = Modifier
//                .size(thumbSize)
//                .offset { IntOffset(thumbOffset.roundToInt(), 0) }
//                .draggable(
//                    orientation = Orientation.Horizontal,
//                    state = rememberDraggableState { delta ->
//                        thumbOffset += delta
//                        if (thumbOffset < thumbMixOffset) {
//                            thumbOffset = thumbMixOffset
//                        } else if (thumbOffset > thumbMaxOffset) {
//                            thumbOffset = thumbMaxOffset
//                        }
//                    },
//                    onDragStopped = {
//                        if (containerWidth > 0) {
//                            state.seekTo((thumbOffset - thumbMixOffset) / containerWidth)
//                        }
//                    }
//                )
//        )
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
private fun ProgressBarThumb(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface
    ) {}
}

@Composable
internal fun FullscreenButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_fullscreen_24),
            contentDescription = null
        )
    }
}
