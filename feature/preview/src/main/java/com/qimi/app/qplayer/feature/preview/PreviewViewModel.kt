package com.qimi.app.qplayer.feature.preview

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor() : ViewModel() {

    private val _movieWindowUiState: MutableStateFlow<MovieWindowUiState> =
        MutableStateFlow(MovieWindowUiState.Initial)

    val movieWindowUiState: StateFlow<MovieWindowUiState> = _movieWindowUiState.asStateFlow()

    fun play(url: String) {
        _movieWindowUiState.value = MovieWindowUiState.Playing(url)
    }

}

sealed interface MovieWindowUiState {
    data object Initial : MovieWindowUiState
    data class Playing(val url: String) : MovieWindowUiState
}

