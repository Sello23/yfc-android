package com.yourfitness.pt.ui.features.dashboard.inductions

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.yourfitness.common.domain.date.formatEeeeMmmDd
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.toAge
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.common.utils.dialPhoneNumber
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.DialogConfirmCompleteInductionBinding
import com.yourfitness.pt.domain.models.InductionInfo
import com.yourfitness.pt.domain.values.CONFIRMED
import com.yourfitness.pt.network.dto.ProfileInfoDto
import com.yourfitness.pt.network.dto.fullName
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConfirmCompleteInductionDialog :
    MviBottomSheetDialogFragment<ConfirmCompleteInductionIntent, ConfirmCompleteInductionState, ConfirmCompleteInductionViewModel>() {

    override val viewModel: ConfirmCompleteInductionViewModel by viewModels()
    override val binding: DialogConfirmCompleteInductionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.apply {
            toolbar.root.background = ContextCompat.getDrawable(
                requireContext(),
                com.yourfitness.common.R.drawable.background_grey_toolbar_bottom_sheet
            )
            toolbar.textTitle.text = getString(R.string.confirm_complete_induction_title)
            actionMain.text = getString(R.string.confirm_induction)
            actionMain.setOnClickListener {
                viewModel.intent.value = ConfirmCompleteInductionIntent.OnConfirmClick
            }
            toolbar.btnClose.setOnClickListener {
                requireDialog().dismiss()
            }
        }
    }

    override fun renderState(state: ConfirmCompleteInductionState) {
        when (state) {
            is ConfirmCompleteInductionState.Loading -> showLoading(true)
            is ConfirmCompleteInductionState.Error -> showLoading(false)
            is ConfirmCompleteInductionState.Loaded -> showInductionInfo(state.induction)
        }
    }

    private fun showInductionInfo(client: InductionInfo) {
        binding.apply {
            userProfile.apply {
                client.induction.profileInfo?.let { setupMainInfoFields(it) }
            }

            facilityInfo.apply {
                textName.text = client.facilityName
                textAddress.text = client.facilityAddress
                textDate.isVisible = true
                textDate.text = (client.induction.createdAt ?: 0).toMilliseconds().toDate().formatEeeeMmmDd()
                Glide.with(root).load(client.facilityLogo.toImageUri()).into(imageFacility)
            }
        }
    }


    private fun setupMainInfoFields(profileInfo: ProfileInfoDto) {
        binding.apply {
            Glide.with(requireContext()).load(profileInfo.mediaId?.toImageUri()).into(avatar)
            mainInfo.text = getString(
                R.string.pt_facility_info,
                profileInfo.fullName,
                profileInfo.birthday?.toAge().toString()
            )
            phoneNumber.text = profileInfo.phoneNumber
            phoneNumber.setOnClickListener {
                profileInfo.phoneNumber?.let { it1 -> requireContext().dialPhoneNumber(it1) }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(RESULT, bundleOf(CONFIRMED to viewModel.isConfirmed))
    }

    companion object {
        const val RESULT = "confirm_complete_dialog_result"
    }
}
