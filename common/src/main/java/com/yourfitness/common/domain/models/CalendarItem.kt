package com.yourfitness.common.domain.models

import android.content.Context
import androidx.annotation.StringRes
import com.yourfitness.common.R
import com.yourfitness.common.domain.date.toMilliseconds

sealed class CalendarView {

    data class Header(val startDate: Long?, val endDate: Long? = null) : CalendarView()

    data class CalendarItem(
        val facilityId: String = "",
        val objectId: String? = null,
        val facilityName: String = "",
        val coachName: String = "",
        val objectName: String = "",
        val day: Long? = null,
        val time: Long = 0L,
        val timeTo: Long = 0L,
        val date: Long = 0L,
        val address: String = "",
        val icon: String = "",
        val types: String = "",
        val classEntryLeadTime: Int = 0,
        val classCancellationTime: Int = 0,
        val status: String? = null,
        val statusBg: Int? = null,
        val statusBuilder: ((String, Context) -> String)? = null,
        val labelBuilder: ((Context) -> String)? = null,
        val actionLabel: Pair<Int, Int>? = null,
        val repeats: Int = 0
    ) : CalendarView() {
        companion object {
            fun empty(day: Long): CalendarItem = CalendarItem(day = day)

            fun CalendarItem.isEmpty(): Boolean = status == null
        }
    }
}

data class Week(
    val startDate: Long,
    val endDate: Long
)

sealed class CalendarDayViewItem {
    class HeaderItem(val date: Long) : CalendarDayViewItem()
    class Item(
        val day: Long?,
        val facilityName: String? = null,
        val facilityId: String? = null,
        val className: String? = null,
        val time: Long? = null,
        val date: Long? = null,
        val address: String? = null,
        val types: String? = null,
        val icon: String? = null,
        val classEntryLeadTime: Int? = null,
        val classCancellationTime: Int? = null,
        val coachName: String? = null,
        val classId: String? = null
    ) : CalendarDayViewItem()
}

sealed class ClassCalendarDayViewItem(val resource: Int, open val dateInMillis: Long?) {

    class HeaderItem(override val dateInMillis: Long) :
        ClassCalendarDayViewItem(R.layout.item_payment_history_header, dateInMillis)

    class Item(
        val day: Long?,
        val id: String? = null,
        val name: String? = null,
        val datetimeInSeconds: Long? = null,
        var address: String? = null,
        var types: String? = null,
        var icon: String? = null,
        val classEntryLeadTime: Int? = null,
        var availableSpots: Int? = null,
        var instructor: String? = null,
        var isBooked: Boolean? = false,
        var credits: Int? = null,
        var isNotAvailable: Boolean? = null,
        var isCancelAvailable: Boolean? = null,
    ) : ClassCalendarDayViewItem(
        R.layout.item_class_calendar_day,
        datetimeInSeconds?.toMilliseconds()
    )
}
