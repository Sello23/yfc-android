package com.yourfitness.coach.ui.features.progress.visited_class

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.databinding.FragmentVisitedClassesBinding
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.coach.ui.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VisitedClassesFragment : MviFragment<VisitedClassIntent, VisitedClassState, VisitedClassesViewModel>() {

    override val binding: FragmentVisitedClassesBinding by viewBinding()
    override val viewModel: VisitedClassesViewModel by viewModels()
    private val visitedClassesAdapter by lazy { VisitedClassAdapter(::onItemClick) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.isVisible = false
    }

    override fun renderState(state: VisitedClassState) {
        when (state) {
            is VisitedClassState.Success -> showVisitedClasses(state)
        }
    }

    private fun showVisitedClasses(state: VisitedClassState.Success) {
        if (state.visitedClass.isNotEmpty()) {
            binding.root.isVisible = true
            binding.imageIcQuestion.setOnClickListener { viewModel.navigator.navigate(Navigation.BonusHint(state.maxVisits)) }
            visitedClassesAdapter.setData(state.visitedClass)
            binding.visitedStudiosList.adapter = visitedClassesAdapter
            binding.visitedStudiosList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun onItemClick(facility: FacilityEntity) {
        if (facility.classification == "Gym") {
            viewModel.navigator.navigate(Navigation.FacilityDetails(facility, Classification.GYM))
        } else {
            viewModel.navigator.navigate(Navigation.FacilityDetails(facility, Classification.STUDIO))
        }
    }
}