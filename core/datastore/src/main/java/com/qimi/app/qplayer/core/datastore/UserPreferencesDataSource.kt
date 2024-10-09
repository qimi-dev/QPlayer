package com.qimi.app.qplayer.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.qimi.app.qplayer.core.model.data.PlayingSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val DEFAULT_BUFFER_DURATIONS: Long = 50

private val BUFFER_DURATIONS = longPreferencesKey("BUFFER_DURATIONS")

class UserPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<Preferences>
) {

    val playingSettings: Flow<PlayingSettings> = userPreferences.data.map {
        PlayingSettings(
            bufferDurations = it[BUFFER_DURATIONS] ?: DEFAULT_BUFFER_DURATIONS
        )
    }

    suspend fun setBufferDurations(bufferDurations: Long) {
        userPreferences.edit {
            it[BUFFER_DURATIONS] = bufferDurations
        }
    }

}






