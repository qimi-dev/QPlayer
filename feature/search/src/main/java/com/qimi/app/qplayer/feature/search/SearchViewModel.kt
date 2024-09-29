package com.qimi.app.qplayer.feature.search

import android.util.Log
import kotlin.math.pow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qimi.app.qplayer.core.data.repository.MoviesRepository
import com.qimi.app.qplayer.core.model.data.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository
) : ViewModel() {

    private val _searchUiState: MutableStateFlow<SearchUiState> =
        MutableStateFlow(SearchUiState.Idle)

    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    private val cachedMovies: MutableList<Movie> = mutableListOf()

    private var cachedSearchContext: String = ""

    private var cachedSearchPageIndex: Int = 1

    private var cachedSearchPageCount: Int = 1

    private val isAllowExpandSearch: Boolean
        get() = cachedSearchPageIndex <= cachedSearchPageCount

    fun onSearch(searchContext: String) {
        // 重置搜索数据，更新搜索状态
        cachedMovies.clear()
        cachedSearchContext = searchContext
        cachedSearchPageIndex = 1
        cachedSearchPageCount = 1
        onExpandSearch()
    }

    fun onExpandSearch() {
        // 判断当前是否支持继续搜索
        if (cachedSearchPageIndex > cachedSearchPageCount) {
            return
        }
        // 更新搜索状态
        _searchUiState.value = SearchUiState.View(
            isSearching = true,
            isAllowExpandSearch = isAllowExpandSearch,
            movies = cachedMovies
        )
        viewModelScope.launch(Dispatchers.IO) {
            repeat(3) {
                moviesRepository.fetchMovieList(
                    keyword = cachedSearchContext,
                    pageIndex = cachedSearchPageIndex
                ).onSuccess {
                    if (it.code != 1) {
                        // 搜索失败重试
                        return@onSuccess
                    }
                    // 搜索成功，更新状态
                    cachedMovies.addAll(it.list)
                    cachedSearchPageIndex++
                    cachedSearchPageCount = it.pageCount
                    _searchUiState.value = SearchUiState.View(
                        isSearching = false,
                        isAllowExpandSearch = isAllowExpandSearch,
                        movies = cachedMovies
                    )
                    return@launch
                }
                delay(2.0.pow(it).seconds)
            }
            // 搜索完全失败，则恢复状态
            _searchUiState.value = SearchUiState.View(
                isSearching = false,
                isAllowExpandSearch = isAllowExpandSearch,
                movies = cachedMovies
            )
        }
    }

}

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data class View(
        val isSearching: Boolean,
        val isAllowExpandSearch: Boolean,
        val movies: List<Movie>
    ) : SearchUiState
}
