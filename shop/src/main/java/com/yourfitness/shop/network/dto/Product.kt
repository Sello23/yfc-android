package com.yourfitness.shop.network.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Product(
    @SerializedName("at") val active: Boolean = false,
    @SerializedName("bn") val brandName: String? = null,
    @SerializedName("did") val defaultImageId: String? = null,
    @SerializedName("ids") val images: List<String>? = null,
    @SerializedName("ds") val description: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("in") val info: @RawValue Map<String, Any>? = null,
    @SerializedName("nm") val name: String? = null,
    @SerializedName("rrp") val price: Long? = null,
    @SerializedName("rec") val redeemableCoins: Long? = null,
    @SerializedName("vn") val vendorName: String? = null,
    @SerializedName("ci") val colors: List<ColorData>? = null,
    @SerializedName("lat") val latitude: Double? = null,
    @SerializedName("lnt") val longitude: Double? = null,
    @SerializedName("vid") val vendorImageId: String? = null,
    @SerializedName("va") val vendorAddress: String? = null,
    @SerializedName("sc") val subcategory: String? = null,
    @SerializedName("sl") val stockLevel: String? = null,
    @SerializedName("ua") val updatedAt: Long? = null,
    @SerializedName("gen") val gender: String? = null,
    @SerializedName("bi") val brandImage: String? = null,
) : Parcelable

@Parcelize
data class ColorData(
    @SerializedName("cn") val color: String? = null,
    @SerializedName("cid") val colorId: String? = null,
    @SerializedName("df") val default: Boolean = false,
    @SerializedName("did") val defaultImageId: String? = null,
    @SerializedName("ids") val images: List<String>? = null,
    @SerializedName("szs") val sizes: List<SizeData>? = null,
) : Parcelable

@Parcelize
data class SizeData(
    @SerializedName("sz") val size: String? = null,
    @SerializedName("sid") val sizeId: Int? = null,
    @SerializedName("sl") val stockLevel: String? = null,
    @SerializedName("tp") val type: String? = null,
    @SerializedName("sq") val sequence: Int? = null,
) : Parcelable
