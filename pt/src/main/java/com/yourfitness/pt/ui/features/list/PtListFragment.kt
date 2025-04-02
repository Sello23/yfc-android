package com.yourfitness.pt.ui.features.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.FragmentPtListBinding
import com.yourfitness.pt.domain.models.TrainerCard
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PtListFragment : MviFragment<PtListIntent, PtListState, PtListViewModel>() {

    override val binding: FragmentPtListBinding by viewBinding()
    override val viewModel: PtListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (arguments?.getString("force").equals("1")) findNavController().navigateUp()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.root.isVisible = viewModel.isMyPt
        binding.listItems.isVisible = false
        binding.toolbar.toolbar.title = getString(R.string.select_your_trainer)
        setupToolbar(binding.toolbar.root)
        viewModel.intent.value = PtListIntent.ScreenOpened
    }

    override fun renderState(state: PtListState) {
        when (state) {
            is PtListState.Loading -> showLoading(true)
            is PtListState.FacilitiesLoaded -> {
                binding.listItems.isVisible = true
                showItems(state.ptItems, state.expandedStates, ::onPtExpandedClicked)
            }
            is PtListState.Error -> showError(state.error)
            is PtListState.UpdatePtExpandedStates -> updatePtExpandedStates(state.expandedStates, state.pos)
        }
    }

    fun showItems(
        ptItems: List<TrainerCard>,
        expandedStates: Map<String, Boolean>,
        onExpandedClick: (String, Int) -> Unit
    ) {
        showLoading(false)
        binding.listItems.adapter = PersonalTrainerAdapter(
            ptItems,
            expandedStates,
            ::onPtDetailsClicked,
            ::onPtBookClicked,
            onExpandedClick,
            ::onPtZeroSessionsClicked,
        )
    }

    private fun onPtDetailsClicked(item: TrainerCard) {
        viewModel.intent.value = PtListIntent.PtDetailsTapped(item.id)
    }

    private fun onPtBookClicked(item: TrainerCard) {
        viewModel.intent.value = PtListIntent.PtBookTapped(item.id, item.availableSessions)
    }

    private fun onPtExpandedClicked(id: String, pos: Int) {
        viewModel.intent.value = PtListIntent.UpdatePtExpandedState(id, pos)
    }

    private fun onPtZeroSessionsClicked(item: TrainerCard) {
        viewModel.intent.value = PtListIntent.PtZeroSessionsTapped(item.id)
    }

    fun updatePtExpandedStates(expandedStates: Map<String, Boolean>, pos: Int) {
        try {
            (binding.listItems.adapter as PersonalTrainerAdapter).apply {
                setData(expandedStates)
                notifyItemChanged(pos)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun setVisibility(isVisible: Boolean) {
        binding.root.isVisible = isVisible
        binding.toolbar.root.isVisible = false
    }
}
