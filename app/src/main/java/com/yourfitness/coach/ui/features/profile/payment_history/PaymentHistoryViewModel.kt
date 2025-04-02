package com.yourfitness.coach.ui.features.profile.payment_history

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.date.mmDdToDate
import com.yourfitness.coach.domain.date.setYearStart
import com.yourfitness.coach.domain.date.toDate
import com.yourfitness.coach.domain.models.ViewItem
import com.yourfitness.coach.domain.models.toViewItem
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.PaymentHistory
import com.yourfitness.coach.ui.features.profile.payment_history.PaymentHistoryFragment.Companion.DATE_TYPE_OLDEST
import com.yourfitness.coach.ui.features.profile.payment_history.PaymentHistoryFragment.Companion.DATE_TYPE_RESENT
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.network.dto.SettingsRegion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    val navigator: Navigator,
    private val restApi: YFCRestApi,
    private val region: RegionSettingsManager,
) : MviViewModel<Any, Any>() {

    private var paymentHistoryList = listOf<PaymentHistory>()
    private var currency = ""

    fun fetchData() {
        viewModelScope.launch {
            try {
                state.postValue(PaymentHistoryState.Loading)
                val paymentHistory = restApi.getPaymentHistory()
                paymentHistoryList = paymentHistory
                region.getSettings()?.currency?.let { currency = it }
                state.postValue(PaymentHistoryState.Success(paymentHistory))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(PaymentHistoryState.Error(error))
            }
        }
    }

    fun setupRecyclerViewDataSortByYear() {
        val paymentHistory = mutableListOf<ViewItem>()
        val yearList = mutableListOf<Long>()
        paymentHistoryList.forEach { yearList.add(it.time?.toDate()?.setYearStart()?.timeInMillis ?: STANDARD_DATA) }
        yearList.toSet().forEach {
            paymentHistory.add(ViewItem.HeaderItem(it))
            paymentHistoryList.forEach { history ->
                if (it == history.time?.toDate()?.setYearStart()?.timeInMillis) {
                    paymentHistory.add(history.toViewItem(currency))
                }
            }
        }
        state.postValue(PaymentHistoryState.UpdateAdapter(paymentHistory))
    }

    fun sortList(startDate: String, endDate: String, dateType: String) {
        when {
            startDate != START_DATE && endDate != END_DATE -> {
                val start = startDate.mmDdToDate()?.time ?: STANDARD_DATA
                val end = endDate.mmDdToDate()?.time ?: STANDARD_DATA
                val dayList = mutableListOf<Long>()
                var paymentHistory = mutableListOf<ViewItem>()
                paymentHistoryList.forEach {
                    if (it.time?.toDate()?.time in start..end) {
                        dayList.add(it.time?.toDate()?.time ?: STANDARD_DATA)
                    }
                }
                paymentHistory = sort(dateType, dayList, paymentHistory)
                state.postValue(PaymentHistoryState.UpdateAdapter(paymentHistory))
            }
            startDate == START_DATE && endDate == END_DATE -> {
                when (dateType) {
                    DATE_TYPE_OLDEST -> {
                        val paymentHistory = mutableListOf<ViewItem>()
                        val yearList = mutableListOf<Long>()
                        val dayList = mutableListOf<Long>()
                        paymentHistoryList.forEach { dayList.add(it.time?.toDate()?.time ?: STANDARD_DATA) }
                        paymentHistoryList.forEach { yearList.add(it.time?.toDate()?.setYearStart()?.timeInMillis ?: STANDARD_DATA) }
                        yearList.toSet().sortedBy { it }.reversed().forEach { time ->
                            paymentHistory.add(ViewItem.HeaderItem(time))
                            paymentHistoryList.sortedBy { it.time?.toDate()?.time }.forEach { history ->
                                if (time == history.time?.toDate()?.setYearStart()?.timeInMillis) {
                                    paymentHistory.add(history.toViewItem(currency))
                                }
                            }
                        }
                        state.postValue(PaymentHistoryState.UpdateAdapter(paymentHistory))
                    }
                    DATE_TYPE_RESENT -> {
                        val paymentHistory = mutableListOf<ViewItem>()
                        val yearList = mutableListOf<Long>()
                        paymentHistoryList.forEach { yearList.add(it.time?.toDate()?.setYearStart()?.timeInMillis ?: STANDARD_DATA) }
                        yearList.toSet().sortedBy { it }.forEach {
                            paymentHistory.add(ViewItem.HeaderItem(it))
                            paymentHistoryList.forEach { history ->
                                if (it == history.time?.toDate()?.setYearStart()?.timeInMillis) {
                                    paymentHistory.add(history.toViewItem(currency))
                                }
                            }
                        }
                        state.postValue(PaymentHistoryState.UpdateAdapter(paymentHistory))
                    }
                }
            }
            startDate != START_DATE && endDate == END_DATE -> {
                val start = startDate.mmDdToDate()?.time ?: STANDARD_DATA
                val end = Calendar.getInstance().timeInMillis
                val dayList = mutableListOf<Long>()
                var paymentHistory = mutableListOf<ViewItem>()
                paymentHistoryList.forEach {
                    if (it.time?.toDate()?.time in start..end) {
                        dayList.add(it.time?.toDate()?.time ?: STANDARD_DATA)
                    }
                }
                paymentHistory = sort(dateType, dayList, paymentHistory)
                state.postValue(PaymentHistoryState.UpdateAdapter(paymentHistory))
            }
            startDate == START_DATE && endDate != END_DATE -> {
                var start = Calendar.getInstance().timeInMillis
                paymentHistoryList.forEach {
                    if ((it.time?.toDate()?.time ?: STANDARD_DATA) < start) {
                        start = it.time?.toDate()?.time ?: STANDARD_DATA
                    }
                }
                val end = endDate.mmDdToDate()?.time ?: STANDARD_DATA
                val dayList = mutableListOf<Long>()
                var paymentHistory = mutableListOf<ViewItem>()
                paymentHistoryList.forEach {
                    if (it.time?.toDate()?.time in start..end) {
                        dayList.add(it.time?.toDate()?.time ?: STANDARD_DATA)
                    }
                }
                paymentHistory = sort(dateType, dayList, paymentHistory)
                state.postValue(PaymentHistoryState.UpdateAdapter(paymentHistory))
            }
        }
    }

    private fun sort(dateType: String, dayList: MutableList<Long>, paymentHistory: MutableList<ViewItem>): MutableList<ViewItem> {
        val paymentHistory = paymentHistory
        when (dateType) {
            DATE_TYPE_OLDEST -> {
                dayList.toSet().sortedBy { it }.forEach {
                    paymentHistory.add(ViewItem.HeaderItem(it, false))
                    paymentHistoryList.forEach { history ->
                        if (it == history.time?.toDate()?.time) {
                            paymentHistory.add(history.toViewItem(currency))
                        }
                    }
                }
            }
            DATE_TYPE_RESENT -> {
                dayList.toSet().sortedBy { it }.reversed().forEach {
                    paymentHistory.add(ViewItem.HeaderItem(it, false))
                    paymentHistoryList.forEach { history ->
                        if (it == history.time?.toDate()?.time) {
                            paymentHistory.add(history.toViewItem(currency))
                        }
                    }
                }
            }
        }
        return paymentHistory
    }

    fun setupRecyclerViewDataSortByPeriod(startDate: String, endDate: String) {
        val start = startDate.mmDdToDate()?.time ?: STANDARD_DATA
        val end = endDate.mmDdToDate()?.time ?: STANDARD_DATA
        val dayList = mutableListOf<Long>()
        val paymentHistory = mutableListOf<ViewItem>()
        paymentHistoryList.forEach {
            if (it.time?.toDate()?.time in start..end) {
                dayList.add(it.time?.toDate()?.time ?: STANDARD_DATA)
            }
        }
        dayList.toSet().forEach {
            paymentHistory.add(ViewItem.HeaderItem(it, false))
            paymentHistoryList.forEach { history ->
                if (it == history.time?.toDate()?.time) {
                    paymentHistory.add(history.toViewItem(currency))
                }
            }
        }
        state.postValue(PaymentHistoryState.UpdateAdapter(paymentHistory))
    }

    fun setupRecyclerViewDataSortByStartDate(startDate: String) {
        val start = startDate.mmDdToDate()?.time ?: STANDARD_DATA
        val end = Calendar.getInstance().timeInMillis
        val dayList = mutableListOf<Long>()
        val paymentHistory = mutableListOf<ViewItem>()
        paymentHistoryList.forEach {
            if (it.time?.toDate()?.time in start..end) {
                dayList.add(it.time?.toDate()?.time ?: STANDARD_DATA)
            }
        }
        dayList.toSet().forEach {
            paymentHistory.add(ViewItem.HeaderItem(it, false))
            paymentHistoryList.forEach { history ->
                if (it == history.time?.toDate()?.time) {
                    paymentHistory.add(history.toViewItem(currency))
                }
            }
        }
        state.postValue(PaymentHistoryState.UpdateAdapter(paymentHistory))
    }

    fun setupRecyclerViewDataSortByStartEndDate(endDate: String) {
        var start = Calendar.getInstance().timeInMillis
        paymentHistoryList.forEach {
            if ((it.time?.toDate()?.time ?: STANDARD_DATA) < start) {
                start = it.time?.toDate()?.time ?: STANDARD_DATA
            }
        }
        val end = endDate.mmDdToDate()?.time ?: STANDARD_DATA
        val dayList = mutableListOf<Long>()
        val paymentHistory = mutableListOf<ViewItem>()
        paymentHistoryList.forEach {
            if (it.time?.toDate()?.time in start..end) {
                dayList.add(it.time?.toDate()?.time ?: STANDARD_DATA)
            }
        }
        dayList.toSet().forEach {
            paymentHistory.add(ViewItem.HeaderItem(it, false))
            paymentHistoryList.forEach { history ->
                if (it == history.time?.toDate()?.time) {
                    paymentHistory.add(history.toViewItem(currency))
                }
            }
        }
        state.postValue(PaymentHistoryState.UpdateAdapter(paymentHistory))
    }

    companion object {
        const val START_DATE = "Start date"
        const val END_DATE = "End date"
        const val STANDARD_DATA = 1L
    }
}

open class PaymentHistoryState {
    object Loading : PaymentHistoryState()
    data class Success(
        val paymentHistory: List<PaymentHistory>
    ) : PaymentHistoryState()

    data class UpdateAdapter(
        val paymentHistoryList: List<ViewItem>
    ) : PaymentHistoryState()

    data class Error(val error: Exception) : PaymentHistoryState()
}