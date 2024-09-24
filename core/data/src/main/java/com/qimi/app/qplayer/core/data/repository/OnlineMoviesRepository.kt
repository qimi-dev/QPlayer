package com.qimi.app.qplayer.core.data.repository

import com.qimi.app.qplayer.core.model.data.MovieList
import com.qimi.app.qplayer.core.network.NetworkDataSource
import com.qimi.app.qplayer.core.network.model.asExternalModel
import javax.inject.Inject

internal class OnlineMoviesRepository @Inject constructor(
    private val networkSource: NetworkDataSource
): MoviesRepository {

    override suspend fun fetchMovieList(action: String): Result<MovieList> {
        return networkSource.fetchMovieList(action).map { it.asExternalModel() }
    }

}