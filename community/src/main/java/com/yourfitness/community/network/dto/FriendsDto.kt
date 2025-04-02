package com.yourfitness.community.network.dto

import com.google.gson.annotations.SerializedName

data class FriendsDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("mediaID") val mediaId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("progressLevelID") val progressLevelId: String? = null,
    @SerializedName("progressLevelMediaID") val progressLevelMediaId: String? = null,
    @SerializedName("progressLevelName") val progressLevelName: String? = null,
    @SerializedName("surname") val surname: String? = null,

    @SerializedName("isFriend") val isFriend: Boolean? = false,
    @SerializedName("requestIn") val requestIn: Boolean? = false,
    @SerializedName("requestOut") val requestOut: Boolean? = false,

    @SerializedName("workoutDate") val workoutDate: Long?,
    @SerializedName("viewed") val viewed: Boolean?,
    @SerializedName("friendID") val friendId: String? = null,
)

data class SearchFriendsDto(
    @SerializedName("profiles") val profiles: List<FriendsDto>? = null,
)

val FriendsDto.fullName get() = listOfNotNull(name, surname).joinToString(" ")