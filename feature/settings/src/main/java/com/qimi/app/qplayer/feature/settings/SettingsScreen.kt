package com.qimi.app.qplayer.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class SettingsDestination(
    val titleId: Int
) {
    MAIN_SETTINGS(
        titleId = R.string.setting
    ),
    PLAYING_SETTINGS(
        titleId = R.string.playing_setting
    )
}

@Composable
internal fun SettingsRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val topDestination: SettingsDestination = viewModel.topDestination
    val onBackHandler: () -> Unit = remember {
        {
            if (!viewModel.onBackHandler()) {
                onBackClick()
            }
        }
    }
    BackHandler(onBack = onBackHandler)
    val playingSettingsUiState: PlayingSettingsUiState by viewModel.playingSettingsUiState.collectAsState()
    SettingsScreen(
        topDestination = topDestination,
        playingSettingsUiState = playingSettingsUiState,
        navigateTo = viewModel::navigateTo,
        onBackClick = onBackHandler,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    topDestination: SettingsDestination,
    playingSettingsUiState: PlayingSettingsUiState,
    navigateTo: (SettingsDestination) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = topDestination,
                        label = "SettingAnimation",
                        transitionSpec = {
                            fadeIn(animationSpec = tween(700))
                                .togetherWith(fadeOut(animationSpec = tween(700)))
                        }
                    ) {
                        Text(text = stringResource(it.titleId))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = topDestination,
            modifier = Modifier.padding(it),
            label = "SettingAnimation",
            transitionSpec = {
                fadeIn(animationSpec = tween(700))
                    .togetherWith(fadeOut(animationSpec = tween(700)))
            }
        ) {
            when (it) {
                SettingsDestination.MAIN_SETTINGS -> {
                    MainSettings(navigateTo=  navigateTo)
                }
                SettingsDestination.PLAYING_SETTINGS -> {
                    PlayingSettings(
                        playingSettingsUiState = playingSettingsUiState
                    )
                }
            }
        }
    }
}

@Composable
internal fun MainSettings(
    navigateTo: (SettingsDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SettingGroup {
                // 播放设置
                EntranceItem(
                    title = stringResource(R.string.playing_setting),
                    icon = painterResource(R.drawable.ic_video_settings_24),
                    onClick = { navigateTo(SettingsDestination.PLAYING_SETTINGS) }
                )
                // 管理存储空间
                EntranceItem(
                    title = stringResource(R.string.manage_store_space),
                    icon = painterResource(R.drawable.ic_storage_24),
                    onClick = {}
                )
            }
        }
        item {
            SettingGroup {
                // 关于QPlayer
                EntranceItem(
                    title = stringResource(R.string.about_software),
                    icon = painterResource(R.drawable.ic_question_24),
                    onClick = {}
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayingSettings(
    playingSettingsUiState: PlayingSettingsUiState,
    modifier: Modifier = Modifier
) {
    if (playingSettingsUiState !is PlayingSettingsUiState.Success) {
        return
    }
    val bufferDurations: Long = playingSettingsUiState.bufferDurations
    val setBufferDurations: (Long) -> Unit = playingSettingsUiState.setBufferDurations
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SettingGroup {
                CommonItem(
                    title = stringResource(R.string.buffer_durations),
                    content = {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { setBufferDurations((bufferDurations - 10).coerceIn(10, 900)) }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_remove_24),
                                    contentDescription = null
                                )
                            }
                            Text(
                                text = "$bufferDurations",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                            )
                            IconButton(
                                onClick = { setBufferDurations((bufferDurations + 10).coerceIn(10, 900)) }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_add_24),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
internal fun SettingGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            content()
        }
    }
}

@Composable
internal fun EntranceItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    tailText: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 0.dp, shape = RoundedCornerShape(16.dp), clip = true)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .shadow(elevation = 0.dp, shape = CircleShape, clip = true)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium
        )
        if (tailText != null) {
            Text(
                text = tailText,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null
        )
    }
}

@Composable
internal fun CommonItem(
    title: String,
    content: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 0.dp, shape = RoundedCornerShape(16.dp), clip = true)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        content()
    }
}


























