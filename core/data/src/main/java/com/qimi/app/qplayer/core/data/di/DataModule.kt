package com.qimi.app.qplayer.core.data.di

import com.qimi.app.qplayer.core.data.repository.MoviesRepository
import com.qimi.app.qplayer.core.data.repository.OfflineUserDataRepository
import com.qimi.app.qplayer.core.data.repository.OnlineMoviesRepository
import com.qimi.app.qplayer.core.data.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindsMoviesRepository(
        moviesRepository: OnlineMoviesRepository
    ): MoviesRepository

    @Binds
    internal abstract fun bindsUserDataRepository(
        userDataRepository: OfflineUserDataRepository
    ): UserDataRepository

}