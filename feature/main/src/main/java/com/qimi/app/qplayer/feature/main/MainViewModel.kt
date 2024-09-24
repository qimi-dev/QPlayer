package com.qimi.app.qplayer.feature.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qimi.app.qplayer.core.data.repository.MoviesRepository
import com.qimi.app.qplayer.core.model.data.MovieList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _movieListUiState: MutableStateFlow<MovieListUiState> =
        MutableStateFlow(MovieListUiState.Loading)

    val movieListUiState: StateFlow<MovieListUiState> = _movieListUiState.asStateFlow()

    init {
        refreshMovieList()
    }

    private fun refreshMovieList() {
        viewModelScope.launch(Dispatchers.IO) {
            _movieListUiState.value = MovieListUiState.Loading
            moviesRepository.fetchMovieList("detail")
                .onSuccess {
                    if (it.code == 1) {
                        _movieListUiState.value = MovieListUiState.Success(it)
                    } else {
                        _movieListUiState.value = MovieListUiState.Failure
                    }
                }.onFailure {
                    _movieListUiState.value = MovieListUiState.Failure
                }
        }
    }

}

sealed interface MovieListUiState {
    data object Loading : MovieListUiState
    data class Success(val list: MovieList) : MovieListUiState
    data object Failure : MovieListUiState
}
