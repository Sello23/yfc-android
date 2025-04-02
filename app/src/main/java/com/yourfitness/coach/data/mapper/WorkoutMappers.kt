package com.yourfitness.coach.data.mapper

import com.yourfitness.coach.data.entity.WorkoutEntity
import com.yourfitness.coach.domain.date.toMilliseconds
import com.yourfitness.coach.network.dto.Workout
import com.yourfitness.common.domain.date.today
import java.util.UUID

fun Workout.toEntity(): WorkoutEntity {
    return WorkoutEntity(
        id = id ?: UUID.randomUUID().toString(),
        createdAt = createdAt.toMilliseconds() ?: today().time,
        trackedAt = trackedAt.toMilliseconds() ?: today().time,
        userId = userId ?: UUID.randomUUID().toString(),
        manual = manual
    )
}
