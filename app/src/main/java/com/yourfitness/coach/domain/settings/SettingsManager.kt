package com.yourfitness.coach.domain.settings

import com.yourfitness.coach.data.dao.SettingsDao
import com.yourfitness.coach.data.entity.DayLimits
import com.yourfitness.coach.data.entity.SettingsEntity
import com.yourfitness.coach.data.mapper.toEntity
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.date.today
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    private val restApi: YFCRestApi,
    private val settingsDao: SettingsDao,
    private val commonStorage: CommonPreferencesStorage
) {
    private val mutex = Mutex()

    suspend fun getSettings(forceUpdate: Boolean = false): SettingsEntity? = mutex.withLock {
        var savedSettings = getSavedSettings()
        val needUpdate = savedSettings == null || forceUpdate ||
                today().time - savedSettings.lastUpdateTime > MIN_SETTINGS_UPDATE_INTERVAL
        if (needUpdate) {
            try {
                savedSettings = restApi.getSettingsGlobal().toEntity()
            } catch (e: Exception) {
                Timber.e(e)
            }
            savedSettings?.let { settingsDao.saveSettings(it) }
        }

        updateCorpLeaderboardDates(savedSettings)
        return savedSettings
    }

    private fun updateCorpLeaderboardDates(savedSettings: SettingsEntity?) {
        savedSettings?.let {
            commonStorage.intercorporateStart = it.corporateLeaderboardStartDate
            commonStorage.intercorporateEnd = it.corporateLeaderboardEndDate
            commonStorage.dubaiStart = it.dubai3030ChallengeStartDate
            commonStorage.dubaiEnd = it.dubai3030ChallengeEndDate
        }
    }

    suspend fun getDayLimits(): DayLimits? {
        val limits = getSavedDayLimits()
        if (limits == null) getSettings(true)
        else return limits
        return getSavedDayLimits()
    }

    suspend fun getSavedSettings() = settingsDao.read()

    suspend fun getSavedDayLimits() = settingsDao.readDayLimits()

    companion object {
        const val MIN_SETTINGS_UPDATE_INTERVAL = 10 * 60 * 1000
    }
}
