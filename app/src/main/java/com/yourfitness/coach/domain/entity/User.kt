package com.yourfitness.coach.domain.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.yourfitness.common.network.dto.Gender
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @SerializedName("name")val name: String = "",
    @SerializedName("surname")val surname: String = "",
    @SerializedName("email")val email: String = "",
    @SerializedName("mediaId")val mediaId: String = "",
    @SerializedName("region")val region: String = "",
    @SerializedName("phone")val phone: String = "",
    @SerializedName("otpId")val otpId: String = "",
    @SerializedName("birthday")val birthday: Long = 0,
    @SerializedName("gender")val gender: Gender = Gender.OTHER,
    @SerializedName("corporationId")val corporationId: String = ""
) : Parcelable

val User.fullName get() = listOfNotNull(name, surname).joinToString(" ")

val String.nameInitials get() = split(" ").take(2).joinToString("") { it.first().uppercase() }