package com.yourfitness.coach.domain.fb_remote_config

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourfitness.coach.BuildConfig
import com.yourfitness.coach.R
import com.yourfitness.coach.domain.fb_remote_config.models.Dubai30x30Restrictions
import com.yourfitness.coach.domain.helpers.MutexHelper
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.CommonPreferencesStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRemoteConfigRepository @Inject constructor(
    private val navigator: Navigator,
    private val mutexHelper: MutexHelper,
    private val commonStorage: CommonPreferencesStorage
) {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    private val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 30
    }

    val needUpdate: Boolean
        get() = BuildConfig.VERSION_CODE < commonStorage.minVersion

    val title: String
        get() = commonStorage.title
    val description: String
        get() = commonStorage.description

    val dubai3030Enabled: Boolean
        get() = commonStorage.dubai3030Enabled

    val sendStepsMinInterval: Long
        get() = commonStorage.sendStepsMinInterval

    val restrictions: Dubai30x30Restrictions
        get() = Dubai30x30Restrictions(
            commonStorage.registeredAfter,
            commonStorage.restrictionStart,
            commonStorage.restrictionEnd,
            commonStorage.gymIds.toList(),
        )

    init {
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.fb_remote_config)
        autoFetchConfig()

        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate : ConfigUpdate) {
                Timber.d("Config update: ${configUpdate.updatedKeys}")

                val updateFunctions = mutableSetOf<() -> Unit>()

                if (configUpdate.updatedKeys.contains(DUBAI_3030_ENABLED)) {
                    updateFunctions.add(::fetchDubai3030Status)
                }

                if (configUpdate.updatedKeys.contains(HARD_VERSION)) {
                    updateFunctions.add(::fetchAndProcessHardUpdate)
                }

                if (configUpdate.updatedKeys.contains(SEND_STEPS_MIN_INTERVAL)) {
                    updateFunctions.add(::fetchSendStepsInterval)
                }

                if (configUpdate.updatedKeys.contains(DUBAI_30_X_30_RESTRICTIONS)) {
                    updateFunctions.add(::fetDubai30x30Restrictions)
                }

                if (updateFunctions.isNullOrEmpty()) return

                remoteConfig.activate().addOnCompleteListener {
                    updateFunctions.forEach { it.invoke() }
                }
            }

            override fun onError(error : FirebaseRemoteConfigException) {
                Timber.e("Config update error with code: " + error.code, error)
            }
        })
    }

    private fun autoFetchConfig() {
        CoroutineScope(Dispatchers.IO).launch {
            fetchRemoteConfigs()
        }
    }

    suspend fun fetchRemoteConfigs() = mutexHelper.remoteConfigMutex.withLock {
        try {
            remoteConfig.fetchAndActivate().await()
            fetchHardUpdate()
            fetchDubai3030Status()
            fetchSendStepsInterval()
            fetDubai30x30Restrictions()
        } catch (error: Exception) {
            Timber.e(error)
        }
    }

    private fun fetchHardUpdate() {
        try {
            val version = remoteConfig.getString(HARD_VERSION)
            commonStorage.minVersion = version.toInt()
            commonStorage.description = remoteConfig.getString(HARD_DESCRIPTION_TEXT)
            commonStorage.title = remoteConfig.getString(HARD_TITLE_TEXT)
        } catch (error: Exception) {
            Timber.e(error)
        }
    }

    private fun fetchAndProcessHardUpdate() {
        fetchHardUpdate()
        if (needUpdate) navigator.navigate(Navigation.HardUpdate)
    }

    private fun fetchDubai3030Status() {
        commonStorage.dubai3030Enabled = remoteConfig.getBoolean(DUBAI_3030_ENABLED)
    }

    private fun fetchSendStepsInterval() {
        commonStorage.sendStepsMinInterval = remoteConfig.getLong(SEND_STEPS_MIN_INTERVAL)
    }

    private fun fetDubai30x30Restrictions() {
        val dataJson = remoteConfig.getString(DUBAI_30_X_30_RESTRICTIONS)
        val listType = object : TypeToken<Dubai30x30Restrictions?>() {}.type
        val restrictions: Dubai30x30Restrictions = Gson().fromJson(dataJson, listType)
        commonStorage.apply {
            registeredAfter = restrictions.registeredAfter
            restrictionStart = restrictions.start
            restrictionEnd = restrictions.end
            gymIds = restrictions.gymIds.toSet()
        }
    }

    companion object {
        const val HARD_DESCRIPTION_TEXT = "hardDescrText"
        const val HARD_TITLE_TEXT = "hardTitleText"
        const val HARD_VERSION = "hardVersion"
        const val DUBAI_3030_ENABLED = "dubai3030Enabled"
        const val SEND_STEPS_MIN_INTERVAL = "sendStepsMinInterval"
        const val DUBAI_30_X_30_RESTRICTIONS = "dubai30x30Restrictions"
    }
}
