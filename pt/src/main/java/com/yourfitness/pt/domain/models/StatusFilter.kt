package com.yourfitness.pt.domain.models

import com.yourfitness.pt.ui.utils.getStatusDataList

data class StatusFilter(
    val allStatuses: List<Pair<String, Int>> = getStatusDataList(),
    val selectedStatuses: MutableSet<String> = mutableSetOf(EMPTY_STATE),
    val statusAmounts: Map<String, Int> = mapOf()
) {
    companion object {
        const val EMPTY_STATE = "all"
    }
}

