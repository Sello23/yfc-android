package com.yourfitness.pt.ui.features.payments.payment_options

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.yourfitness.common.databinding.FragmentPaymentOptionsBinding
import com.yourfitness.common.domain.models.BuyOptionData
import com.yourfitness.common.domain.models.CreditCard
import com.yourfitness.common.domain.payment.PaymentService
import com.yourfitness.common.ui.features.payments.add_credit_card.AddCreditCardBottomSheetDialogFragment
import com.yourfitness.common.ui.features.payments.payment_options.PaymentOptionIntent
import com.yourfitness.common.ui.features.payments.payment_options.PaymentOptionState
import com.yourfitness.common.ui.features.payments.payment_options.PaymentOptionsAdapter
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.pt.R
import com.yourfitness.pt.domain.values.SESSIONS_PACKAGE
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common

@AndroidEntryPoint
class PtPaymentOptionsFragment :
    MviFragment<PaymentOptionIntent, PaymentOptionState, PtPaymentOptionsViewModel>(),
    PaymentService.Callback {

    override val binding: FragmentPaymentOptionsBinding by viewBinding()
    override val viewModel: PtPaymentOptionsViewModel by viewModels()

    private val adapter by lazy { PaymentOptionsAdapter(onItemClick = viewModel::onPaymentOptionClick) }
    private val paymentService = PaymentService(this)

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
            is PtPaymentOptionsState.Loaded -> showLoading(false)
            is PaymentOptionState.Error -> showError(state.error)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBuyCredits.text = getString(R.string.buy_sessions)
        setupToolbar(binding.toolbar.root)
        setupBottomBar()
        binding.rvPaymentMethod.adapter = adapter
        viewModel.options.observe(viewLifecycleOwner) { data ->
            showLoading(false)
            adapter.submitList(data)
        }
        viewModel.paymentData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                viewModel.paymentData.value = null
                data.cardId?.let { paymentService.payBySavedCard(this, it, data.clientSecret) }
                    ?: data.card?.let { paymentService.payByCard(this, it, data.clientSecret) }
            }
        }
    }

    private fun setupBottomBar() {
        setupPrice()
        with(binding) {
            tvYouWillPaymentNow.setText(common.string.payment_method_screen_you_will_pay)
            tvFuturePayments.isVisible = false
            tvRenewalPrice.isVisible = false
            btnSubscribe.isVisible = false
            btnBuyCredits.isVisible = true
            btnBuyCredits.setOnClickListener {
                viewModel.intent.value = PtPaymentOptionsIntent.PaymentConfirmed
            }
        }
    }

    override fun setupToolbar(toolbar: Toolbar) {
        super.setupToolbar(toolbar)
        toolbar.title = getString(common.string.payment_options)
    }

    private fun setupPrice() {
        val packageData = requireArguments().getParcelable<BuyOptionData>(SESSIONS_PACKAGE)
        val sessionsNumber = packageData?.optionAmount ?: 0
        val price: Int = packageData?.price ?: 0
        val currency: String = packageData?.currency.orEmpty()
        val label = price.formatAmount(currency.uppercase())
        val sessionsLabel = resources.getQuantityString(R.plurals.sessions_number, sessionsNumber, sessionsNumber)
        binding.tvCurrentPrice.text =  "$label/$sessionsLabel"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        paymentService.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSuccessPayment(amount: Long?, currency: String?) {
        viewModel.intent.value = PtPaymentOptionsIntent.PaymentSucceeded
    }

    override fun onCancelPayment() {
        viewModel.intent.value = PtPaymentOptionsIntent.PaymentCanceled
    }

    override fun onErrorPayment(error: String) {
        showLoading(false)
        viewModel.intent.value = PtPaymentOptionsIntent.PaymentFailed
    }
}
