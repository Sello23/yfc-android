package com.yourfitness.coach.data.mapper

import com.google.gson.annotations.SerializedName
import com.yourfitness.coach.data.entity.SettingsEntity
import com.yourfitness.coach.network.dto.SettingsGlobal
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.domain.date.today

fun SettingsGlobal.toEntity(): SettingsEntity {
    return SettingsEntity(
        bonuses = bonuses,
        classCancellationTime = classCancellationTime,
        classEntryLeadTime = classEntryLeadTime,
        maxCaloriesPerDay = maxCaloriesPerDay,
        maxPointsPerDay = maxPointsPerDay,
        maxStepsPerDay = maxStepsPerDay,
        pointLevel = pointLevel,
        pointLevelUp = pointLevelUp,
        rewardLevel = rewardLevel,
        rewardLevelUp = rewardLevelUp,
        gymsNearbyMetersLimit = gymsNearbyMetersLimit,
        pointsPerStep = pointsPerStep,
        pointsPerCalorie = pointsPerCalorie,
        lastUpdateTime = today().time,
        dubai3030ChallengeStartDate = dubai3030ChallengeStartDate?.toMilliseconds(),
        dubai3030ChallengeEndDate = dubai3030ChallengeEndDate?.toMilliseconds(),
        caloriesDayLimit = caloriesLimitForCalculationPerDay,
        stepsDayLimit = stepsLimitForCalculationPerDay,
        corporateLeaderboardStartDate = corporateLeaderboardStartDate?.toMilliseconds(),
        corporateLeaderboardEndDate = corporateLeaderboardEndDate?.toMilliseconds()
    )
}
