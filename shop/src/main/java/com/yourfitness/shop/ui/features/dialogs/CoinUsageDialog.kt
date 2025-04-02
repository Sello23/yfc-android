package com.yourfitness.shop.ui.features.dialogs

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.common.ui.utils.formatCoins
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.toCoins
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.DialogCoinUsageBinding
import com.yourfitness.shop.ui.constants.Constants.Companion.COINS_COUNT
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CoinUsageDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogCoinUsageBinding by viewBinding()

    @Inject
    lateinit var navigator: ShopNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.buttonClose.setOnClickListener { findNavController().navigateUp() }
        binding.action.setOnClickListener { findNavController().navigateUp() }
        setupCoinsInfo()
    }

    private fun setupCoinsInfo() {
        val args = requireArguments()
        val coins = args.getLong(COINS_COUNT)
        val coinsCost = args.getDouble("coins_cost").toCoins()
        val currency = args.getString("currency").orEmpty().uppercase()

        binding.coinsAmount.text = resources.getQuantityString(
            com.yourfitness.common.R.plurals.profile_screen_format_coins,
            coins.toInt(),
            coins.formatCoins()
        )
        binding.currencyAmount.text = (coinsCost * coins).formatAmount(currency)
    }
}
