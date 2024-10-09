package com.qimi.app.qplayer.feature.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qimi.app.qplayer.core.data.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val destinations: MutableList<SettingsDestination> =
        mutableStateListOf(SettingsDestination.MAIN_SETTINGS)

    val topDestination: SettingsDestination by derivedStateOf { destinations.last() }

    val playingSettingsUiState: StateFlow<PlayingSettingsUiState> =
        userDataRepository.playingSettings.map {
            PlayingSettingsUiState.Success(it.bufferDurations, ::setBufferDurations)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = PlayingSettingsUiState.Loading
        )

    private fun setBufferDurations(bufferDurations: Long) {
        viewModelScope.launch {
            userDataRepository.setBufferDurations(bufferDurations)
        }
    }

    fun navigateTo(destination: SettingsDestination) {
        destinations.add(destination)
    }

    fun onBackHandler(): Boolean {
        if (destinations.size == 1) {
            return false
        }
        destinations.removeLast()
        return true
    }

}

sealed interface PlayingSettingsUiState {
    data object Loading : PlayingSettingsUiState
    data class Success(
        val bufferDurations: Long,
        val setBufferDurations: (Long) -> Unit
    ) : PlayingSettingsUiState
}


