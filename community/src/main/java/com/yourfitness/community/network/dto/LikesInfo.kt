package com.yourfitness.community.network.dto

import com.google.gson.annotations.SerializedName
import com.yourfitness.common.domain.date.today

data class LikesInfo(
    @SerializedName("friendsImages") val images: List<String>? = null,
    @SerializedName("likesCount") val likesCount: Int? = null,
    @SerializedName("newLikesCount") val newLikesCount: Int? = null,
    val updatedAt: Long = today().time,
)

