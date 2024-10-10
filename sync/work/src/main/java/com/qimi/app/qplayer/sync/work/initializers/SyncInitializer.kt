package com.qimi.app.qplayer.sync.work.initializers

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.impl.WorkManagerImpl
import com.qimi.app.qplayer.sync.work.workers.SyncWorker

object SyncInitializer {

    const val SYNC_WORK_NAME = "SyncWorkName"

    fun initialize(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            SyncWorker.createRequest()
        )
    }

}

