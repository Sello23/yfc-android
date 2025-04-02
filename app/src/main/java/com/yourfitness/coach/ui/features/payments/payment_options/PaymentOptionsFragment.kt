package com.yourfitness.coach.ui.features.payments.payment_options

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.*
import com.yourfitness.coach.R
import com.yourfitness.common.domain.analytics.AnalyticsManager
import com.yourfitness.common.domain.analytics.SubscriptionEvents
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.coach.ui.features.facility.booking_class_calendar.BookingClassCalendarFragment
import com.yourfitness.coach.ui.features.progress.coin_reward.RewardFragment
import com.yourfitness.coach.ui.navigation.Navigation
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.yourfitness.common.R as common

@AndroidEntryPoint
class PaymentOptionsFragment : MviFragment<PaymentOptionIntent, PaymentOptionState, PaymentOptionsViewModel>(),
    PaymentService.Callback {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override val binding: FragmentPaymentOptionsBinding by viewBinding()
    override val viewModel: PaymentOptionsViewModel by viewModels()

    private val adapter by lazy { PaymentOptionsAdapter(onItemClick = viewModel::onPaymentOptionClick) }
    private val paymentService = PaymentService(this)
    private val flow: PaymentFlow? by lazy { requireArguments().get("flow") as? PaymentFlow }
    private val isSubscriptionFlow by lazy { flow?.isSubscriptionFlow == true }
    private val isCreditsFlow by lazy { flow?.isCreditsFlow == true }

    override fun onStart() {
        super.onStart()
        setFragmentResultListener(AddCreditCardBottomSheetDialogFragment.RESULT_KEY_CREDIT_CARD) { _, bundle ->
            bundle.getParcelable<CreditCard>(AddCreditCardBottomSheetDialogFragment.BUNDLE_KEY_CREDIT_CARD)
                ?.let { creditCard -> viewModel.onAddedCard(creditCard) }
            clearFragmentResult(AddCreditCardBottomSheetDialogFragment.RESULT_KEY_CREDIT_CARD)
        }
        setFragmentResultListener(RewardFragment.REQUEST_KEY_DETACH_EVENT) { _, _ ->
            if (isSubscriptionFlow) {
                flow?.let { viewModel.navigator.navigate(Navigation.SuccessPaymentSubscription(it)) }
            }
            clearFragmentResult(RewardFragment.REQUEST_KEY_DETACH_EVENT)
        }
    }

    override fun onStop() {
        super.onStop()
        clearFragmentResultListener(AddCreditCardBottomSheetDialogFragment.RESULT_KEY_CREDIT_CARD)
        clearFragmentResultListener(RewardFragment.REQUEST_KEY_DETACH_EVENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBuyCredits.text = getString(R.string.buy_credits)
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
        with(binding) {
            tvYouWillPaymentNow.setText(if (isSubscriptionFlow) common.string.payment_method_screen_you_will_pay_now else common.string.payment_method_screen_one_time_payment)
            tvFuturePayments.isVisible = isSubscriptionFlow
            tvRenewalPrice.isVisible = isSubscriptionFlow
            btnSubscribe.isVisible = isSubscriptionFlow
            btnBuyCredits.isVisible = isCreditsFlow
            btnSubscribe.setOnClickListener { viewModel.subscribe() }
            btnBuyCredits.setOnClickListener { viewModel.buyCredits() }
        }
        setupPrice()
    }

    override fun setupToolbar(toolbar: Toolbar) {
        super.setupToolbar(toolbar)
        toolbar.title = getString(common.string.payment_options)
    }

    override fun renderState(state: PaymentOptionState) {
        when (state) {
            is PaymentOptionState.Loading -> showLoading(true)
            is PaymentOptionState.Error -> showError(state.error)
        }
    }

    private fun setupPrice() {
        val creditData = requireArguments().getParcelable<BuyOptionData>("credit_data")
        val price: Long = requireArguments().getLong("price")
        val currency: String = requireArguments().getString("currency").orEmpty()
        val label = price.formatAmount(currency)
        binding.tvCurrentPrice.text = if (isSubscriptionFlow) {
            label
        } else {
            "$label/${
                getString(R.string.credits_screen_credits_format, creditData?.optionAmount ?: 0)
            }"
        }

        binding.apply {
            if (viewModel.oldPrice != null) {
                tvRenewalPrice.isVisible = false
                tvFuturePayments.isVisible = false
                tvCurrentPrice.text = label
            } else {
                tvRenewalPrice.text =
                    getString(R.string.subscription_screen_monthly_plan_price_format, label)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        paymentService.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSuccessPayment(amount: Long?, currency: String?) {
        analyticsManager.trackEvent(SubscriptionEvents.payed(amount, currency))
        when {
            isSubscriptionFlow -> {
                showLoading(false)
                viewModel.subscriptionPaid(flow)
            }
            flow == PaymentFlow.BUY_CREDITS_FROM_PROFILE -> {
                view?.postDelayed({
                    showLoading(false)
                    viewModel.navigator.navigate(Navigation.Profile)
                }, 1000)
            }
            flow == PaymentFlow.BUY_CREDITS_FROM_SCHEDULE -> {
                view?.postDelayed({
                    showLoading(false)
                    val bundle = requireArguments()
                    bundle.putInt("purchased_credits", viewModel.getPurchasedCredits())
                    setFragmentResult(BookingClassCalendarFragment.REQUEST_KEY_CREDITS_PURCHASED, bundle)
                    viewModel.navigator.navigate(Navigation.BackToClassCalendar)
                }, 3000)
            }
            flow == PaymentFlow.BUY_CREDITS_FROM_PROGRESS -> {
                view?.postDelayed({
                    showLoading(false)
                    viewModel.intent.postValue(PaymentOptionIntent.OpenProgress)
                }, 1000)
            }
            flow == PaymentFlow.BUY_CREDITS_FROM_MAP -> {
                view?.postDelayed({
                    showLoading(false)
                    viewModel.navigator.navigate(Navigation.Map())
                }, 1000)
            }
        }
    }

    override fun onCancelPayment() {
        showLoading(false)
    }

    override fun onErrorPayment(error: String) {
        showLoading(false)
        if (flow == PaymentFlow.BUY_CREDITS_FROM_SCHEDULE) {
            viewModel.navigator.navigate(Navigation.PaymentError)
        } else {
            showMessage(error)
        }
    }
}