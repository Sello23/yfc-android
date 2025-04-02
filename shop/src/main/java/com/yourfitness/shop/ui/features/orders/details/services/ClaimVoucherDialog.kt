package com.yourfitness.shop.ui.features.orders.details.services

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yourfitness.common.databinding.DialogTwoActionBinding
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.shop.R
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.yourfitness.common.R as common

@AndroidEntryPoint
class ClaimVoucherDialog : MviBottomSheetDialogFragment<ClaimVoucherIntent, ClaimVoucherState, ClaimVoucherViewModel>() {

    override val viewModel: ClaimVoucherViewModel by viewModels()
    override val binding: DialogTwoActionBinding by viewBinding()

    @Inject
    lateinit var navigator: ShopNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(
            binding.toolbar.root,
            getString(R.string.claim_voucher_title),
            dismissId = common.id.close
        )

        setupBottomInsets(binding.root)
        binding.buttonRetry.text = getString(R.string.claim_voucher)
        binding.buttonRetry.setOnClickListener {
            viewModel.intent.postValue(ClaimVoucherIntent.ClaimVoucherRequested)
        }
        binding.buttonBack.text = getString(R.string.not_now)
        binding.buttonBack.setOnClickListener { findNavController().navigateUp() }
        binding.image.setImageResource(R.drawable.ic_claim_voucher)

        binding.message.text = getString(R.string.claim_voucher_msg)
    }

    override fun renderState(state: ClaimVoucherState) {
        when (state) {
            is ClaimVoucherState.Loading -> showLoading(true)
            is ClaimVoucherState.Error -> showLoading(false)
            is ClaimVoucherState.VoucherLoaded -> {
                showLoading(false)
                dialog?.hide()
                navigator.navigate(ShopNavigation.ClaimVoucherConfirmed(state.data))
            }
        }
    }
}
