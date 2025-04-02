package com.yourfitness.common.domain.models

import android.os.Parcelable
import com.yourfitness.common.network.dto.CreditCardDetailsResponse
import com.yourfitness.common.network.dto.Packages
import com.yourfitness.common.network.dto.PtPackages
import kotlinx.parcelize.Parcelize

@Parcelize
data class BuyOptionData(
    val plan: String,
    val optionAmount: Int,
    val price: Int,
    val currency: String,
    var isSelected: Boolean = false,
    val amountString: String = "",
    val active: Boolean,
) : Parcelable

fun Packages.toCreditData(currency: String): BuyOptionData {
    return BuyOptionData(
        plan = name.orEmpty(),
        optionAmount = credits ?: 0,
        price = cost ?: 0,
        currency = currency,
        active = active ?: false
    )
}

fun PtPackages.toSessionData(currency: String): BuyOptionData {
    return BuyOptionData(
        plan = name.orEmpty(),
        optionAmount = sessions ?: 0,
        price = cost ?: 0,
        currency = currency,
        active = active ?: false
    )
}

fun BuyOptionData.toRequestData(): PtPackages {
    return PtPackages(
        active = active,
        cost = price,
        sessions = optionAmount,
        name = plan
    )
}

data class SavedCreditCard(
    val id: String,
    val brand: String,
    val expMonth: Int,
    val expYear: Int,
    val funding: String,
    val lastDigits: String,
)

fun CreditCardDetailsResponse.toSavedCreditCard(): SavedCreditCard {
    return SavedCreditCard(
        id = id,
        brand = brand,
        expMonth = expMonth,
        expYear = expYear,
        funding = funding,
        lastDigits = lastDigits
    )
}