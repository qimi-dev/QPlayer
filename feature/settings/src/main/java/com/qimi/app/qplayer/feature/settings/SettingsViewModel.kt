package com.qimi.app.qplayer.feature.settings

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val destinations: MutableList<SettingsDestination> =
        mutableStateListOf(SettingsDestination.MAIN_SETTING)

    val topDestination: SettingsDestination by derivedStateOf { destinations.last() }

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

