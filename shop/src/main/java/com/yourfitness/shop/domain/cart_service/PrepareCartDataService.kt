package com.yourfitness.shop.domain.cart_service

import com.yourfitness.common.ui.utils.toCoins
import com.yourfitness.common.ui.utils.toCurrencyRounded
import com.yourfitness.shop.data.entity.CartDataEntity
import javax.inject.Inject
import kotlin.math.max

class PrepareCartDataService @Inject constructor() {
    // Coins equivalent price
    fun getOverallPrice(data: List<CartDataEntity>, coinsValue: Double): Long {
        return data.sumOf {
            getItemPricePart(it, coinsValue) * it.cartItem.amount
        }
    }

    // Price without coins usage
    fun getWholePrice(data: List<CartDataEntity>): Long {
        return data.sumOf { it.product.product.price * it.cartItem.amount }
    }

    // Price with coins usage
    fun getPriceWithoutCoins(data: List<CartDataEntity>, coinsValue: Double): Long {
        return data.sumOf {
            (it.product.product.price - getItemPricePart(it, coinsValue)) * it.cartItem.amount
        }
    }

    // All used coins sum
    fun getOverallCoins(data: List<CartDataEntity>): Long {
        return data.sumOf { it.cartItem.coinsPart * it.cartItem.amount }
    }

    fun getItemPricePart(item: CartDataEntity, coinsValue: Double): Long {
        val coins = item.cartItem.coinsPart
        // TODO check rounding
        val coinsPrice = (coinsValue * coins).toCurrencyRounded().toCoins()
        return max(0L, coinsPrice)
    }

    fun getItemPriceWithoutCoins(item: CartDataEntity, coinsValue: Double): Long {
        val price = (item.product.product.price - getItemPricePart(item, coinsValue)) * item.cartItem.amount
        return max(0L, price)
    }

    fun getCoverImage(item: CartDataEntity): String {
        return item.product
            .colors.find { it.itemColor.color == item.cartItem.color }
            ?.images?.first()
            ?.image ?: item.product.product.defaultImageId
    }

    fun getColorImage(item: CartDataEntity): String {
        return item.product
            .colors.find { it.itemColor.color == item.cartItem.color }
            ?.itemColor?.defaultImageId.orEmpty()
    }

    fun getColor(item: CartDataEntity): String {
        return item.product
            .colors.find { it.itemColor.color == item.cartItem.color }
            ?.itemColor?.color.orEmpty()
    }
}
