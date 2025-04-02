package com.yourfitness.shop.ui.features.payment.payment_options

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.*
import com.yourfitness.common.domain.models.CreditCard
import com.yourfitness.common.domain.payment.PaymentService
import com.yourfitness.common.ui.features.payments.add_credit_card.AddCreditCardBottomSheetDialogFragment
import com.yourfitness.common.ui.features.payments.payment_options.PaymentOptionIntent
import com.yourfitness.common.ui.features.payments.payment_options.PaymentOptionState
import com.yourfitness.common.ui.features.payments.payment_options.PaymentOptionsAdapter
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.applyTextColorRes
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.FragmentProductsPaymentOptionsBinding
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common

@AndroidEntryPoint
open class PaymentOptionsFragment :
    MviFragment<PaymentOptionIntent, PaymentOptionState, ProductsPaymentOptionsViewModel>(),
    PaymentService.Callback {

    override val binding: FragmentProductsPaymentOptionsBinding by viewBinding()
    override val viewModel: ProductsPaymentOptionsViewModel by viewModels()

    private val adapter by lazy { PaymentOptionsAdapter(onItemClick = viewModel::onPaymentOptionClick) }
    private val paymentService = PaymentService(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(binding.toolbar.root)
        binding.toolbar.toolbar.title = getString(common.string.payment_options)
        binding.subtotal.btnCheckout.text = getString(R.string.purchase)
        binding.rvPaymentMethod.adapter = adapter
        viewModel.options.observe(viewLifecycleOwner) { data ->
            adapter.submitList(data)
        }
        viewModel.paymentData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                viewModel.paymentData.value = null
                data.cardId?.let { paymentService.payBySavedCard(this, it, data.clientSecret) }
                    ?: data.card?.let { paymentService.payByCard(this, it, data.clientSecret) }
            }
        }
        binding.subtotal.btnCheckout.setOnClickListener {
            viewModel.intent.value = ProductPaymentOptionIntent.PaymentConfirmed(
                getString(R.string.city),
                getString(R.string.street),
                getString(common.string.details)
            )
        }
    }

    override fun onStart() {
        super.onStart()
        setFragmentResultListener(AddCreditCardBottomSheetDialogFragment.RESULT_KEY_CREDIT_CARD) { _, bundle ->
            bundle.getParcelable<CreditCard>(AddCreditCardBottomSheetDialogFragment.BUNDLE_KEY_CREDIT_CARD)
                ?.let { creditCard -> viewModel.onAddedCard(creditCard) }
            clearFragmentResult(AddCreditCardBottomSheetDialogFragment.RESULT_KEY_CREDIT_CARD)
        }
    }

    override fun onStop() {
        super.onStop()
        clearFragmentResultListener(AddCreditCardBottomSheetDialogFragment.RESULT_KEY_CREDIT_CARD)
    }

    override fun renderState(state: PaymentOptionState) {
        when (state) {
            is PaymentOptionState.Loading -> showLoading(true)
            is ProductPaymentOptionState.Loaded -> showLoading(false)
            is PaymentOptionState.Error -> showError(state.error)
            is ProductPaymentOptionState.DataLoaded -> {
                showLoading(false)
                setupOverallView(state)
            }
        }
    }

    private fun setupOverallView(state: ProductPaymentOptionState.DataLoaded) {
        binding.subtotal.itemsAmount.text = resources.getQuantityString(R.plurals.items_amount, state.itemsCount, state.itemsCount)
        binding.subtotal.currencyPrice.text = state.priceWithoutCoins.formatAmount(state.currency).uppercase()
        binding.subtotal.oldCurrencyPrice.isVisible = state.price != state.priceWithoutCoins
        binding.subtotal.currencyPrice.applyTextColorRes(
            if (binding.subtotal.oldCurrencyPrice.isVisible) R.color.card_swipe_background
            else com.yourfitness.common.R.color.black
        )
        binding.subtotal.oldCurrencyPrice.text = state.price.formatAmount()
        binding.subtotal.oldCurrencyPrice.paintFlags =
            binding.subtotal.oldCurrencyPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        binding.subtotal.coinsPrice.text = buildCoinsPricePartText(
            state.overallCoins,
            state.currency,
            state.overallPrice
        )
        binding.subtotal.coinsPrice.applyTextColorRes(
            if (state.overallCoins == 0L) com.yourfitness.common.R.color.gray_light
            else com.yourfitness.common.R.color.black
        )
    }

    private fun buildCoinsPricePartText(
        coins: Long,
        currency: String,
        currencyValue: Long
    ): String {
        val currencyPart = currencyValue.formatAmount(currency).uppercase()
        return getString(R.string.cart_screen_redemption_data, coins, currencyPart)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        paymentService.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSuccessPayment(amount: Long?, currency: String?) {
        viewModel.intent.value = ProductPaymentOptionIntent.PaymentSucceeded
    }

    override fun onCancelPayment() {
        showLoading(false)
        viewModel.intent.value = ProductPaymentOptionIntent.PaymentCanceled
    }

    override fun onErrorPayment(error: String) {
        showLoading(false)
        viewModel.intent.value = ProductPaymentOptionIntent.PaymentFailed
    }
}
