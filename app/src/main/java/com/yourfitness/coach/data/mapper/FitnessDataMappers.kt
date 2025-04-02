package com.yourfitness.coach.data.mapper

import com.yourfitness.coach.data.entity.FitnessDataBackendEntity
import com.yourfitness.coach.network.dto.GetAchievements
import com.yourfitness.common.domain.date.milliseconds
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.domain.date.utcToZone
import java.time.ZoneOffset

fun GetAchievements.toEntity(offset: ZoneOffset): FitnessDataBackendEntity {
    return FitnessDataBackendEntity(
        steps = steps?.toDouble() ?: 0.0,
        calories = calories?.toDouble() ?: 0.0,
        entryDate = trackedAt?.toMilliseconds()?.utcToZone(offset)?.milliseconds ?: 0L,
        points = score ?: 0.0,
        durationSec = duration ?: 0L,
        distanceMeter = distance ?: 0.0,
    )
}
