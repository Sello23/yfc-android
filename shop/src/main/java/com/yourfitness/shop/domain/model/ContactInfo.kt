package com.yourfitness.shop.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactInfo(
    val fullName: String,
    val email: String,
    val phoneNumber: String
) : Parcelable

@Parcelize
data class AddressInfo(
    val city: String,
    val street: String,
    val addressDetails: String,
) : Parcelable

fun AddressInfo?.toInfoString(cityLabel: String, streetLabel: String, detailsLabel: String): String {
    var res = ""
    if (this == null) return res
    if (detailsLabel.isNotBlank()) res = "$detailsLabel: $addressDetails"
    return "$cityLabel: $city $streetLabel: $street $res".trim()
}

