package com.qimi.app.qplayer.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qimi.app.qplayer.core.model.data.Movie

@Entity(tableName = "cached_movie")
data class CachedMovieEntity(
    @PrimaryKey(autoGenerate = true)
    val cacheId: Int = 0,
    @ColumnInfo(name = "movie_id")
    val movieId: Int,
    @ColumnInfo(name = "type")
    val type: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "image")
    val image: String,
    @ColumnInfo(name = "movie_class")
    val movieClass: String,
    @ColumnInfo(name = "remark")
    val remark: String,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "urls")
    val urls: String,
    @ColumnInfo(name = "score")
    val score: String
)

fun CachedMovieEntity.asExternalModel() = Movie(
    id = movieId,
    name = name,
    image = image,
    movieClass = movieClass,
    remark = remark,
    content = content,
    urls = urls,
    score = score
)