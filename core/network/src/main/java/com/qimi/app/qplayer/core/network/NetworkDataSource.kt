package com.qimi.app.qplayer.core.network

import com.qimi.app.qplayer.core.network.model.NetworkMovieList

interface NetworkDataSource {

    suspend fun fetchMovieList(keyword: String? = null): Result<NetworkMovieList>

}