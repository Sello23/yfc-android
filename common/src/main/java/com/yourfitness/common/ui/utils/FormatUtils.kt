package com.yourfitness.common.ui.utils

import com.yourfitness.common.domain.date.toLocalDate
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.domain.date.todayLocal
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.time.Period
import java.util.*
import kotlin.math.ln
import kotlin.math.pow

fun Number.formatAmount(currency: String = "", round: Boolean = false): String {
    val decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    val decimalFormat =
        if (round) DecimalFormat("#,###", decimalFormatSymbols)
        else DecimalFormat("#,###.##", decimalFormatSymbols)
    var value = decimalFormat.format(this.toDouble() / 100)
    val parts = value.split(".")
    if (parts.size == 2) {
       value = value.padEnd(parts.first().length + 3, '0')
    }
    if (currency.isBlank()) {
        return value
    }
    return "$currency $value"
}

fun Long.formatCoins(): String {
    val decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    val decimalFormat = DecimalFormat("#,###", decimalFormatSymbols)
    return decimalFormat.format(this)
}

fun Double.roundTo(n: Int): Double {
    return try {
        "%.${n}f".format(Locale.US, this).toDouble()
    } catch (e: Exception) {
        Timber.e(e)
        0.0
    }
}

fun doubleToStringNoDecimal(number: Double): String? {
    val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
    formatter.applyPattern("#,###")
    return formatter.format(number)
}

fun doubleToStringNoDecimal(number: Number): String? {
    val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
    formatter.applyPattern("#,###")
    return formatter.format(number)
}

val Int.stringCommaSeparated get(): String? = doubleToStringNoDecimal(toDouble())


fun Long.toStringNoDecimal(): String =  doubleToStringNoDecimal(this.toDouble()).orEmpty()

fun Int?.toStringNoDecimal(): String =  doubleToStringNoDecimal(this?.toDouble() ?: 0.0).orEmpty()

fun Double.formatTwoDecimalSigns(): String? {
    val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
    formatter.applyPattern("###.##")
    return formatter.format(this)
}

fun Long.formatProgressAmount(): String {
    val suffixes = listOf("k", "mln", "bln")
    if (this < 1000) return toString()
    val exp = (ln(this.toDouble()) / ln(1000.0)).toInt()
    val resultVal = (this / 1000.0.pow(exp.toDouble()))
    val resultData = if ((resultVal * 10).toInt() % 10 == 0) {
        "%d%s" to resultVal.toInt()
    } else {
        "%.1f%s" to resultVal
    }
    return String.format(
        resultData.first,
        resultData.second,
        suffixes[exp - 1]
    )
}

fun Long.roundNoCoins(): Long = (this / 100) * 100

fun Long.toCoins(): Long = this * 100

fun Double.toCoins(): Long = (this * 100).toLong()

fun Double.toCurrencyRounded(): Double {
    return roundTo(2)
}

fun Long.toAge(): Int {
    return Period.between(
        Date(this.toMilliseconds()).toLocalDate(),
        todayLocal()
    ).years
}
