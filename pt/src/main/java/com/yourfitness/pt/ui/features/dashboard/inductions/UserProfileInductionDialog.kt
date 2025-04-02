package com.yourfitness.pt.ui.features.dashboard.inductions

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourfitness.common.domain.date.formatEeeeMmmDd
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.R
import com.yourfitness.pt.data.mappers.toEntity
import com.yourfitness.pt.domain.models.InductionInfo
import com.yourfitness.pt.domain.values.CLIENT
import com.yourfitness.pt.domain.values.CLIENT_STRING
import com.yourfitness.pt.network.dto.PtClientDto
import com.yourfitness.pt.ui.features.user_profile.UserProfileBaseDialog
import com.yourfitness.pt.ui.features.user_profile.UserProfileBaseViewModel
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class UserProfileInductionDialog : UserProfileBaseDialog() {

    override val viewModel: UserProfileBaseViewModel by viewModels()

    @Inject
    lateinit var ptNavigator: PtNavigator

    private var induction: InductionInfo? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setupToolbar(
                binding.toolbar.root,
                getString(R.string.induction_details),
                dismissId = com.yourfitness.common.R.id.close
            )
            actionMain.text = getString(R.string.complete_induction)
            actionSecondary.isVisible = false
            userProfile.apply {
                scheduleInfo.root.isVisible = false
                space1.isVisible = false
                separator1.isVisible = false
                sessionsAvailable.root.isVisible = false
                sessionsConducted.root.isVisible = false
            }

            actionMain.setOnClickListener {
                openConfirmDialog()
            }
        }

        induction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(CLIENT, InductionInfo::class.java)
        } else {
            requireArguments().getParcelable(CLIENT)
        }
        if (induction == null) {
            val data = requireArguments().getString(CLIENT_STRING)
            try {
                val type = object : TypeToken<InductionInfo>() {}.type
                induction = Gson().fromJson(data, type)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        induction?.let { showInductionInfo(it) }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.configureDialogView()
        return dialog
    }

    override fun Dialog.configureDialogView() {
        setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            lifecycleScope.launch {
                delay(150)
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun showInductionInfo(client: InductionInfo) {
        binding.apply {
            userProfile.apply {
                client.induction.profileInfo?.let { setupMainInfoFields(it.toEntity()) }
            }

            userProfile.facilityInfo.apply {
                textName.text = client.facilityName
                textAddress.text = client.facilityAddress
                textDate.isVisible = true
                textDate.text = (client.induction.createdAt ?: 0).toMilliseconds().toDate().formatEeeeMmmDd()
                Glide.with(root).load(client.facilityLogo.toImageUri()).into(imageFacility)
            }
        }
    }

    private fun openConfirmDialog() {
        val note = binding.inputContainer.text.toString().trim()
        induction?.let {
            if (requireArguments().getString(CLIENT_STRING) == null) {
                ptNavigator.navigate(PtNavigation.ConfirmCompleteInduction(it, note))
            } else {
                ptNavigator.navigate(PtNavigation.ConfirmCompleteInduction2(Gson().toJson(it), note))
            }
        }
    }
}
