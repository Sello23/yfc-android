package com.yourfitness.pt.domain.models

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import java.util.*

sealed class CalendarData(val type: Int, val id: String)

class DayRow(val date: Date, id: String = "") : CalendarData(0, id)

open class TimeSlot(
    val time: Date,
    val enabled: Boolean = true,
    type: Int = 1,
    id: String
) : CalendarData(type, id)

sealed class ActionTimeSlot(
    time: Date,
    val slotAmount: Int = 2,
    type: Int, id: String
) : TimeSlot(time, type = type, id = id)

class SelectedTimeSlot(
    time: Date,
    val timeTo: Date,
    val timeSecondSlot: Date,
    id: String
) : ActionTimeSlot(time, type = 2, id = id)

class FilledTimeSlot(
    val uiData: CardSettings,
    val facility: String,
    val facilityImg: String,
    time: Date,
    id: String
) : ActionTimeSlot(time, type = 3, id = id)

data class CardSettings(
    @ColorRes val bgColor: Int,
    @StringRes val status: Int,
    @DrawableRes val statusIcon: Int? = null,
    @StringRes val statusInfo: Int? = null,
    val isLight: Boolean = true,
)
