package com.yourfitness.coach.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CalendarBookClassData(
    val scheduleId: String,
    val className: String,
    val conflictClassId: String,
    val conflictClassName: String,
    val time: Long,
    val date: Long,
    val coachName: String,
    val address: String,
    val facilityName: String,
    val icon: String,
    val credits: Int,
    val availableBonusCredits: Int,
    val isConflictAvailable: Boolean = false,
    val isRebook: Boolean = false
) : Parcelable
