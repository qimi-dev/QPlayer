package com.qimi.app.qplayer.core.data.repository

import com.qimi.app.qplayer.core.database.dao.CachedMovieDao
import com.qimi.app.qplayer.core.database.model.CachedMovieEntity
import com.qimi.app.qplayer.core.database.model.asExternalModel
import com.qimi.app.qplayer.core.model.data.Movie
import com.qimi.app.qplayer.core.model.data.MovieList
import com.qimi.app.qplayer.core.network.NetworkDataSource
import com.qimi.app.qplayer.core.network.model.asExternalModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class OfflineFirstMoviesRepository @Inject constructor(
    private val networkSource: NetworkDataSource,
    private val cachedMovieDao: CachedMovieDao
): MoviesRepository {

    override suspend fun fetchMovieList(keyword: String, pageIndex: Int, type: Int): Result<MovieList> {
        return networkSource.fetchMovieList(keyword, pageIndex, type).map { it.asExternalModel() }
    }

    override fun getCachedCommonMovies(): Flow<List<Movie>> {
        return cachedMovieDao.getCachedMovies(0)
            .map { it.map(CachedMovieEntity::asExternalModel) }
    }

    override suspend fun sync(): Boolean {
        return fetchAndSaveMovies(20)
                && fetchAndSaveMovies(82)
                && fetchAndSaveMovies(0)
    }

    private suspend fun fetchAndSaveMovies(type: Int): Boolean {
        return fetchMovieList(type = type).onSuccess { it ->
            cachedMovieDao.deleteCachedMoviesByType(type)
            cachedMovieDao.insertCachedMovies(
                it.list.map {
                    CachedMovieEntity(
                        movieId = it.id,
                        type = type,
                        name = it.name,
                        image = it.image,
                        movieClass = it.movieClass,
                        remark = it.remark,
                        content = it.content,
                        urls = it.urls,
                        score = it.score
                    )
                }
            )
        }.isSuccess
    }

}