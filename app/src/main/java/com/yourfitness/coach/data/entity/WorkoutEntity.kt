package com.yourfitness.coach.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.coach.data.entity.WorkoutEntity.Companion.WORKOUT_TABLE

@Entity(tableName = WORKOUT_TABLE)
data class WorkoutEntity(
    @PrimaryKey
    val id: String,
    val createdAt: Long,
    val trackedAt: Long,
    val userId: String?,
    val manual: Boolean
) {
    companion object {
        const val WORKOUT_TABLE = "workout_data"
    }
}
