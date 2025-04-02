package com.yourfitness.coach.ui.features.payments.referral_code_invalid

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogReferralCodeInvalidDialogBinding
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.coach.ui.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReferralCodeInvalidDialogFragment : MviDialogFragment<Any, Any, ReferralCodeInvalidViewModel>() {

    override val binding: DialogReferralCodeInvalidDialogBinding by viewBinding()
    override val viewModel: ReferralCodeInvalidViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuClicked(it) }
        binding.toolbar.toolbar.title = getString(R.string.subscription_screen_referral_code_invalid_text)
        binding.buttonRetry.setOnClickListener {
            dialog?.hide()
            viewModel.navigator.navigate(Navigation.ReferralCode())
        }
        binding.textBackToSubscription.setOnClickListener {
            viewModel.navigator.navigate(Navigation.Subscription(PaymentFlow.BUY_SUBSCRIPTION_FROM_PROFILE))
        }
    }

    private fun onMenuClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
        return true
    }
}