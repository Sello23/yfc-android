package com.yourfitness.common.domain.date

import timber.log.Timber
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
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

fun date(year: Int, month: Int, day: Int): Date {
    val c = Calendar.getInstance()
    c.set(year, month, day)
    return c.time
}

fun today(): Date {
    return Date()
}

fun todayZonedNull(offset: ZoneOffset?): ZonedDateTime? {
    if (offset == null) return null
    return todayZoned(offset)
}

fun todayZoned(offset: ZoneOffset): ZonedDateTime {
    val zoneId = ZoneId.ofOffset("UTC", offset)
    return ZonedDateTime.now(zoneId)
}

fun ZonedDateTime.utcToZone(offset: ZoneOffset): ZonedDateTime {
    val zoneId = ZoneId.ofOffset("UTC", offset)
    return toLocalDate().atTime(LocalTime.MIN).atZone(zoneId)
}

fun Long.utcToZone(offset: ZoneOffset): ZonedDateTime {
    val zoneId = ZoneId.ofOffset("UTC", offset)
    return LocalDateTime.ofEpochSecond(this / 1000, 0, offset)
        .atZone(zoneId)
        .atDayStart()
}

fun Long.toDateDMmmYyyy(): String = DATE_FORMAT_D_MMM_YYYY.format(toMilliseconds()._toLocalDateTime())

fun Long.toDateDMmmYyyyUtc(): String = DATE_FORMAT_D_MMM_YYYY.format(toMilliseconds()._toUtcDateTime())

fun LocalDate.utcToZoneNull(offset: ZoneOffset?): ZonedDateTime? {
    if (offset == null) return null
    return this.utcToZone(offset)
}

fun LocalDate.utcToZone(offset: ZoneOffset): ZonedDateTime {
    val zoneId = ZoneId.ofOffset("UTC", offset)
    return this.atTime(LocalTime.MIN).atZone(zoneId)
}

fun ZonedDateTime.atDayStart(): ZonedDateTime = with(LocalTime.of(0, 0, 0))

fun ZonedDateTime.atDayEnd(): ZonedDateTime = with(LocalTime.of(23, 59, 59))

val ZonedDateTime.milliseconds
    get() = toInstant().toEpochMilli()

fun todayLocal(): LocalDate {
    return LocalDate.now()
}

fun todayLocal(offset: ZoneOffset): LocalDate {
    val zoneId = ZoneId.ofOffset("UTC", offset)
    return LocalDate.now(zoneId)
}

fun Long.toMilliseconds(): Long {
    return this * 1000
}

fun Long.toSeconds(): Long {
    return this / 1000
}

fun Int.secToMinutes(): Int {
    return this / 60
}

fun Date.minusYears(amount: Int): Date {
    val calendar = this.toCalendar()
    calendar.add(Calendar.YEAR, amount)
    return calendar.toDate()
}

fun Long.toDay(): String = DAY.format(_toLocalDateTime())

fun Long.toDMmmmYyyy(): String = DATE_FORMAT_D_MMMM_YYYY.format(_toLocalDateTime())

fun Long.toMmmmYyyy(): String = DATE_FORMAT_MMMM_YYYY.format(_toLocalDateTime())

fun Long.toDd(): String = DATE_FORMAT_DD.format(toMilliseconds()._toLocalDateTime())

fun Long.toEee(): String = DATE_FORMAT_EEE.format(toMilliseconds()._toLocalDateTime())

fun Long.toDdMs(): String = DATE_FORMAT_DD.format(_toLocalDateTime())

fun Long.toEeeMs(): String = DATE_FORMAT_EEE.format(_toLocalDateTime())

fun Date?.formatEeeeMmmDd(): String? {
    return if (this != null) DATE_FORMAT_EEEE_MM_DD.format(time._toLocalDateTime()) else null
}

fun Date.addDays(amount: Int): Date {
    val calendar = this.toCalendar()
    calendar.add(Calendar.DATE, amount)
    return calendar.toDate()
}

fun Date.addMs(amount: Int): Date {
    val calendar = this.toCalendar()
    calendar.add(Calendar.MILLISECOND, amount)
    return calendar.toDate()
}

fun Calendar.toDate(): Date {
    return this.time
}

fun Long?.toDate(): Date? {
    return this?.toDate()
}

fun Long.toDate(): Date {
    return Date(this)
}

fun Date.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar
}

fun Date.formatDateToISO(): String {
    val currentTime = this.toCalendar().apply {
        set(Calendar.MILLISECOND, 0)
    }
    return DATE_FORMAT_ISO.format(currentTime.timeInMillis._toLocalDateTime())
}

fun Date.year(): Int {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar.get(Calendar.YEAR)
}

fun month(): Int = Calendar.getInstance().get(Calendar.MONTH)

fun Date.formatted(): String = DATE_FORMAT_MMMM_D_YYYY.format(time._toLocalDateTime())

fun Long.formatted(): String = DATE_FORMAT_MMMM_D_YYYY.format(LocalDateTime.ofEpochSecond(this, 0, ZoneOffset.UTC))

fun getNowUtcFormatted(): String {
    return Calendar.getInstance().time.getNowUtcFormatted()
}

fun now(): Long {
    return Calendar.getInstance().timeInMillis
}

fun Date.sameDay(date: Date): Boolean {
    val c1 = Calendar.getInstance().apply { time = this@sameDay }
    val c2 = Calendar.getInstance().apply { time = date }
    return c1[Calendar.YEAR] == c2[Calendar.YEAR] && c1[Calendar.DAY_OF_YEAR] == c2[Calendar.DAY_OF_YEAR]
}

fun ZonedDateTime.sameDay(date: ZonedDateTime): Boolean {
    return year == date.year && dayOfYear == date.dayOfYear
}

fun Date.getNowUtcFormatted(): String {
    val defaultTimeZone = TimeZone.getDefault()
    TimeZone.setDefault(TimeZone.getTimeZone("GMT"))
    val time = this.formatDateToISO()
    TimeZone.setDefault(defaultTimeZone)
    return time
}

fun ZonedDateTime.toISO(): String = DATE_FORMAT_ISO.withZone(ZoneId.of("GMT")).format(this)

fun Date.setDayStartTimeGMT(): Calendar {
    val currentTime = Calendar.getInstance().apply {
        time = this@setDayStartTimeGMT
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        timeZone = TimeZone.getTimeZone("GMT")
    }
    return currentTime
}

fun Date.setDayStartTime(): Calendar {
    val currentTime = Calendar.getInstance().apply {
        time = this@setDayStartTime
    }
    currentTime.set(Calendar.HOUR_OF_DAY, 0)
    currentTime.set(Calendar.MINUTE, 0)
    currentTime.set(Calendar.SECOND, 0)
    currentTime.set(Calendar.MILLISECOND, 0)
    return currentTime
}

fun Date.setDayEndTime(): Calendar {
    val currentTime = Calendar.getInstance().apply {
        time = this@setDayEndTime
    }
    currentTime.set(Calendar.HOUR_OF_DAY, 23)
    currentTime.set(Calendar.MINUTE, 59)
    currentTime.set(Calendar.SECOND, 59)
    currentTime.set(Calendar.MILLISECOND, 0)
    return currentTime
}

fun Date.setDayStart(): Date {
    return setDayStartTime().time
}

fun Date.setDayEnd(): Date {
    return setDayEndTime().time
}

fun LocalDate.setMonthStartTime(): Calendar {
    val initTime = this.toDate()
    val currentTime = Calendar.getInstance().apply {
        time = initTime
    }
    currentTime.set(Calendar.DAY_OF_MONTH, 1)
    currentTime.set(Calendar.HOUR_OF_DAY, 0)
    currentTime.set(Calendar.MINUTE, 0)
    currentTime.set(Calendar.SECOND, 0)
    currentTime.set(Calendar.MILLISECOND, 0)
    return currentTime
}

fun LocalDate.setMonthEndTime(): Calendar {
    val initTime = this.toDate()
    val currentTime = Calendar.getInstance().apply {
        time = initTime
    }
    currentTime.set(Calendar.DAY_OF_MONTH, lengthOfMonth())
    currentTime.set(Calendar.HOUR_OF_DAY, 23)
    currentTime.set(Calendar.MINUTE, 59)
    currentTime.set(Calendar.SECOND, 59)
    currentTime.set(Calendar.MILLISECOND, 0)
    return currentTime
}

fun LocalDate.getWeekNumber(): Int {
    return this[WeekFields.of(Locale.US).weekOfYear()]
}


fun LocalDate.setMonthStart(): Date {
    return setMonthStartTime().time
}

fun LocalDate.setMonthEnd(): Date {
    return setMonthEndTime().time
}

fun Date.subtractMonths(amount: Int): Date {
    val calendar = this.toCalendar()
    calendar.add(Calendar.YEAR, -amount)
    return calendar.time
}

fun Date.timeFormatted(): String {
    return DateTimeFormatter.ofPattern(FORMAT_TIME, Locale.US).format(time._toLocalDateTime())
}

fun Date.dayOfWeekFormatted(): String {
    return DateTimeFormatter.ofPattern(FORMAT_DAY_OF_WEEK, Locale.US).format(time._toLocalDateTime())
}

fun LocalDate.toDate(): Date = Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())

fun Date.toLocalDate(): LocalDate = toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

enum class TimeDifference(val timeDifference: Int) {
    SECOND(0), MINUTE(1), HOUR(2), DAY(3), MONTH(4), YEAR(5);
}

fun dateTimeDifference(currentTime: Long, previousTime: Long, timeDifference: TimeDifference): Long {
    return when (timeDifference.timeDifference) {
        0 -> (currentTime - previousTime) / MILLISECONDS
        1 -> (currentTime - previousTime) / MILLISECONDS / SECONDS
        2 -> (currentTime - previousTime) / MILLISECONDS / SECONDS / MINUTES
        3 -> (currentTime - previousTime) / MILLISECONDS / SECONDS / MINUTES / HOUR
        4 -> (currentTime - previousTime) / MILLISECONDS / SECONDS / MINUTES / HOUR / MONTH
        else -> (currentTime - previousTime) / MILLISECONDS / SECONDS / MINUTES / HOUR / MONTH / YEAR
    }
}

fun Long.toDateDayOfWeekMonth(): String = DATE_FORMAT_DDD_MMMM_DD.format(toMilliseconds()._toLocalDateTime())

fun Long.toDateDayOfWeekMonthMs(): String = DATE_FORMAT_DDD_MMMM_DD.format(_toLocalDateTime())

fun Long.toDateTime(): String = DATE_FORMAT_HH_MM.format(toMilliseconds()._toLocalDateTime())

fun Long.toDateTimeMs(): String {
    val format = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    return format.format(_toLocalDateTime())
}

private fun Long._toLocalDateTime() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

private fun Long._toUtcDateTime() = Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC"))

fun Long?.sameMonthAs(date: Long?): Boolean {
    return this?.toDate()?.toLocalDate()?.monthValue == date?.toDate()?.toLocalDate()?.monthValue
}

fun Long.toCustomFormat(): String = DATE_FORMAT_CUSTOM.format(_toLocalDateTime())

fun Long.toDateTimeUtc0(): String {
    val format = SimpleDateFormat("HH:mm", Locale.US)
    format.timeZone = TimeZone.getTimeZone("UTC");
    return format.format(Date(this.toMilliseconds()))
}

fun nowTimeValues() : Pair<Int, Int> {
    val c = Calendar.getInstance()
    return c[Calendar.HOUR_OF_DAY] * 60 + c[Calendar.MINUTE] to c[Calendar.DAY_OF_WEEK]
}

fun Date.dateTimeValues() : Pair<Int, Int> {
    val c = Calendar.getInstance()
    c.time = this
    return c[Calendar.HOUR_OF_DAY] * 60 + c[Calendar.MINUTE] to c[Calendar.DAY_OF_WEEK]
}

fun Long.minutesOfDay() : Int {
    val c = Calendar.getInstance().apply {
        timeZone = TimeZone.getTimeZone("GMT")
        time = toMilliseconds().toDate()
    }
    return c[Calendar.HOUR_OF_DAY] * 60 + c[Calendar.MINUTE]
}

fun Long.getMillisTimeZoneConverted(offset: ZoneOffset, dayPeriod: LocalTime = LocalTime.MIN): Long { // Input: seconds
    Timber.tag("issue").e(">>> BEFORE = ${this.toMilliseconds()}  => ${this.toMilliseconds().toDate()}")

    val c = LocalDateTime.ofEpochSecond(this, 0, offset)
        .toLocalDate()
        .atTime(dayPeriod)
        .toInstant(offset)
        .toEpochMilli()

    Timber.tag("issue").e(">>> AFTER = $c  => ${c.toDate()}")
    return c
}

fun Long.toISOString(offset: ZoneOffset): String { // Input: seconds
    val instant = Instant.ofEpochSecond(this)
    return ZonedDateTime.ofInstant(instant, offset)
        .with(LocalTime.MIN)
        .withZoneSameInstant(ZoneOffset.UTC)
        .format(DateTimeFormatter.ISO_INSTANT)
}

fun OffsetDateTime.toSpikeString(): String {
    return DATE_FORMAT_SPIKE.format(this)
}

private fun Long.toISOStringBase(offset: ZoneOffset, formatter: DateTimeFormatter): String { // Input: seconds
    Timber.tag("issue").e(">>> CONVERT ${this.toMilliseconds()}  => ${this.toMilliseconds().toDate()}")

    val zoneId = ZoneId.ofOffset("UTC", offset)

    val c = LocalDateTime.ofEpochSecond(this, 0, offset)
        .toLocalDate()
        .atTime(LocalTime.MIN)
        .atZone(zoneId)

    val f = formatter.withZone(ZoneId.of("GMT")).format(c)
    Timber.tag("issue").e(">>> CONVERTED = $f")
    return f
}

fun Long.toTimeZone(offset: ZoneOffset): String {
    val zoneId = ZoneId.ofOffset("UTC", offset)

    val c = LocalDateTime.ofEpochSecond(this / 1000, 0, offset)
        .toLocalDate()
        .atTime(LocalTime.MIN)
    val date = ZonedDateTime.of(c, zoneId)
    return DATE_FORMAT_TIME_ZONE.withZone(ZoneId.of("GMT")).format(date)
}

fun Long.toTimeZonTest(offset: ZoneOffset): String {
    val zoneId = ZoneId.ofOffset("UTC", offset)
    val date = ZonedDateTime.of(LocalDateTime.ofEpochSecond(this / 1000, 0, offset), zoneId)
    return DATE_FORMAT_TIME_ZONE_TEST.format(date)
}

private const val MILLISECONDS = 1000
private const val SECONDS = 60
private const val MINUTES = 60
private const val HOUR = 24
private const val YEAR = 365
private const val MONTH = 30

private const val FORMAT_SPIKE = "yyyy-MM-dd"
private val DATE_FORMAT_SPIKE = DateTimeFormatter.ofPattern(FORMAT_SPIKE, Locale.US)
private const val FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss'Z'"
private val DATE_FORMAT_ISO = DateTimeFormatter.ofPattern(FORMAT_ISO, Locale.US)
private val DATE_FORMAT_MMMM_D_YYYY = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)
private const val FORMAT_TIME = "HH:mm"
private const val FORMAT_DAY_OF_WEEK = "EEE, MMM d"
private val DAY = DateTimeFormatter.ofPattern("d", Locale.US)
private val DATE_FORMAT_EEEE_MM_DD = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.US)
private val DATE_FORMAT_D_MMMM_YYYY = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)
private val DATE_FORMAT_MMMM_YYYY = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
private val DATE_FORMAT_DD = DateTimeFormatter.ofPattern("dd", Locale.US)
private val DATE_FORMAT_EEE = DateTimeFormatter.ofPattern("EEE", Locale.US)
private val DATE_FORMAT_HH_MM = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
private val DATE_FORMAT_DDD_MMMM_DD = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US)
private val DATE_FORMAT_CUSTOM = DateTimeFormatter.ofPattern("MMMM dd yyyy '('EEEE')'", Locale.US)
private val DATE_FORMAT_TIME_ZONE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US)
private val DATE_FORMAT_D_MMM_YYYY = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)
private val DATE_FORMAT_TIME_ZONE_TEST = DateTimeFormatter.ofPattern("yyyy MMM dd  HH:mm:ss zzzZZZZZ", Locale.US)

const val DAY_DURATION_MS = 24 * 60 * 60 * 1000
const val DAY_END_MINUTES = 24 * 60

val weekDays : Map<Int, String> = mapOf(
    Calendar.SUNDAY to "Sunday",
    Calendar.MONDAY to "Monday",
    Calendar.TUESDAY to "Tuesday",
    Calendar.WEDNESDAY to "Wednesday",
    Calendar.THURSDAY to "Thursday",
    Calendar.FRIDAY to "Friday",
    Calendar.SATURDAY to "Saturday",
)
