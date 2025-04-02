package com.yourfitness.shop.network.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VoucherResponse(
    @SerializedName("clientBirthday") val clientBirthday: Long,
    @SerializedName("clientImage") val clientImage: String,
    @SerializedName("clientName") val clientName: String,
    @SerializedName("id") val id: String,
    @SerializedName("number") val number: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("vendorAddress") val vendorAddress: String,
    @SerializedName("vendorDefaultImageId") val vendorDefaultImageId: String,
    @SerializedName("vendorName") val vendorName: String,
    @SerializedName("vendorPhone") val vendorPhone: String
) : Parcelable