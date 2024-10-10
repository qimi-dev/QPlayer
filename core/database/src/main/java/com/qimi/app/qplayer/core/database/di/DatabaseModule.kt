package com.qimi.app.qplayer.core.database.di

import android.content.Context
import androidx.room.Room
import com.qimi.app.qplayer.core.database.QPlayerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun providesQPlayerDatabase(
        @ApplicationContext context: Context
    ): QPlayerDatabase = Room.databaseBuilder(
        context,
        QPlayerDatabase::class.java,
        "qplayer_database"
    ).build()

}


