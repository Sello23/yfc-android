package com.yourfitness.shop.ui.features.orders.cart.enter_coins

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.common.ui.utils.toCoins
import com.yourfitness.common.ui.utils.toCurrencyRounded
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.DialogInputCoinsBinding
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common

const val REQUEST_CODE_ENTER_COINS = "53"

@AndroidEntryPoint
class EnterCoinsDialogFragment : MviBottomSheetDialogFragment<Any, Any, EnterCoinsViewModel>() {

    override val binding: DialogInputCoinsBinding by viewBinding()
    override val viewModel: EnterCoinsViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setupToolbar()
        setupViews()
        binding.buttonAction.setOnClickListener { onApplyClicked() }
        setupInputListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuItemClicked(it) }
        setupToolbar(
            toolbar = binding.toolbar.root,
            title = getString(R.string.cart_select_redemption),
            com.yourfitness.common.R.id.close
        )
    }

    private fun setupViews() {
        binding.currentRedemptionValue.text = getString(
            R.string.cart_screen_redemption_data,
            viewModel.maxRedemption,
            (viewModel.coinsValue * viewModel.maxRedemption).toCurrencyRounded().toCoins()
                .formatAmount(viewModel.currency)
        )

        binding.balanceRedemptionValue.text = getString(
            R.string.cart_screen_redemption_data,
            viewModel.availableCoins,
            (viewModel.coinsValue * viewModel.availableCoins).toCurrencyRounded().toCoins()
                .formatAmount(viewModel.currency)
        )
    }

    override fun renderState(state: Any) {
        when (state) {
            is EnterCoinsState.UpdateAction -> {
                binding.buttonAction.isEnabled = state.errorType == null
                showAmountError(state.errorType, state.maxRedemption)
            }
        }
    }

    private fun onApplyClicked() {
        val bundle = bundleOf(
            "enteredCoins" to viewModel.enteredCoins
        )
        setFragmentResult(REQUEST_CODE_ENTER_COINS, bundle)
        dismiss()
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            common.id.close -> dismiss()
        }
        return true
    }

    private fun setupInputListeners() {
        binding.inputCoins.transformationMethod = HideReturnsTransformationMethod.getInstance()
        binding.inputCoins.requestFocus()

        binding.inputCoins.doOnTextChanged { coins, _, _, _ ->
            try {
                val amount = (coins ?: "").trim()
                if (binding.inputCoins.text.toString() != amount.toString()) {
                    binding.inputCoins.setText(amount)
                    binding.inputCoins.setSelection(amount.length)
                }
                viewModel.intent.value =
                    EnterCoinsIntent.EnteredCoinsChanged(amount.toString().toLong())
            } catch (_: Exception) {
            }
        }
    }

    private fun showAmountError(errorType: EnterAmountError?, maxRedemption: Long) {
        val background =
            if (errorType == null) common.drawable.edit_text_background
            else R.drawable.edit_text_background_amount_error
        binding.inputCoins.setBackgroundResource(background)
        binding.textAmountError.isVisible = errorType != null
        if (errorType != null) {
            binding.textAmountError.text = if (errorType == EnterAmountError.NOT_ENOUGH_ERROR) {
                getString(R.string.not_enough_coins_error)
            } else {
                resources.getQuantityString(R.plurals.coins_amount_error, maxRedemption.toInt(), maxRedemption)
            }
        }

    }
}

enum class EnterAmountError {
    NOT_ENOUGH_ERROR,
    LIMIT_ERROR
}
