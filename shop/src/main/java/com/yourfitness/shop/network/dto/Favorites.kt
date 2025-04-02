package com.yourfitness.shop.network.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Favorites(
    @SerializedName("productId") val favorites: List<String> = listOf(),
) : Parcelable