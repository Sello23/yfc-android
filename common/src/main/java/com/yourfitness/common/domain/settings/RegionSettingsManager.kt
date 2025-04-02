package com.yourfitness.common.domain.settings

import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.data.dao.RegionSettingsDao
import com.yourfitness.common.data.entity.RegionSettingsEntity
import com.yourfitness.common.data.mappers.toEntity
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.utils.getRegionZoneOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionSettingsManager @Inject constructor(
    private val restApi: CommonRestApi,
    private val settingsDao: RegionSettingsDao,
    private val commonStorage: CommonPreferencesStorage
) {
    private val mutex = Mutex()

    suspend fun getSettings(forceUpdate: Boolean = false): RegionSettingsEntity? = mutex.withLock {
        var savedSettings = settingsDao.read()
        val needUpdate = savedSettings == null || forceUpdate ||
                today().time - savedSettings.lastUpdateTime > MIN_SETTINGS_UPDATE_INTERVAL
        if (needUpdate) {
            try {
                savedSettings = restApi.getSettingsRegion().toEntity()
            } catch (e: Exception) {
                Timber.e(e)
            }
            savedSettings?.let { settingsDao.saveSettings(it) }
        }

        updateZoneOffset(savedSettings)
        return savedSettings
    }

    private fun updateZoneOffset(savedSettings: RegionSettingsEntity?) {
        savedSettings?.let {
            commonStorage.zoneOffsetValue = it.timeZoneOffset
        }
    }

    suspend fun getZoneOffset(): ZoneOffset? {
        getSettings()
        return commonStorage.zoneOffset
    }

    companion object {
        const val MIN_SETTINGS_UPDATE_INTERVAL = 5 * 60 * 1000
    }
}
