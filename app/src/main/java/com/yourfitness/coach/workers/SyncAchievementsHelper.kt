package com.yourfitness.coach.workers

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.map

suspend fun Context.syncAchievements(onDone: suspend () -> Unit) {
    val workManager = WorkManager.getInstance(this)

    val dataSyncWorkRequest = OneTimeWorkRequestBuilder<SyncAchievementsWorker>()
        .build()

    workManager.enqueueUniqueWork(
        "syncSpikeAchievementsWork",
        ExistingWorkPolicy.KEEP,
        dataSyncWorkRequest
    )

    workManager.getWorkInfoByIdFlow(dataSyncWorkRequest.id).map { workInfos -> workInfos.state }
        .collect { workState ->
            when (workState) {
                WorkInfo.State.SUCCEEDED, WorkInfo.State.FAILED,
                WorkInfo.State.CANCELLED, WorkInfo.State.BLOCKED -> {
                    onDone()
                }

                else -> {}
            }
        }
}