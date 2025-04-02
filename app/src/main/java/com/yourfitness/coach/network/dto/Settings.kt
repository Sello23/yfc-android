package com.yourfitness.coach.network.dto

import com.google.gson.annotations.SerializedName

data class SettingsGlobal(
    @SerializedName("bonuses") val bonuses: List<Bonuses>? = null,
    @SerializedName("classCancellationTime") val classCancellationTime: Int? = null,
    @SerializedName("classEntryLeadTime") val classEntryLeadTime: Int? = null,
    @SerializedName("maxCaloriesPerDay") val maxCaloriesPerDay: Double? = null,
    @SerializedName("maxPointsPerDay") val maxPointsPerDay: Double? = null,
    @SerializedName("maxStepsPerDay") val maxStepsPerDay: Double? = null,
    @SerializedName("pointLevel") val pointLevel: Long? = null,
    @SerializedName("pointLevelUp") val pointLevelUp: String? = null,
    @SerializedName("rewardLevel") val rewardLevel: Long? = null,
    @SerializedName("rewardLevelUp") val rewardLevelUp: String? = null,
    @SerializedName("gymsNearbyMetersLimit") val gymsNearbyMetersLimit: Int? = null,
    @SerializedName("pointsPerStep") val pointsPerStep: Double? = null,
    @SerializedName("pointsPerCalorie") val pointsPerCalorie: Double? = null,
    @SerializedName("dubai3030ChallengeStartDate") val dubai3030ChallengeStartDate: Long? = null,
    @SerializedName("dubai3030ChallengeEndDate") val dubai3030ChallengeEndDate: Long? = null,
    @SerializedName("caloriesLimitForCalculationPerDay") val caloriesLimitForCalculationPerDay: Long? = null,
    @SerializedName("stepsLimitForCalculationPerDay") val stepsLimitForCalculationPerDay: Long? = null,
    @SerializedName("corporateLeaderboardStartDate") val corporateLeaderboardStartDate: Long? = null,
    @SerializedName("corporateLeaderboardEndDate") val corporateLeaderboardEndDate: Long? = null
)

data class Bonuses(
    @SerializedName("amount") val amount: String? = null,
    @SerializedName("color") val color: String? = null,
    @SerializedName("credits") val credits: String? = null,
    @SerializedName("name") val name: String? = null,
)
