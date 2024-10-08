package com.qimi.app.qplayer.core.data.repository

import com.qimi.app.qplayer.core.model.data.PlayingSettings
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {

    val playingSettings: Flow<PlayingSettings>

    suspend fun setPlayingProgress(progress: Float)

}