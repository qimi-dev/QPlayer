package com.qimi.app.qplayer.core.database.di

import com.qimi.app.qplayer.core.database.QPlayerDatabase
import com.qimi.app.qplayer.core.database.dao.CachedMovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {

    @Provides
    fun providesCachedMovieDao(
        database: QPlayerDatabase
    ): CachedMovieDao = database.cachedMovieDao()

}

