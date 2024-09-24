package com.qimi.app.qplayer.core.network.model

import com.google.gson.annotations.SerializedName
import com.qimi.app.qplayer.core.model.data.Movie

data class NetworkMovie(
    @SerializedName("vod_id")
    val id: Int,
    @SerializedName("vod_name")
    val name: String,
    @SerializedName("vod_pic")
    val image: String,
    @SerializedName("vod_class")
    val movieClass: String,
    @SerializedName("vod_remarks")
    val remark: String,
    @SerializedName("vod_content")
    val content: String,
    @SerializedName("vod_play_url")
    val urls: String
)

fun NetworkMovie.asExternalModel() = Movie(
    id = id,
    name = name,
    image = image,
    movieClass = movieClass,
    remark = remark,
    content = content,
    urls = urls,
)