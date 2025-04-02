package com.yourfitness.coach.domain.spike

import com.yourfitness.coach.domain.models.ProviderFitData
import java.time.OffsetDateTime
import java.time.ZoneOffset

abstract class FitnessRepository {

    abstract suspend fun disconnect(): Boolean

    abstract suspend fun fetchActivities(
        startDate: OffsetDateTime,
        endDate: OffsetDateTime,
        offset: ZoneOffset,
        dataMap: MutableMap<Long, ProviderFitData>
    ): Unit?

    abstract suspend fun fetchSteps(
        startDate: OffsetDateTime,
        endDate: OffsetDateTime,
        offset: ZoneOffset,
        dataMap: MutableMap<Long, ProviderFitData>
    ): Unit?
}
