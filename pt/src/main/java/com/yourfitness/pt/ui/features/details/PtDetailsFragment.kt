package com.yourfitness.pt.ui.features.details

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getString
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.ChipGroup
import com.yourfitness.common.databinding.ItemAmenityBinding
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.R
import com.yourfitness.pt.data.entity.EducationEntity
import com.yourfitness.pt.data.entity.PtCombinedEntity
import com.yourfitness.pt.data.entity.fullName
import com.yourfitness.pt.databinding.FragmentPtDetailsBinding
import com.yourfitness.pt.domain.models.FacilityInfo
import com.yourfitness.pt.ui.features.calendar.CalendarFragment
import com.yourfitness.pt.ui.features.details.adapters.FacilitiesAdapter
import com.yourfitness.pt.ui.features.details.adapters.QualificationAdapter
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.utils.dialPhoneNumber
import com.yourfitness.common.utils.emailTo
import com.yourfitness.common.utils.openUserInstagram

@AndroidEntryPoint
class PtDetailsFragment : MviFragment<PtDetailsIntent, PtDetailsState, PtDetailsViewModel>() {

    override val binding: FragmentPtDetailsBinding by viewBinding()
    override val viewModel: PtDetailsViewModel by viewModels()
    private val facilitiesAdapter: FacilitiesAdapter by lazy { FacilitiesAdapter() }
    private val qualificationAdapter: QualificationAdapter by lazy { QualificationAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.leading.setOnClickListener { findNavController().navigateUp() }
        binding.rvFacilities.adapter = facilitiesAdapter
        binding.rvQualification.adapter = qualificationAdapter
        binding.bookBtn.btnCheckout.setOnClickListener {
            viewModel.intent.value = PtDetailsIntent.BookSessionTapped
        }
        setupCalendarResultListener()
    }

    override fun renderState(state: PtDetailsState) {
        when (state) {
            is PtDetailsState.Loading -> showLoading(true)
            is PtDetailsState.Error -> showLoading(false)
            is PtDetailsState.Loaded -> {
                showGeneralInfo(state.pt)
                showFacilities(state.facilities, state.location)
                showQualifications(state.pt.data.educations.orEmpty())
            }
            is PtDetailsState.FacilitiesUpdated -> {
                showFacilities(state.facilities, state.location, state.expanded)
            }
            is PtDetailsState.GeneralInfoUpdated -> showGeneralInfo(state.pt)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun showGeneralInfo(pt: PtCombinedEntity) {
        Glide.with(requireContext()).load(pt.data.mediaId?.toImageUri()).into(binding.imageProfile)
        binding.textName.text = pt.data.fullName
        val balance = pt.balance?.amount ?: 0
//        pt.data.isBookable?.let {
        isTrainerBookable(false)
//        }
        binding.textLabel.text =
            getString(if (balance == 0) R.string.personal_trainer else R.string.your_personal_trainer)
        binding.textSessions.text = if (balance == 0) getString(R.string.trainer_availability)
        else resources.getQuantityString(R.plurals.available_sessions, balance, balance)
        binding.textSessions.setCompoundDrawables(
            start = if (balance == 0) com.yourfitness.common.R.drawable.ic_calendar_blue
            else R.drawable.ic_barbell
        )

        if (balance == 0) {
            binding.textSessions.setOnClickListener {
                viewModel.intent.value = PtDetailsIntent.OpenAvailabilityCalendar
            }
        }
        binding.infoBloc.originalText = pt.data.description.orEmpty()
        binding.seeMore.isVisible = binding.infoBloc.canExpand()
        binding.infoBloc.setOnClickListener {}
        binding.seeMore.setOnClickListener { processDescriptionTap() }
        binding.telephone.setOnClickListener {
            pt.data.phoneNumber?.let { it1 -> requireContext().dialPhoneNumber(it1) } }
        binding.emailAddress.setOnClickListener {
            pt.data.email?.let { it1 -> requireContext().emailTo(it1) }
        }
        binding.instagram.setOnClickListener {
            pt.data.instagram?.let { it1 -> requireContext().openUserInstagram(it1) }
        }
        showFocusAreas(pt.data.focusAreas.orEmpty())
        binding.bookBtn.btnCheckout.text =
            if (balance == 0) getString(R.string.buy_and_book_session) else getString(R.string.book_session)
    }

    private fun isTrainerBookable(isBookable:Boolean){
        binding.textLabel.isVisible = isBookable
        binding.separator11.isVisible = !isBookable
        binding.contact.isVisible = !isBookable
        binding.separator12.isVisible = !isBookable
        binding.generalInfoLabel.isVisible = !isBookable
        binding.separator1.isVisible = isBookable
        binding.textSessions.isVisible = isBookable
        binding.bookBtn.btnCheckout.isVisible = isBookable
    }

    private fun processDescriptionTap() {
        binding.infoBloc.toggle()
        binding.seeMore.text =
            getString(if (binding.infoBloc.expanded) R.string.hide else R.string.see_more)
    }

    private fun showFocusAreas(items: List<String>) {
        val parent = binding.groupFocusAreas
        parent.removeAllViews()
        items.forEach {
            val binding = ItemAmenityBinding.inflate(layoutInflater, parent, false)
            binding.check.text = it
            parent.addView(binding.root)
        }
        parent.setChildrenEnabled(false)
    }

    private fun showFacilities(
        facilities: List<FacilityInfo>,
        location: LatLng,
        expanded: Boolean = false
    ) {
        val data = if (expanded) facilities else facilities.take(2)
        binding.seeAllBtn.isVisible = facilities.size > 2
        binding.seeAllBtn.text = getString(if (expanded) R.string.hide else common.string.filters_see_all)
        binding.seeAllBtn.setOnClickListener {
            if (expanded) viewModel.intent.value = PtDetailsIntent.FacilitiesSeeLess
            else viewModel.intent.value = PtDetailsIntent.FacilitiesSeeAll
        }
        facilitiesAdapter.setData(data, location)
        facilitiesAdapter.notifyDataSetChanged()
    }

    private fun showQualifications(
        qualifications: List<EducationEntity>,
    ) {
        qualificationAdapter.setData(qualifications)
        qualificationAdapter.notifyDataSetChanged()
    }

    private fun setupCalendarResultListener() {
        setFragmentResultListener(CalendarFragment.RESULT) { _, _ ->
            viewModel.intent.postValue(PtDetailsIntent.UpdateGeneralInfo)
            setupCalendarResultListener()
            clearFragmentResultListener(CalendarFragment.RESULT)
        }
    }
}

fun ChipGroup.setChildrenEnabled(enable: Boolean) {
    children.forEach { it.isEnabled = enable }
}
