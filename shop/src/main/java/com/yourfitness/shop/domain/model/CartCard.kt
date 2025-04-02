package com.yourfitness.shop.domain.model

abstract class BaseCard()

data class CartCard(
    val uuid: String,
    val title: String,
    val subtitle: String,
    val currency: String,
    val discountPrice: Long,
    val wholePrice: Long,
    val coinsAmount: Long,
    val coinsPriceEquivalent: Long,
    val maxCoins: Long = 0,
    val coverImage: String,
    val color: String,
    val size: String,
    val sizeType: String,
    val status: Int = -1,
    val statusText: String = "",
) : BaseCard()
