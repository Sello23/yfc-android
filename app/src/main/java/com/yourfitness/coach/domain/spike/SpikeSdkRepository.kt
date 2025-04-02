package com.yourfitness.coach.domain.spike

import android.content.Context
import com.spikeapi.SpikeConnection
import com.spikeapi.SpikeDataTypes
import com.spikeapi.SpikeEnvironment
import com.spikeapi.healthconnect.HealthConnectAvailability
import com.spikeapi.model.SpikeActivitiesSummaryData
import com.spikeapi.model.SpikeData
import com.spikeapi.model.SpikeDataType
import com.spikeapi.model.SpikeStepsIntradayData
import com.yourfitness.coach.BuildConfig.SPIKE_AUTH_TOKEN
import com.yourfitness.coach.BuildConfig.SPIKE_CALLBACK_URL
import com.yourfitness.coach.BuildConfig.SPIKE_CLIENT_ID
import com.yourfitness.coach.domain.date.dateTimeToSec
import com.yourfitness.coach.domain.date.dateToSec
import com.yourfitness.coach.domain.date.toMilliseconds
import com.yourfitness.coach.domain.helpers.MutexHelper
import com.yourfitness.coach.domain.models.ProviderFitData
import com.yourfitness.common.CommonPreferencesStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

class SpikeSdkRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val commonStorage: CommonPreferencesStorage,
    private val mutexHelper: MutexHelper,
) : FitnessRepository() {
    private var spikeConnection: SpikeConnection? = null

    suspend fun openHealthConnectConnection(profileId: String) = mutexHelper.openConnectionMutex.withLock {
            try {
                if (spikeConnection != null) return@withLock
                spikeConnection = SpikeConnection.createConnection(
                    context = context,
                    appId = SPIKE_CLIENT_ID,
                    authToken = SPIKE_AUTH_TOKEN,
                    customerEndUserId = profileId,
                    callbackUrl = SPIKE_CALLBACK_URL,
                    env = SpikeEnvironment.PROD,
                    logger = Logger()
                )
                commonStorage.spikeUserId = spikeConnection?.getSpikeUserId() ?: ""
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

    fun isHealthConnectInstalled(): Boolean {
        try {
            val availability = spikeConnection?.getHealthConnectAvailability() ?: return false

            return availability == HealthConnectAvailability.INSTALLED
        } catch (e: Exception) {
            Timber.e(e)
            return false
        }
    }

    suspend fun notGrantedPermissions(permissions: Set<String>): Set<String> {
        try {
            val notGrantedPermissions = mutableSetOf<String>()

            permissions.forEach {
                val granted = spikeConnection?.hasHealthPermissionsGranted(setOf(it)) ?: false
                if (!granted) notGrantedPermissions.add(it)
            }

            return notGrantedPermissions
        } catch (e: Exception) {
            Timber.e(e)
            return permissions
        }
    }

    fun getRequiredPermissions(): Set<String> {
        val stepsPermissions =
            spikeConnection?.getRequiredHealthPermissionsMetadata(SpikeDataTypes.STEPS_INTRADAY)
                .orEmpty()
        val caloriesPermissions =
            spikeConnection?.getRequiredHealthPermissionsMetadata(SpikeDataTypes.CALORIES).orEmpty()
        val distancePermissions =
            spikeConnection?.getRequiredHealthPermissionsMetadata(SpikeDataTypes.DISTANCE).orEmpty()
        val permissions = stepsPermissions union caloriesPermissions union distancePermissions union listOf("android.permission.health.READ_BASAL_METABOLIC_RATE")
        return permissions
    }

    override suspend fun fetchSteps(
        startDate: OffsetDateTime,
        endDate: OffsetDateTime,
        offset: ZoneOffset,
        dataMap: MutableMap<Long, ProviderFitData>
    ) = try {
        getDataForPeriod(
            SpikeDataTypes.STEPS_INTRADAY,
            startDate,
            endDate
        ) { extractedData ->
            if (extractedData !is SpikeStepsIntradayData) return@getDataForPeriod

            extractedData.entries.forEach { item ->
                try {

                    val date = item.date?.dateToSec(offset).toMilliseconds()

                    date?.let {
                        var steps = 0.0//item.value?.toDouble() ?: 0.0
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
                            dataMap[date] = ProviderFitData(
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
        }
    } catch (e: Exception) {
        Timber.e(e)
    }

    override suspend fun fetchActivities(
        startDate: OffsetDateTime,
        endDate: OffsetDateTime,
        offset: ZoneOffset,
        dataMap: MutableMap<Long, ProviderFitData>
    ) = try {
        getDataForPeriod(
            SpikeDataTypes.ACTIVITIES_SUMMARY,
            startDate,
            endDate
        ) { extractedData ->
            if (extractedData !is SpikeActivitiesSummaryData) return@getDataForPeriod

            extractedData.entries.forEach {
                try {
                    val date = it.date?.dateToSec(offset).toMilliseconds()

                    if (date != null) {
                        val calories = it.caloriesActive ?: 0
                        val distance = it.distance ?: 0.0

                        val mapItem = dataMap[date]
                        if (mapItem == null) {
                            dataMap[date] = ProviderFitData(
                                calories = calories.toDouble(),
                                distance = distance,
                                date = date
                            )
                        } else {
                            dataMap[date] = mapItem.copy(
                                calories = calories.toDouble(),
                                distance = distance,
                            )
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
    }

    private suspend fun <T : SpikeData> getDataForPeriod(
        dataType: SpikeDataType<T>,
        from: OffsetDateTime,
        to: OffsetDateTime,
        extractData: (data: T?) -> Unit
    ) {
        try {
            val extractedData = spikeConnection?.extractData(dataType, from, to)

            extractData(extractedData)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override suspend fun disconnect(): Boolean = mutexHelper.closeConnectionMutex.withLock {
        spikeConnection?.close()
        spikeConnection = null
        true
    }
}
