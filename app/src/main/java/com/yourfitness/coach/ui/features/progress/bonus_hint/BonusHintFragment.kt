package com.yourfitness.coach.ui.features.progress.bonus_hint

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogBonusHintBinding
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BonusHintFragment : MviDialogFragment<Any, Any, BonusHintViewModel>() {

    override val binding: DialogBonusHintBinding by viewBinding()
    override val viewModel: BonusHintViewModel by viewModels()

    private val adapter by lazy { BonusHintAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuClicked(it) }
        binding.buttonActionPrimary.setOnClickListener { dismiss() }
        val visits = requireArguments().get("visits") as Int
        viewModel.getBonusCredits(visits)
    }

    override fun renderState(state: Any) {
        when (state) {
            is BonusHintState.Loading -> showLoading(true)
            is BonusHintState.Success -> setupRecyclerView(state)
            is BonusHintState.Error -> showLoading(false)
        }
    }

    fun showLoading(isLoading: Boolean) {
        binding.progress.root.isVisible = isLoading
        binding.groupContent.isVisible = !isLoading
    }

    private fun setupRecyclerView(state: BonusHintState.Success) {
        showLoading(false)
        adapter.setData(state.bonusCredits)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun onMenuClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
        return true
    }
}