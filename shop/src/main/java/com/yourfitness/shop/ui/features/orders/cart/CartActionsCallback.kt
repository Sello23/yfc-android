package com.yourfitness.shop.ui.features.orders.cart

import com.yourfitness.shop.domain.model.CartCard

interface CartActionsCallback {
    fun onCoinsPartChangeTapped(item: CartCard)
}