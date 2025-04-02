package com.yourfitness.coach.domain.spike

import com.yourfitness.coach.PreferencesStorage
import com.yourfitness.coach.domain.date.dateToSec
import com.yourfitness.coach.domain.date.toMilliseconds
import com.yourfitness.coach.domain.models.ProviderFitData
import com.yourfitness.coach.network.firebase.FirebaseRestApi
import com.yourfitness.coach.ui.features.profile.connected_devices.ProviderType
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.date.toSpikeString
import timber.log.Timber
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.math.max

class SpikeFirebaseRepository @Inject constructor(
    private val firebaseApi: FirebaseRestApi,
    private val commonStorage: CommonPreferencesStorage,
    private val storage: PreferencesStorage,
): FitnessRepository() {

    override suspend fun disconnect(): Boolean = false

    override suspend fun fetchActivities(
        startDate: OffsetDateTime,
        endDate: OffsetDateTime,
        offset: ZoneOffset,
        dataMap: MutableMap<Long, ProviderFitData>
    ) = try {
        val provider = storage.fitDataProvider
        val activitiesData = firebaseApi.getHealthData(
            commonStorage.spikeUserId,
            provider?.value ?: "",
            "activities",
            startDate.toSpikeString(),
            endDate.toSpikeString(),
        )

        activitiesData.data?.summary?.forEach {
            try {
                it.activity?.let { item ->
                    val date = item.date?.dateToSec(offset).toMilliseconds()

                    date?.let {
                        val mapItem = dataMap[date]
                        val steps = item.steps?.toDouble() ?: 0.0
                        val calories = (item.caloriesActive ?: 0.0).toDouble()
                        val distance = item.distance?.toDouble() ?: 0.0
                        if (mapItem == null) {
                            dataMap[date] = ProviderFitData(
                                steps = steps,
                                calories = calories,
                                distance = distance,
                                date = date,
                                healthServiceSteps = steps,
                            )
                        } else {
                            dataMap[date] = mapItem.copy(
                                distance = distance,
                                steps = if (mapItem.steps == 0.0) steps else mapItem.steps,
                                calories = calories,
                                healthServiceSteps = if (mapItem.steps == 0.0) steps else mapItem.steps,
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        val needDistance = provider?.needStreamDistance() ?: false
        val needSteps = provider?.needSteps() ?: false
        activitiesData.data?.stream?.forEach {
            try {
                it.activity?.let { item ->
                    val date = item.date?.dateToSec(offset).toMilliseconds()

                    date?.let {
                        val mapItem = dataMap[date]
                        val steps = item.steps?.toDouble() ?: 0.0
                        val calories = item.calories ?: 0L
                        val duration = item.movingTime ?: 0L
                        val distance = item.distance?.toDouble() ?: 0.0

                        if (item.manual) {
                            if (mapItem != null) {
                                dataMap[date] = mapItem.copy(
                                    distance = max(mapItem.distance - distance, 0.0),
                                    steps = max(mapItem.steps - steps, 0.0),
                                    calories = max(mapItem.calories - calories, 0.0),
                                    healthServiceSteps = mapItem.steps + steps,
                                    manualSteps = steps
                                )
                            }
                        } else {
                            if (mapItem == null) {
                                dataMap[date] = ProviderFitData(
                                    steps = steps,
                                    calories = calories.toDouble(),
                                    distance = distance,
                                    duration = duration,
                                    date = date,
                                    healthServiceSteps = steps
                                )
                            } else {
                                dataMap[date] = mapItem.copy(
                                    duration = mapItem.duration + duration,
                                    distance = if (needDistance) mapItem.distance + distance else mapItem.distance,
                                    steps = if (needSteps) mapItem.steps + steps else mapItem.steps,
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
    }

    override suspend fun fetchSteps(
        startDate: OffsetDateTime,
        endDate: OffsetDateTime,
        offset: ZoneOffset,
        dataMap: MutableMap<Long, ProviderFitData>
    ) {}

    private fun ProviderType.needStreamDistance(): Boolean {
        return this == ProviderType.POLAR
    }

    private fun ProviderType.needSteps(): Boolean {
        return this == ProviderType.SUUNTO
    }

    private fun ProviderType.needStreamMovingTime(): Boolean {
        return this == ProviderType.POLAR || this == ProviderType.SUUNTO
    }
}
