package com.qimi.app.qplayer.feature.preview

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.qimi.app.qplayer.core.model.data.Movie
import com.qimi.app.qplayer.core.ui.CompactPlayerController
import com.qimi.app.qplayer.core.ui.Player
import com.qimi.app.qplayer.core.ui.PlayerState
import kotlinx.coroutines.delay

@Composable
internal fun PreviewRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val previewUiState: PreviewUiState by viewModel.previewUiState.collectAsState()
    PreviewScreen(
        previewUiState = previewUiState,
        playerState = viewModel.playerState,
        onPlay = viewModel::play,
        onStop = viewModel::stop,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
internal fun PreviewScreen(
    previewUiState: PreviewUiState,
    playerState: PlayerState,
    onPlay: (Int) -> Unit,
    onStop: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlayerShow: Boolean by remember { mutableStateOf(true) }
    BackHandler {
        onStop()
        isPlayerShow = false
        onBackClick()
    }
    Scaffold {
        Column(modifier = modifier.padding(it)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Black)
            ) {
                if (isPlayerShow) {
                    CompactPlayerWindow(
                        playerState = playerState,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Spacer(modifier = Modifier.fillMaxSize())
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MovieDescription(movie = previewUiState.movie)
                }
                item {
                    MovieSelection(
                        movieUrls = previewUiState.movieUrls,
                        selectedIndex = previewUiState.selectedIndex,
                        onSelectIndex = onPlay
                    )
                }
            }
        }
    }
}

@Composable
internal fun CompactPlayerWindow(
    playerState: PlayerState,
    modifier: Modifier = Modifier
) {
    Player(
        state = playerState,
        modifier = modifier
    ) {
        CompactPlayerController(
            state = playerState,
            onFullscreen = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
internal fun MovieDescription(
    movie: Movie,
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
                text = movie.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Text(
                text = movie.score,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
        if (movie.content.isNotEmpty()) {
            val annotatedText = remember(movie) {
                val spanned = HtmlCompat.fromHtml(movie.content, HtmlCompat.FROM_HTML_MODE_LEGACY)
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
    movieUrls: List<Pair<String, String>>,
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
                    movieUrls.forEachIndexed { index, pair ->
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
                    itemsIndexed(movieUrls) { index, item ->
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