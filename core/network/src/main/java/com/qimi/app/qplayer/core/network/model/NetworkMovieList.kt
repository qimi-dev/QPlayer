package com.qimi.app.qplayer.core.network.model

import com.google.gson.annotations.SerializedName


data class NetworkMovieList(
    @SerializedName("code")
    val code: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("pagecount")
    val pageCount: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("list")
    val list: List<NetworkMovie>
)

