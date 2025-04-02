package com.yourfitness.coach.domain.models

import com.yourfitness.coach.R
import com.yourfitness.coach.domain.date.toFullDate
import com.yourfitness.coach.network.dto.PaymentHistory

sealed class ViewItem(val resource: Int) {
    class HeaderItem(val date: Long?, val isYear: Boolean = true) : ViewItem(com.yourfitness.common.R.layout.item_payment_history_header)
    class Item(
        val created: String?,
        val title: String?,
        val amount: Int?,
        val boughtNumber: Int?,
        val paymentIntent: String?,
        val currency: String,
        val duration: Int?,
    ) :
        ViewItem(R.layout.item_payment_history)
}

fun PaymentHistory.toViewItem(currency: String): ViewItem.Item {
    return ViewItem.Item(
        created = this.time?.toFullDate(),
        title = this.paymentDestination,
        amount = this.amount,
        boughtNumber = this.paymentDestination?.getAmountValue(this),
        paymentIntent = this.paymentIntent,
        currency = currency,
        duration = this.duration,
    )
}

fun String.getAmountValue(payment: PaymentHistory): Int? {
    return if (this == PaymentItems.CREDITS.value) payment.creditsAmount
    else if (this == PaymentItems.PT_SESSIONS.value) payment.ptSessionsAmount
    else null
}

enum class PaymentItems(val value: String) {
    MEMBERSHIP("membership"),
    PT_SESSIONS("personalTrainerSessions"),
    CREDITS("credits"),
    ONE_TIME("oneTimeSubscription"),
}