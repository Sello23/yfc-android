package com.yourfitness.pt.ui.features.user_profile

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.toAge
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.common.utils.dialPhoneNumber
import com.yourfitness.common.utils.emailTo
import com.yourfitness.pt.R
import com.yourfitness.pt.data.entity.ProfileInfo
import com.yourfitness.pt.databinding.DialogUserProfileActionsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class UserProfileBaseDialog :
    MviBottomSheetDialogFragment<UserProfileBaseIntent, UserProfileBaseState, UserProfileBaseViewModel>() {

    override val binding: DialogUserProfileActionsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.configureDialogView()
        return dialog
    }

    override fun Dialog.configureDialogView() {
        setOnShowListener { dialogInterface ->
            setupBottomInsets(binding.root)
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            lifecycleScope.launch {
                delay(150)
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun renderState(state: UserProfileBaseState) {
        when (state) {
            is UserProfileBaseState.Loading -> showLoading(true)
            is UserProfileBaseState.Error -> showLoading(false)
        }
    }

    protected fun setupMainInfoFields(profileInfo: ProfileInfo) {
        binding.userProfile.apply {
            Glide.with(requireContext()).load(profileInfo.mediaId.toImageUri()).into(avatar)
            mainInfo.text = getString(
                R.string.pt_facility_info,
                profileInfo.fullName,
                profileInfo.birthday.toAge().toString()
            )
            email.text = profileInfo.email
            email.setOnClickListener {
                requireContext().emailTo(profileInfo.email)
            }
            phoneNumber.text = profileInfo.phoneNumber
            phoneNumber.setOnClickListener {
                requireContext().dialPhoneNumber(profileInfo.phoneNumber)
            }
        }
    }
}
