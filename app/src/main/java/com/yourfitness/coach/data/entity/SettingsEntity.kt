package com.yourfitness.coach.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.coach.data.entity.SettingsEntity.Companion.SETTINGS_TABLE
import com.yourfitness.coach.network.dto.Bonuses

@Entity(tableName = SETTINGS_TABLE)
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 0,
    val bonuses: List<Bonuses>?,
    val classCancellationTime: Int?,
    val classEntryLeadTime: Int?,
    val maxCaloriesPerDay: Double?,
    val maxPointsPerDay: Double?,
    val maxStepsPerDay: Double?,
    val pointLevel: Long?,
    val pointLevelUp: String?,
    val rewardLevel: Long?,
    val rewardLevelUp: String?,
    val gymsNearbyMetersLimit: Int?,
    val pointsPerStep: Double?,
    val pointsPerCalorie: Double?,
    val lastUpdateTime: Long,
    val isSynchronizing: Boolean = false,
    val caloriesDayLimit: Long?,
    val stepsDayLimit: Long?,
    val dubai3030ChallengeStartDate: Long? = null,
    val dubai3030ChallengeEndDate: Long? = null,
    val corporateLeaderboardStartDate: Long? = null,
    val corporateLeaderboardEndDate: Long? = null,
) {
    companion object {
        const val SETTINGS_TABLE = "settings_table"

        val empty
            get() = SettingsEntity(
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                true,
                null,
                null
            )
    }
}

data class DayLimits(
    val caloriesDayLimit: Long?,
    val stepsDayLimit: Long?
)
