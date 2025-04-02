package com.yourfitness.pt.ui.utils

import android.content.Context
import androidx.annotation.StringRes
import com.yourfitness.pt.R
import com.yourfitness.common.R as common
import com.yourfitness.pt.domain.models.CardSettings
import com.yourfitness.pt.domain.models.StatusFilter

val statusDataMap = mutableMapOf(
    SessionStatus.REQUESTED.value to CardSettings(
        bgColor = R.color.color_status_requested,
        status = R.string.status_requested,
    ),
    SessionStatus.BOOKED.value to CardSettings(
        bgColor = R.color.color_status_booked,
        status = R.string.status_booked,
        statusIcon = R.drawable.ic_status_settings,
    ),
    SessionStatus.COMPLETED.value to CardSettings(
        bgColor = R.color.color_status_completed,
        status = R.string.status_completed,
        statusIcon = common.drawable.ic_status_attantion,
        statusInfo = R.string.status_info,
        isLight = false
    ),
    SessionStatus.CONFIRMED.value to CardSettings(
        bgColor = R.color.color_status_confirmed,
        status = R.string.status_confirmed,
        statusIcon = R.drawable.ic_status_smile,
    ),
    SessionStatus.CANCELLATION_REQUESTED.value to CardSettings(
        bgColor = R.color.color_status_cancellation_requested,
        status = R.string.status_cancellation_requested,
    ),
    SessionStatus.CHARGED.value to CardSettings(
        bgColor = R.color.color_status_charged,
        status = R.string.status_charged,
    ),
    SessionStatus.BLOCKED_SLOT.value to CardSettings(
        bgColor = R.color.color_status_blocked_slot,
        status = R.string.status_blocked,
        isLight = false
    )
)

enum class SessionStatus(val value: String) {
    REQUESTED("requested"),
    BOOKED("booked"),
    COMPLETED("completed"),
    CONFIRMED("confirmed"),
    CANCELLATION_REQUESTED("cancellation_requested"),
    CHARGED("charged"),
    TEMPORARY("temporary"),
    PT("pt"),
    CANCEL("canceled"),
    BLOCKED_SLOT("blocked_slot")
}


fun getLocalisedStatus(status: String, context: Context): String {
    val stringId = statusDataMap[status]?.status ?: return ""
    return context.getString(stringId)
}

fun String.getLocalisedAction(ptRole: Boolean): Pair<Int, Int>? {
    val stringRes =  when (this) {
        SessionStatus.REQUESTED.value, SessionStatus.CANCELLATION_REQUESTED.value ->
            if (ptRole) R.string.manage_request else null
        SessionStatus.BOOKED.value -> if (ptRole) R.string.cancel_session else null /*R.string.cancel_session*/
        SessionStatus.COMPLETED.value -> if (ptRole) R.string.cancel_session else R.string.confirm_training
        else -> null
    }

    val colorRes = if ((ptRole && (this == SessionStatus.COMPLETED.value || this == SessionStatus.BOOKED.value)) ||
        (!ptRole && this == SessionStatus.BOOKED.value)) {
        com.yourfitness.common.R.color.issue_red
    } else {
        com.yourfitness.common.R.color.main_active
    }

    return if (stringRes == null) null else stringRes to colorRes
}

fun String.getStatusBg(): Int? {
    return statusDataMap[this]?.bgColor
}

fun getPtLocalisedLabel(context: Context): String {
    return context.getString(R.string.personal_trainer)
}

fun getStatusDataList(): List<Pair<String, Int>> {
    val list = mutableListOf(StatusFilter.EMPTY_STATE to R.string.all_label)
    list.addAll(statusDataMap
        .filter {
            it.key != SessionStatus.CANCELLATION_REQUESTED.value &&
                    it.key != SessionStatus.CHARGED.value
        }
        .mapNotNull {
            if (it.key == SessionStatus.REQUESTED.value) it.key to R.string.new_label
            else it.key to it.value.status
        })
    return list
}
