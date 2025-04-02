package com.yourfitness.pt.ui.features.dashboard.clients

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.pt.R
import com.yourfitness.pt.data.mappers.toEntity
import com.yourfitness.pt.domain.values.CLIENT
import com.yourfitness.pt.network.dto.PtClientDto
import com.yourfitness.pt.ui.features.user_profile.UserProfileBaseDialog
import com.yourfitness.pt.ui.features.user_profile.UserProfileBaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileClientDialog : UserProfileBaseDialog() {

    override val viewModel: UserProfileBaseViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setupToolbar(
                binding.toolbar.root,
                getString(R.string.client_details),
                dismissId = com.yourfitness.common.R.id.close
            )
            inputContainer.isVisible = false
            actionMain.isVisible = false
            actionSecondary.isVisible = false
            userProfile.apply {
                scheduleInfo.root.isVisible = false
                facilityInfo.root.isVisible = false
                space1.isVisible = false
                space2.isVisible = false
                sessionsAvailable.root.isVisible = true
                sessionsConducted.root.isVisible = true
            }
        }

        val client = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(CLIENT, PtClientDto::class.java)
        } else {
            requireArguments().getParcelable(CLIENT)
        }
        client?.let { showClientInfo(it) }
    }

    private fun showClientInfo(client: PtClientDto) {
        binding.apply {
            toolbar.root.title = getString(R.string.client_details)
            userProfile.apply {
                client.profileInfo?.let { setupMainInfoFields(it.toEntity()) }

                sessionsAvailable.apply {
                    title.text = getString(R.string.sessions_available_label)
                    amount.text = (client.remainingSessions ?: 0).toString()
                    amount.setCompoundDrawables(start = R.drawable.ic_barbell)
                }

                sessionsConducted.apply {
                    title.text = getString(R.string.sessions_conducted_label)
                    amount.text = (client.conductedSessions ?: 0).toString()
                }
            }
        }
    }
}
