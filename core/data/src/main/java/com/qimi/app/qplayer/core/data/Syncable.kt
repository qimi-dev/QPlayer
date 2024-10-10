package com.qimi.app.qplayer.core.data

interface Syncable {

    suspend fun sync(): Boolean

}