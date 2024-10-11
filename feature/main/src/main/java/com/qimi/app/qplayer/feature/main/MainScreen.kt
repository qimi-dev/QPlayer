package com.qimi.app.qplayer.feature.main

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.Coil
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.util.CoilUtils
import com.qimi.app.qplayer.core.model.data.Movie
import kotlinx.coroutines.Dispatchers

@Composable
internal fun MainRoute(
    onSearchMovie: () -> Unit,
    onSettingsClick: () -> Unit,
    onPreviewMovie: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val mainUiState by viewModel.mainUiState.collectAsStateWithLifecycle()

    MainScreen(
        mainUiState = mainUiState,
        onSearchMovie = onSearchMovie,
        onSettingsClick = onSettingsClick,
        onPreviewMovie = onPreviewMovie,
        onRefreshMovies = viewModel::refreshMovies,
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(
    mainUiState: MainUiState,
    onSearchMovie: () -> Unit,
    onSettingsClick: () -> Unit,
    onPreviewMovie: (Movie) -> Unit,
    onRefreshMovies: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchBarField(
                        onClick = onSearchMovie,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null
                    )
                },
                actions = {
                    Spacer(modifier = Modifier.width(24.dp))
                }
            )
        },
        modifier = modifier
    ) {
        val refreshState = rememberPullToRefreshState()
        var isRefreshing by remember { mutableStateOf(false) }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                onRefreshMovies {
                    isRefreshing = false
                }
            },
            state = refreshState,
            modifier = Modifier.fillMaxSize().padding(it)
        ) {
            CompositionLocalProvider(
                LocalOverscrollConfiguration provides null
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        CommonMoviesSection(
                            movies = mainUiState.commonMovies,
                            onPreviewMovie = onPreviewMovie,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                    }
                    item {
                        VarietyShowMoviesSection(
                            movies = mainUiState.varietyShowMovies,
                            onPreviewMovie = onPreviewMovie,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                    }
                    item {
                        LatestMoviesSection(
                            movies = mainUiState.latestMovies,
                            onPreviewMovie = onPreviewMovie,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SearchBarField(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.scale(0.8f)
            )
            BasicTextField(
                value = "",
                onValueChange = { },
                enabled = false,
                modifier = Modifier.padding(vertical = 8.dp),
                textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.outline),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
internal fun CommonMoviesSection(
    movies: List<Movie>,
    onPreviewMovie: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    MoviesSection(
        label = stringResource(R.string.movie),
        movies = movies,
        onPreviewMovie = onPreviewMovie,
        modifier = modifier
    )
}

@Composable
internal fun VarietyShowMoviesSection(
    movies: List<Movie>,
    onPreviewMovie: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    MoviesSection(
        label = stringResource(R.string.variety_show),
        movies = movies,
        onPreviewMovie = onPreviewMovie,
        modifier = modifier
    )
}

@Composable
internal fun LatestMoviesSection(
    movies: List<Movie>,
    onPreviewMovie: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    MoviesSection(
        label = stringResource(R.string.newest_addition),
        movies = movies,
        onPreviewMovie = onPreviewMovie,
        modifier = modifier
    )
}

@Composable
internal fun MoviesSection(
    label: String,
    movies: List<Movie>,
    onPreviewMovie: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    if (movies.isNotEmpty()) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = movies
                ) {
                    MovieCard(
                        movie = it,
                        onClick = { onPreviewMovie(it) },
                        modifier = Modifier.size(120.dp, 160.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun MovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context: Context = LocalContext.current
    val imageRequest: ImageRequest = remember {
        ImageRequest.Builder(context)
            .data(movie.image)
            .dispatcher(Dispatchers.IO)
            .diskCacheKey(movie.image)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.onSurface)
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                contentScale = ContentScale.FillBounds
            )
            if (movie.movieClass.isNotEmpty()) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    shape = RoundedCornerShape(8.dp, 0.dp, 0.dp, 0.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                ) {
                    Text(
                        text = movie.movieClass,
                        modifier = Modifier.padding(8.dp, 4.dp),
                        color = MaterialTheme.colorScheme.surface,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Surface(
                modifier = Modifier.wrapContentWidth().align(Alignment.TopStart),
                shape = RoundedCornerShape(0.dp, 0.dp, 8.dp, 0.dp),
                color = MaterialTheme.colorScheme.onSurface
            ) {
                Text(
                    text = movie.name,
                    modifier = Modifier.padding(8.dp, 4.dp),
                    color = MaterialTheme.colorScheme.surface,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
