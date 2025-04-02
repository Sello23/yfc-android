package com.yourfitness.coach.ui.features.facility.dialog.upcoming_class

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogUpcomingClassBinding
import com.yourfitness.coach.domain.date.toDateMmmmDYyyy
import com.yourfitness.coach.domain.models.BookedClass
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.data.entity.fullName
import com.yourfitness.common.ui.utils.toImageUri
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpcomingClassDialogFragment : DialogFragment() {

    private val binding: DialogUpcomingClassBinding by viewBinding(createMethod = CreateMethod.BIND)
    private val viewModel: UpcomingClassViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_upcoming_class, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbarDialog.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
        binding.buttonSuccessfullyPassed.setOnClickListener { dialog?.dismiss() }
        observeIntent()
        showDialog()
    }

    private fun showDialog() {
        val gym = requireArguments().get("facility") as BookedClass
        val profile = requireArguments().get("profile") as ProfileEntity
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.textGymAddress.text = gym.address
        binding.textClassName.text = gym.className
        binding.textCoachName.text = gym.coachName
        binding.textGymType.text = gym.facilityName
        Glide.with(binding.root).load(gym.icon.toImageUri()).into(binding.imageGymLogo)
        Glide.with(binding.root).load(profile.mediaId?.toImageUri()).into(binding.imageProfile)
        binding.textUsername.text = profile.fullName
        binding.textDate.text = profile.birthday?.toDateMmmmDYyyy()
        viewModel.sendFacilityVisitInfo(gym.facilityId, profile.id)
        viewModel.writeVisitFacility(gym.facilityId)
        binding.buttonSuccessfullyPassed.setOnClickListener {
            viewModel.navigator.navigate(Navigation.Map())
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