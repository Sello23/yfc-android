package com.yourfitness.coach.domain.fb_remote_config.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Dubai30x30Restrictions(
    @SerializedName("profileRegisteredAfter") val registeredAfter: Long = 0,
    @SerializedName("restrictionStart") val start: Long = 0,
    @SerializedName("restrictionEnd") val end: Long = 0,
    @SerializedName("ids") val gymIds: List<String> = emptyList(),
) : Parcelable
