package com.yourfitness.coach

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourfitness.coach.PreferenceKeys.FACILITIES_LOADED
import com.yourfitness.coach.PreferenceKeys.FACILITY_TAB
import com.yourfitness.coach.PreferenceKeys.FITNESS_DATA_PROVIDER
import com.yourfitness.coach.PreferenceKeys.LAST_POINTS
import com.yourfitness.coach.PreferenceKeys.LAST_VISITS
import com.yourfitness.coach.PreferenceKeys.LAST_VOUCHER_REWARDS
import com.yourfitness.coach.PreferenceKeys.PREVIOUS_FITNESS_DATA_PROVIDER
import com.yourfitness.coach.PreferenceKeys.PROVIDER_LAST_DISCONNECT_DATE
import com.yourfitness.coach.PreferenceKeys.PROVIDER_LAST_CONNECT_DATE
import com.yourfitness.coach.PreferenceKeys.PROVIDER_LAST_FETCH_DATE
import com.yourfitness.coach.PreferenceKeys.PROVIDER_PREVIOUS_LAST_CONNECT_DATE
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.features.profile.connected_devices.ProviderType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    private val gson = Gson()

    var facilityTab: Classification
        get() = Classification.values()[sharedPreferences.getInt(FACILITY_TAB, 0)]
        set(value) = sharedPreferences.edit().putInt(FACILITY_TAB, value.ordinal).apply()

    var lastPoints: Long
        get() = sharedPreferences.getLong(LAST_POINTS, 0L)
        set(value) = sharedPreferences.edit().putLong(LAST_POINTS, value).apply()

    var lastVisits: Map<String, Int>
        get() = readMap(LAST_VISITS)
        set(value) = writeMap(LAST_VISITS, value)

    var lastVoucherRewards: Int
        get() = sharedPreferences.getInt(LAST_VOUCHER_REWARDS, 0)
        set(value) = sharedPreferences.edit().putInt(LAST_VOUCHER_REWARDS, value).apply()

    var facilitiesLoaded: Int // -1: not loaded; 0: start loading; 1: loaded
        get() = sharedPreferences.getInt(FACILITIES_LOADED, -1)
        set(value) = sharedPreferences.edit().putInt(FACILITIES_LOADED, value).apply()

    var fitDataProvider: ProviderType?
        get() {
            val mode = sharedPreferences.getString(FITNESS_DATA_PROVIDER, "")
            return if (mode?.isNotBlank() == true) ProviderType.valueOf(mode) else null
        }
        set(value) {
            fitDataProvider?.let {  prevFitDataProvider = it }
            sharedPreferences.edit().putString(FITNESS_DATA_PROVIDER, value?.toString() ?: "").apply()
        }

    var prevFitDataProvider: ProviderType?
        get() {
            val mode = sharedPreferences.getString(PREVIOUS_FITNESS_DATA_PROVIDER, "")
            return if (mode?.isNotBlank() == true) ProviderType.valueOf(mode) else null
        }
        set(value) = sharedPreferences.edit().putString(PREVIOUS_FITNESS_DATA_PROVIDER, value?.toString() ?: "").apply()

    var providerLastFetchDate: Long
        get() = sharedPreferences.getLong(PROVIDER_LAST_FETCH_DATE, 0L)
        set(value) = sharedPreferences.edit().putLong(PROVIDER_LAST_FETCH_DATE, value).apply()

    var providerLastConnectDate: Long
        get() = sharedPreferences.getLong(PROVIDER_LAST_CONNECT_DATE, 0L)
        set(value) {
            providerPrevLastConnectDate = providerLastConnectDate
            sharedPreferences.edit().putLong(PROVIDER_LAST_CONNECT_DATE, value).apply()
        }

    var providerPrevLastConnectDate: Long
        get() = sharedPreferences.getLong(PROVIDER_PREVIOUS_LAST_CONNECT_DATE, 0L)
        set(value) = sharedPreferences.edit().putLong(PROVIDER_PREVIOUS_LAST_CONNECT_DATE, value).apply()

    var providerLastDisconnectDate: Long
        get() = sharedPreferences.getLong(PROVIDER_LAST_DISCONNECT_DATE, 0L)
        set(value) = sharedPreferences.edit().putLong(PROVIDER_LAST_DISCONNECT_DATE, value).apply()

    fun clear() {
        sharedPreferences.edit()
            .remove(FACILITY_TAB)
            .remove(LAST_POINTS)
            .remove(LAST_VISITS)
            .remove(LAST_VOUCHER_REWARDS)
            .remove(FACILITIES_LOADED)
            .remove(FITNESS_DATA_PROVIDER)
            .remove(PREVIOUS_FITNESS_DATA_PROVIDER)
            .remove(PROVIDER_LAST_FETCH_DATE)
            .remove(PROVIDER_LAST_CONNECT_DATE)
            .remove(PROVIDER_PREVIOUS_LAST_CONNECT_DATE)
            .remove(PROVIDER_LAST_DISCONNECT_DATE)
            .apply()
    }

    private fun readMap(key: String): Map<String, Int> {
        val type = object : TypeToken<Map<String, Int>>() {}.type
        val json = sharedPreferences.getString(key, "{}")
        return gson.fromJson(json, type)
    }

    private fun writeMap(key: String, value: Map<String, Int>) {
        val json = gson.toJson(value)
        sharedPreferences.edit().putString(key, json).apply()
    }
}

private object PreferenceKeys {
    const val FACILITY_TAB = "facility_tab"
    const val LAST_POINTS = "last_points"
    const val LAST_VISITS = "last_visits"
    const val LAST_VOUCHER_REWARDS = "last_voucher_rewards"
    const val FACILITIES_LOADED = "facilities_loaded"
    const val FITNESS_DATA_PROVIDER = "fitness_data_provider"
    const val PREVIOUS_FITNESS_DATA_PROVIDER = "previous_fitness_data_provider"
    const val PROVIDER_LAST_FETCH_DATE = "provider_last_fetch_date"
    const val PROVIDER_LAST_CONNECT_DATE = "provider_last_connect_date"
    const val PROVIDER_PREVIOUS_LAST_CONNECT_DATE = "provider_previous_last_connect_date"
    const val PROVIDER_LAST_DISCONNECT_DATE = "provider_last_disconnect_date"
}
