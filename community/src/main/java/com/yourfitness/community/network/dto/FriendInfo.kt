package com.yourfitness.community.network.dto

import com.google.gson.annotations.SerializedName
import com.yourfitness.common.network.dto.Challenge

data class FriendInfoDto(
    @SerializedName("challenges") val challenges: List<Challenge>? = null,
    @SerializedName("profileInfo") val profileInfo: FriendsDto? = null,
    @SerializedName("totalScore") val totalScore: TotalScoreDto? = null,
    @SerializedName("workouts") val workouts: List<WorkoutInfoDto>? = null,
)

data class TotalScoreDto(
    @SerializedName("calories") val calories: Int? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("steps") val steps: Int? = null,
)

data class WorkoutInfoDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("calories") val calories: Int? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("steps") val steps: Int? = null,
    @SerializedName("date") val date: Long? = null,
    @SerializedName("isLiked") val isLiked: Boolean? = null,
    @SerializedName("likesCount") val likesCount: Long? = null,
)

data class MyLikesBody(
    @SerializedName("friendId") val friendId: String,
    @SerializedName("liked") val likes: List<String>,
    @SerializedName("unliked") val unliked: List<String>,
)