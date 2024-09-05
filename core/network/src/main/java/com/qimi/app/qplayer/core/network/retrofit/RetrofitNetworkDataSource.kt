package com.qimi.app.qplayer.core.network.retrofit

import com.qimi.app.qplayer.core.network.NetworkDataSource
import com.qimi.app.qplayer.core.network.model.NetworkMovieList
import com.qimi.app.qplayer.core.network.retrofit.adapters.result.ResultCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitNetworkDataSource : NetworkDataSource {

    private val networkApi: MovieSourceNetworkApi = Retrofit.Builder()
        .baseUrl("https://api.wujinapi.me/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(ResultCallAdapterFactory.create())
        .build()
        .create(MovieSourceNetworkApi::class.java)

    override suspend fun fetchMovieList(): Result<NetworkMovieList> =
        networkApi.fetchMovieList()

}