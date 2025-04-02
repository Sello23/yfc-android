package com.yourfitness.community.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("community", Context.MODE_PRIVATE)


    fun clearCommunityData() {
        sharedPreferences.edit()
            .apply()
    }
}

private object PreferenceKeys {
}