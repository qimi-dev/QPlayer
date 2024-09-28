package com.qimi.app.qplayer.feature.preview

import android.content.pm.ActivityInfo
import android.view.Window
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.qimi.app.qplayer.core.ui.CompactPlayerController
import com.qimi.app.qplayer.core.ui.Player
import com.qimi.app.qplayer.core.ui.PlayerState

@Composable
internal fun PreviewRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val previewUiState: PreviewUiState by viewModel.previewUiState.collectAsState()
    PreviewScreen(
        previewUiState = previewUiState,
        onPlay = viewModel::play,
        onStop = viewModel::stop,
        onBackClick = onBackClick,
        onEnterFullScreen = viewModel::enterFullScreen,
        onExitFullScreen = viewModel::exitFullScreen,
        modifier = modifier
    )
}

@Composable
internal fun PreviewScreen(
    previewUiState: PreviewUiState,
    onPlay: (Int) -> Unit,
    onStop: () -> Unit,
    onBackClick: () -> Unit,
    onEnterFullScreen: () -> Unit,
    onExitFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold {
        when (previewUiState.previewMode) {
            PreviewMode.NON_FULLSCREEN -> {
                CompactPreviewScreen(
                    previewUiState = previewUiState,
                    onPlay = onPlay,
                    onStop = onStop,
                    onBackClick = onBackClick,
                    onEnterFullScreen = onEnterFullScreen,
                    modifier = modifier.padding(it)
                )
            }
            PreviewMode.FULLSCREEN -> {
                ExpandedPreviewScreen(
                    previewUiState = previewUiState,
                    onExitFullScreen = onExitFullScreen,
                    modifier = modifier.padding(it)
                )
            }
        }
    }
}

@Composable
internal fun ExpandedPreviewScreen(
    previewUiState: PreviewUiState,
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
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        // 退出全屏时回退所有的配置
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
        }
    }
    BackHandler(onBack = onExitFullScreen)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        CompactPlayer(
            playerState = previewUiState.playerState,
            onBack = onExitFullScreen,
            onEnterFullScreen = onExitFullScreen,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
internal fun CompactPreviewScreen(
    previewUiState: PreviewUiState,
    onPlay: (Int) -> Unit,
    onStop: () -> Unit,
    onBackClick: () -> Unit,
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
    val onBack: () -> Unit = remember(onStop, activity, onBackClick) {
        {
            activity.enableEdgeToEdge()
            onStop()
            isReadyToRelease = true
            onBackClick()
        }
    }
    BackHandler(onBack = onBack)
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(if (!isReadyToRelease) Color.Black else Color.Transparent)
        ) {
            CompactPlayer(
                playerState = previewUiState.playerState,
                onBack = onBack,
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
                    name = previewUiState.name,
                    score = previewUiState.score,
                    description = previewUiState.description
                )
            }
            item {
                MovieSelection(
                    urls = previewUiState.urls,
                    selectedIndex = previewUiState.selectedUrlIndex,
                    onSelectIndex = onPlay
                )
            }
        }
    }
}

@Composable
internal fun CompactPlayer(
    playerState: PlayerState,
    onBack: () -> Unit,
    onEnterFullScreen: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    Player(
        state = playerState,
        modifier = modifier.alpha(if (visible) 1f else 0f)
    ) {
        CompactPlayerController(
            state = playerState,
            onBack = onBack,
            onEnterFullScreen = onEnterFullScreen,
            modifier = Modifier.fillMaxSize()
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
                            Icons.Outlined.KeyboardArrowDown
                        } else {
                            Icons.AutoMirrored.Outlined.KeyboardArrowRight
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