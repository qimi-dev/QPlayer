package com.qimi.app.qplayer.feature.preview

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.qimi.app.qplayer.core.model.data.Movie

@Composable
internal fun PreviewRoute(
    movie: Movie,
    modifier: Modifier = Modifier,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val movieWindowUiState by viewModel.movieWindowUiState.collectAsState()
    PreviewScreen(
        movie = movie,
        onSelectUrl = viewModel::play,
        movieWindowUiState = movieWindowUiState,
        modifier = modifier
    )
}

@Composable
internal fun PreviewScreen(
    movie: Movie,
    onSelectUrl: (String) -> Unit,
    movieWindowUiState: MovieWindowUiState,
    modifier: Modifier = Modifier
) {
    Scaffold {
        Column(modifier = modifier.padding(it)) {
            MovieWindow(
                movieWindowUiState = movieWindowUiState
            )
            MovieDescription(
                movie = movie,
                onSelectUrl = onSelectUrl
            )
        }
    }
}

@Composable
internal fun MovieWindow(
    movieWindowUiState: MovieWindowUiState,
    modifier: Modifier = Modifier
) {
    val context: Context = LocalContext.current
    val player: ExoPlayer = remember { ExoPlayer.Builder(context).build() }
    LaunchedEffect(movieWindowUiState) {
        when(movieWindowUiState) {
            is MovieWindowUiState.Initial -> Unit
            is MovieWindowUiState.Playing -> {
                val mediaItem = MediaItem.fromUri(movieWindowUiState.url)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }
    // TODO 使用常见的横屏比例
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        AndroidView(
            factory = { PlayerView(context) },
            modifier = Modifier.fillMaxSize(),
            update = {
                it.player = player
                it.useController = false
            }
        )
    }
}

@Composable
internal fun MovieDescription(
    movie: Movie,
    onSelectUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    val urls: List<Pair<String, String>> by remember {
        derivedStateOf {
            movie.urls
                .split("#")
                .map {
                    val (first, second) = it.split("$")
                    first to second
                }
        }
    }
    var selectedIndex: Int by remember(urls) { mutableStateOf(0) }
    LaunchedEffect(selectedIndex) {
        onSelectUrl(urls[selectedIndex].second)
    }
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = movie.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
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
            if (movie.content.isNotEmpty()) {
                Text(
                    text = movie.content,
                    modifier = Modifier.animateContentSize(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (expanded) Int.MAX_VALUE else 1
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.select_movie),
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(urls) { index, item ->
                    FilterChip(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                        label = {
                            Text(text = item.first)
                        }
                    )
                }
            }
        }
    }
}

