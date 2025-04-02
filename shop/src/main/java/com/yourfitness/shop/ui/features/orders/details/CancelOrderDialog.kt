package com.yourfitness.shop.ui.features.orders.details

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.yourfitness.common.databinding.DialogErrorOneActionBinding
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.common.ui.utils.formatCoins
import com.yourfitness.common.ui.utils.setCustomFont
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.shop.R
import com.yourfitness.shop.ui.constants.Constants.Companion.CURRENCY
import com.yourfitness.shop.ui.navigation.ShopNavigator
import javax.inject.Inject
import com.yourfitness.common.R as common

const val REQUEST_CONFIRM_CANCEL = "129"
const val CANCEL_CONFIRMED = "cancel_confirmed"

class CancelOrderDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogErrorOneActionBinding by viewBinding()

    @Inject
    lateinit var navigator: ShopNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.cancel_request_title),
            dismissId = common.id.close
        )

        binding.message.text = buildRefundMessageText()
        binding.buttonConfirm.text = getString(R.string.cancel_request_action)
        binding.buttonConfirm.setOnClickListener {
            setFragmentResult(REQUEST_CONFIRM_CANCEL, bundleOf(CANCEL_CONFIRMED to true))
            findNavController().navigateUp()
        }
    }

    private fun buildRefundMessageText(): SpannableString {
        val totalCoins = requireArguments().getLong("total_coins")
        val totalPrice = requireArguments().getLong("total_price")
        val currency = requireArguments().getString(CURRENCY).orEmpty()
        var reddemData = totalPrice.formatAmount(currency).uppercase()
        if (totalCoins > 0) {
            reddemData += " + ${
                resources.getQuantityString(
                    common.plurals.profile_screen_format_coins,
                    totalCoins.toInt(),
                    totalCoins.formatCoins()
                )
            }"
        }
        val text = getString(R.string.cancel_request_msg, reddemData)

        return SpannableString(text).apply {
            val start = text.indexOf(reddemData)
            setCustomFont(requireContext(), start, start + reddemData.length, common.font.tajawal_bold)
        }
    }
}
