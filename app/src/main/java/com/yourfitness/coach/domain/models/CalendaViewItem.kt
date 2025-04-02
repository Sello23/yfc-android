package com.yourfitness.coach.domain.models

import com.yourfitness.coach.R
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.coach.network.dto.Schedule
import com.yourfitness.common.domain.models.CalendarDayViewItem
import com.yourfitness.common.domain.models.ClassCalendarDayViewItem


/*fun BookedClass.toCalendarWeekViewItem(): CalendarWeekViewItem.Item {
    return CalendarWeekViewItem.Item(
        day = 0L,
        facilityName = facilityName,
        className = className,
        time = time,
        date = date,
        address = address,
        types = types,
        icon = icon,
        classEntryLeadTime = classEntryLeadTime,
        classCancellationTime = classCancellationTime,
        id = facilityId,
        coachName = coachName,
        classId = classId
    )
}*/

//fun CalendarWeekViewItem.Item.toBookedClass(): BookedClass {
//    return BookedClass(
//        facilityName = facilityName ?: "",
//        className = className ?: "",
//        time = time ?: 0L,
//        date = date ?: 0L,
//        address = address ?: "",
//        types = types ?: "",
//        icon = icon ?: "",
//        classEntryLeadTime = classEntryLeadTime ?: 0,
//        classCancellationTime = classCancellationTime ?: 0,
//        coachName = coachName ?: "",
//        facilityId = id ?: "",
//        classId = classId
//    )
//}
//    class HeaderItem(override val dateInMillis: Long) :
//        ClassCalendarDayViewItem(R.layout.item_payment_history_header, dateInMillis)
//
//    class Item(
//        val day: Long?,
//        val id: String? = null,
//        val name: String? = null,
//        val datetimeInSeconds: Long? = null,
//        var address: String? = null,
//        var types: String? = null,
//        var icon: String? = null,
//        val classEntryLeadTime: Int? = null,
//        var availableSpots: Int? = null,
//        var instructor: String? = null,
//        var isBooked: Boolean? = false,
//        var credits: Int? = null,
//        var isNotAvailable: Boolean? = null,
//        var isCancelAvailable: Boolean? = null,
//    ) : ClassCalendarDayViewItem(R.layout.item_class_calendar_day, datetimeInSeconds?.toMilliseconds())
//}
//
//fun BookedClass.toCalendarDayViewItem(): CalendarDayViewItem.Item {
//    return CalendarDayViewItem.Item(
//        day = 0L,
//        facilityName = facilityName,
//        facilityId = facilityId,
//        className = className,
//        time = time,
//        date = date,
//        address = address,
//        types = types,
//        icon = icon,
//        classEntryLeadTime = classEntryLeadTime,
//        classCancellationTime = classCancellationTime,
//        coachName = coachName,
//        classId = classId
//    )
//}

fun CalendarDayViewItem.Item.toBookedClass(): BookedClass {
    return BookedClass(
        facilityName = facilityName ?: "",
        facilityId = facilityId ?: "",
        className = className ?: "",
        time = time ?: 0L,
        date = date ?: 0L,
        address = address ?: "",
        types = types ?: "",
        icon = icon ?: "",
        classEntryLeadTime = classEntryLeadTime ?: 0,
        classCancellationTime = classCancellationTime ?: 0,
        coachName = coachName ?: "",
        classId = classId
    )
}

fun Schedule.toClassCalendarDayViewItem(): ClassCalendarDayViewItem.Item {
    return ClassCalendarDayViewItem.Item(
        id = id,
        day = 0L,
        datetimeInSeconds = from,
        instructor = instructorId,
    )
}
