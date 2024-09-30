package com.qimi.app.qplayer.core.data.repository

import com.qimi.app.qplayer.core.model.data.MovieList
import com.qimi.app.qplayer.core.network.model.NetworkMovieList

interface MoviesRepository {

    suspend fun fetchMovieList(
        keyword: String = "",
        pageIndex: Int = 1,
        type: Int = 0
    ): Result<MovieList>

}