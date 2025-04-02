package com.yourfitness.coach.domain.models

import com.yourfitness.coach.data.entity.FitnessDataEntity
import com.yourfitness.coach.network.dto.PutAchievements
import com.yourfitness.common.domain.date.toTimeZone
import java.time.ZoneOffset

data class ProviderFitData(
    val steps: Double = 0.0,
    val calories: Double = 0.0,
    val date: Long = 0L,
    val duration: Long = 0L, //seconds
    val distance: Double = 0.0, //meters
    val healthServiceSteps: Double = 0.0,
    val manualSteps: Double = 0.0,
)


fun FitnessDataEntity.toPutAchievementsModel(offset: ZoneOffset): PutAchievements {
    return PutAchievements(
        steps = this.steps.toInt(),
        calories = this.calories.toInt(),
        date = entryDate.toTimeZone(offset),
        duration = durationSec,
        healthServiceSteps = this.allStepsSum.toLong(),
        manualSteps = this.manualSteps.toLong()
    )
}
