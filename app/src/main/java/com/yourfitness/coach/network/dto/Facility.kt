package com.yourfitness.coach.network.dto

import com.google.gson.annotations.SerializedName
import com.yourfitness.common.domain.models.WorkTimeDto
import com.yourfitness.pt.network.dto.PersonalTrainerDto

data class FacilityResponse(
    @SerializedName("f") var facilities: List<FacilityDto>? = null,
    @SerializedName("pt") var personaTrainers: List<PersonalTrainerDto>? = null,
    @SerializedName("v") var version: String? = null
)

data class FacilityDto(
    @SerializedName("pid") var personalTrainersIds: List<String>? = null,
    @SerializedName("ad") var address: String? = null,
    @SerializedName("am") var amenities: List<String>? = null,
    @SerializedName("ct") var city: String? = null,
    @SerializedName("cls") var classification: String? = null,
    @SerializedName("cd") var contactDetails: String? = null,
    @SerializedName("cc") var customClasses: List<ClassDto>? = null,
    @SerializedName("ds") var description: String? = null,
    @SerializedName("ea") var emailAddress: String? = null,
    @SerializedName("fo") var femaleOnly: Boolean? = null,
    @SerializedName("glr") var gallery: List<String>? = null,
    @SerializedName("gr") var group: String? = null,
    @SerializedName("ic") var icon: String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("ins") var instructors: List<InstructorDto>? = null,
    @SerializedName("gl") var coordinates: CoordinatesDto? = null,
    @SerializedName("lc") var licensedClasses: List<ClassDto>? = null,
    @SerializedName("n") var name: String? = null,
    @SerializedName("rg") var region: String? = null,
    @SerializedName("tp") var types: List<String>? = null,
    @SerializedName("ua") var updatedAt: Int? = null,
    @SerializedName("ws") var schedule: List<WorkTimeDto>? = null,
    @SerializedName("al") var accessLimitationMessage: String? = null,
    @SerializedName("dal") var displayAccessLimitationMessage: Boolean? = null,
    @SerializedName("ya") var isYfcGym: Boolean? = null, )

data class CoordinatesDto (
    @SerializedName("lt") var latitude: Double? = null,
    @SerializedName("lg") var longitude: Double? = null,
)

data class InstructorDto(
    @SerializedName("ca") var createdAt: Int? = null,
    @SerializedName("ds") var description: String? = null,
    @SerializedName("fid") var facilityID: String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("n") var name: String? = null,
    @SerializedName("pid") var photoID: String? = null,
    @SerializedName("ua") var updatedAt: Int? = null
)

data class ClassDto(
    @SerializedName("ap") var availablePlaces: Int? = null,
    @SerializedName("ca") var createdAt: Int? = null,
    @SerializedName("ds") var description: String? = null,
    @SerializedName("fid") var facilityID: String? = null,
    @SerializedName("gid") var galleryIDs: List<String>? = null,
    @SerializedName("iid") var iconID: String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("lpc") var licensedProviderClassID: String? = null,
    @SerializedName("lid") var licensedProviderID: String? = null,
    @SerializedName("n") var name: String? = null,
    @SerializedName("pr") var price: Int? = null,
    @SerializedName("pc") var priceCoins: Int? = null,
    @SerializedName("tp") var type: String? = null,
    @SerializedName("ua") var updatedAt: Int? = null
)

enum class Classification(val value: String) {
    GYM("Gym"),
    STUDIO("Studio"),
    TRAINER("Trainer")
}

data class FacilityVisitInfo(
    @SerializedName("facilityID") val facilityID: String? = null,
    @SerializedName("userID") val userID: String? = null,
)