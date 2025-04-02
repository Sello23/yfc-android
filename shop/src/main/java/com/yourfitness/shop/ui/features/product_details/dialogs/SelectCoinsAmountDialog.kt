package com.yourfitness.shop.ui.features.product_details.dialogs

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.slider.RangeSlider
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.setTextColorRes
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.toCoins
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.DialogSelectCoinsAmountBinding
import com.yourfitness.shop.ui.constants.Constants.Companion.COINS_AMOUNT
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.max
import kotlin.math.roundToLong


@AndroidEntryPoint
open class SelectCoinsAmountDialog :
    MviDialogFragment<SelectCoinsAmountIntent, SelectCoinsAmountState, SelectCoinsAmountViewModel>() {

    override val binding: DialogSelectCoinsAmountBinding by viewBinding()
    override val viewModel: SelectCoinsAmountViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.close.setOnClickListener { dismiss() }
        binding.oldPrice.paintFlags = binding.oldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.oldPrice.text = viewModel.initialPrice.formatAmount(viewModel.currency.uppercase())
        binding.sliderContent.limitsRange.setCompoundDrawables(start = com.yourfitness.common.R.drawable.ic_profile_coins)
        binding.action.text = getString(
            if (viewModel.flow == SelectCoinsFlow.ADD_TO_CART) R.string.add_to_cart
            else R.string.filters_apply
        )
        binding.action.setOnClickListener {
            setFragmentResult(RESULT, bundleOf(COINS_AMOUNT to viewModel.coinsUsed))
            dismiss()
        }
        setupSlider()
    }

    private fun setupSlider() {
        val max = viewModel.maxCoins.toFloat()
        binding.sliderContent.apply {
            priceRange.text = getString(R.string.coins_redeemed)
            textLimitMin.isVisible = false
            textLimitMax.isVisible = false
            limitsRange.setTextColorRes(com.yourfitness.common.R.color.text_gray)
            limitsRange.setTextAppearance(
                requireContext(),
                com.yourfitness.common.R.style.TextAppearance_YFC_Subheading1
            )

            sliderRange.apply {
                valueFrom = 0f
                valueTo = max
                values = listOf(viewModel.coinsUsed.toFloat())
                setupLimitRangeLabel()

                addOnChangeListener { slider, value, fromUser ->
                    viewModel.intent.value =
                        SelectCoinsAmountIntent.RangeChanged(value.roundToLong())
                }
            }
        }
    }

    override fun renderState(state: SelectCoinsAmountState) {
        when (state) {
            is SelectCoinsAmountState.Loading -> showLoading(true, R.id.content)
            is SelectCoinsAmountState.Update -> setupLimitRangeLabel()
        }
    }

    private fun setupLimitRangeLabel() {
        val priceEquivalent = (viewModel.coinsUsed * viewModel.coinsCost).toCoins()
        val priceEquivalentText = priceEquivalent.formatAmount(viewModel.currency.uppercase())
        binding.sliderContent.limitsRange.text = getString(
            R.string.cart_screen_redemption_data,
            viewModel.coinsUsed,
            priceEquivalentText
        )
        binding.oldPrice.isInvisible = viewModel.coinsUsed == 0L
        binding.price.text = max(
            0,
            viewModel.initialPrice - priceEquivalent
        ).formatAmount(viewModel.currency.uppercase())
        binding.price.setTextColorRes(
            if (viewModel.coinsUsed == 0L) com.yourfitness.common.R.color.text_gray
            else com.yourfitness.common.R.color.issue_red
        )
    }

    companion object {
        const val RESULT = "select_coins_amount"
    }
}
