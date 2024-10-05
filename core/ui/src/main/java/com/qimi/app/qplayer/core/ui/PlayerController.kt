package com.qimi.app.qplayer.core.ui

import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerController(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    onClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {},
    onAdjustBrightness: (Float) -> Unit = {},
    onAdjustVolume: (Float) -> Unit = {}
) {
    var touchableWidth: Int by remember { mutableIntStateOf(0) }
    var touchableHeight: Int by remember { mutableIntStateOf(0) }
    var dragX: Float by remember { mutableFloatStateOf(0f) }
    var dragY: Float by remember { mutableFloatStateOf(0f) }
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
                    },
                    state = rememberDraggable2DState { delta ->
                        if (delta.x.absoluteValue < delta.y.absoluteValue) {
                            // 高度大于宽度，判断为亮度、声音调节
                            if (dragX < touchableWidth / 2) {
                                onAdjustBrightness(delta.y / touchableHeight)
                            } else {
                                onAdjustVolume(delta.y / touchableHeight)
                            }
                        } else {
                            // 宽度大于高度，判断为进度调节

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
fun PlayProgressBar(
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






















