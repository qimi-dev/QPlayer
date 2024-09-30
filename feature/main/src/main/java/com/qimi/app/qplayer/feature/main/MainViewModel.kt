package com.qimi.app.qplayer.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qimi.app.qplayer.core.data.repository.MoviesRepository
import com.qimi.app.qplayer.core.model.data.Movie
import com.qimi.app.qplayer.core.model.data.MovieList
import com.qimi.app.qplayer.core.ui.retry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _mainUiState: MutableStateFlow<MainUiState> =
        MutableStateFlow(
            MainUiState(
                commonMovies = listOf(),
                varietyShowMovies = listOf(),
                latestMovies = listOf()
            )
        )

    val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

    init {
        fetchCommonMovies()
        fetchLatestMovies()
        fetchVarietyShowMovies()
    }

    private fun fetchCommonMovies() = fetchMovies(type = 20) { res ->
        _mainUiState.update {
            it.copy(commonMovies = res.list)
        }
    }

    private fun fetchVarietyShowMovies() = fetchMovies(type = 82) { res ->
        _mainUiState.update {
            it.copy(varietyShowMovies = res.list)
        }
    }

    private fun fetchLatestMovies() = fetchMovies { res ->
        _mainUiState.update {
            it.copy(latestMovies = res.list)
        }
    }

    private fun fetchMovies(
        type: Int = 0,
        onSuccess: (MovieList) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            retry {
                moviesRepository.fetchMovieList(type = type).onSuccess { res ->
                    if (res.code != 1) {
                        return@onSuccess
                    }
                    onSuccess(res)
                    return@retry true
                }
                return@retry false
            }
        }
    }

}

data class MainUiState(
    val commonMovies: List<Movie>,
    val varietyShowMovies: List<Movie>,
    val latestMovies: List<Movie>
)