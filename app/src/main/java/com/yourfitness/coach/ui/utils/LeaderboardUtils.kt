package com.yourfitness.coach.ui.utils

import android.content.Context
import com.yourfitness.coach.R
import com.yourfitness.coach.ui.features.more.challenges.leaderboard.LeaderboardAdapter2
import com.yourfitness.common.ui.utils.toStringNoDecimal

object LeaderboardUtils {
    fun getScore(context: Context, score: Int?, measurement: String): String {
        return when (measurement) {
            LeaderboardAdapter2.CALORIES -> {
                context.resources.getQuantityString(
                    R.plurals.calories_plural_format,
                    score ?: 0,
                    score.toStringNoDecimal()
                )
            }
            LeaderboardAdapter2.POINTS -> {
                context.resources.getQuantityString(
                    R.plurals.points_plural_format,
                    score ?: 0,
                    score.toStringNoDecimal()
                )
            }
            LeaderboardAdapter2.STEPS -> {
                context.resources.getQuantityString(
                    R.plurals.steps_plural_format,
                    score ?: 0,
                    score.toStringNoDecimal()
                )
            }
            else -> score.toStringNoDecimal()
        }
    }
}