package com.yourfitness.common.ui.utils

fun CharSequence.trimLine(): CharSequence{
    var updatedName = this.replace(Regex("\\s{2,}"), " ")
    if (this.isNotEmpty() && updatedName[0].toString() == " ") {
        updatedName = updatedName.removePrefix(" ")
    }
    return updatedName
}

