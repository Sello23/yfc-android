package com.yourfitness.pt.ui.features.payments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.yourfitness.common.databinding.FragmentBuyOptionsBinding
import com.yourfitness.common.domain.models.BuyOptionData
import com.yourfitness.common.ui.features.payments.buy_options.BuyOptionsState
import com.yourfitness.common.ui.features.payments.buy_options.CreditsAdapter
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.pt.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class BuySessionsOptionsFragment : MviFragment<Any, BuyOptionsState, BuySessionsOptionsViewModel>() {

    override val binding: FragmentBuyOptionsBinding by viewBinding()
    override val viewModel: BuySessionsOptionsViewModel by viewModels()

    private val adapter by lazy { CreditsAdapter(onItemClick = viewModel::onCreditClick) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(binding.toolbar.root)
        binding.tvTitle.text = getString(R.string.session_buy_options_title)
        binding.tvSubtitle.text = getString(R.string.session_buy_options_subtitle)
        binding.rvCredits.adapter = adapter
        viewModel.creditsData.observe(viewLifecycleOwner) { data ->
            val updatedData = data.map {
                var amount = 0
                try {
                    amount = it.optionAmount
                } catch (e: NumberFormatException) {
                    Timber.e(e)
                }
                it.copy(
                    amountString = resources.getQuantityString(R.plurals.sessions_number, amount, amount)
                )
            }
            adapter.submitList(updatedData)
            binding.bottomBar.isVisible = updatedData.isNotEmpty()
        }
        viewModel.selectedCreditData.observe(viewLifecycleOwner, this::configureProceedBtn)
        binding.btnProceed.setOnClickListener { viewModel.onProceedClick() }
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            setFragmentResult(REQUEST_CODE_COMMON, bundleOf())
//            findNavController().navigateUp()
//        }
//
//        binding.toolbar.toolbar.setNavigationOnClickListener {
//            setFragmentResult(REQUEST_CODE_COMMON, bundleOf())
//            findNavController().navigateUp()
//        }
    }

    override fun setupToolbar(toolbar: Toolbar) {
        super.setupToolbar(toolbar)
        toolbar.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun renderState(state: BuyOptionsState) {
        when (state) {
            is BuyOptionsState.Loading -> showLoading(true)
            is BuyOptionsState.Success -> showLoading(false)
            is BuyOptionsState.Error -> showError(state.error)
        }
    }

    private fun configureProceedBtn(data: BuyOptionData?) {
        val price = data?.price?.formatAmount(data.currency).orEmpty()
        binding.btnProceed.text =
            getString(com.yourfitness.common.R.string.proceed_to_payment, price)
    }
}
