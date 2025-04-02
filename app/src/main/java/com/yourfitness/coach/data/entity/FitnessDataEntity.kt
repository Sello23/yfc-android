package com.yourfitness.coach.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.coach.data.entity.FitnessDataEntity.Companion.FITNESS_DATA_TABLE

@Entity(tableName = FITNESS_DATA_TABLE)
data class FitnessDataEntity(
    @PrimaryKey
    val entryDate: Long,
    val steps: Double,
    val calories: Double,
    val points: Double,
    val durationSec: Long,
    val distanceMeters: Double,
    @ColumnInfo(name = "allStepsSum", defaultValue = "0") val allStepsSum: Double,
    @ColumnInfo(name = "manualSteps", defaultValue = "0") val manualSteps: Double,
    val syncDate: Long? = null
) {
    companion object {
        const val FITNESS_DATA_TABLE = "fitness_data"
        val empty get() = FitnessDataEntity(0L, 0.0, 0.0, 0.0, 0L, 0.0, 0.0,  0.0, 0L)
    }
}
