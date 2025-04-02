package com.yourfitness.shop.ui.features.orders.details.services

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import com.yourfitness.common.databinding.DialogOneActionBinding
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setCustomFont
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.shop.R
import com.yourfitness.shop.ui.navigation.ShopNavigator
import javax.inject.Inject

class VoucherClaimedSuccessfullyDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogOneActionBinding by viewBinding()

    @Inject
    lateinit var navigator: ShopNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.voucher_claimed_title),
            dismissId = com.yourfitness.common.R.id.close
        )
        binding.buttonConfirm.setOnClickListener { dismiss() }

        val name = requireArguments().getString(NAME).orEmpty()
        val number = requireArguments().getString(NUMBER).orEmpty()
        binding.message.text = getString(R.string.voucher_claimed_msg, name, number).buildSpannedText(name, number)
    }

    private fun String.buildSpannedText(span1: String, span2: String): SpannableString {
        return SpannableString(this).apply {
            val span1Start = indexOf(span1)
            val span2Start = indexOf(span2)
            setCustomFont(requireContext(), span1Start, span1Start + span1.length, com.yourfitness.common.R.font.work_sans_bold)
            setCustomFont(requireContext(), span2Start, span2Start + span2.length, com.yourfitness.common.R.font.work_sans_bold)
        }
    }

    companion object {
        const val NAME = "name"
        const val NUMBER = "number"
    }
}
