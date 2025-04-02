package com.yourfitness.coach

object BannerState {

    var isSubscriptionBannerClose = false
    var isCreditsBannerClose = false

    fun closeSubscriptionBanner() {
        isSubscriptionBannerClose = true
    }

    fun closeCreditsBanner() {
        isCreditsBannerClose = true
    }

    fun setDefaultValue() {
        isSubscriptionBannerClose = false
        isCreditsBannerClose = false
    }
}