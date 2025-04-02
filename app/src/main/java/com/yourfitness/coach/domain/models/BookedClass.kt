package com.yourfitness.coach.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookedClass(
    val facilityName: String,
    val className: String,
    val time: Long,
    val date: Long,
    val address: String,
    val types: String,
    val icon: String,
    val classEntryLeadTime: Int,
    val classCancellationTime: Int,
    val facilityId: String,
    val coachName: String,
    val classId: String? = null
) : Parcelable
