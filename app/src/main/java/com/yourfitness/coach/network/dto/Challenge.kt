package com.yourfitness.coach.network.dto

import com.google.gson.annotations.SerializedName

data class ChallengeLeaderboard(
    @SerializedName("count") val count: Int? = null,
    @SerializedName("entries") val entries: List<Entries>? = null,
    @SerializedName("rank") val rank: Entries? = null,
)

data class Entries(
    @SerializedName("mediaID") val mediaId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("surname") val surname: String? = null,
    @SerializedName("rank") val rank: Int? = null,
    @SerializedName("score") val score: Int? = null,
    @SerializedName("profileID") val profileId: String? = null,
    @SerializedName("corporationID") val corporationID: String? = null,
)

data class Leaderboard(
    @SerializedName("calories") val calories: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: Int? = null,
    @SerializedName("score") val score: Int? = null,
    @SerializedName("steps") val steps: Int? = null,
)

data class AccessCode(
    @SerializedName("accessCode") val accessCode: String
)

val Entries.fullName get() = listOfNotNull(name, surname).joinToString(" ")