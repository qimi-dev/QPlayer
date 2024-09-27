package com.qimi.app.qplayer.feature.main

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.qimi.app.qplayer.core.model.data.Movie
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Dispatcher

@Composable
internal fun MainRoute(
    onSearchMovie: () -> Unit,
    onPreviewMovie: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val latestMovieListUiState by viewModel.latestMovieListUiState.collectAsStateWithLifecycle()

    MainScreen(
        latestMovieListUiState = latestMovieListUiState,
        onSearchMovie = onSearchMovie,
        onPreviewMovie = onPreviewMovie,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(
    latestMovieListUiState: LatestMovieListUiState,
    onSearchMovie: () -> Unit,
    onPreviewMovie: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchBarField(
                        onClick = onSearchMovie,
                        modifier = Modifier.wrapContentWidth()
                    )
                },
                navigationIcon = {
                    OutlinedCard(
                        onClick = {},
                        modifier = Modifier.size(48.dp).padding(8.dp),
                        shape = CircleShape
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_head),
                            contentDescription = null
                        )
                    }
                },
                actions = {

                }
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it)
        ) {
            LatestMovieList(
                uiState = latestMovieListUiState,
                onPreviewMovie = onPreviewMovie,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
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
                imageVector = Icons.Outlined.Search,
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
internal fun LatestMovieList(
    uiState: LatestMovieListUiState,
    onPreviewMovie: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState is LatestMovieListUiState.Success) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.newest_addition),
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.moveList.list) {
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
