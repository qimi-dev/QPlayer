package com.qimi.app.qplayer.feature.search

import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.qimi.app.qplayer.core.model.data.Movie
import com.qimi.app.qplayer.core.model.data.MovieList
import com.qimi.app.qplayer.feature.search.navigation.SearchRoute
import kotlinx.coroutines.Dispatchers

@Composable
internal fun SearchRoute(
    onBackClick: () -> Unit,
    onPreviewMovie: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchUiState: SearchUiState by viewModel.searchUiState.collectAsState()
    SearchScreen(
        searchUiState = searchUiState,
        onSearch = viewModel::onSearch,
        onExpandSearch = viewModel::onExpandSearch,
        onPreviewMovie = onPreviewMovie,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreen(
    searchUiState: SearchUiState,
    onSearch: (String) -> Unit,
    onExpandSearch: () -> Unit,
    onPreviewMovie: (Movie) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchContext: String by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val searchFocusRequester: FocusRequester = remember { FocusRequester() }
    var isFirstEnter: Boolean by rememberSaveable { mutableStateOf(true) }
    BackHandler {
        // 退出当前页面时提前清楚焦点和虚拟键盘
        focusManager.clearFocus()
        onBackClick()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchBarField(
                        context = searchContext,
                        onContextChange = { searchContext = it },
                        onContextReset = {
                            searchContext = ""
                            searchFocusRequester.requestFocus()
                        },
                        onDone = {
                            focusManager.clearFocus()
                            onSearch(searchContext)
                        },
                        focusRequester = searchFocusRequester,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // 放置在 TextField 后方防止重组过程中异常
                    LaunchedEffect(isFirstEnter) {
                        if (isFirstEnter) {
                            searchFocusRequester.requestFocus()
                        }
                        isFirstEnter = false
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onBackClick() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            onSearch(searchContext)
                        },
                        enabled = searchUiState !is SearchUiState.View || !searchUiState.isSearching
                    ) {
                        Text(text = stringResource(R.string.search))
                    }
                }
            )
        },
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(it)) {
            when (searchUiState) {
                is SearchUiState.Idle -> {
                    SearchCandidateArea(modifier = Modifier.fillMaxSize())
                }
                is SearchUiState.View -> {
                    if (searchUiState.isSearching && searchUiState.movies.isEmpty()) {
                        SearchingResultArea(modifier = Modifier.fillMaxSize())
                    } else {
                        SearchResultArea(
                            searchUiState = searchUiState,
                            onPreviewMovie = onPreviewMovie,
                            onExpandSearch = onExpandSearch,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SearchBarField(
    context: String,
    onContextChange: (String) -> Unit,
    onContextReset: () -> Unit,
    onDone: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.scale(0.8f)
            )
            BasicTextField(
                value = context,
                onValueChange = onContextChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .focusRequester(focusRequester),
                textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.outline),
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = { onDone() }
                )
            )
            if (context.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Rounded.Clear,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.scale(0.8f).clickable { onContextReset() }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SearchCandidateArea(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
//        Text(
//            text = stringResource(R.string.search_history),
//            style = MaterialTheme.typography.titleMedium
//        )
//        FlowRow(
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            repeat(10) {
//                ElevatedSuggestionChip(
//                    onClick = {},
//                    label = {
//                        Text(text = "搜索历史${it}")
//                    },
//                    colors = SuggestionChipDefaults.suggestionChipColors(
//                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
//                        labelColor = MaterialTheme.colorScheme.onSurface
//                    ),
//                    elevation = SuggestionChipDefaults.suggestionChipElevation()
//                )
//            }
//        }
    }
}

@Composable
internal fun SearchResultArea(
    searchUiState: SearchUiState.View,
    onPreviewMovie: (Movie) -> Unit,
    onExpandSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context: Context = LocalContext.current
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(
            items = searchUiState.movies,
            key = { it.id }
        ) { movie ->
            val imageRequest: ImageRequest = remember {
                ImageRequest.Builder(context)
                    .data(movie.image)
                    .dispatcher(Dispatchers.IO)
                    .diskCacheKey(movie.image)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
            }
            Surface(
                onClick = { onPreviewMovie(movie) },
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.size(120.dp, 160.dp).padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black,
                    ) {
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f).padding(8.dp)
                    ) {
                        Text(
                            text =  "${movie.name} ${movie.score}",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        val annotatedText = remember(movie) {
                            val spanned = HtmlCompat.fromHtml(movie.content, HtmlCompat.FROM_HTML_MODE_LEGACY)
                            val text = spanned.toString()
                            buildAnnotatedString { append(text) }
                        }
                        Text(
                            text = annotatedText,
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
        item {
            if (searchUiState.isAllowExpandSearch) {
                if (!searchUiState.isSearching) {
                    LaunchedEffect(Unit) { onExpandSearch() }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.loading),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "到底了",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
internal fun SearchingResultArea(
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.height(120.dp),
            contentScale = ContentScale.FillHeight
        )
    }
}





