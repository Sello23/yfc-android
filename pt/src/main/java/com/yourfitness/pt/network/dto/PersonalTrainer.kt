package com.yourfitness.pt.network.dto

import com.google.gson.annotations.SerializedName

data class PersonalTrainerDto(
    @SerializedName("id") var id: String? = null,
    @SerializedName("am") var amenities: List<String>? = null,
    @SerializedName("fa") var focusAreas: List<String>? = null,
    @SerializedName("ds") var description: String? = null,
    @SerializedName("n") var name: String? = null,
    @SerializedName("s") var surname: String? = null,
    @SerializedName("b") var birthday: String? = null,
    @SerializedName("e") var email: String? = null,
    @SerializedName("pn") var phoneNumber: String? = null,
    @SerializedName("mid") var mediaId: String? = null,
    @SerializedName("ed") var educations: List<EducationDto>? = null,
    @SerializedName("fid") var facilityIDs: List<String>? = null,
    @SerializedName("bo") var isBookable: Boolean? = null,
    @SerializedName("in") var instagram: String? = null)

data class EducationDto(
    @SerializedName("i") var institute: String? = null,
    @SerializedName("q") var qualification: String? = null,
    @SerializedName("y") var createdAt: Int? = null
)
