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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository
) : ViewModel() {

    val mainUiState: StateFlow<MainUiState> =
        combine(
            moviesRepository.getCachedMovies(20),
            moviesRepository.getCachedMovies(82),
            moviesRepository.getCachedMovies(0)
        ) { commonMovies, varietyShowMovies, latestMovies ->
            MainUiState(
                commonMovies = commonMovies,
                varietyShowMovies = varietyShowMovies,
                latestMovies = latestMovies
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MainUiState(listOf(), listOf(), listOf())
        )

    fun refreshMovies(onCompleted: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            moviesRepository.sync()
            onCompleted()
        }
    }

}

data class MainUiState(
    val commonMovies: List<Movie>,
    val varietyShowMovies: List<Movie>,
    val latestMovies: List<Movie>
)