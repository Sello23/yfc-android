package com.yourfitness.pt.ui.features.training_calendar.actions

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.yourfitness.common.domain.date.toDateDayOfWeekMonthMs
import com.yourfitness.common.domain.date.toDateTimeMs
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.toAge
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.common.utils.dialPhoneNumber
import com.yourfitness.common.utils.emailTo
import com.yourfitness.pt.R
import com.yourfitness.pt.data.entity.ProfileInfo
import com.yourfitness.pt.data.entity.SessionEntity
import com.yourfitness.pt.network.dto.PtClientDto
import com.yourfitness.pt.ui.features.user_profile.UserProfileBaseDialog
import com.yourfitness.pt.ui.features.user_profile.UserProfileBaseIntent
import com.yourfitness.pt.ui.features.user_profile.UserProfileBaseState
import com.yourfitness.pt.ui.features.user_profile.UserProfileBaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileActionsDialog : UserProfileBaseDialog() {

    override val viewModel: UserProfileActionsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(
            binding.toolbar.root,
            getString(viewModel.flowData.tittle),
            dismissId = com.yourfitness.common.R.id.close
        )

        binding.apply {
            inputContainer.isVisible = false
            userProfile.apply {
                sessionsAvailable.root.isVisible = false
                sessionsConducted.root.isVisible = false
            }
        }

        binding.actionMain.apply {
            text = getString(viewModel.flowData.actionMain)
            setOnClickListener {
                viewModel.intent.value = UserProfileBaseIntent.MainActionButtonClicked
            }
        }

        binding.actionSecondary.apply {
            visibility = if (viewModel.flowData.actionSecondary == null) View.GONE else View.VISIBLE
            text = viewModel.flowData.actionSecondary?.let { getString(it) }
            setOnClickListener {
                viewModel.intent.value = UserProfileActionsIntent.SecondaryActionButtonClicked
            }
        }
    }

    override fun renderState(state: UserProfileBaseState) {
        when (state) {
            is UserProfileActionsState.DataLoaded -> setupViews(state.session, state.facilityData)
        }
    }

    private fun setupViews(session: SessionEntity, facilityData: Triple<String, String, String>) {
        binding.userProfile.apply {
            setupMainInfoFields(session.profileInfo)

            scheduleInfo.apply {
                time.text = requireContext().getString(
                    com.yourfitness.common.R.string.training_calendar_screen_time_interval,
                    session.from.toDateTimeMs(),
                    session.to.toDateTimeMs()
                )
                date.text = session.from.toDateDayOfWeekMonthMs()
            }

            facilityInfo.apply {
                textName.text = facilityData.first
                textAddress.text = facilityData.third
                Glide.with(root).load(facilityData.second.toImageUri()).into(imageFacility)
            }
        }
    }
}
