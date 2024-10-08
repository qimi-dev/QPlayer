package com.qimi.app.qplayer.core.ui

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerController(
    modifier: Modifier = Modifier,
    shouldShowController: Boolean = true,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    onClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {},
    onLeftVerticalDrag: (Float) -> Unit = {},
    onRightVerticalDrag: (Float) -> Unit = {},
    onHorizontalDragStarted: () -> Unit,
    onHorizontalDragging: (Float) -> Unit,
    onHorizontalDragStopped: () -> Unit
) {
    var touchableWidth: Int by remember { mutableIntStateOf(0) }
    var touchableHeight: Int by remember { mutableIntStateOf(0) }
    var dragX: Float by remember { mutableFloatStateOf(0f) }
    var dragY: Float by remember { mutableFloatStateOf(0f) }
    val effectiveDistance: Float = LocalDensity.current.run { 24.dp.toPx() }
    var isEffectiveTouch: Boolean by remember { mutableStateOf(true) }
    var isHorizontalControl: Boolean by remember { mutableStateOf(false) }
    var isVerticalControl: Boolean by remember { mutableStateOf(false) }
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged {
                    touchableWidth = it.width
                    touchableHeight = it.height
                }
                .draggable2D(
                    onDragStarted = {
                        dragX = it.x
                        dragY = it.y
                        // 判断起点是否在有效范围内
                        if (dragX < effectiveDistance || dragX > (touchableWidth - effectiveDistance)) {
                            // 水平方向超出距离
                            isEffectiveTouch = false
                        } else if (dragY < effectiveDistance || dragY > (touchableHeight - effectiveDistance)) {
                            // 垂直方向超出距离
                            isEffectiveTouch = false
                        } else {
                            // 未超出范围、触摸事件有效
                            isEffectiveTouch = true
                        }
                    },
                    onDragStopped = {
                        // 判断是否为进度调节
                        when {
                            isHorizontalControl -> {
                                isHorizontalControl = false
                                onHorizontalDragStopped()
                            }
                            isVerticalControl -> isVerticalControl = false
                        }
                    },
                    state = rememberDraggable2DState { delta ->
                        if (!isEffectiveTouch) {
                            // 当前触摸无效
                            return@rememberDraggable2DState
                        }
                        if (isHorizontalControl || isVerticalControl) {
                            when {
                                isHorizontalControl -> {
                                    // 水平进度调节
                                    onHorizontalDragging(delta.x / touchableWidth)
                                }
                                isVerticalControl -> {
                                    // 垂直亮度、声音调节
                                    if (dragX < touchableWidth / 2) {
                                        onLeftVerticalDrag(delta.y / touchableHeight)
                                    } else {
                                        onRightVerticalDrag(delta.y / touchableHeight)
                                    }
                                }
                            }
                            return@rememberDraggable2DState
                        }
                        if (delta.x.absoluteValue > delta.y.absoluteValue) {
                            // 标记为水平进度调节
                            isHorizontalControl = true
                            onHorizontalDragStarted()
                        } else {
                            // 标记为垂直亮度、声音调节
                            isVerticalControl = true
                            if (dragX < touchableWidth / 2) {
                                onLeftVerticalDrag(delta.y / touchableHeight)
                            } else {
                                onRightVerticalDrag(delta.y / touchableHeight)
                            }
                        }
                    }
                )
                .combinedClickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onClick,
                    onDoubleClick = onDoubleClick
                )
        ) {
            if (shouldShowController) {
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
}

@Composable
fun BackButton(
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
fun HomeButton(
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
fun PlayButton(
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
fun ProgressBar(
    contentPercent: Float,
    contentBufferedPercent: Float,
    onProgressChange: (Float) -> Unit,
    onProgressDragStarted: () -> Unit,
    onProgressDragging: (Float) -> Unit,
    onProgressDragStopped: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.wrapContentHeight(),
        contentAlignment = Alignment.CenterStart
    ) {
        ProgressBarTrack(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth()
        )
        ProgressBarTrack(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.fillMaxWidth(contentBufferedPercent)
        )
        ProgressBarTrack(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(contentPercent)
        )
        ProgressBarThumb(
            progress = contentPercent,
            onProgressChange = onProgressChange,
            onProgressDragStarted = onProgressDragStarted,
            onProgressDragging = onProgressDragging,
            onProgressDragStopped = onProgressDragStopped
        )
    }
}

@Composable
private fun ProgressBarTrack(
    color: Color,
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .height(thickness)
            .shadow(elevation = 0.dp, shape = CircleShape, clip = true)
            .background(color)
    )
}

@Composable
private fun ProgressBarThumb(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    onProgressDragStarted: () -> Unit,
    onProgressDragging: (Float) -> Unit,
    onProgressDragStopped: () -> Unit,
    modifier: Modifier = Modifier
) {
    var trackWidth: Int by remember { mutableIntStateOf(0) }
    var thumbWidth: Int by remember { mutableIntStateOf(0) }
    var dragX: Float by remember { mutableFloatStateOf(0f) }
    val offset: Float by remember(progress) {
        derivedStateOf { trackWidth * progress - thumbWidth / 2f }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { trackWidth = it.width }
            .draggable(
                orientation = Orientation.Horizontal,
                onDragStarted = {
                    dragX = it.x
                    onProgressDragStarted()
                    onProgressDragging((dragX / trackWidth).coerceIn(0f, 1f))
                },
                state = rememberDraggableState { delta ->
                    dragX += delta
                    onProgressDragging((dragX / trackWidth).coerceIn(0f, 1f))
                },
                onDragStopped = {
                    dragX = 0f
                    onProgressDragStopped()
                }
            ).pointerInput(Unit) {
                detectTapGestures {
                    onProgressChange((it.x / trackWidth).coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_play_24),
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .onSizeChanged { thumbWidth = it.width }
                .offset { IntOffset(offset.roundToInt(), 0) }
        )
    }
}

@Composable
fun FullscreenButton(
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






















