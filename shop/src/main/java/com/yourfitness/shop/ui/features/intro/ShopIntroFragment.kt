package com.yourfitness.shop.ui.features.intro

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import com.yourfitness.common.ui.utils.toCoins
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.common.ui.utils.formatCoins
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.setupTopInsets
import com.yourfitness.shop.R
import com.yourfitness.shop.data.ShopStorage
import com.yourfitness.shop.databinding.FragmentShopIntroBinding
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShopIntroFragment : MviFragment<Any, Any, MviViewModel<Any, Any>>() {

    override val binding: FragmentShopIntroBinding by viewBinding()
    override val viewModel = MviViewModel<Any, Any>()

    @Inject
    lateinit var shopNavigator: ShopNavigator
    @Inject
    lateinit var shopStorage: ShopStorage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shopStorage.introShown = true

        setupTopInsets(binding.introMain.root)
        setupTopInsets(binding.introNoCoins.root)
        setupTopInsets(binding.buttonClose)
        setupBottomInsets(binding.introNoCoins.actionContainer)
        binding.buttonClose.setOnClickListener {
            shopNavigator.navigate(ShopNavigation.ShopCategories)
        }

        binding.introNoCoins.helperAction.setOnClickListener { shopNavigator.navigate(ShopNavigation.HowToEarnCoins()) }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            shopNavigator.navigate(ShopNavigation.ShopCategories)
        }

        val args = requireArguments()
        val coins = args.getInt("coins_amount")

        if (coins > 0) setupBaseIntro(coins)
        else setupNoCoinsIntro()
    }

    private fun setupBaseIntro(coins: Int) {
        val args = requireArguments()
        val coinsCost = args.getFloat("coins_cost").toDouble().toCoins()
        val currency = args.getString("currency").orEmpty().uppercase()

        binding.introMain.root.isVisible = true
        binding.introNoCoins.root.isVisible = false
        val coinsString = resources.getQuantityString(
            com.yourfitness.common.R.plurals.profile_screen_format_coins,
            coins,
            coins.toLong().formatCoins()
        )
        val costString = (coinsCost * coins).formatAmount(currency)
        binding.introMain.coinsAmount.text = getString(
            R.string.shop_intro_coins_info,
            coinsString,
            costString,
        )
        binding.introMain.action.setOnClickListener {
            shopNavigator.navigate(ShopNavigation.ShopCategories)
        }
    }

    private fun setupNoCoinsIntro() {
        binding.introNoCoins.root.isVisible = true
        binding.introMain.root.isVisible = false
        binding.introNoCoins.action.setOnClickListener {
            shopNavigator.navigate(ShopNavigation.ShopCategories)
        }
    }
}
