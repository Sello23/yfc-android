package com.yourfitness.coach.network.dto

import com.google.gson.annotations.SerializedName

data class Schedule(
    @SerializedName("bookedPlaces") val bookedPlaces: Int? = null,
    @SerializedName("createdAt") val createdAt: Long? = null,
    @SerializedName("customClassID") val customClassId: String? = null,
    @SerializedName("from") val from: Long? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("instructorID") val instructorId: String? = null,
    @SerializedName("licensedClassID") val licensedClassId: String? = null,
    @SerializedName("to") val to: Long? = null,
    @SerializedName("updatedAt") val updatedAt: Long? = null,
)