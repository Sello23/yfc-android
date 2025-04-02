package com.yourfitness.common.domain.models

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkTimeData(
    @StringRes val textId: Int,
    val args: String?,
    @DrawableRes val bgId: Int,
    @DrawableRes val iconId: Int,
    val isAccessible: Boolean = false
) : Parcelable

@Parcelize
data class WorkTimeDto(
    @SerializedName("f") var from: Long? = null,
    @SerializedName("t") var to: Long? = null,
    @SerializedName("w") var weekDay: String? = null
) : Parcelable