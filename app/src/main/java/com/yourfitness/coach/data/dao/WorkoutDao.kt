package com.yourfitness.coach.data.dao

import androidx.room.*
import com.yourfitness.coach.data.entity.WorkoutEntity
import com.yourfitness.coach.data.entity.WorkoutEntity.Companion.WORKOUT_TABLE

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWorkouts(workouts: List<WorkoutEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun writeWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM $WORKOUT_TABLE")
    suspend fun readAllData(): List<WorkoutEntity>

    @Query("DELETE FROM $WORKOUT_TABLE WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}
