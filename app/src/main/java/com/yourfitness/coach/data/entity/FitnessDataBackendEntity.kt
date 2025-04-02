package com.yourfitness.coach.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.coach.data.entity.FitnessDataBackendEntity.Companion.BACKEND_FIT_DATA_TABLE

@Entity(tableName = BACKEND_FIT_DATA_TABLE)
data class FitnessDataBackendEntity(
    @PrimaryKey
    val entryDate: Long,
    val steps: Double,
    val calories: Double,
    val points: Double,
    val durationSec: Long,
    val distanceMeter: Double,
    val isSynced: Int = 1
) {
    companion object {
        const val BACKEND_FIT_DATA_TABLE = "backend_fit_data"
        val empty get() = FitnessDataBackendEntity(0L, 0.0, 0.0, 0.0, 0L, 0.0)
    }
}
