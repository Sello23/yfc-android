package com.yourfitness.common.domain.models

import com.yourfitness.common.domain.models.PaymentMethod

data class PaymentOption(
    val method: PaymentMethod,
    val iconResId: Int,
    val title: String,
    val subtitle: String,
    var isSelected: Boolean,
)