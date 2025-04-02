package com.yourfitness.pt.ui.features.calendar.confirm

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.yourfitness.common.databinding.DialogTwoActionBinding
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setTextColorRes
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.pt.R
import com.yourfitness.pt.domain.values.SESSION_ID
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.common.R as common
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmSessionDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogTwoActionBinding by viewBinding()

    @Inject
    lateinit var navigator: PtNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(
            binding.toolbar.root,
            getString(R.string.confirm_booking_title),
            dismissId = common.id.close
        )
        setupBottomInsets(binding.root)
        binding.image.setImageResource(com.yourfitness.common.R.drawable.ic_success)
        binding.message.text = getString(R.string.confirm_booking_msg)
        binding.buttonRetry.text = getString(R.string.confirm_booking_action)
        binding.buttonRetry.setOnClickListener {
            val id = requireArguments().getString(SESSION_ID)
            if (id == null)
                dismiss()
            else
                navigator.navigate(PtNavigation.ProcessConfirmSession(id))
        }
        binding.buttonBack.setTextColorRes(com.yourfitness.common.R.color.issue_red)
        binding.buttonBack.text = getString(R.string.confirm_booking_action_negative)
        binding.buttonBack.setOnClickListener { dismiss() }
    }
}
