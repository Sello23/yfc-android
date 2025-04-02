package com.yourfitness.coach.ui.features.facility.class_operations

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.models.*
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class ClassOperationsViewModel<I, S>(
    private val restApi: YFCRestApi,
    open val navigator: Navigator,
) : MviViewModel<I, S>() {

    protected fun cancelClass(data: CalendarBookClassData) {
        navigator.navigate(Navigation.ConfirmCancelClass(data))
    }

    fun cancelClassConfirmed(data: CalendarBookClassData) {
        viewModelScope.launch {
            try {
                postLoadingState()
                restApi.cancelBookedClass(data.scheduleId)
                navigator.navigate(Navigation.ConfirmCancelResult(data.credits))
                onClassCanceled(data)
            } catch (error: Exception) {
                Timber.e(error)
                postErrorState(error)
            }
        }
    }

    protected abstract fun postLoadingState()

    protected abstract fun postErrorState(error: Exception)

    protected abstract fun onClassCanceled(data: CalendarBookClassData)

}
