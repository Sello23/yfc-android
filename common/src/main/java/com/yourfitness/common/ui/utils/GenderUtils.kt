package com.yourfitness.common.ui.utils

fun String.toLocalGenderValue(): String {
    return when (lowercase()) {
        "male" -> "Men"
        "female" -> "Women"
        else -> this
    }
}

fun String.fromLocalGenderValue(): String {
    return when (lowercase()) {
        "men" -> "Male"
        "women" -> "Female"
        else -> this
    }
}

fun String.getSortWeight(): Int {
    return when (lowercase()) {
        "men" -> 1
        "women" -> 2
        else -> 3
    }
}
