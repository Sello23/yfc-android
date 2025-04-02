package com.yourfitness.coach.ui.features.facility.dialog.are_you_here_right_now

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.yourfitness.coach.R
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.isAvailable
import com.yourfitness.coach.data.entity.timetable
import com.yourfitness.coach.data.entity.workTimeData
import com.yourfitness.coach.databinding.DialogAreYouHereRightNowBinding
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.isFragmentInBackStack
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.toImageUri
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AreYouHereRightNowDialogFragment : MviDialogFragment<AreYouHereRightNowIntent, AreYouHereRightNowState, AreYouHereRightNowViewModel>() {

    override val binding: DialogAreYouHereRightNowBinding by viewBinding()
    override val viewModel: AreYouHereRightNowViewModel by viewModels()

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        return inflater.inflate(R.layout.dialog_are_you_here_right_now, container, false)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomInsets(binding.root)
        binding.toolbar.toolbar.title = getString(R.string.map_screen_are_you_here_text)
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
        observeIntent()
        showDialog()
    }

    private fun showDialog() {
        val gym = requireArguments().get("facility") as FacilityEntity
        val profile = requireArguments().get("profile") as ProfileEntity
        val latLng = requireArguments().get("latLng") as LatLng
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.infoMessageContainer.root.isVisible = gym.displayAccessLimitationMessage
        binding.infoMessageContainer.stubMessage.text = gym.accessLimitationMessage?.trim()

        val workTime = gym.workTimeData
        binding.workTime.root.apply {
            isVisible = workTime != null
            if (workTime != null) {
                text = getString(workTime.textId, workTime.args)
                val theme = requireActivity().theme
                background = ResourcesCompat.getDrawable(resources, workTime.bgId, theme)
                setCompoundDrawables(end = workTime.iconId)
            }
            setOnClickListener {
                viewModel.navigator.navigate(Navigation.TimetableInfo(gym.timetable))
            }
        }

        binding.textGymAddress.text = gym.address
        binding.textGymName.text = gym.name
        binding.textGymType.text = gym.types?.joinToString(", ")
        Glide.with(binding.root).load(gym.icon?.toImageUri()).into(binding.imageGymLogo)
        Glide.with(binding.root).load(gym.gallery?.first()?.toImageUri()).into(binding.imageGym)
        val isGymAvailable = gym.isAvailable
        binding.buttonYesWantToGo.apply {
            setOnClickListener {
                if (isGymAvailable) {
                    viewModel.intent.value = AreYouHereRightNowIntent.ShowAccessCard(gym, profile)
                    dialog?.hide()
                } else {
                    onImNotHere(latLng, profile)
                    text = getString(R.string.map_screen_no_im_not_here_text)
                }
            }
            if (!isGymAvailable) text = getString(R.string.map_screen_no_im_not_here_text)
        }

        binding.textNoImNotHere.apply {
            isVisible = isGymAvailable
            setOnClickListener { onImNotHere(latLng, profile) }
        }
    }

    private fun onImNotHere(
        latLng: LatLng,
        profile: ProfileEntity
    ) {
        if (findNavController().isFragmentInBackStack(R.id.fragment_manual_search)) {
            dialog?.dismiss()
        } else {
            viewModel.navigator.navigate(Navigation.ManualSearch(latLng, profile))
        }
    }

    private fun onMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
    }

    private fun observeIntent() {
        viewModel.intent.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.intent.value = null
                viewModel.handleIntent(it)
            }
        }
    }
}
