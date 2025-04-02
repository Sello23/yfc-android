package com.yourfitness.pt.network.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class SessionDto(
    @SerializedName("id") val id: String?,
    @SerializedName("facilityId") val facilityId: String?,
    @SerializedName("personalTrainerId") val personalTrainerId: String?,
    @SerializedName("profileId") val profileId: String?,
    @SerializedName("from") val from: Long?,
    @SerializedName("to") val to: Long?,
    @SerializedName("status") val status: String?,
    @SerializedName("chainId") val chainId: String?,
    @SerializedName("deleted") val deleted: Boolean?,
    @SerializedName("repeats") val repeats: Int?,
    @SerializedName("profileInfo") val profileInfo: ProfileInfoDto?
)

@Parcelize
data class ProfileInfoDto(
    @SerializedName("name") val name: String?,
    @SerializedName("surname") val surname: String?,
    @SerializedName("birthday") val birthday: Long?,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("mediaId") val mediaId: String?
) : Parcelable

data class SessionDtoWrapper(
    @SerializedName("sessions") val sessions: List<SessionDto>?,
    @SerializedName("totalCount") val totalCount: Int?
)

data class BookSessionDto(
    @SerializedName("facilityId") val facilityId: String,
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String,
    @SerializedName("personalTrainerId") val personalTrainerId: String,
)

data class BlockTimeSlotDto(
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String,
    @SerializedName("repeats") val repeats: Int,
)

data class BlockTimeSlotsWrapper(
    @SerializedName("chains") val chains: List<BlockTimeSlotDto>,
)

data class DeleteBlockSlotListDto(
    @SerializedName("timeSlotIDs") val timeSlotIds: List<String>,
    @SerializedName("timeSlotChainIDs") val timeSlotChainIds: List<String>,
)

data class StatusDto(@SerializedName("status") val status: String)

val ProfileInfoDto.fullName get() = listOfNotNull(name, surname).joinToString(" ")