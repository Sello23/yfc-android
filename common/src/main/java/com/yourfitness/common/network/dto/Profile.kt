package com.yourfitness.common.network.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileResponse(
    @SerializedName("info") val info: Profile? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("created_at") val createdAt: Long? = null,
) : Parcelable

@Parcelize
data class Profile(
    @SerializedName("email") val email: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("surname") val surname: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("instagram") val instagram: String? = null,
    @SerializedName("media_id") val mediaId: String? = null,
    @SerializedName("gender") val gender: Gender? = null,
    @SerializedName("birthday") val birthday: Long? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("voucher") val voucher: String? = null,
    @SerializedName("complimentaryAccess") val complimentaryAccess: Boolean? = null,
    @SerializedName("platform") val platform: String? = null,
    @SerializedName("push_token") val pushToken: String? = null,
    @SerializedName("corporationID") val corporationId: String? = null,
    @SerializedName("personalTrainer") val personalTrainer: Boolean? = false,
    @SerializedName("bookable") val bookable: Boolean? = null,
    @SerializedName("accessWorkoutPlans") val accessWorkoutPlans: Boolean? = null
) : Parcelable

enum class Gender {
    @SerializedName("0") MALE,
    @SerializedName("1") FEMALE,
    @SerializedName("2") OTHER,
    @SerializedName("UNKNOWN") UNKNOWN
}

val Profile.fullName get() = listOfNotNull(name, surname).joinToString(" ")
