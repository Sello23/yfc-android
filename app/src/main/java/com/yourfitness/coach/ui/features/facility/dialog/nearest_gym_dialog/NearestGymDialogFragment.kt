package com.yourfitness.coach.ui.features.facility.dialog.nearest_gym_dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.yourfitness.coach.R
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.workTimeData
import com.yourfitness.coach.databinding.DialogNearestGymBinding
import com.yourfitness.coach.domain.date.toDateMmmmDYyyy
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.data.entity.fullName
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.WindowUtils.getDisplayWidth
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.toImageUri
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NearestGymDialogFragment : DialogFragment() {

    private val binding: DialogNearestGymBinding by viewBinding()
    private val viewModel: NearestGymViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onStart() {
        requireDialog().window?.apply {
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            val width = getDisplayWidth(-48)
            setLayout(width, height)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        super.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbarDialog.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
        binding.buttonSuccessfullyPassed.setOnClickListener { dialog?.dismiss() }
        showDialog()
    }

    private fun showDialog() {
        val isPtRole = requireArguments().getBoolean("is_pt_role")
        val gym = requireArguments().get("facility") as FacilityEntity
        val profile = requireArguments().get("profile") as ProfileEntity
        binding.textGymAddress.text = gym.address
        binding.textGymName.text = gym.name
        binding.textGymType.text = gym.types?.joinToString(", ")
        Glide.with(binding.root).load(gym.icon?.toImageUri()).into(binding.imageGymLogo)
        Glide.with(binding.root)
            .load(profile.mediaId?.toImageUri())
            .placeholder(ContextCompat.getDrawable(requireContext(), com.yourfitness.common.R.color.main_active))
            .dontAnimate()
            .into(binding.imageProfile)
        binding.textUsername.text = profile.fullName
        binding.textUsername.isVisible = !isPtRole
        binding.textDate.isVisible = !isPtRole
        binding.ptName.isVisible = isPtRole
        binding.ptLabel.isVisible = isPtRole
        binding.ptName.text = profile.fullName
        binding.textDate.text = profile.birthday?.toDateMmmmDYyyy()
        viewModel.sendFacilityVisitInfo(gym.id, profile.id)
        viewModel.writeVisitFacility(gym)
        binding.buttonSuccessfullyPassed.setOnClickListener {
            viewModel.navigator.navigate(
                if (isPtRole) Navigation.PtDashboard
                else Navigation.Map()
            )
        }

        val workTime = gym.workTimeData
        binding.workTime.root.apply {
            isVisible = workTime != null
            if (workTime != null) {
                text = getString(workTime.textId, workTime.args)
                val theme = requireActivity().theme
                background = ResourcesCompat.getDrawable(resources, workTime.bgId, theme)
                setCompoundDrawables(end = workTime.iconId)
            }
        }
    }

    private fun onMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
    }
}
