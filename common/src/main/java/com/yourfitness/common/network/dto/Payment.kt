package com.yourfitness.common.network.dto

import com.google.gson.annotations.SerializedName

data class PaymentReceipt(
    @SerializedName("receipt") val receipt: String? = null,
)

data class PackagePaymentRequest(
    @SerializedName("package") val data: PackageRequest
)

data class PackageRequest(
    @SerializedName("active") val active: Boolean,
    @SerializedName("cost") val cost: Long,
    @SerializedName("credits") val credits: Int,
    @SerializedName("name") val name: String
)

data class PackagePaymentResponse(
    @SerializedName("amount") val amount: Int,
    @SerializedName("clientSecret") val clientSecret: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("description") val description: String
)

data class CreditCardResponse(
    @SerializedName("Details") val details: List<CreditCardDetailsResponse>,
)

data class CreditCardDetailsResponse(
    @SerializedName("id") val id: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("expMonth") val expMonth: Int,
    @SerializedName("expYear") val expYear: Int,
    @SerializedName("funding") val funding: String,
    @SerializedName("lastDigits") val lastDigits: String,
)