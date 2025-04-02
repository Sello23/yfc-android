package com.yourfitness.shop.ui.features.product_details.dialogs

import android.os.Bundle
import android.view.View
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.DialogCoinRedemptionBinding
import com.yourfitness.shop.ui.constants.Constants.Companion.REDEEMABLE_AMOUNT
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CoinRedemptionDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogCoinRedemptionBinding by viewBinding()

    @Inject
    lateinit var navigator: ShopNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.coins_redemption),
            dismissId = com.yourfitness.common.R.id.close
        )
        val redeemableValue = requireArguments().getString(REDEEMABLE_AMOUNT).orEmpty()
        binding.message.text = getString(R.string.coin_redemption_info, redeemableValue)
        binding.buttonGotIt.setOnClickListener { dismiss() }
    }
}
