package com.yourfitness.common.ui.mvi

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment

abstract class MviBottomSheetDialogFragment<I, S, VM : MviViewModel<I, S>> :
    BaseBottomSheetDialogFragment() {

    abstract val viewModel: VM

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        observeIntent()
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) {
            if (it != null) {
                renderState(it)
            }
        }
    }

    private fun observeIntent() {
        viewModel.intent.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.intent.value = null
                viewModel.handleIntent(it)
            }
        }
    }

    open fun renderState(state: S) {
    }
}