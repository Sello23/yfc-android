package com.yourfitness.common.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CreditCard(
    var cardNumber: String = "",
    var cardHolder: String = "",
    var expMonth: Int = 0,
    var expYear: Int = 0,
    var cvv: String = ""
) : Parcelable {

    val cardNumberOnly4DigitsVisible: String
        get() = "**** **** **** ${cardNumber.substring(cardNumber.length - 4, cardNumber.length)}"

    val expDate: String
        get() {
            if (expMonth == 0 || expYear == 0) {
                return ""
            }
            return "$expMonth/${expYear - 2000}"
        }
}