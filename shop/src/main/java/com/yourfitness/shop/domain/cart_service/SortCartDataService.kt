package com.yourfitness.shop.domain.cart_service

import com.yourfitness.shop.data.entity.ProductDataEntity
import javax.inject.Inject

enum class SortType {
    LOW_TO_HIGH,
    HIGH_TO_LOW
}

class SortCartDataService @Inject constructor() {
    fun sortByPrice(data: List<ProductDataEntity>, type: SortType?): List<ProductDataEntity> {
        if (type == null) return data
        val sortedData = data.sortedBy {
            it.product.price
        }
        return if (type == SortType.LOW_TO_HIGH) sortedData else sortedData.reversed()
    }
}
