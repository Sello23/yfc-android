package com.yourfitness.coach.ui.features.facility.class_operations

import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import com.yourfitness.coach.domain.models.CalendarBookClassData
import com.yourfitness.common.ui.mvi.MviFragment

abstract class ClassOperationsFragment<I, S, VM : ClassOperationsViewModel<I, S>> :
    MviFragment<I, S, VM>() {

    override fun onStart() {
        super.onStart()
        setFragmentResultListeners(this)
    }

    protected open fun setFragmentResultListeners(fragment: Fragment?) {
        fragment?.setFragmentResultListener(REQUEST_KEY_CANCEL_CONFIRMATION) { _, bundle ->
            (bundle.get("data") as? CalendarBookClassData)
                ?.let { viewModel.cancelClassConfirmed(it) }
            clearFragmentResult(REQUEST_KEY_CANCEL_CONFIRMATION)
        }
    }

    override fun onStop() {
        super.onStop()
        clearFragmentResultListeners(this)
    }

    protected open fun clearFragmentResultListeners(fragment: Fragment?) {
        fragment?.clearFragmentResultListener(REQUEST_KEY_CANCEL_CONFIRMATION)
    }

    companion object {
        const val REQUEST_KEY_CANCEL_CONFIRMATION = "request_key_cancel_confirmation"
    }
}