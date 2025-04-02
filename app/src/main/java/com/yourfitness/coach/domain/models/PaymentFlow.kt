package com.yourfitness.coach.domain.models

enum class PaymentFlow {
    BUY_SUBSCRIPTION_FROM_FACILITY,
    BUY_SUBSCRIPTION_FROM_PROFILE,
    BUY_SUBSCRIPTION_FROM_PROGRESS,
    BUY_SUBSCRIPTION_FROM_MAP,
    BUY_SUBSCRIPTION_FROM_SIGN_UP,
    BUY_CREDITS_FROM_PROFILE,
    BUY_CREDITS_FROM_SCHEDULE,
    BUY_CREDITS_FROM_PROGRESS,
    BUY_CREDITS_FROM_MAP;

    val isCreditsFlow: Boolean by lazy {
        this in listOf(
            BUY_CREDITS_FROM_PROFILE,
            BUY_CREDITS_FROM_SCHEDULE,
            BUY_CREDITS_FROM_PROGRESS,
            BUY_CREDITS_FROM_MAP
        )
    }
    val isSubscriptionFlow: Boolean by lazy {
        this in listOf(
            BUY_SUBSCRIPTION_FROM_PROFILE,
            BUY_SUBSCRIPTION_FROM_FACILITY,
            BUY_SUBSCRIPTION_FROM_PROGRESS,
            BUY_SUBSCRIPTION_FROM_MAP,
            BUY_SUBSCRIPTION_FROM_SIGN_UP
        )
    }
}