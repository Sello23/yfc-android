package com.yourfitness.pt.domain.models

import android.os.Parcelable
import com.yourfitness.pt.network.dto.PtInductionDto
import kotlinx.parcelize.Parcelize

@Parcelize
data class InductionInfo(
    val induction: PtInductionDto,
    val facilityName: String,
    val facilityLogo: String,
    val facilityAddress: String
) : ClientInductionData, Parcelable

interface ClientInductionData