package com.yourfitness.common.data

import android.content.Context
import android.content.SharedPreferences
import com.yourfitness.common.data.PreferenceKeys.ACCESS_TOKEN
import com.yourfitness.common.data.PreferenceKeys.FIRST_START
import com.yourfitness.common.data.PreferenceKeys.REFRESH_TOKEN
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    var accessToken: String?
        get() = getToken(ACCESS_TOKEN)
        set(token) = setToken(ACCESS_TOKEN, token)

    var refreshToken: String?
        get() = getToken(REFRESH_TOKEN)
        set(token) = setToken(REFRESH_TOKEN, token)

    var firstStart: Boolean
        get() = sharedPreferences.getBoolean(FIRST_START, true)
        set(value) = sharedPreferences.edit().putBoolean(FIRST_START, value).apply()

    fun clearTokens() {
        sharedPreferences.edit()
            .remove(ACCESS_TOKEN)
            .remove(REFRESH_TOKEN)
            .apply()
    }

    private fun getToken(name: String): String? {
        return sharedPreferences.getString(name, null)
    }

    private fun setToken(name: String, value: String?) {
        sharedPreferences.edit().putString(name, value).apply()
    }
}

private object PreferenceKeys {
    const val ACCESS_TOKEN = "access_token"
    const val REFRESH_TOKEN = "refresh_token"
    const val FIRST_START = "first_start"
}