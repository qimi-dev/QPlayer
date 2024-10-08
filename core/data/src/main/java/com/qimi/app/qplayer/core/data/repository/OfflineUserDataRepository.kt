package com.qimi.app.qplayer.core.data.repository

import com.qimi.app.qplayer.core.datastore.UserPreferencesDataSource
import com.qimi.app.qplayer.core.model.data.PlayingSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class OfflineUserDataRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource
) : UserDataRepository {

    override val playingSettings: Flow<PlayingSettings> =
        userPreferencesDataSource.playingSettings

    override suspend fun setPlayingProgress(progress: Float) =
        userPreferencesDataSource.setPlayingProgress(progress)

}