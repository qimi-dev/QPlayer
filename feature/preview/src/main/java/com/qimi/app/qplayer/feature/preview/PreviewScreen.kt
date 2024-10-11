package com.qimi.app.qplayer.feature.preview

import android.content.ContentResolver
import android.content.pm.ActivityInfo
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsStartWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.qimi.app.qplayer.core.ui.AppState
import com.qimi.app.qplayer.core.ui.BackButton
import com.qimi.app.qplayer.core.ui.FullscreenButton
import com.qimi.app.qplayer.core.ui.HomeButton
import com.qimi.app.qplayer.core.ui.LocalAppState
import com.qimi.app.qplayer.core.ui.PlayButton
import com.qimi.app.qplayer.core.ui.ProgressBar
import com.qimi.app.qplayer.core.ui.Player
import com.qimi.app.qplayer.core.ui.PlayerController
import com.qimi.app.qplayer.core.ui.PlayerState
import com.qimi.app.qplayer.core.ui.rememberPlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
internal fun PreviewRoute(
    onBackClick: () -> Unit,
    onBackHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val playerUiState: PlayerUiState by viewModel.playerUiState.collectAsState()
    val contentUiState: ContentUiState by viewModel.contentUiState.collectAsState()
    val playerState: PlayerState? = playerUiState.playerState
    if (playerState != null) {
        PreviewScreen(
            playerUiState = playerUiState,
            contentUiState = contentUiState,
            playerState = playerState,
            onBackClick = onBackClick,
            onBackHomeClick = onBackHomeClick,
            modifier = modifier
        )
    }
}

@Composable
internal fun PreviewScreen(
    playerUiState: PlayerUiState,
    contentUiState: ContentUiState,
    playerState: PlayerState,
    onBackClick: () -> Unit,
    onBackHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var previewMode: PreviewMode by rememberSaveable { mutableStateOf(PreviewMode.COMMON) }
    // 处理视频选集切换播放
    var selectedUrlIndex: Int by rememberSaveable { mutableIntStateOf(0) }
    var lastSelectedUrlIndex: Int by rememberSaveable { mutableIntStateOf(-1) }
    LaunchedEffect(selectedUrlIndex) {
        if (lastSelectedUrlIndex == selectedUrlIndex) {
            return@LaunchedEffect
        }
        lastSelectedUrlIndex = selectedUrlIndex
        playerState.prepare(
            contentUiState.urls[selectedUrlIndex].second
        )
        playerState.play()
    }
    val activity: ComponentActivity = LocalContext.current as ComponentActivity
    val window: Window = activity.window
    // 处理屏幕亮度
    val currentBrightness: Float = playerUiState.brightnessState.brightness
    DisposableEffect(currentBrightness) {
        if (currentBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            val contentResolver: ContentResolver = activity.contentResolver
            val currentRealBrightness: Float = Settings.System
                .getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
            playerUiState.setBrightness(currentRealBrightness)
        } else {
            val attributes = window.attributes
            attributes.screenBrightness = currentBrightness
            window.attributes = attributes
        }
        onDispose {
            val attributes = window.attributes
            attributes.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window.attributes = attributes
        }
    }
    // 处理窗口属性变化
    val windowInsetsController = remember(window) {
        WindowInsetsControllerCompat(window, window.decorView)
    }
    val appState: AppState = LocalAppState.current
    DisposableEffect(previewMode) {
        when (previewMode) {
            PreviewMode.COMMON -> {
                appState.setSystemBarStyle(
                    statusBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK)
                )
                onDispose {  }
            }
            PreviewMode.LANDSCAPE -> {
                windowInsetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                onDispose {
                    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }
    val volumeState: VolumeState = playerUiState.volumeState
    val brightnessState: BrightnessState = playerUiState.brightnessState
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().then(
                if (previewMode == PreviewMode.COMMON) Modifier.padding(innerPadding) else Modifier
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        when (previewMode) {
                            PreviewMode.COMMON -> Modifier.height(200.dp)
                            PreviewMode.LANDSCAPE -> Modifier.fillMaxHeight()
                        }
                    ).background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                PreviewPlayer(
                    title = playerUiState.title,
                    previewMode = previewMode,
                    playerState = playerState,
                    adjustBrightness = playerUiState.adjustBrightness,
                    adjustVolume = playerUiState.adjustVolume,
                    onBackClick = {
                        appState.setSystemBarStyle(
                            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                        )
                        onBackClick()
                    },
                    onBackHomeClick = {
                        appState.setSystemBarStyle(
                            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                        )
                        onBackHomeClick()
                    },
                    onEnterFullScreen = {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        previewMode = PreviewMode.LANDSCAPE
                    },
                    onExitFullScreen = {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        previewMode = PreviewMode.COMMON
                    },
                    modifier = Modifier.fillMaxSize()
                )
                VolumeIndicator(
                    volumeState = volumeState,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .widthIn(max = 240.dp)
                        .fillMaxWidth(0.8f)
                )
                BrightnessIndicator(
                    brightnessState = brightnessState,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .widthIn(max = 240.dp)
                        .fillMaxWidth(0.8f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PreviewDescription(
                        name = contentUiState.name,
                        score = contentUiState.score,
                        description = contentUiState.description
                    )
                }
                item {
                    PreviewSelection(
                        urls = contentUiState.urls,
                        selectedIndex = selectedUrlIndex,
                        onSelectIndex = { selectedUrlIndex = it }
                    )
                }
            }
        }
    }
}

@Composable
internal fun PreviewPlayer(
    title: String,
    previewMode: PreviewMode,
    playerState: PlayerState,
    adjustBrightness: (Float) -> Unit,
    adjustVolume: (Float) -> Unit,
    onBackClick: () -> Unit,
    onBackHomeClick: () -> Unit,
    onEnterFullScreen: () -> Unit,
    onExitFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isReadyToDestroy: Boolean by remember { mutableStateOf(false) }
    val onBackHandle: () -> Unit = remember(onBackClick, onExitFullScreen) {
        {
            when (previewMode) {
                PreviewMode.COMMON -> {
                    playerState.stop()
                    isReadyToDestroy = true
                    onBackClick()
                }
                PreviewMode.LANDSCAPE -> onExitFullScreen()
            }
        }
    }
    val onBackHomeHandle: () -> Unit = remember(onBackHomeClick) {
        {
            playerState.stop()
            isReadyToDestroy = true
            onBackHomeClick()
        }
    }
    BackHandler(onBack = onBackHandle)
    // 处理视频播放进度相关业务
    val contentPosition: Long by playerState.produceContentPositionState()
    val contentDuration: Long by playerState.produceContentDurationState()
    val contentBufferedPosition: Long by playerState.produceContentBufferedPositionState()
    val contentBufferedPercent: Float by remember {
        derivedStateOf {
            if (contentDuration != 0L) contentBufferedPosition * 1f / contentDuration else 0f
        }
    }
    val contentPercent: Float by remember {
        derivedStateOf {
            if (contentDuration != 0L) contentPosition * 1f / contentDuration else 0f
        }
    }
    var isProgressChanging: Boolean by remember { mutableStateOf(false) }
    var contentPercentToDisplay: Float by remember { mutableFloatStateOf(contentPercent) }
    LaunchedEffect(contentPercent) {
        if (!isProgressChanging) {
            contentPercentToDisplay = contentPercent
        }
    }
    // 处理视频控制组件的显示和隐藏事务
    val scope: CoroutineScope = rememberCoroutineScope()
    var showControllerHandler: Job? by remember { mutableStateOf(null) }
    var shouldShowController: Boolean by rememberSaveable { mutableStateOf(true) }
    var showControllerLock: Boolean by rememberSaveable { mutableStateOf(false) }
    val showController: (isLock: Boolean) -> Unit = remember {
        { isLock ->
            val lastHandler: Job? = showControllerHandler
            showControllerHandler = scope.launch {
                lastHandler?.cancelAndJoin()
                shouldShowController = true
                if (isLock || showControllerLock) {
                    showControllerLock = true
                    return@launch
                }
                delay(5_000)
                shouldShowController = false
            }
        }
    }
    val hideController: (isDelay: Boolean) -> Unit = remember {
        { isDelay ->
            val lastHandler: Job? = showControllerHandler
            showControllerHandler = scope.launch {
                lastHandler?.cancelAndJoin()
                showControllerLock = false
                if (isDelay) {
                    delay(5_000)
                }
                shouldShowController = false
            }
        }
    }
    LaunchedEffect(Unit) {
        // 进行页面或者页面重建的时候延时隐藏控制组件
        if (shouldShowController) {
            hideController(true)
        }
    }
    Player(
        state = playerState,
        modifier = modifier.alpha(if (isReadyToDestroy) 0f else 1f)
    ) {
        PlayerController(
            shouldShowController = shouldShowController,
            onClick = {
                // 单点屏幕显示或者隐藏控制台
                if (shouldShowController) hideController(false) else showController(false)
            },
            onDoubleClick = {
                // 双击屏幕开始、暂停视频
                if (playerState.isPlaying) playerState.pause() else playerState.play()
            },
            onLeftVerticalDrag = {
                // 控制视频亮度
                adjustBrightness(it)
            },
            onRightVerticalDrag = {
                // 控制视频音量
                adjustVolume(it)
            },
            onHorizontalDragStarted = {
                showController(true)
                isProgressChanging = true
            },
            onHorizontalDragging = {
                // 将手指滑动的进程映射到进度滑动
                contentPercentToDisplay = (contentPercentToDisplay + it / 3f).coerceIn(0f, 1f)
            },
            onHorizontalDragStopped = {
                hideController(true)
                if ((contentPercentToDisplay - contentPercent).absoluteValue >= 0.01) {
                    playerState.seekTo((contentDuration * contentPercentToDisplay).toLong())
                }
                isProgressChanging = false
            },
            modifier = Modifier.fillMaxSize(),
            topBar = {
                when (previewMode) {
                    PreviewMode.COMMON -> CommonPlayerTopBar(
                        onBack = onBackHandle,
                        onBackHome = onBackHomeHandle
                    )
                    PreviewMode.LANDSCAPE -> LandscapePlayerTopBar(
                        title = title,
                        onBack = onBackHandle
                    )
                }
            },
            bottomBar = {
                PlayerBottomBar(
                    previewMode = previewMode,
                    isPlaying = playerState.isPlaying,
                    shouldShowPlayButton = playerState.shouldShowPlayButton,
                    contentPosition = contentPosition,
                    contentDuration = contentDuration,
                    contentPercent = contentPercentToDisplay,
                    contentBufferedPercent = contentBufferedPercent,
                    onPlay = {
                        showController(false)
                        playerState.play()
                    },
                    onPause = {
                        showController(false)
                        playerState.pause()
                    },
                    onProgressChange = {
                        showController(false)
                        playerState.seekTo((contentDuration * it).toLong())
                    },
                    onProgressDragStarted = {
                        showController(true)
                        isProgressChanging = true
                    },
                    onProgressDragging = {
                        contentPercentToDisplay = it
                    },
                    onProgressDragStopped = {
                        hideController(true)
                        playerState.seekTo(
                            (contentDuration * contentPercentToDisplay).toLong()
                        )
                        isProgressChanging = false
                    },
                    onEnterFullScreen = {
                        showController(false)
                        onEnterFullScreen()
                    },
                    onExitFullScreen = {
                        showController(false)
                        onExitFullScreen()
                    }
                )
            }
        )
    }
}

@Composable
internal fun CommonPlayerTopBar(
    onBack: () -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundBrush: Brush = Brush.verticalGradient(listOf(Color.Black, Color.Transparent))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundBrush),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton(onBack = onBack)
        HomeButton(onBackHome = onBackHome)
    }
}

@Composable
internal fun LandscapePlayerTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundBrush: Brush = Brush.verticalGradient(listOf(Color.Black, Color.Transparent))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundBrush)
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier.windowInsetsStartWidth(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
            )
        )
        BackButton(onBack = onBack)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
internal fun PlayerBottomBar(
    previewMode: PreviewMode,
    isPlaying: Boolean,
    shouldShowPlayButton: Boolean,
    contentPosition: Long,
    contentDuration: Long,
    contentPercent: Float,
    contentBufferedPercent: Float,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onProgressChange: (Float) -> Unit,
    onProgressDragStarted: () -> Unit,
    onProgressDragging: (Float) -> Unit,
    onProgressDragStopped: () -> Unit,
    onEnterFullScreen: () -> Unit,
    onExitFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundBrush: Brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
    when (previewMode) {
        PreviewMode.COMMON -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .background(backgroundBrush),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (shouldShowPlayButton) {
                    PlayButton(
                        onClick = {
                            if (isPlaying) onPause() else onPlay()
                        },
                        iconPainter = painterResource(
                            if (isPlaying) com.qimi.app.qplayer.core.ui.R.drawable.ic_pause_24 else com.qimi.app.qplayer.core.ui.R.drawable.ic_play_arrow_24
                        ),
                        modifier = Modifier.wrapContentSize()
                    )
                }
                ProgressBar(
                    contentPercent = contentPercent,
                    contentBufferedPercent = contentBufferedPercent,
                    onProgressChange = onProgressChange,
                    onProgressDragStarted = onProgressDragStarted,
                    onProgressDragging = onProgressDragging,
                    onProgressDragStopped = onProgressDragStopped,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                )
                FullscreenButton(
                    onClick = onEnterFullScreen,
                    iconPainter = painterResource(com.qimi.app.qplayer.core.ui.R.drawable.ic_fullscreen_24),
                    modifier = Modifier.wrapContentSize()
                )
            }
        }
        PreviewMode.LANDSCAPE -> {
            val contentTime: String = remember(contentPosition) {
                val duration: Duration = Duration.ofMillis(contentPosition)
                String.format(Locale.CHINA, "%02d:%02d:%02d", duration.toHours() % 24, duration.toMinutes() % 60, duration.seconds % 60)
            }
            val durationTime: String = remember(contentDuration) {
                val duration: Duration = Duration.ofMillis(contentDuration)
                String.format(Locale.CHINA, "%02d:%02d:%02d", duration.toHours() % 24, duration.toMinutes() % 60, duration.seconds % 60)
            }
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(backgroundBrush)
                    .padding(horizontal = 24.dp)
            ) {
                ProgressBar(
                    contentPercent = contentPercent,
                    contentBufferedPercent = contentBufferedPercent,
                    onProgressChange = onProgressChange,
                    onProgressDragStarted = onProgressDragStarted,
                    onProgressDragging = onProgressDragging,
                    onProgressDragStopped = onProgressDragStopped,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (shouldShowPlayButton) {
                        PlayButton(
                            onClick = {
                                if (isPlaying) onPause() else onPlay()
                            },
                            iconPainter = painterResource(
                                if (isPlaying) com.qimi.app.qplayer.core.ui.R.drawable.ic_pause_24 else com.qimi.app.qplayer.core.ui.R.drawable.ic_play_arrow_24
                            ),
                            modifier = Modifier.wrapContentSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${contentTime}/${durationTime}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    FullscreenButton(
                        onClick = onExitFullScreen,
                        iconPainter = painterResource(com.qimi.app.qplayer.core.ui.R.drawable.ic_fullscreen_24),
                        modifier = Modifier.wrapContentSize()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
internal fun PreviewDescription(
    name: String,
    score: String,
    description: String,
    modifier: Modifier = Modifier
) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Text(
                text = score,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
        if (description.isNotEmpty()) {
            val annotatedText = remember(description) {
                val spanned = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                val text = spanned.toString()
                buildAnnotatedString { append(text) }
            }
            Text(
                text = annotatedText,
                modifier = Modifier
                    .animateContentSize()
                    .clickable(
                        interactionSource = null,
                        indication = null
                    ) { expanded = !expanded },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = if (expanded) Int.MAX_VALUE else 1
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PreviewSelection(
    urls: List<Pair<String, String>>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded: Boolean by rememberSaveable { mutableStateOf(false) }
    val lazyRowState: LazyListState = rememberLazyListState()
    Column(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.select_movie),
                style = MaterialTheme.typography.titleMedium
            )
            if (lazyRowState.canScrollForward || lazyRowState.canScrollBackward || expanded) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Rounded.KeyboardArrowDown
                        } else {
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight
                        },
                        contentDescription = null
                    )
                }
            }
        }
        AnimatedContent(
            targetState = expanded,
            label = "MovieSelection",
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 90)) + expandVertically())
                    .togetherWith(fadeOut(animationSpec = tween(90)) + shrinkVertically())
            }
        ) { isExpanded ->
            if (isExpanded) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.heightIn(max = 240.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(
                        items = urls
                    ) { index, item ->
                        FilterChip(
                            selected = selectedIndex == index,
                            onClick = { onSelectIndex(index) },
                            label = {
                                Text(
                                    text = item.first,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        )
                    }
                }
            } else {
                LazyRow(
                    state = lazyRowState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(urls) { index, item ->
                        FilterChip(
                            selected = selectedIndex == index,
                            onClick = { onSelectIndex(index) },
                            label = {
                                Text(text = item.first)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VolumeIndicator(
    volumeState: VolumeState,
    modifier: Modifier = Modifier
) {
    if (volumeState.isShow) {
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
                        if (volumeState.volume == 0f) com.qimi.app.qplayer.core.ui.R.drawable.ic_volume_off_24 else com.qimi.app.qplayer.core.ui.R.drawable.ic_volume_up_24
                    ),
                    contentDescription = null
                )
                LinearProgressIndicator(
                    progress = { volumeState.volume },
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surface,
                    trackColor = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun BrightnessIndicator(
    brightnessState: BrightnessState,
    modifier: Modifier = Modifier
) {
    if (brightnessState.isShow) {
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
                    painter = painterResource(com.qimi.app.qplayer.core.ui.R.drawable.ic_brightness_24),
                    contentDescription = null
                )
                LinearProgressIndicator(
                    progress = { brightnessState.brightness },
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surface,
                    trackColor = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
