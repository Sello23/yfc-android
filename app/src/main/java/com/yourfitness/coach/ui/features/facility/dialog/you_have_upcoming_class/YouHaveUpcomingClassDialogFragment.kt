package com.yourfitness.coach.ui.features.facility.dialog.you_have_upcoming_class

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogYouHaveUpcomingClassBinding
import com.yourfitness.coach.domain.models.BookedClass
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.domain.date.toDateDayOfWeekMonth
import com.yourfitness.common.domain.date.toDateTime
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.network.dto.ProfileResponse
import com.yourfitness.common.ui.utils.toImageUri
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class YouHaveUpcomingClassDialogFragment : BottomSheetDialogFragment() {

    private val binding: DialogYouHaveUpcomingClassBinding by viewBinding(createMethod = CreateMethod.BIND)
    private val viewModel: YouHaveUpcomingClassViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_you_have_upcoming_class, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbar.title = getString(R.string.map_screen_you_have_upcoming_class)
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
        observeIntent()
        setupCardView()
        showLoading(false)
        observeState()
    }

    private fun setupCardView() {
        val fitnessClass = requireArguments().get("facility") as BookedClass
        val profile = requireArguments().get("profile") as ProfileEntity
        binding.viewBookedClass.apply {
            viewSeparator.isVisible = false
            actionLabel.isVisible = false
            title.text = fitnessClass.className
            subtitle.text = fitnessClass.coachName
            address.text = fitnessClass.address
            info.text = fitnessClass.facilityName
            date.text = fitnessClass.date.toDateDayOfWeekMonth()
            time.text = fitnessClass.time.toDateTime()
            Glide.with(requireContext()).load(fitnessClass.icon.toImageUri())
                .into(binding.viewBookedClass.imageIcon)
        }
        binding.buttonGiveAccess.setOnClickListener {
            dialog?.hide()
            viewModel.navigator.navigate(Navigation.UpcomingClass(fitnessClass, profile))
        }
        binding.textGoToGym.setOnClickListener { viewModel.getSubscription() }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) {
            if (it != null) {
                renderState(it)
            }
        }
    }

    fun renderState(state: Any) {
        when (state) {
            is DialogState.Loading -> showLoading(true)
            is DialogState.BookedClassError -> showBookedClassError()
            is DialogState.NearestGym -> showAreYouHereDialog(state)
            is DialogState.LoadSubscription -> findGym(state)
            is DialogState.SubscriptionError -> showSubscriptionError()
        }
    }

    private fun showAreYouHereDialog(state: DialogState.NearestGym) {
        showLoading(false)
        viewModel.navigator.navigate(Navigation.AreYouHereRightNow(state.item, state.profile, state.latLng))
    }

    private fun showBookedClassError() {
        viewModel.navigator.navigate(Navigation.BitQuitHere)
    }

    private fun showSubscriptionError() {
        showLoading(false)
        dialog?.hide()
        viewModel.navigator.navigate(Navigation.SubscriptionError)
    }

    private fun findGym(state: DialogState.LoadSubscription) {
        showLoading(false)
        if (state.subscription.autoRenewal) {
            viewModel.findNearestGym()
            dialog?.hide()
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

    fun showLoading(isLoading: Boolean) {
        val progress = requireActivity().findViewById<View>(R.id.progress)
        progress.isVisible = isLoading
    }
}