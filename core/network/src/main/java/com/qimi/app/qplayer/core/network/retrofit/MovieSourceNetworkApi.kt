package com.qimi.app.qplayer.core.network.retrofit

import com.qimi.app.qplayer.core.network.model.NetworkMovieList
import retrofit2.http.GET

internal interface MovieSourceNetworkApi {

    @GET("api.php/provide/vod")
    suspend fun fetchMovieList(): Result<NetworkMovieList>

}