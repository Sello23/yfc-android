package com.yourfitness.coach.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yourfitness.coach.domain.fitness_info.FitnessInfoService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncAchievementsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val service: FitnessInfoService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            service.syncSpikeAchievements()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}