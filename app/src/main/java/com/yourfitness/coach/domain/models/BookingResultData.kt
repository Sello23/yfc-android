package com.yourfitness.coach.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookingResultData(
    val purchasedCredits: Int?,
    val userName: String,
    val date: Long,
    val time: Long,
    val credits: Int,
    val bonusesCredits: Int,
    val isRebook: Boolean
) : Parcelable
