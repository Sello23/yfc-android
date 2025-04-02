package com.yourfitness.coach.ui.features.payments.success_cancel_subscription

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogSuccessCancelSubscriptionBinding
import com.yourfitness.coach.domain.date.formatDateDdMmYyyy
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setCustomFont
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import com.yourfitness.common.R as common

@AndroidEntryPoint
class SuccessCancelSubscriptionDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogSuccessCancelSubscriptionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.success_cancel_subscription_title),
            dismissId = R.id.close
        )
        setupDescription()
        binding.btnGotIt.setOnClickListener { dismiss() }
    }

    private fun setupDescription() {
        val expiredDate = requireArguments().getLong("expired_date", 0)
        val date = Date(expiredDate * 1000).formatDateDdMmYyyy().orEmpty()
        val text = getString(R.string.success_cancel_subscription_description_format, date)
        binding.tvDescription.text = SpannableString(text).apply {
            val startIndex = text.indexOf(date)
            setCustomFont(
                requireContext(),
                startIndex,
                startIndex + date.length,
                common.font.tajawal_bold
            )
        }
    }
}