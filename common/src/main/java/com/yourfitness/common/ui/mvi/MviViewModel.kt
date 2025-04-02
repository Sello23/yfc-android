package com.yourfitness.common.ui.mvi

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class MviViewModel<I, S> : ViewModel() {

    val intent = MutableLiveData<I?>()
    val state = MutableLiveData<S?>()

    open fun handleIntent(intent: I) {
    }
}