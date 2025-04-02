package com.yourfitness.coach.ui.features.profile.payment_history.webview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yourfitness.coach.databinding.FragmentPaymentHistoryWebViewBinding
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentHistoryWebViewFragment : MviFragment<Any, Any, PaymentHistoryWebViewViewModel>() {
    override val binding: FragmentPaymentHistoryWebViewBinding by viewBinding()
    override val viewModel: PaymentHistoryWebViewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val paymentIntent = requireArguments().get("paymentIntent") as String
        viewModel.fetchData(paymentIntent)
        binding.buttonClose.setOnClickListener { findNavController().navigateUp() }
    }

    override fun renderState(state: Any) {
        when (state) {
            is PaymentHistoryWebViewState.Loading -> showLoading(true)
            is PaymentHistoryWebViewState.Success -> showData(state)
            is PaymentHistoryWebViewState.Error -> {
                showLoading(false)
                showMessage(state.error.message)
            }
        }
    }

    private fun showData(state: PaymentHistoryWebViewState.Success) {
        showLoading(false)
        binding.webview.loadUrl(state.stripeUrl.receipt ?: "")
        binding.webview.settings.builtInZoomControls = true
        binding.webview.settings.loadWithOverviewMode = true
        binding.webview.settings.useWideViewPort = true
    }
}