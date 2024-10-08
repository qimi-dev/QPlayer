package com.qimi.app.qplayer.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.qimi.app.qplayer.core.model.data.PlayingSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val PLAYING_SETTING_PROGRESS = floatPreferencesKey("PLAYING_SETTING_PROGRESS")

class UserPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<Preferences>
) {

    val playingSettings: Flow<PlayingSettings> = userPreferences.data.map {
        PlayingSettings(
            progress = it[PLAYING_SETTING_PROGRESS] ?: 0f
        )
    }

    suspend fun setPlayingProgress(progress: Float) {
        userPreferences.edit {
            it[PLAYING_SETTING_PROGRESS] = progress
        }
    }

}






