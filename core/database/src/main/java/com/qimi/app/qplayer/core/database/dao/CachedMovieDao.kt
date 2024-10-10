package com.qimi.app.qplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.qimi.app.qplayer.core.database.model.CachedMovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedMovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedMovies(movies: List<CachedMovieEntity>)

    @Query("SELECT * FROM cached_movie WHERE type = :type")
    fun getCachedMovies(type: Int): Flow<List<CachedMovieEntity>>

    @Query("DELETE FROM cached_movie WHERE type = :type")
    suspend fun deleteCachedMoviesByType(type: Int)

}