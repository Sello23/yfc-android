package com.yourfitness.coach.ui.features.payments.redeem_voucher

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogVoucherCodeResultBinding
import com.yourfitness.coach.domain.referral.VoucherResult
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.getColorCompat
import com.yourfitness.common.ui.utils.setColor
import com.yourfitness.common.ui.utils.setCustomFont
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RedeemVoucherResultDialog :
    MviBottomSheetDialogFragment<RedeemVoucherIntent, RedeemVoucherState, RedeemVoucherViewModel>() {

    override val binding: DialogVoucherCodeResultBinding by viewBinding()
    override val viewModel: RedeemVoucherViewModel by viewModels()

    private val statusDataMap = mapOf(
        VoucherResult.SUCCESS to RedeemResultData(
            com.yourfitness.common.R.drawable.ic_success,
            R.string.voucher_code_redeemed,
            null,
            com.yourfitness.common.R.string.got_it,
        ),
        VoucherResult.ERROR to RedeemResultData(
            R.drawable.ic_error2,
            R.string.voucher_code_is_invalid,
            R.string.error_redeemed,
            com.yourfitness.common.R.string.btn_retry,
            R.string.subscription_screen_back_subscription_text,
            false
        )
    )

    private val statusData: RedeemResultData by lazy {
        statusDataMap[viewModel.flow] ?: statusDataMap.values.first()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setupBottomInsets(root)
            toolbar.apply {
                btnClose.setOnClickListener { dismiss() }
                textTitle.text = getString(statusData.tittle)
            }
            image.setImageResource(statusData.image)
            message.text = statusData.message?.let { getString(it) }
            actionMain.apply {
                text = getString(statusData.actionMain)
                setOnClickListener {
                    viewModel.intent.value = RedeemVoucherIntent.MainActionButtonClicked
                }
            }
            actionSecondary.apply {
                isVisible = statusData.actionSecondary != null
                text = statusData.actionSecondary?.let { getString(it) }
                setOnClickListener {
                    viewModel.intent.value = RedeemVoucherIntent.SecondaryActionButtonClicked
                }
            }
            bonusCoins.isVisible = statusData.bonusDataVisible
            infoMsg.isVisible = statusData.bonusDataVisible
        }
    }

    override fun renderState(state: RedeemVoucherState) {
        when (state) {
            is RedeemVoucherState.Dismiss -> requireDialog().dismiss()
            is RedeemVoucherState.Loaded -> showInfo(state)
        }
    }

    private fun showInfo(state: RedeemVoucherState.Loaded) {
        binding.apply {
            message.text = if (state.expiresSoon) {
                val timeData = mutableListOf<String>()
                if (state.hours > 0) {
                    timeData.add(resources.getQuantityString(R.plurals.hour, state.hours.toInt(), state.hours))
                }
                if (state.minutes > 0) {
                    timeData.add(resources.getQuantityString(R.plurals.minute, state.minutes.toInt(), state.minutes))
                }
                val expireText = timeData.joinToString(" ")
                getString(R.string.successfully_redeemed_expired_soon, expireText).buildSpannedText(expireText)
            } else {
                getString(R.string.successfully_redeemed)
            }
            bonusCoins.text = resources.getQuantityString(R.plurals.coins_plural_format, state.bonus, state.bonus)
            bonusCoins.isVisible = state.bonus > 0
            infoMsg.isVisible = state.bonus > 0
        }
    }

    private fun String.buildSpannedText(span: String): SpannableString {
        return SpannableString(this).apply {
            val spanStart = indexOf(span)
            val color = requireContext().getColorCompat(com.yourfitness.common.R.color.issue_red_dark)
            setColor(color, spanStart, spanStart + span.length)
            setCustomFont(requireContext(), spanStart, spanStart + span.length, com.yourfitness.common.R.font.tajawal_bold)
        }
    }

    companion object {
        data class RedeemResultData(
            @DrawableRes val image: Int,
            @StringRes val tittle: Int,
            @StringRes val message: Int?,
            @StringRes val actionMain: Int,
            @StringRes val actionSecondary: Int? = null,
            val bonusDataVisible: Boolean = true
        )
    }
}
