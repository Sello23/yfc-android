package com.yourfitness.coach.ui.features.payments.subscription

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentSubscriptionBinding
import com.yourfitness.coach.databinding.ItemExpandableChildBinding
import com.yourfitness.coach.databinding.ItemExpandableRootBinding
import com.yourfitness.coach.domain.date.formatDateDMmmYyyy
import com.yourfitness.coach.ui.features.facility.map.REQUEST_CODE_COMMON
import com.yourfitness.coach.ui.features.payments.confirmation_cancel_subscription.ConfirmationCancelSubscriptionDialog
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.common.ui.utils.setCustomFont
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import com.yourfitness.common.R as common

@AndroidEntryPoint
class SubscriptionFragment :
    MviFragment<SubscriptionIntent, SubscriptionState, SubscriptionViewModel>() {

    override val binding: FragmentSubscriptionBinding by viewBinding()
    override val viewModel: SubscriptionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionListeners()
        setupScreenResultListeners()
        setupNavigationListeners()
        setupCodeAppliedListener()
    }

    override fun renderState(state: SubscriptionState) {
        when (state) {
            is SubscriptionState.Loading -> showLoading(true)
            is SubscriptionState.Subscription -> setupSubscription(state)
            is SubscriptionState.Error -> showError(state.error)
        }
    }

    private fun setupNavigationListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            setFragmentResult(REQUEST_CODE_COMMON, bundleOf())
            findNavController().navigateUp()
        }

        binding.arrowBack.setOnClickListener {
            setFragmentResult(REQUEST_CODE_COMMON, bundleOf())
            findNavController().navigateUp()
        }
    }

    private fun setupScreenResultListeners() {
        setFragmentResultListener(ConfirmationCancelSubscriptionDialog.RESULT_KEY_CONFIRMATION) { requestKey, _ ->
            if (requestKey == ConfirmationCancelSubscriptionDialog.RESULT_KEY_CONFIRMATION) {
                viewModel.confirmCancel()
            }
            setupScreenResultListeners()
        }
    }

    private fun setupCodeAppliedListener() {
        setFragmentResultListener(REFERRAL_CODE) { _, bundle ->
            val codeApplied = bundle.getBoolean(REFERRAL_CODE_APPLIED)
            if (codeApplied) viewModel.checkReferralCode()
            clearFragmentResult(REFERRAL_CODE)
            setupCodeAppliedListener()
        }
    }

    private fun setupActionListeners() {
        binding.btnProcced.setOnClickListener { viewModel.proceedToPayment() }
        binding.btnCancel.setOnClickListener { viewModel.cancelSubscription() }
        binding.btnResubscribe.setOnClickListener { viewModel.resubscribe() }
    }

    private fun setupSubscription(state: SubscriptionState.Subscription) {
        showLoading(false)

        setupMonthlyOptionDescription(state.subscription?.complementaryAccess, state.subscription?.isPaidSubscription)

        state.subscription?.let {
            disableVoucherArea()
            setupSubscriptionInfo(it)
            setupOneTimeOptionDescription(it.corporationName)
            return
        }

        setupOneTimeOptionDescription(state.oneTimeOption?.corporationName)
        setupOption(
            state.voucherOption ?: state.monthlyOption,
            getString(R.string.subscription_screen_monthly_plan),
            R.string.subscription_screen_cancel_anytime,
            SubscriptionIntent.MonthlyOptionChosen,
            binding.monthlyOption,
            binding.monthlyOptionDescription
        )
        setupOption(
            state.oneTimeOption,
            resources.getQuantityString(
                R.plurals.number_of_months,
                state.oneTimeOption?.duration ?: 1,
                state.oneTimeOption?.duration ?: 1,
            ),
            R.string.subscription_screen_pay_once,
            SubscriptionIntent.OneTimeOptionChosen,
            binding.oneTimeOption,
            binding.oneTimeOptionDescription
        )

        setupVoucherArea(state.canUseVoucher, state.voucherCode)
        setupPlan(state)
    }

    private fun setupMonthlyOptionDescription(complementaryAccess: Boolean?, isPaidSubscription: Boolean?) {
        binding.monthlyOptionDescription.apply {
            tvOption1.text = getString(R.string.subscription_screen_sign_up_flow_option_1)
            tvOption3.text = getString(R.string.subscription_screen_sign_up_flow_option_3)

            if (complementaryAccess == true && isPaidSubscription == false) {
                tvOption2.text = getString(R.string.subscription_screen_sign_up_flow_option_4)
                ivOption3.isVisible = false
                tvOption3.isVisible = false
            } else {
                tvOption2.text = getString(R.string.subscription_screen_sign_up_flow_option_2)
                ivOption3.isVisible = true
                tvOption3.isVisible = true
            }
        }
    }

    private fun setupOneTimeOptionDescription(corporationName: String?) {
        binding.oneTimeOptionDescription.apply {
            tvOption1.text = getString(R.string.subscription_screen_one_time_flow_option_1, corporationName.orEmpty())
            tvOption2.text = getString(R.string.subscription_screen_one_time_flow_option_2)
            tvOption3.text = getString(R.string.subscription_screen_sign_up_flow_option_1)
        }
    }

    private fun setupSubscriptionInfo(info: SubscriptionInfo) {
        binding.apply {
            monthlyOption.root.isVisible = false
            tvType.isVisible = !info.isComplimentary
            tvStatus.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = if (info.isComplimentary) 0 else resources.getDimensionPixelSize(R.dimen.spacing_1x)
            }
            monthlyOptionDescription.root.isVisible = !info.isOneTime
            monthlyOptionDescription.root.background = null
            monthlyOptionDescription.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                marginStart = 0
                marginEnd = 0
            }
            oneTimeOption.root.isVisible = false
            oneTimeOptionDescription.root.isVisible = info.isOneTime
            oneTimeOptionDescription.root.background = null
            oneTimeOptionDescription.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                marginStart = 0
                marginEnd = 0
            }
            tvTitle.text = getString(R.string.subscription_screen_your_subscription)
            cardActivePlan.apply {
                isVisible = true
                tvType.text = if (info.isOneTime) {
                    resources.getQuantityString(
                        R.plurals.number_of_months,
                        info.duration ?: 1,
                        info.duration ?: 1
                    )
                } else {
                    getString(R.string.subscription_screen_monthly_plan)
                }
            }
            binding.tvNextPayment.isVisible = !info.complementaryAccess || info.isPaidSubscription
            setupNextPaymentLabel(info.nextPaymentDate, info.isOneTime || !info.autoRenewal)

            binding.btnProcced.isVisible = false
            binding.btnResubscribe.isVisible = !info.autoRenewal && !info.isOneTime &&
                    (!info.complementaryAccess || info.isPaidSubscription)
            binding.btnCancel.isVisible = info.autoRenewal && (!info.complementaryAccess || info.isPaidSubscription)
        }
    }

    private fun setupOption(
        option: SubscriptionOption?,
        optionTitleMsg: String,
        @StringRes subtitleRes: Int,
        intent: SubscriptionIntent,
        card: ItemExpandableRootBinding,
        description: ItemExpandableChildBinding,
        ) {
        binding.apply {
            card.root.isVisible = option != null
            description.root.isVisible = option != null && option.isChosen
            option?.let {
                tvTitle.text = getString(R.string.subscription_screen_sign_up_now)
                card.apply {
                    root.isClickable = !it.isChosen
                    root.setOnClickListener { viewModel.intent.value = intent }
                    root.isSelected = it.isChosen
                    cardContent.isSelected = it.isChosen
                    optionTitle.text = optionTitleMsg
                    optionSubtitle.text = getString(subtitleRes)
                    val label = it.price.formatAmount(it.currency)
                    if (option.oldPrice != null) {
                        priceOld.isVisible = true
                        card.discountLabel.isVisible = false
                        price.text = label
                        priceOld.text = getString(R.string.equivalent_to_month_price, it.oldPrice?.formatAmount(it.currency))
                    } else {
                        priceOld.isVisible = false
                        price.text = getString(R.string.subscription_screen_monthly_plan_price_format, label)
                        card.discountLabel.isVisible = option.isNewRate
                    }
                }

                binding.btnProcced.isVisible = true
                binding.btnResubscribe.isVisible = false
                binding.btnCancel.isVisible = false
            }
        }
    }

    private fun setupVoucherArea(canUseVoucher: Boolean, voucherCode: String?) {
        if (canUseVoucher) {
            if (voucherCode != null) {
                binding.textRedeemReferralCode.isVisible = false
                binding.textReferralCodeRedeemed.isVisible = true
                binding.textDiscardReferralCode.isVisible = true
                binding.textDiscardReferralCode.setOnClickListener { onDiscardCode() }
            } else {
                binding.textRedeemReferralCode.isVisible = true
                binding.textReferralCodeRedeemed.isVisible = false
                binding.textDiscardReferralCode.isVisible = false
                binding.textRedeemReferralCode.setOnClickListener { onDiscardCode() }
            }
        } else {
            disableVoucherArea()
        }
    }

    private fun disableVoucherArea() {
        binding.textRedeemReferralCode.isVisible = false
        binding.textReferralCodeRedeemed.isVisible = false
        binding.textDiscardReferralCode.isVisible = false
    }

    private fun disableActionButtons() {
        binding.btnProcced.isVisible = false
        binding.btnCancel.isVisible = false
        binding.btnResubscribe.isVisible = false
    }

    private fun onDiscardCode() {
        viewModel.intent.value = SubscriptionIntent.DiscardCode
    }

    private fun setupNextPaymentLabel(nextPaymentDate: Long?, isResubscribe: Boolean) {
        if (nextPaymentDate == null) return
        val date = Date(nextPaymentDate).formatDateDMmmYyyy().orEmpty()
        val text = getString(
            if (isResubscribe) R.string.subscription_screen_will_terminate
            else R.string.subscription_screen_your_next_payment,
            date
        )
        binding.tvNextPayment.text = SpannableString(text).apply {
            setCustomFont(requireContext(), indexOf(date), length, common.font.work_sans_bold)
        }
    }

    private fun setupPlan(state: SubscriptionState.Subscription) {
        binding.btnProcced.apply {
            val label = if (state.oneTimeOption?.isChosen == true) {
                state.oneTimeOption.price.formatAmount(state.oneTimeOption.currency)
            } else if (state.voucherOption?.isChosen == true) {
                state.voucherOption.price.formatAmount(state.voucherOption.currency)
            } else if (state.monthlyOption?.isChosen == true) {
                state.monthlyOption.price.formatAmount(state.monthlyOption.currency)
            } else null

            val twoOptions = (state.voucherOption != null || state.monthlyOption != null) && state.oneTimeOption != null
            isVisible = label != null || twoOptions
            text = if (label != null) {
                isEnabled = true
                getString(com.yourfitness.common.R.string.proceed_to_payment, label)
            } else {
                isEnabled = false
                getString(com.yourfitness.common.R.string.proceed_to_payment_empty)
            }
        }
    }

    companion object {
        const val REFERRAL_CODE = "referral_code"
        const val REFERRAL_CODE_APPLIED = "referral_code_applied"
    }
}
