package com.yourfitness.coach.data.mapper

import com.yourfitness.coach.data.entity.ScheduleEntity
import com.yourfitness.coach.network.dto.Schedule

fun Schedule.toEntity(): ScheduleEntity {
    return ScheduleEntity(
        createdAt = createdAt ?: 0,
        bookedPlaces = bookedPlaces ?: 0,
        customClassId = customClassId ?: "",
        from = from ?: 0,
        id = id ?: "",
        instructorId = instructorId ?: "",
        licensedClassId = licensedClassId ?: "",
        to = to ?: 0,
        updatedAt = updatedAt ?: 0,
    )
}