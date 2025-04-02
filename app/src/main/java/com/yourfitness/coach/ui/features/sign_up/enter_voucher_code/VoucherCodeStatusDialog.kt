package com.yourfitness.coach.ui.features.sign_up.enter_voucher_code

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogCorporationCodeErrorBinding
import com.yourfitness.coach.domain.referral.VoucherResult
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class VoucherCodeStatusDialog :
    MviBottomSheetDialogFragment<CorporateCodeStatusIntent, CorporateCodeStatusState, CorporateCodeStatusViewModel>() {

    override val binding: DialogCorporationCodeErrorBinding by viewBinding()
    override val viewModel: CorporateCodeStatusViewModel by viewModels()

    private val statusDataMap = mapOf(
        VoucherResult.GENDER_ERROR to StatusData(
            com.yourfitness.common.R.drawable.ic_error,
            R.string.sign_up_error_challenge_code_title,
            R.string.change_gender_to_female,
            com.yourfitness.common.R.string.got_it
        ) { getString(R.string.sign_up_error_gender_msg) },
        VoucherResult.CHALLENGE_SUCCESS to StatusData(
            R.drawable.ic_code_success,
            R.string.sign_up_error_challenge_code_title,
            com.yourfitness.common.R.string.got_it,
            showCloseBtn = false
        ) {
            val name = viewModel.dataList.firstOrNull().orEmpty()
            getString(R.string.sign_up_success_challenge_code_msg, name, name)
        },
        VoucherResult.CORPORATE_REGIONAL_RATE_SUCCESS to newRateData,
        VoucherResult.CORPORATE_NEW_RATE_SUCCESS to newRateData,
        VoucherResult.CORPORATE_COMPLIMENTARY_SUCCESS to StatusData(
            com.yourfitness.common.R.drawable.ic_success,
            R.string.sign_up_success_corporate_code_title,
            com.yourfitness.common.R.string.got_it,
            null,
            false
        ) {
            val name = viewModel.dataList.firstOrNull().orEmpty()
            getString(R.string.complimentary_code_redeemed_msg, name)
        },
        VoucherResult.ERROR to StatusData(
            com.yourfitness.common.R.drawable.ic_cancel,
            R.string.warning,
            com.yourfitness.common.R.string.btn_retry,
            R.string.btn_skip_step
        ) { getString(R.string.sign_up_code_error) },
        VoucherResult.INFLUENCER_DISCOUNT to StatusData(
            com.yourfitness.common.R.drawable.ic_success,
            R.string.sign_up_success_influencer_code_title,
            R.string.buy_subscription,
            com.yourfitness.common.R.string.got_it,
            false,
            messageBuilder = ::buildInfluencerDiscountMsg,
        ),
        VoucherResult.INFLUENCER_DISCOUNT2 to StatusData(
            com.yourfitness.common.R.drawable.ic_success,
            R.string.sign_up_success_influencer_code_title,
            com.yourfitness.common.R.string.got_it,
            null,
            messageBuilder = ::buildInfluencerDiscountMsg,
        ),
        VoucherResult.ERROR2 to StatusData(
            com.yourfitness.common.R.drawable.ic_cancel,
            R.string.code_is_invalid,
            com.yourfitness.common.R.string.btn_retry,
            R.string.subscription_screen_back_subscription_text
        ) { getString(R.string.enter_code_error) },
        VoucherResult.CORPORATE_COMMON to StatusData(
            com.yourfitness.common.R.drawable.ic_success,
            R.string.code_redeemed_title,
            com.yourfitness.common.R.string.got_it,
            null,
        ) { getString(R.string.code_redeemed) },
        VoucherResult.INFLUENCER_PROMOTION to StatusData(
            com.yourfitness.common.R.drawable.ic_success,
            R.string.influencer_code_redeemed_title,
            com.yourfitness.common.R.string.got_it,
            null,
            bonusBuilder = {
                val reward: Int = (viewModel.dataList.firstOrNull())?.toInt() ?: 0
                resources.getQuantityString(R.plurals.coins_plural_format, reward, reward)
            },
            bonusMessageBuilder = { getString(R.string.influencer_code_redeemed_bonus_msg) },
        ) { getString(R.string.influencer_code_redeemed) },
    )

    private fun buildInfluencerDiscountMsg(): String? {
        return try {
            val rate: Int = viewModel.dataList.firstOrNull()?.toInt() ?: 0
            val currency = viewModel.dataList.lastOrNull().orEmpty()
            val rateFormatted = rate.formatAmount(currency.uppercase())
            getString(R.string.sign_up_success_influencer_code_msg, rateFormatted)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private val newRateData: StatusData
        get() = StatusData(
            com.yourfitness.common.R.drawable.ic_success,
            R.string.sign_up_success_corporate_code_title,
            R.string.buy_subscription,
            com.yourfitness.common.R.string.got_it,
            false
        ) {
            try {
                val name = viewModel.dataList.firstOrNull().orEmpty()
                val rate: Int = (viewModel.dataList.getOrNull(1))?.toInt() ?: 0
                val currency = viewModel.dataList.lastOrNull().orEmpty()
                val rateFormatted = rate.formatAmount(currency.uppercase())
                getString(R.string.sign_up_success_corporate_code_msg, name, rateFormatted)
            } catch (e: Exception) {
                Timber.e(e)
                ""
            }

        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireDialog().setCancelable(false)
        requireDialog().setCanceledOnTouchOutside(false)

        val statusData: StatusData = statusDataMap[viewModel.flow] ?: statusDataMap.values.first()
        binding.apply {
            setupBottomInsets(root)
            toolbar.apply {
                btnClose.isVisible = statusData.showCloseBtn
                btnClose.setOnClickListener { dismiss() }
                textTitle.text = getString(statusData.tittle)
            }
            image.setImageResource(statusData.image)
            message.text = statusData.messageBuilder()
            actionMain.apply {
                text = getString(statusData.actionMain)
                setOnClickListener {
                    viewModel.intent.value = CorporateCodeStatusIntent.MainActionButtonClicked
                }
            }
            actionSecondary.apply {
                isVisible = statusData.actionSecondary != null
                text = statusData.actionSecondary?.let { getString(it) }
                setOnClickListener {
                    viewModel.intent.value = CorporateCodeStatusIntent.SecondaryActionButtonClicked
                }
            }
            bonusValue.isVisible = statusData.bonusBuilder != null
            bonusValue.text = statusData.bonusBuilder?.invoke()
            bonusMessage.isVisible = statusData.bonusMessageBuilder != null
            bonusMessage.text = statusData.bonusMessageBuilder?.invoke()
        }
    }

    override fun renderState(state: CorporateCodeStatusState) {
        when (state) {
            is CorporateCodeStatusState.Loading -> showLoading(true)
            is CorporateCodeStatusState.Dismiss -> requireDialog().dismiss()
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progress.root.isVisible = isLoading
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(RETRY_OPTION, bundleOf(RETRY_OPTION to viewModel.retry))
    }

    companion object {
        data class StatusData(
            @DrawableRes val image: Int,
            @StringRes val tittle: Int,
            @StringRes val actionMain: Int,
            @StringRes val actionSecondary: Int? = null,
            val showCloseBtn: Boolean = true,
            val bonusBuilder: (() -> String)? = null,
            val bonusMessageBuilder: (() -> String)? = null,
            val messageBuilder: () -> String?
        )

        const val RETRY_OPTION = "retry_option"
    }
}
