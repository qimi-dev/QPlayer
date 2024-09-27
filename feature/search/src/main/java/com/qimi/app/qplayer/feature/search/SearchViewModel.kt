package com.qimi.app.qplayer.feature.search

import android.util.Log
import kotlin.math.pow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qimi.app.qplayer.core.data.repository.MoviesRepository
import com.qimi.app.qplayer.core.model.data.MovieList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository
) : ViewModel() {

    private val _searchUiState: MutableStateFlow<SearchUiState> =
        MutableStateFlow(SearchUiState.Idle)

    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    fun onSearch(context: String) {
        _searchUiState.value = SearchUiState.Searching
        viewModelScope.launch(Dispatchers.IO) {
            repeat(3) {
                moviesRepository.fetchMovieList(keyword = context).onSuccess {
                    if (it.code != 1) {
                        return@onSuccess
                    }
                    _searchUiState.value = SearchUiState.Success(it)
                    return@launch
                }
                delay(2.0.pow(it).seconds)
            }
        }
    }

}

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Searching : SearchUiState
    data class Success(val movieList: MovieList) : SearchUiState
}
