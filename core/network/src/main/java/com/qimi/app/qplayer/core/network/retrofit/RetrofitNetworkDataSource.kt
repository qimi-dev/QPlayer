package com.qimi.app.qplayer.core.network.retrofit

import com.qimi.app.qplayer.core.network.NetworkDataSource
import com.qimi.app.qplayer.core.network.model.NetworkMovieList
import com.qimi.app.qplayer.core.network.retrofit.adapters.result.ResultCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import javax.inject.Inject
import javax.inject.Singleton

private interface NetworkApi {

    @GET("api.php/provide/vod")
    suspend fun fetchMovieList(
        @Query("ac") action: String? = "detail",
        @Query("wd") keyword: String = "",
        @Query("pg") pageIndex: Int = 0,
        @Query("t") type: Int? = null
    ): Result<NetworkMovieList>

}

@Singleton
internal class RetrofitNetworkDataSource @Inject constructor() : NetworkDataSource {

    private val networkApi: NetworkApi = Retrofit.Builder()
        .baseUrl("https://huawei8.live/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(ResultCallAdapterFactory.create())
        .build()
        .create(NetworkApi::class.java)

    override suspend fun fetchMovieList(
        keyword: String,
        pageIndex: Int,
        type: Int
    ): Result<NetworkMovieList> =
        networkApi.fetchMovieList(keyword = keyword, pageIndex = pageIndex, type = type)

}