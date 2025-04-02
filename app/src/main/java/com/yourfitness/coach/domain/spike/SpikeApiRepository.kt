package com.yourfitness.coach.domain.spike

import com.yourfitness.coach.domain.date.dateTimeToSec
import com.yourfitness.coach.domain.date.dateToSec
import com.yourfitness.coach.domain.date.toDateSimple
import com.yourfitness.coach.domain.date.toMilliseconds
import com.yourfitness.coach.domain.models.ProviderFitData
import com.yourfitness.coach.network.spike.SpikeRestApi
import com.yourfitness.coach.network.spike.dto.DeleteIntegrationData
import com.yourfitness.coach.ui.features.profile.connected_devices.ProviderType
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.date.toSpikeString
import timber.log.Timber
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

const val deepLinkHost = "yourfitness.coach"
const val deppLinkUserId = "user_id"

class SpikeApiRepository @Inject constructor(
    private val spikeApi: SpikeRestApi,
    private val commonStorage: CommonPreferencesStorage,
): FitnessRepository() {

    override suspend fun disconnect(): Boolean {
        return try {
            val id = commonStorage.spikeUserId
            val allProviders = ProviderType.entries.map { it.value }

            if (id.isNotBlank()) {
                val body = DeleteIntegrationData(allProviders)
                val response = spikeApi.deleteIntegration(commonStorage.spikeUserId, body)
                if ((response.count ?: 0) < 1) return false
                commonStorage.spikeUserId = ""
            }
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }

    override suspend fun fetchActivities(
        startDate: OffsetDateTime,
        endDate: OffsetDateTime,
        offset: ZoneOffset,
        dataMap: MutableMap<Long, ProviderFitData>
    ) = try {
        val activitiesData = spikeApi.getActivitiesSummary(
            commonStorage.spikeUserId,
            startDate.toSpikeString(),
            endDate.toSpikeString(),
            offset.totalSeconds
        )
        activitiesData.data?.forEach { item ->
            try {
                val date = item.date?.dateToSec(offset).toMilliseconds()

                date?.let {
                    val mapItem = dataMap[date]
                    val steps = item.steps?.toDouble() ?: 0.0
                    val manualSteps = 0.0
                    val calories = item.caloriesActive?.toDouble() ?: 0.0
                    val duration = item.dailyMovement ?: 0L
                    val distance = item.distance?.toDouble() ?: 0.0
                    if (mapItem == null) {
                        dataMap[date] = ProviderFitData(
                            steps = steps,
                            calories = calories,
                            distance = distance,
                            duration = duration,
                            date = date,
                            healthServiceSteps = steps + manualSteps,
                            manualSteps = manualSteps
                        )
                    } else {
                        dataMap[date] = mapItem.copy(
                            duration = if (mapItem.duration == 0L) duration else mapItem.duration,
                            distance = distance,
                            steps = if (mapItem.steps == 0.0) steps else mapItem.steps,
                            calories = calories,
                            healthServiceSteps = if (mapItem.healthServiceSteps == 0.0) steps + manualSteps else mapItem.healthServiceSteps,
                            manualSteps = if (mapItem.manualSteps == 0.0) manualSteps else mapItem.manualSteps
                        )
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
    ) = try {
        val stepsData = spikeApi.getSteps(
            commonStorage.spikeUserId,
            startDate.toSpikeString(),
            endDate.toSpikeString(),
            offset.totalSeconds
        )

        stepsData.data?.forEach { item ->
            try {
                val date = item.date?.toDateSimple(offset)

                date?.let {
                    var steps = 0.0//item.value ?: 0L
                    var manualSteps = 0.0
                    var duration = 0L

                    item.intradayData?.forEach { entry ->
                        val isManual = entry.metadata?.get("recording_method")?.equals("manual") ?: false

                        if (isManual) {
                            manualSteps += (entry.value ?: 0)
                        } else {
                            steps += (entry.value ?: 0).toDouble()

                            val start = entry.timeStart?.dateTimeToSec()
                            val end = entry.timeEnd?.dateTimeToSec()
                            duration += if (start != null && end != null) {
                                end - start
                            } else {
                                0L
                            }
                        }
                    }

                    val mapItem = dataMap[date]
                    if (mapItem == null) {
                        dataMap[date] =
                            ProviderFitData(
                                steps = steps,
                                duration = duration,
                                date = date,
                                healthServiceSteps = steps + manualSteps,
                                manualSteps = manualSteps
                            )
                    } else {
                        dataMap[date] = mapItem.copy(
                            steps = steps,
                            duration = duration,
                            healthServiceSteps = steps + manualSteps,
                            manualSteps = manualSteps
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
    }


    suspend fun checkIfUserAuthorized(userId: String): Boolean {
        if (userId.isBlank()) return false

        try {
            spikeApi.getUser(userId)
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
