package com.yourfitness.common.domain.models

data class PaymentData(
    val card: CreditCard? = null,
    val cardId: String? = null,
    val clientSecret: String
)