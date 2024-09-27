package com.qimi.app.qplayer.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qimi.app.qplayer.core.data.repository.MoviesRepository
import com.qimi.app.qplayer.core.model.data.MovieList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _latestMovieListUiState: MutableStateFlow<LatestMovieListUiState> =
        MutableStateFlow(LatestMovieListUiState.Loading)

    val latestMovieListUiState: StateFlow<LatestMovieListUiState> = _latestMovieListUiState.asStateFlow()

    init {
        refreshLatestMovieList()
    }

    private fun refreshLatestMovieList() {
        viewModelScope.launch(Dispatchers.IO) {
            _latestMovieListUiState.value = LatestMovieListUiState.Loading
            moviesRepository.fetchMovieList()
                .onSuccess {
                    if (it.code == 1) {
                        _latestMovieListUiState.value = LatestMovieListUiState.Success(it)
                    } else {
                        _latestMovieListUiState.value = LatestMovieListUiState.Failure
                    }
                }.onFailure {
                    _latestMovieListUiState.value = LatestMovieListUiState.Failure
                }
        }
    }

}

sealed interface LatestMovieListUiState {
    data object Loading : LatestMovieListUiState
    data class Success(val moveList: MovieList) : LatestMovieListUiState
    data object Failure : LatestMovieListUiState
}
