package com.qimi.app.qplayer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.qimi.app.qplayer.core.database.dao.CachedMovieDao
import com.qimi.app.qplayer.core.database.model.CachedMovieEntity

@Database(entities = [CachedMovieEntity::class], version = 1)
internal abstract class QPlayerDatabase : RoomDatabase() {

    abstract fun cachedMovieDao(): CachedMovieDao

}