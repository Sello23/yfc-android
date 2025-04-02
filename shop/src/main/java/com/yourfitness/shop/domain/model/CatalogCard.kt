package com.yourfitness.shop.domain.model

import android.net.Uri

data class CatalogCard(
    val dbId: Int,
    val id: String,
    val title: String,
    val subtitle: String,
    val image: Uri,
    val price: String,
    val priceCoinsPart: Long,
    val discountPrice: String,
    val isFavorite: Boolean,
    val distance: String,
    val vendorImageId: Uri?,
    val vendorAddress: String?
)
