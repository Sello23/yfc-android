package com.yourfitness.coach.ui.features.payments.referral_code

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogReferralCodeBinding
import com.yourfitness.coach.ui.features.payments.subscription.SubscriptionFragment
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.hideKeyboard
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReferralCodeDialogFragment : MviDialogFragment<ReferralCodeIntent, ReferralCodeState, ReferralCodeViewModel>() {

    override val binding: DialogReferralCodeBinding by viewBinding()
    override val viewModel: ReferralCodeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setupBottomInsets(root)
            toolbar.toolbar.setOnMenuItemClickListener { onMenuClicked(it) }
            toolbar.toolbar.title = getString(R.string.subscription_screen_referral_code_text)
            binding.enterCode.apply {
                filters += arrayOf(
                    InputFilter.LengthFilter(255),
                    InputFilter.AllCaps()
                )
            }
            buttonRedeem.setOnClickListener {
                binding.buttonRedeem.isClickable = false
                onNextClicked()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(
            SubscriptionFragment.REFERRAL_CODE, bundleOf(
                SubscriptionFragment.REFERRAL_CODE_APPLIED to viewModel.codeApplied
            )
        )
    }

    override fun renderState(state: ReferralCodeState) {
        when (state) {
            is ReferralCodeState.Loading -> showLoading(true)
            is ReferralCodeState.Success -> showLoading(false)
            is ReferralCodeState.Error -> showError(state.error)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            hideKeyboard()
        }
        binding.progress.root.isVisible = isLoading
    }

    private fun onMenuClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
        return true
    }

    private fun onNextClicked() {
        val code = binding.enterCode.text.toString().trim().uppercase()
        viewModel.checkCorporationCode(code)
    }
}