package com.yourfitness.coach.domain.progress.workout

import com.yourfitness.coach.data.dao.WorkoutDao
import com.yourfitness.coach.data.entity.WorkoutEntity
import com.yourfitness.coach.data.mapper.toEntity
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.ManualWorkoutBody
import com.yourfitness.coach.network.dto.Workout
import javax.inject.Inject

class WorkoutManager @Inject constructor(
    private val restApi: YFCRestApi,
    private val workoutDao: WorkoutDao
) {

    suspend fun fetchWorkouts(): List<WorkoutEntity> {
        val workouts = restApi.getWorkouts().map { it.toEntity() }
        workoutDao.updateWorkouts(workouts)
        return workouts
    }

    suspend fun getSavedWorkouts(): List<WorkoutEntity> {
        return workoutDao.readAllData()
    }

    suspend fun saveWorkouts(data: List<Workout>) {
        val workouts = data.map { it.toEntity() }
        workoutDao.updateWorkouts(workouts)
    }

    suspend fun uploadNewWorkouts(trackedAtList: List<String>): List<Workout> {
        val body = ManualWorkoutBody(trackedAtList)
        return restApi.saveManualWorkouts(body)
    }

    suspend fun uploadDeletedWorkouts(ids: String) {
        restApi.deleteManualWorkouts(ids)
    }

    suspend fun deleteByIds(ids: List<String>) {
        workoutDao.deleteByIds(ids)
    }
}
