package com.qimi.app.qplayer.core.network.model

import com.google.gson.annotations.SerializedName

data class NetworkMovie(
    @SerializedName("vod_id")
    val id: Int,
    @SerializedName("vod_name")
    val name: String
)
