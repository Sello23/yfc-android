package com.yourfitness.shop.domain.products

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductFilters(
    val minPrice: Long = 0,
    val maxPrice: Long = PRICE_NO_LIMIT,
    val defaultMinPrice: Long = 0,
    val defaultMaxPrice: Long = PRICE_NO_LIMIT,
    val allTypes: List<String> = emptyList(),
    val types: List<String> = mutableListOf(),
    val allBrands: List<Pair<String, String?>> = emptyList(),
    val brands: List<String> = mutableListOf(),
    val allGenders: List<String> = mutableListOf(),
    val genders: List<String> = mutableListOf(),
) : Parcelable

const val PRICE_NO_LIMIT: Long = Long.MAX_VALUE


fun ProductFilters.isPriceRangeEmpty(): Boolean = defaultMinPrice == 0L && defaultMaxPrice == PRICE_NO_LIMIT
fun ProductFilters.isEmpty(): Boolean = defaultMinPrice == minPrice && defaultMaxPrice == maxPrice &&
        types.isEmpty() && brands.isEmpty() && genders.isEmpty()

fun ProductFilters.reset(): ProductFilters {
    return this.copy(
        minPrice = defaultMinPrice,
        maxPrice = defaultMaxPrice,
        types = mutableListOf(),
        brands = mutableListOf(),
        genders = mutableListOf(),
    )
}
