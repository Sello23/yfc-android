package com.yourfitness.community.domain

import android.content.Context
import android.net.Uri
import com.yourfitness.common.BuildConfig
import com.yourfitness.common.domain.date.toMilliseconds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StepsCaloriesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun getFitnessInfo(date: Long): Pair<Long, Long>? = withContext(Dispatchers.IO) {
        val cursorBackend = context.contentResolver.query(
            Uri.parse("content://com.yourfitness.fitness/fitnessBackendInfo"),
            null, date.toMilliseconds().toString(), null, null
        ) ?: return@withContext null

        var backendSteps = 0.0
        var backendCalories = 0.0
        if (cursorBackend.moveToFirst()) {
            backendSteps = cursorBackend.getDouble(0)
            backendCalories = cursorBackend.getDouble(1)
        }

        val cursor = context.contentResolver.query(
            Uri.parse("content://com.yourfitness.fitness/fitnessInfo"),
            null, date.toMilliseconds().toString(), null, null
        )
        if (cursor == null || !cursor.moveToFirst()) return@withContext backendSteps.toLong() to backendCalories.toLong()

        var steps = 0.0
        var calories = 0.0
        while (!cursor.isAfterLast) {
            steps += cursorBackend.getDouble(0)
            calories += cursorBackend.getDouble(1)
            cursor.moveToNext()
        }

        cursor.close()
        cursorBackend.close()

        return@withContext (backendSteps + steps).toLong() to (backendCalories + calories).toLong()
    }

}
