package com.yourfitness.common

import android.content.Context
import com.yourfitness.common.utils.getRegionZoneOffset
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommonPreferencesStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences =
        context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    var availableCoins: Long
        get() = getLong(AVAILABLE_COINS)
        set(coins) = setLong(AVAILABLE_COINS, coins)


    private fun getLong(name: String): Long {
        return sharedPreferences.getLong(name, -1L)
    }

    private fun setLong(name: String, value: Long) {
        sharedPreferences.edit().putLong(name, value).apply()
    }

    var isPtRole: Boolean
        get() = sharedPreferences.getBoolean(IS_PT_ROLE, false)
        set(isPt) = sharedPreferences.edit().putBoolean(IS_PT_ROLE, isPt).apply()

    var isBookable: Boolean
        get() = sharedPreferences.getBoolean(IS_BOOKLE, false)
        set(isBookable) = sharedPreferences.edit().putBoolean(IS_BOOKLE, isBookable).apply()

    var accessWorkoutPlans: Boolean
        get() = sharedPreferences.getBoolean(ACCESS_WORKOUT_PLANS, false)
        set(accessWorkoutPlans) = sharedPreferences.edit().putBoolean(ACCESS_WORKOUT_PLANS, accessWorkoutPlans).apply()

    var dubaiStart: Long?
        get() {
            val date = sharedPreferences.getLong(DUBAI_START, -1L)
            return if (date == -1L) null
            else date
        }
        set(date) = sharedPreferences.edit().putLong(DUBAI_START, date ?: -1L).apply()

    var dubaiEnd: Long?
        get() {
            val date = sharedPreferences.getLong(DUBAI_END, -1L)
            return if (date == -1L) null
            else date
        }
        set(date) = sharedPreferences.edit().putLong(DUBAI_END, date ?: -1L).apply()

    var intercorporateStart: Long?
        get() {
            val date = sharedPreferences.getLong(INTERCORPORATE_START, -1L)
            return if (date == -1L) null
            else date
        }
        set(date) = sharedPreferences.edit().putLong(INTERCORPORATE_START, date ?: -1L).apply()

    var intercorporateEnd: Long?
        get() {
            val date = sharedPreferences.getLong(INTERCORPORATE_END, -1L)
            return if (date == -1L) null
            else date
        }
        set(date) = sharedPreferences.edit().putLong(INTERCORPORATE_END, date ?: -1L).apply()

    var zoneOffsetValue: Int?
        get() {
            val zone = sharedPreferences.getInt(ZONE_OFFSET, 0)
            return if (zone == -1) null
            else zone
        }
        set(value) = sharedPreferences.edit().putInt(ZONE_OFFSET, value ?: -1).apply()

    val zoneOffset: ZoneOffset?
        get() = zoneOffsetValue?.getRegionZoneOffset

    var minVersion: Int
        get() = sharedPreferences.getInt(HARD_VERSION, 0)
        set(version) = sharedPreferences.edit().putInt(HARD_VERSION, version).apply()

    var title: String
        get() = sharedPreferences.getString(HARD_TITLE_TEXT, "") ?: ""
        set(text) = sharedPreferences.edit().putString(HARD_TITLE_TEXT, text).apply()
    var description: String
        get() = sharedPreferences.getString(HARD_DESCRIPTION_TEXT, "") ?: ""
        set(text) = sharedPreferences.edit().putString(HARD_DESCRIPTION_TEXT, text).apply()

    var dubai3030Enabled: Boolean
        get() = sharedPreferences.getBoolean(DUBAI_3030_ENABLED, false)
        set(enabled) = sharedPreferences.edit().putBoolean(DUBAI_3030_ENABLED, enabled).apply()

    var sendStepsMinInterval: Long
        get() = sharedPreferences.getLong(SEND_STEPS_MIN_INTERVAL, 5)
        set(interval) = sharedPreferences.edit().putLong(SEND_STEPS_MIN_INTERVAL, interval).apply()

    var registeredAfter: Long
        get() = sharedPreferences.getLong(PROFILE_REGISTERED_AFTER, 0)
        set(date) = sharedPreferences.edit().putLong(PROFILE_REGISTERED_AFTER, date).apply()

    var restrictionStart: Long
        get() = sharedPreferences.getLong(RESTRICTION_START, 0)
        set(date) = sharedPreferences.edit().putLong(RESTRICTION_START, date).apply()

    var restrictionEnd: Long
        get() = sharedPreferences.getLong(RESTRICTION_END, 0)
        set(date) = sharedPreferences.edit().putLong(RESTRICTION_END, date).apply()

    var gymIds: Set<String>
        get() = sharedPreferences.getStringSet(RESTRICTION_IDS, setOf()) ?: setOf()
        set(ids) = sharedPreferences.edit().putStringSet(RESTRICTION_IDS, ids).apply()

    var spikeUserId: String
        get() = sharedPreferences.getString(SPIKE_USER_ID, "") ?: ""
        set(text) = sharedPreferences.edit().putString(SPIKE_USER_ID, text).apply()

    fun clear() {
        sharedPreferences.edit()
            .remove(AVAILABLE_COINS)
            .remove(IS_PT_ROLE)
            .remove(DUBAI_START)
            .remove(DUBAI_END)
            .remove(INTERCORPORATE_START)
            .remove(INTERCORPORATE_END)
            .remove(ZONE_OFFSET)
            .remove(SPIKE_USER_ID)
            .apply()
    }

    companion object {
        const val AVAILABLE_COINS = "available_coins"

        const val IS_PT_ROLE = "is_pt_role"
        const val IS_BOOKLE = "is_bookle"
        const val ACCESS_WORKOUT_PLANS = "access_workout_plans"
        const val DUBAI_START = "dubai_start"
        const val DUBAI_END = "dubai_end"
        const val INTERCORPORATE_START = "intercorporate_start"
        const val INTERCORPORATE_END = "intercorporate_end"
        const val ZONE_OFFSET = "zone_offset"

        const val HARD_DESCRIPTION_TEXT = "hardDescrText"
        const val HARD_TITLE_TEXT = "hardTitleText"
        const val HARD_VERSION = "hardVersion"
        const val DUBAI_3030_ENABLED = "dubai3030Enabled"
        const val SEND_STEPS_MIN_INTERVAL = "sendStepsMinInterval"
        const val PROFILE_REGISTERED_AFTER = "profileRegisteredAfter"
        const val RESTRICTION_START = "restrictionStart"
        const val RESTRICTION_END = "restrictionEnd"
        const val RESTRICTION_IDS = "restrictionIds"

        const val SPIKE_USER_ID = "spikeUserId"
    }
}
