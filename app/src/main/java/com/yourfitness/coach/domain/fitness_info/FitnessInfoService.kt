package com.yourfitness.coach.domain.fitness_info

import com.google.gson.Gson
import com.yourfitness.coach.PreferencesStorage
import com.yourfitness.coach.data.entity.FitnessDataBackendEntity
import com.yourfitness.coach.data.entity.FitnessDataEntity
import com.yourfitness.coach.data.mapper.toEntity
import com.yourfitness.coach.domain.date.minuteToSeconds
import com.yourfitness.coach.domain.date.toSeconds
import com.yourfitness.coach.domain.date.today
import com.yourfitness.coach.domain.fb_remote_config.FirebaseRemoteConfigRepository
import com.yourfitness.coach.domain.helpers.MutexHelper
import com.yourfitness.coach.domain.models.ProviderFitData
import com.yourfitness.coach.domain.models.toPutAchievementsModel
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.domain.spike.FitnessRepository
import com.yourfitness.coach.domain.spike.SpikeApiRepository
import com.yourfitness.coach.domain.spike.SpikeFirebaseRepository
import com.yourfitness.coach.domain.spike.SpikeSdkRepository
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.ui.features.profile.connected_devices.ProviderType
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.atDayEnd
import com.yourfitness.common.domain.date.atDayStart
import com.yourfitness.common.domain.date.milliseconds
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.toISOString
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.domain.date.utcToZone
import com.yourfitness.common.domain.settings.RegionSettingsManager
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.round

@Singleton
class FitnessInfoService @Inject constructor(
    private val fitnessRepository: ProviderFitnessDataRepository,
    private val fitnessBackendRepository: BackendFitnessDataRepository,
    private val spikeSdkRepository: SpikeSdkRepository,
    private val spikeApiRepository: SpikeApiRepository,
    private val spikeFirebaseRepository: SpikeFirebaseRepository,
    private val restApi: YFCRestApi,
    private val remoteConfig: FirebaseRemoteConfigRepository,
    private val profileRepository: ProfileRepository,
    private val settingsManager: SettingsManager,
    private val mutexHelper: MutexHelper,
    private val regionSettingsManager: RegionSettingsManager,
    private val preferencesStorage: PreferencesStorage,
    private val commonStorage: CommonPreferencesStorage,
) {

    private val repository: FitnessRepository
        get() = when (preferencesStorage.fitDataProvider) {
            ProviderType.HEALTH_CONNECT -> spikeSdkRepository
            ProviderType.GOOGLE_FIT -> spikeApiRepository
            else -> spikeFirebaseRepository
        }

    suspend fun checkPermissions(): Boolean {
        val provider = preferencesStorage.fitDataProvider
        if (provider == ProviderType.HEALTH_CONNECT) {
            spikeSdkRepository.openHealthConnectConnection(profileRepository.profileId())
        }

        return (provider == ProviderType.HEALTH_CONNECT && spikeSdkRepository.isHealthConnectInstalled() &&
                getHealthConnectPermissions().isEmpty()) ||
                (provider != null && provider != ProviderType.HEALTH_CONNECT &&
                        commonStorage.spikeUserId.isNotBlank() &&
                        spikeApiRepository.checkIfUserAuthorized(commonStorage.spikeUserId))
    }

    suspend fun getHealthConnectPermissions(): Set<String> {
        spikeSdkRepository.openHealthConnectConnection(profileRepository.profileId())

        val allPermissions = spikeSdkRepository.getRequiredPermissions()
        val permissions = spikeSdkRepository.notGrantedPermissions(allPermissions)

        return permissions
    }

    suspend fun isHealthConnectInstalled(): Boolean {
        spikeSdkRepository.disconnect()
        spikeSdkRepository.openHealthConnectConnection(profileRepository.profileId())
        return spikeSdkRepository.isHealthConnectInstalled()
    }

    suspend fun syncSpikeAchievements() = mutexHelper.syncAchievementsMutex.withLock {
        try {
            if (!checkPermissions()) return@withLock

            val offset = regionSettingsManager.getZoneOffset() ?: return

            val backendStartDateISO = getBackendFetchStartDate().toISOString(offset)
            val backendData = loadBackendData(backendStartDateISO, offset)

            val providerStartDate = getProviderFetchStartDate(offset)
            val providerData = loadProviderData(providerStartDate, offset)

            evaluateDataDiff(providerData, backendData, offset)

            if (!canUploadData()) return@withLock

            uploadFitnessData(offset)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    suspend fun closeSpikeConnections(dataType: ProviderType) {
        if (dataType == ProviderType.HEALTH_CONNECT) {
            spikeSdkRepository.disconnect()
        }
    }

    private suspend fun evaluateDataDiff(
        providerData: List<ProviderFitData>,
        backendData: List<FitnessDataBackendEntity>,
        offset: ZoneOffset
    ) {
        val dataDiff = evaluateDataDifference(providerData, backendData, offset)
        Timber.tag("issue_diff").e(dataDiff.joinToString {
            "diff evaluated :=  ${it.entryDate} = ${it.entryDate.toDate()},  steps= ${it.steps},   distance= ${it.distanceMeters},  calories= ${it.calories}\n"
        })
        dataDiff.forEach {
            val entryDateZoned = it.entryDate.utcToZone(offset).atDayStart().milliseconds
            fitnessBackendRepository.markAsNotSynced(entryDateZoned)
        }
        fitnessRepository.saveToDb(dataDiff)
        preferencesStorage.providerLastFetchDate = offset.today().toEpochSecond()
    }

    private suspend fun uploadFitnessData(offset: ZoneOffset): Boolean {
        val preparedData = fitnessRepository.getEntriesToUpload()
            .groupBy { it.entryDate.utcToZone(offset).milliseconds }
        if (preparedData.isEmpty()) return false

        val preUploadData = preparedData.entries.map { entry ->
            val sortedEntry = entry.value.sortedBy { it.entryDate }
            FitnessDataEntity(
                entry.value.maxOf { it.entryDate },
                entry.value.sumOf { it.steps },
                entry.value.sumOf { it.calories },
                entry.value.sumOf { it.points },
                entry.value.sumOf { it.durationSec },
                entry.value.sumOf { it.distanceMeters },
                sortedEntry.last().allStepsSum,
                sortedEntry.last().manualSteps
            )
        }
        val dataToUpload = preUploadData.map { it.toPutAchievementsModel(offset) }
        Timber.tag("issue_to_upload").e(dataToUpload.joinToString { obj ->
            "*** prepared for upload:  ${obj.date} steps= ${obj.steps}, cal= ${obj.calories},  dur= ${obj.duration}\n"
        })
        withContext(NonCancellable) {
            val uploadedDiffs = restApi.updateAchievements(dataToUpload)
                .map { it.toEntity(offset) }

            preparedData.entries.map { data ->
                fitnessRepository.saveToDb(data.value.map { it.copy(syncDate = today().time) })
            }

            uploadedDiffs.forEach {
                val entryDateZoned = it.entryDate.utcToZone(offset)
                val record = fitnessBackendRepository.getRecordForDay(entryDateZoned)
                val updatedRecord = record?.copy(
                    steps = record.steps + it.steps,
                    calories = record.calories + it.calories,
                    points = record.points + it.points,
                    durationSec = record.durationSec + it.durationSec,
                ) ?: FitnessDataBackendEntity(
                    entryDateZoned.milliseconds, it.steps, it.calories, it.points,
                    it.durationSec, it.distanceMeter
                )
                fitnessBackendRepository.saveToDb(updatedRecord)
            }
        }

        return true
    }

    private suspend fun canUploadData(): Boolean {
        val now = today().time
        val lastSyncDate = fitnessRepository.getLastSyncDate() ?: -1
        return lastSyncDate < 0 || (now - lastSyncDate) > remoteConfig.sendStepsMinInterval.minuteToSeconds()
            .toMilliseconds()
    }

    private suspend fun loadBackendData(
        startDate: String,
        offset: ZoneOffset
    ): List<FitnessDataBackendEntity> {
        return if (fitnessBackendRepository.hasData()) {
            fitnessBackendRepository.getAllData()
        } else {
            val backendData = fitnessBackendRepository.loadFromServer(startDate, offset)
            fitnessBackendRepository.saveToDb(backendData)
            backendData
        }
    }

    private suspend fun loadProviderData(startDate: Long, offset: ZoneOffset): List<ProviderFitData> {
        return try {

            val nowZoned = offset.today().with(LocalTime.MIN)

            val instant = Instant.ofEpochSecond(startDate)
            var startDateZoned = OffsetDateTime.ofInstant(instant, offset).with(LocalTime.MIN)

            var endDateZoned = if (preferencesStorage.fitDataProvider == ProviderType.HEALTH_CONNECT) {
                spikeSdkRepository.openHealthConnectConnection(profileRepository.profileId())
                nowZoned
            } else {
//                startDateZoned.getNextStepTimeZoned(nowZoned)
                nowZoned
            }

            val dataMap = mutableMapOf<Long, ProviderFitData>()

            while (!startDateZoned.isAfter(endDateZoned)) {
                repository.fetchSteps(startDateZoned, endDateZoned, offset, dataMap)
                repository.fetchActivities(startDateZoned, endDateZoned, offset, dataMap)

                startDateZoned = endDateZoned.plusDays(1)
                endDateZoned = startDateZoned.getNextStepTimeZoned(nowZoned)
            }

            dataMap.values.toList()
        } catch (e: Exception) {
            Timber.e(e)
            return emptyList()
        }
    }

    private suspend fun getBackendFetchStartDate(): Long {
        return fitnessBackendRepository.lastEntryDate().toSeconds() ?: profileRepository.createdAt()
    }

    private suspend fun getProviderFetchStartDate(offset: ZoneOffset): Long {
        val lastFetchDate = preferencesStorage.providerLastFetchDate
        val lastConnectDate = preferencesStorage.providerLastConnectDate
        val prevLastConnectDate = preferencesStorage.providerPrevLastConnectDate
        val lastDisconnectDate = preferencesStorage.providerLastDisconnectDate

        return if (lastFetchDate > 0L && lastConnectDate > 0L) {
            val minFetchDate = offset.today().minusDays(60).toEpochSecond()

            if (preferencesStorage.prevFitDataProvider == preferencesStorage.fitDataProvider) {
                max(prevLastConnectDate, minFetchDate)
            } else if (lastDisconnectDate > 0L) {
                max(lastFetchDate, minFetchDate)
            } else {
                max(lastConnectDate, minFetchDate)
            }
        } else {
            val lastEntryDate = fitnessBackendRepository.lastEntryDate().toSeconds() ?: profileRepository.createdAt()
            val minFirstTimeFetchDate = offset.today().minusDays(90).toEpochSecond()
            max(lastEntryDate, minFirstTimeFetchDate)
        }
    }

    private suspend fun evaluateDataDifference(
        providerData: List<ProviderFitData>,
        backendData: List<FitnessDataBackendEntity>,
        offset: ZoneOffset
    ): List<FitnessDataEntity> {
        val savedDiffs = fitnessRepository.getEntriesToUpload()
            .groupBy { it.entryDate.utcToZone(offset).milliseconds }

        val settings = settingsManager.getSettings(true)
        return providerData.mapNotNull { googleFit ->
            val providerEntryDate = googleFit.date.utcToZone(offset).milliseconds

            val server = backendData.find {
                val serverEntryDate = it.entryDate.utcToZone(offset).milliseconds
                serverEntryDate == providerEntryDate
            }

            val localDiff = savedDiffs.entries.find { it.key == providerEntryDate }?.value

            val steps = round(
                max(googleFit.steps - (server?.steps ?: 0.0)
                        - (localDiff?.sumOf { it.steps }?.toLong() ?: 0L), 0.0
                )
            )
            val calories = round(
                max(googleFit.calories - (server?.calories ?: 0.0)
                        - (localDiff?.sumOf { it.calories }?.toLong() ?: 0L), 0.0
                )
            )
            val distance = round(
                max(googleFit.distance - (server?.distanceMeter ?: 0.0)
                        - (localDiff?.sumOf { it.distanceMeters }?.toLong() ?: 0L), 0.0
                )
            )

            val currentStepsForDay = (localDiff?.sumOf { it.steps } ?: .0) + (server?.steps ?: .0)
            val currentCaloriesForDay =
                (localDiff?.sumOf { it.calories } ?: .0) + (server?.calories ?: .0)

            val stepsDayLimit = settings?.stepsDayLimit ?: 0L
            var stepsPoints = 0.0
            if (currentStepsForDay + steps <= stepsDayLimit) {
                stepsPoints = steps * (settings?.pointsPerStep ?: 0.0)
            }

            val caloriesDayLimit = settings?.caloriesDayLimit ?: 0L
            var caloriesPoints = 0.0
            if (currentCaloriesForDay + calories <= caloriesDayLimit) {
                caloriesPoints = calories * (settings?.pointsPerCalorie ?: 0.0)
            }

            val points = stepsPoints + caloriesPoints

            val duration = max(googleFit.duration - (server?.durationSec ?: 0L)
                    - (localDiff?.sumOf { it.durationSec } ?: 0L), 0L)

            if (steps > 0.0 || calories > 0.0) {
                FitnessDataEntity(
                    googleFit.date,
                    steps,
                    calories,
                    points,
                    duration,
                    distance,
                    googleFit.healthServiceSteps,
                    googleFit.manualSteps,
                )
            } else {
                null
            }
        }
    }

    suspend fun getFitnessDataForDay(date: ZonedDateTime): FitnessDataBackendEntity {
        val fitnessData =
            fitnessBackendRepository.getRecordForDay(date) ?: FitnessDataBackendEntity.empty
        val fitnessDiff = getLocalDiff(date) ?: FitnessDataEntity.empty

        return fitnessData.copy(
            steps = fitnessData.steps + fitnessDiff.steps,
            calories = fitnessData.calories + fitnessDiff.calories,
            durationSec = fitnessData.durationSec + fitnessDiff.durationSec,
            points = fitnessData.points + fitnessDiff.points,
        )
    }

    private suspend fun getLocalDiff(date: ZonedDateTime): FitnessDataEntity? {
        val start = date.atDayStart().milliseconds
        val end = date.atDayEnd().milliseconds
        val diffList = fitnessRepository.getNotSyncedDataForDay(start, end)
        if (diffList.isEmpty()) return null

        var entryDate = 0L
        var steps = 0.0
        var calories = 0.0
        var distance = 0.0
        var points = 0.0
        var duration = 0L
        diffList.forEach {
            if (it.entryDate > entryDate) entryDate = it.entryDate
            steps += it.steps
            calories += it.calories
            distance += it.distanceMeters
            points += it.points
            duration += it.durationSec
        }


        return FitnessDataEntity(
            entryDate,//diffList.maxOf { it.entryDate },
            steps,//diffList.sumOf { it.steps },
            calories,//diffList.sumOf { it.calories },
            points,//diffList.sumOf { it.points },
            duration,//diffList.sumOf { it.durationSec }
            distance,
            diffList.last().allStepsSum,
            diffList.last().manualSteps,
        )
    }

    suspend fun generateProviderFitnessDbJson(): String {
        val data = fitnessRepository.getAllEntries().map {
            GoogleFitServerDiff(
                entryDate = Date(it.entryDate).toString(),
                steps = it.steps,
                calories = it.calories,
                durationSec = it.durationSec,
                syncDate = it.syncDate?.let { date -> Date(date).toString() }
            )
        }
        return Gson().toJson(data)
    }

    suspend fun generateServerFitnessDbJson(): String {
        val data = fitnessBackendRepository.getAllData().map {
            ServerFitnessData(
                entryDate = Date(it.entryDate).toString(),
                steps = it.steps,
                calories = it.calories,
                points = it.points,
                durationSec = it.durationSec,
            )
        }
        return Gson().toJson(data)
    }

    private fun OffsetDateTime.getNextStepTimeZoned(now: OffsetDateTime): OffsetDateTime {
        val step = 6L
        val unit = ChronoUnit.DAYS
        val endTime = plus(step, unit)
        return if (endTime.isBefore(now)) endTime else now
    }
}

private data class GoogleFitServerDiff(
    val entryDate: String,
    val steps: Double,
    val calories: Double,
    val durationSec: Long,
    val syncDate: String? = null
)

private data class ServerFitnessData(
    val entryDate: String,
    val steps: Double,
    val calories: Double,
    val points: Double,
    val durationSec: Long,
)
