package com.yourfitness.coach.domain.date

import com.yourfitness.common.domain.date.setDayStartTime
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.toMilliseconds
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


fun ZoneOffset.today(): OffsetDateTime {
    return  OffsetDateTime.now(this)
}

fun todayWeekday(): Int {
    val c = Calendar.getInstance()
    return c[Calendar.DAY_OF_WEEK]
}

fun Long?.toSeconds(): Long? {
    return if (this != null) this / 1000 else null
}

fun Long?.toMilliseconds(): Long? {
    return if (this != null) this * 1000 else null
}

fun Long?.toStartDayInMillis(): Long? {
    return toMilliseconds().toDate()?.setDayStartTime()?.timeInMillis
}

fun Long?.millisToLocalDate(): LocalDate? {
    return this?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
}

fun Date?.formatDate(): String? {
    return if (this != null) DATE_FORMAT.format(time._toLocalDateTime()) else null
}

fun Date.formatCalendarRFC(): String {
    val currentTime = Calendar.getInstance().apply {
        time = this@formatCalendarRFC
    }
    return DATE_FORMAT_RFC.format(currentTime.timeInMillis._toLocalDateTime())
}

fun Date.setDayEndTime(): Long {
    val currentTime = Calendar.getInstance().apply {
        time = this@setDayEndTime
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }
    return currentTime.timeInMillis
}

fun String.toDate(): Date? {
    val dateString = this
    val format = DATE_FORMAT_YYYY_MM_DD
    return format.parse(dateString)
}

fun String.toDateSimple(offset: ZoneOffset): Long {
    val formatter = DATE_FORMAT_RFC
    val localDateTime = LocalDate.parse(this, formatter)
    val instant = localDateTime.atStartOfDay().toInstant(offset)
    return instant.toEpochMilli()
}

fun String.toDateISO(offset: ZoneOffset): Long {
    val formatter = DATE_FORMAT_ISO
    val localDateTime = LocalDateTime.parse(this, formatter).with(LocalTime.MIN)
    val instant = localDateTime.toInstant(offset)
    return instant.toEpochMilli()
}

fun String.toFullDate(): String {
    val dateTime : ZonedDateTime = OffsetDateTime.parse(this)
        .toZonedDateTime()
        .withZoneSameInstant(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US)
    return dateTime.format(formatter)
}

fun String.mmDdToDate(): Date? {
    val dateString = this
    val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    return format.parse(dateString)
}

fun Date?.formatDateMmDd(): String? {
    return if (this != null) DATE_FORMAT_MM_DD_YYYY.format(time._toLocalDateTime()) else null
}

fun Date?.formatDateDdMmYyyy(): String? {
    return if (this != null) DATE_FORMAT_DD_DD_YYYY.format(time._toLocalDateTime()) else null
}

fun Date.setYearStart(): Calendar {
    val currentTime = Calendar.getInstance().apply {
        time = this@setYearStart
    }
    currentTime.set(Calendar.HOUR_OF_DAY, 0)
    currentTime.set(Calendar.MINUTE, 0)
    currentTime.set(Calendar.SECOND, 0)
    currentTime.set(Calendar.MILLISECOND, 0)
    currentTime.set(Calendar.MONTH, 0)
    currentTime.set(Calendar.DAY_OF_MONTH, 1)
    return currentTime
}

fun Date?.formatDateMmmDd(): String? {
    return if (this != null) DATE_FORMAT_MMM_DD_YYYY.format(time._toLocalDateTime()) else null
}

fun Date?.formatDateDMmmYyyy(): String? {
    return if (this != null) DATE_FORMAT_D_MMM_YYYY.format(time._toLocalDateTime()) else null
}

fun Long.toSeconds(): Long {
    return this / 1000
}

fun Long.minuteToSeconds(): Long {
    return this * 60
}

fun daysOfWeek(): Array<DayOfWeek> {
    return arrayOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )
}

fun LocalDate.toMilliseconds(): Long {
    return toSeconds().toMilliseconds()
}

fun LocalDate.toSeconds(): Long {
    return atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
}

fun String.ddMmDdddToLong(): Long {
    val format = DATE_FORMAT
    val date: LocalDateTime = LocalDateTime.parse(this, format)
    return date.toInstant(ZoneOffset.UTC).toEpochMilli()
}

fun String.dateTimeToSec(): Long {
    return LocalDateTime
        .parse(this, DATETIME_FORMAT)
        .toEpochSecond(ZoneOffset.UTC)
}

fun String.dateToSec(offset: ZoneOffset): Long {
    return LocalDate
        .parse(this, DATE_FORMAT_RFC)
        .atStartOfDay()
        .toEpochSecond(offset)
}

fun Long.toDateMmmmDYyyy(): String = DATE_FORMAT_MMMM_D_YYYY.format(toMilliseconds()._toLocalDateTime())

private fun Long._toLocalDateTime() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US)
private val DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
private val DATE_FORMAT_RFC = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
private val DATE_FORMAT_MM_DD_YYYY = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US)
private val DATE_FORMAT_DD_DD_YYYY = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.US)
private val DATE_FORMAT_MMM_DD_YYYY = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.US)
private val DATE_FORMAT_YYYY_MM_DD = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val DATE_FORMAT_D_MMM_YYYY = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)
private val DATE_FORMAT_MMMM_D_YYYY = DateTimeFormatter.ofPattern("MMMM d yyyy", Locale.US)
private val DATE_FORMAT_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
