package com.qimi.app.qplayer.core.data.repository

import com.qimi.app.qplayer.core.data.Syncable
import com.qimi.app.qplayer.core.model.data.Movie
import com.qimi.app.qplayer.core.model.data.MovieList
import kotlinx.coroutines.flow.Flow

interface MoviesRepository : Syncable {

    suspend fun fetchMovieList(
        keyword: String = "",
        pageIndex: Int = 1,
        type: Int = 0
    ): Result<MovieList>

    fun getCachedCommonMovies(): Flow<List<Movie>>

}