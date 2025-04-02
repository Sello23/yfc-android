package com.yourfitness.pt.data

import android.content.Context
import android.content.SharedPreferences
import com.yourfitness.pt.data.PreferenceKeys.HAS_SELECTED_TIME
import com.yourfitness.pt.data.PreferenceKeys.SELECTED_DATE
import com.yourfitness.pt.data.PreferenceKeys.SELECTED_REPEATED_WEEKS
import com.yourfitness.pt.data.PreferenceKeys.USER_SESSIONS_SYNCED_AT
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PtStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("pt", Context.MODE_PRIVATE)

    var userSessionsSyncedAt: String?
        get() = getLastSyncedAt(USER_SESSIONS_SYNCED_AT)
        set(time) = setLastSyncedAt(USER_SESSIONS_SYNCED_AT, time)

    var selectedDate: Long?
        get() {
            val res = sharedPreferences.getLong(SELECTED_DATE, -1L)
            return if (res == -1L) null else res
        }
        set(value) = sharedPreferences.edit().putLong(SELECTED_DATE, value ?: -1L).apply()

    var hasSelectedTime: Boolean
        get() = sharedPreferences.getBoolean(HAS_SELECTED_TIME, false)
        set(value) = sharedPreferences.edit().putBoolean(HAS_SELECTED_TIME, value).apply()

    var selectedRepeatedWeeks: Int
        get() = sharedPreferences.getInt(SELECTED_REPEATED_WEEKS, -1)
        set(value) = sharedPreferences.edit().putInt(SELECTED_REPEATED_WEEKS, value).apply()

    fun clearPtData() {
        sharedPreferences.edit()
            .remove(USER_SESSIONS_SYNCED_AT)
            .remove(SELECTED_DATE)
            .remove(HAS_SELECTED_TIME)
            .remove(SELECTED_REPEATED_WEEKS)
            .apply()
    }

    private fun getLastSyncedAt(name: String): String? {
        return sharedPreferences.getString(name, null)
    }

    private fun setLastSyncedAt(name: String, value: String?) {
        sharedPreferences.edit().putString(name, value).apply()
    }
}

private object PreferenceKeys {
    const val USER_SESSIONS_SYNCED_AT = "user_sessions_synced_at"
    const val SELECTED_DATE = "selected_date"
    const val HAS_SELECTED_TIME = "has_selected_time"
    const val SELECTED_REPEATED_WEEKS = "selected_repeated_weeks"
}