package com.qimi.app.qplayer.core.model.data

data class MovieList(
    val code: Int,
    val page: Int,
    val pageCount: Int,
    val limit: Int,
    val total: Int,
    val list: List<Movie>
)
