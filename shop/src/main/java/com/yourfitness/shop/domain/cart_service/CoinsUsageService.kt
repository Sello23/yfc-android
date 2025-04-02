package com.yourfitness.shop.domain.cart_service

import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.shop.domain.orders.CartRepository
import javax.inject.Inject
import kotlin.math.max

class CoinsUsageService @Inject constructor(
    private val commonStorage: CommonPreferencesStorage,
    private val cartRepository: CartRepository,
    private val cartService: PrepareCartDataService,
) {
    suspend fun notUsedCoins(): Long {
        val cartItems = cartRepository.readCartItemsWithData()
        val usedInCartCoins = cartService.getOverallCoins(cartItems)
        val coinBalance = commonStorage.availableCoins
        return max(coinBalance - usedInCartCoins, 0L)
    }
}
