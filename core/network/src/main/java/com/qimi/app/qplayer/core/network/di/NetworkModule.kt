package com.qimi.app.qplayer.core.network.di

import com.qimi.app.qplayer.core.network.NetworkDataSource
import com.qimi.app.qplayer.core.network.retrofit.RetrofitNetworkDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface NetworkModule {

    @Binds
    fun bindsRetrofitNetworkDataSource(
        impl: RetrofitNetworkDataSource
    ): NetworkDataSource

}