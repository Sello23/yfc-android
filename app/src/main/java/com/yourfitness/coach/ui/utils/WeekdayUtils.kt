package com.yourfitness.coach.ui.utils

import android.content.Context
import com.yourfitness.coach.R
import java.util.Calendar

fun Context.getLocalizedWeekday(weekday: String): String {
    return getString(when (weekday.lowercase()) {
        Weekday.SUNDAY.label.lowercase() -> R.string.sunday
        Weekday.MONDAY.label.lowercase() -> R.string.monday
        Weekday.TUESDAY.label.lowercase() -> R.string.tuesday
        Weekday.WEDNESDAY.label.lowercase() -> R.string.wednesday
        Weekday.THURSDAY.label.lowercase() -> R.string.thursday
        Weekday.FRIDAY.label.lowercase() -> R.string.friday
        Weekday.SATURDAY.label.lowercase() -> R.string.saturday
        else -> R.string.sunday
    })
}

fun String?.getWeekdayValue(): Int {
    return when(this?.lowercase()) {
        Weekday.SUNDAY.label.lowercase() -> Calendar.SUNDAY
        Weekday.MONDAY.label.lowercase() -> Calendar.MONDAY
        Weekday.TUESDAY.label.lowercase() -> Calendar.TUESDAY
        Weekday.WEDNESDAY.label.lowercase() -> Calendar.WEDNESDAY
        Weekday.THURSDAY.label.lowercase() -> Calendar.THURSDAY
        Weekday.FRIDAY.label.lowercase() -> Calendar.FRIDAY
        Weekday.SATURDAY.label.lowercase() -> Calendar.SATURDAY
        else -> Calendar.SUNDAY
    }
}

enum class Weekday(val label: String) {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday")
}