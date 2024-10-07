package com.qimi.app.qplayer.feature.preview

import android.content.ContentResolver
import android.content.pm.ActivityInfo
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.qimi.app.qplayer.core.ui.BackButton
import com.qimi.app.qplayer.core.ui.FullscreenButton
import com.qimi.app.qplayer.core.ui.HomeButton
import com.qimi.app.qplayer.core.ui.PlayButton
import com.qimi.app.qplayer.core.ui.PlayProgressBar
import com.qimi.app.qplayer.core.ui.Player
import com.qimi.app.qplayer.core.ui.PlayerController
import com.qimi.app.qplayer.core.ui.PlayerState
import com.qimi.app.qplayer.core.ui.rememberPlayerState

@Composable
internal fun PreviewRoute(
    onBackClick: () -> Unit,
    onBackHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val playerUiState: PlayerUiState by viewModel.playerUiState.collectAsState()
    val contentUiState: ContentUiState by viewModel.contentUiState.collectAsState()
    val playerState: PlayerState = rememberPlayerState(viewModel.exoPlayer)
    PreviewScreen(
        playerUiState = playerUiState,
        contentUiState = contentUiState,
        playerState = playerState,
        onStop = viewModel::stop,
        onBackClick = onBackClick,
        onBackHomeClick = onBackHomeClick,
        modifier = modifier
    )
}

@Composable
internal fun PreviewScreen(
    playerUiState: PlayerUiState,
    contentUiState: ContentUiState,
    playerState: PlayerState,
    onStop: () -> Unit,
    onBackClick: () -> Unit,
    onBackHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFullscreen: Boolean by rememberSaveable { mutableStateOf(false) }
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
    if (!isFullscreen) {
        CompactPreviewScreen(
            playerUiState = playerUiState,
            contentUiState = contentUiState,
            playerState = playerState,
            selectedUrlIndex = selectedUrlIndex,
            onPlay = { selectedUrlIndex = it },
            onStop = onStop,
            onBackClick = onBackClick,
            onBackHomeClick = onBackHomeClick,
            onEnterFullScreen = { isFullscreen = true },
            modifier = modifier
        )
    } else {
        ExpandedPreviewScreen(
            playerUiState = playerUiState,
            contentUiState = contentUiState,
            playerState = playerState,
            onExitFullScreen = { isFullscreen = false },
            modifier = modifier
        )
    }
}

@Composable
internal fun ExpandedPreviewScreen(
    playerUiState: PlayerUiState,
    contentUiState: ContentUiState,
    playerState: PlayerState,
    onExitFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity: ComponentActivity = LocalContext.current as ComponentActivity
    val window: Window = activity.window
    val windowInsetsController = remember(window) {
        WindowInsetsControllerCompat(window, window.decorView)
    }
    DisposableEffect(Unit) {
        // 切换为传感器方向横屏，隐藏状态栏
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        // 退出全屏时回退所有的配置
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    BackHandler(onBack = onExitFullScreen)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ExpandedPlayer(
            playerUiState = playerUiState,
            contentUiState = contentUiState,
            playerState = playerState,
            onBack = onExitFullScreen,
            onExitFullScreen = onExitFullScreen,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal,
                    )
                )
        )
    }
}

@Composable
internal fun CompactPreviewScreen(
    playerUiState: PlayerUiState,
    contentUiState: ContentUiState,
    playerState: PlayerState,
    selectedUrlIndex: Int,
    onPlay: (Int) -> Unit,
    onStop: () -> Unit,
    onBackClick: () -> Unit,
    onBackHomeClick: () -> Unit,
    onEnterFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity: ComponentActivity = LocalContext.current as ComponentActivity
    var isReadyToRelease: Boolean by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // 状态栏设置为黑色背景
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK)
        )
    }
    val onBackHandle: () -> Unit = remember(onStop, activity) {
        {
            activity.enableEdgeToEdge()
            onStop()
            isReadyToRelease = true
        }
    }
    val onBack: () -> Unit = remember(onStop, activity, onBackClick) {
        {
            onBackHandle()
            onBackClick()
        }
    }
    val onBackHome: () -> Unit = remember(onStop, activity, onBackHomeClick) {
        {
            onBackHandle()
            onBackHomeClick()
        }
    }
    BackHandler(onBack = onBack)
    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Black)
            ) {
                CompactPlayer(
                    playerUiState = playerUiState,
                    playerState = playerState,
                    onBack = onBack,
                    onBackHome = onBackHome,
                    onEnterFullScreen = onEnterFullScreen,
                    modifier = Modifier.fillMaxSize(),
                    visible = !isReadyToRelease
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MovieDescription(
                        name = contentUiState.name,
                        score = contentUiState.score,
                        description = contentUiState.description
                    )
                }
                item {
                    MovieSelection(
                        urls = contentUiState.urls,
                        selectedIndex = selectedUrlIndex,
                        onSelectIndex = onPlay
                    )
                }
            }
        }
    }
}

@Composable
internal fun CompactPlayer(
    playerUiState: PlayerUiState,
    playerState: PlayerState,
    onBack: () -> Unit,
    onBackHome: () -> Unit,
    onEnterFullScreen: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    val shouldShowController: Boolean = playerUiState.controllerState
    val volumeState: VolumeState = playerUiState.volumeState
    val brightnessState: BrightnessState = playerUiState.brightnessState
    val currentPosition: Long by playerState.produceCurrentPositionState()
    val duration: Long by playerState.produceDurationState()
    val contentPercentage: Float by remember {
        derivedStateOf {
            if (duration == 0L) 0f else currentPosition * 1f / duration
        }
    }
    val bufferedPercentage: Float by playerState.produceBufferedPercentageState()
    Player(
        state = playerState,
        modifier = modifier.alpha(if (visible) 1f else 0f)
    ) {
        PlayerController(
            onClick = {
                // 单点屏幕显示控制台
                playerUiState.setControllerVisibility(!shouldShowController)
            },
            onDoubleClick = {
                // 双击屏幕开始、暂停视频
                if (playerState.isPlaying) playerState.pause() else playerState.play()
            },
            onAdjustVolume = playerUiState.adjustVolume,
            onAdjustBrightness = playerUiState.adjustBrightness,
            onAdjustProgress = {
                playerState.seekTo((contentPercentage + it).coerceIn(0f, 1f))
            },
            modifier = Modifier.fillMaxSize(),
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
                VolumeIndicator(
                    volumeState = volumeState,
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.5f)
                )
                BrightnessIndicator(
                    brightnessState = brightnessState,
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.5f)
                )
            },
            bottomBar = {
                if (shouldShowController) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (playerState.shouldShowPlayButton) {
                            PlayButton(
                                onClick = {
                                    playerUiState.setControllerVisibility(true)
                                    if (playerState.isPlaying) playerState.pause() else playerState.play()
                                },
                                iconPainter = painterResource(
                                    if (playerState.isPlaying) com.qimi.app.qplayer.core.ui.R.drawable.ic_pause_24 else com.qimi.app.qplayer.core.ui.R.drawable.ic_play_arrow_24
                                ),
                                modifier = Modifier.wrapContentSize()
                            )
                        }
                        PlayProgressBar(
                            contentPercentage = contentPercentage,
                            bufferedPercentage = bufferedPercentage,
                            onSeekTo = {
                                playerUiState.setControllerVisibility(true)
                                playerState.seekTo(it)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FullscreenButton(
                            onClick = {
                                playerUiState.setControllerVisibility(true)
                                onEnterFullScreen()
                            },
                            iconPainter = painterResource(com.qimi.app.qplayer.core.ui.R.drawable.ic_fullscreen_24),
                            modifier = Modifier.wrapContentSize()
                        )
                    }
                }
            }
        )
    }
}

@Composable
internal fun ExpandedPlayer(
    playerUiState: PlayerUiState,
    contentUiState: ContentUiState,
    playerState: PlayerState,
    onBack: () -> Unit,
    onExitFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowController: Boolean = playerUiState.controllerState
    val volumeState: VolumeState = playerUiState.volumeState
    val brightnessState: BrightnessState = playerUiState.brightnessState
    val currentPosition: Long by playerState.produceCurrentPositionState()
    val duration: Long by playerState.produceDurationState()
    val contentPercentage: Float by remember {
        derivedStateOf {
            if (duration == 0L) 0f else currentPosition * 1f / duration
        }
    }
    val bufferedPercentage: Float by playerState.produceBufferedPercentageState()
    Player(
        state = playerState,
        modifier = modifier
    ) {
        PlayerController(
            onClick = {
                // 单点屏幕显示控制台
                playerUiState.setControllerVisibility(!shouldShowController)
            },
            onDoubleClick = {
                // 双击屏幕开始、暂停视频
                if (playerState.isPlaying) playerState.pause() else playerState.play()
            },
            onAdjustVolume = playerUiState.adjustVolume,
            onAdjustBrightness = playerUiState.adjustBrightness,
            onAdjustProgress = {
                playerState.seekTo((contentPercentage + it).coerceIn(0f, 1f))
            },
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
                            text = contentUiState.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            },
            content = {
                VolumeIndicator(
                    volumeState = volumeState,
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.5f)
                )
                BrightnessIndicator(
                    brightnessState = brightnessState,
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.5f)
                )
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
                            contentPercentage = contentPercentage,
                            bufferedPercentage = bufferedPercentage,
                            onSeekTo = {
                                playerUiState.setControllerVisibility(true)
                                playerState.seekTo(it)
                            },
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (playerState.shouldShowPlayButton) {
                                PlayButton(
                                    onClick = {
                                        playerUiState.setControllerVisibility(true)
                                        if (playerState.isPlaying) playerState.pause() else playerState.play()
                                    },
                                    iconPainter = painterResource(
                                        if (playerState.isPlaying) com.qimi.app.qplayer.core.ui.R.drawable.ic_pause_24  else com.qimi.app.qplayer.core.ui.R.drawable.ic_play_arrow_24
                                    ),
                                    modifier = Modifier.wrapContentSize()
                                )
                            }
                            FullscreenButton(
                                onClick = {
                                    playerUiState.setControllerVisibility(true)
                                    onExitFullScreen()
                                },
                                iconPainter = painterResource(com.qimi.app.qplayer.core.ui.R.drawable.ic_fullscreen_24),
                                modifier = Modifier.wrapContentSize()
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
internal fun MovieDescription(
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
internal fun MovieSelection(
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
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    urls.forEachIndexed { index, pair ->
                        FilterChip(
                            selected = selectedIndex == index,
                            onClick = { onSelectIndex(index) },
                            label = {
                                Text(text = pair.first)
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
    val animatedProgress by animateFloatAsState(
        targetValue = volumeState.volume,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
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
                        if (animatedProgress == 0f) com.qimi.app.qplayer.core.ui.R.drawable.ic_volume_off_24 else com.qimi.app.qplayer.core.ui.R.drawable.ic_volume_up_24
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
}

@Composable
fun BrightnessIndicator(
    brightnessState: BrightnessState,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = brightnessState.brightness,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
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
                    progress = { animatedProgress },
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surface,
                    trackColor = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
