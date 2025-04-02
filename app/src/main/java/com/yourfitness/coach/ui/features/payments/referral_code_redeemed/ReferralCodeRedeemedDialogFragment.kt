package com.yourfitness.coach.ui.features.payments.referral_code_redeemed

import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogReferralCodeRedeemedBinding
import com.yourfitness.coach.ui.features.payments.subscription.SubscriptionFragment
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReferralCodeRedeemedDialogFragment : MviDialogFragment<Any, Any, ReferralCodeRedeemedViewModel>() {

    override val binding: DialogReferralCodeRedeemedBinding by viewBinding()
    override val viewModel: ReferralCodeRedeemedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuClicked(it) }
        binding.toolbar.toolbar.title = getString(R.string.subscription_screen_referral_code_redeemed_text)
        binding.buttonGotIt.setOnClickListener { dismiss() }
        showData()
    }

    private fun showData() {
        val bonusCredits = requireArguments().get("bonusCredits") as String
        val newCost = requireArguments().getString("newCost")

        binding.textBonusCredits.isVisible = newCost == null
        if (newCost != null) {
            binding.textCreatedAfterPurchase.text =
                getString(R.string.subscription_screen_new_cost_text, newCost)
        } else {
            binding.textBonusCredits.text =
                getString(R.string.subscription_screen_bonus_coins_text, bonusCredits)
            binding.textCreatedAfterPurchase.text =
                getString(R.string.subscription_screen_will_credited_text)
        }
    }

    private fun onMenuClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> {
                dismiss()
            }
        }
        return true
    }
}