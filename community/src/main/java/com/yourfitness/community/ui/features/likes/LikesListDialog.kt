package com.yourfitness.community.ui.features.likes

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourfitness.common.ui.features.fitness_calendar.toPx
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.comunity.R
import com.yourfitness.comunity.databinding.DialogLikesListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.min

@AndroidEntryPoint
class LikesListDialog :
    MviBottomSheetDialogFragment<LikesListIntent, LikesListState, LikesListViewModel>() {

    override val binding: DialogLikesListBinding by viewBinding()
    override val viewModel: LikesListViewModel by viewModels()

    private val likesAdapter by lazy { LikesAdapter(viewModel.workoutDateUtc, ::onLikeTap) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        updateScrollableHeight(viewModel.minAmount)

        setupToolbar(
            binding.toolbar.root,
            dismissId = com.yourfitness.common.R.id.close
        )
        binding.toolbar.title.text = getString(R.string.likes)
        binding.toolbar.newLikes.apply {
            isVisible = viewModel.newLikes > 0
            text = if (viewModel.newLikes > 99) getString(R.string.max_new_likes_label)
            else getString(R.string.new_likes_label, viewModel.newLikes)
        }
        binding.likesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.gotIt.setOnClickListener { dismiss() }
    }

    private fun updateScrollableHeight(amount: Int) {
        binding.likesList.updateLayoutParams<ViewGroup.LayoutParams> {
            height = (min(amount, 7) * 80).toPx()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.configureDialogView()
        return dialog
    }

    override fun renderState(state: LikesListState) {
        when (state) {
            is LikesListState.Error -> {
                binding.progressContainer.root.isVisible = false
                showError(Throwable(getString(com.yourfitness.common.R.string.error_something_went_wrong)))
            }

            is LikesListState.LikesLoaded -> {
                updateScrollableHeight(state.data.size)
                binding.likesList.adapter = likesAdapter
                likesAdapter.setData(state.data, state.fitnessData)
                likesAdapter.notifyDataSetChanged()
                binding.progressContainer.root.isVisible = false
            }
        }
    }

    private fun onLikeTap(friendId: String) {
        viewModel.intent.value = LikesListIntent.Details(friendId)
    }

    override fun onDestroy() {
        setFragmentResult(RESULT, bundleOf())
        super.onDestroy()
    }

    companion object {
        const val RESULT = "likes_details_result"
    }
}
