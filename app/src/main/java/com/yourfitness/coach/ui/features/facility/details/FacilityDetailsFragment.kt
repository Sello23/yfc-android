package com.yourfitness.coach.ui.features.facility.details

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.yourfitness.coach.data.entity.ClassEntity
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.classTypes
import com.yourfitness.coach.data.entity.classes
import com.yourfitness.coach.data.entity.timetable
import com.yourfitness.coach.data.entity.workTimeData
import com.yourfitness.coach.databinding.FragmentFacilityDetailsBinding
import com.yourfitness.coach.databinding.ItemFacilityClassBinding
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.features.facility.details.pt_list.FacilityPersonalTrainersAdapter
import com.yourfitness.coach.ui.features.facility.map.REQUEST_CODE_COMMON
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.databinding.ItemAmenityBinding
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.PagerIndicator
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.common.ui.views.GalleryAdapter
import com.yourfitness.pt.domain.models.TrainerCard
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class FacilityDetailsFragment :
    MviFragment<FacilityDetailsIntent, Any, FacilityDetailsViewModel>() {

    override val binding: FragmentFacilityDetailsBinding by viewBinding()
    override val viewModel: FacilityDetailsViewModel by viewModels()
    private val pagerIndicator by lazy { PagerIndicator() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomInsets(binding.bottomBar)
        setupToolbar(binding.toolbar)
        showDetails(viewModel.facility)
        showGymAccess(false)
        pagerIndicator.setupViewpager(binding.indicators, binding.pagerGallery)
        binding.buttonGetAccess.setOnClickListener {
            viewModel.navigator.navigate(Navigation.Subscription(PaymentFlow.BUY_SUBSCRIPTION_FROM_FACILITY))
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            setFragmentResult(REQUEST_CODE_COMMON, bundleOf())
            findNavController().navigateUp()
        }

        binding.toolbar.setNavigationOnClickListener {
            setFragmentResult(REQUEST_CODE_COMMON, bundleOf())
            findNavController().navigateUp()
        }
    }

    private fun showDetails(item: FacilityEntity) {
        showGallery(item.gallery)
        showTags(item.amenities)
        if (viewModel.classification == Classification.STUDIO) {
            showClasses(item.classes)
        }
        binding.textTitle.text = item.name
        binding.textAddress.text = item.address
        binding.textDescription.text = item.description
        binding.textLadies.isVisible = item.femaleOnly ?: false
        binding.textTypes.text = when (viewModel.classification) {
            Classification.GYM -> item.types?.take(3)?.joinToString(", ")
            else -> item.classTypes.sorted().take(3).joinToString(", ")
        }
        val workTime = item.workTimeData
        binding.workTime.root.apply {
            isVisible = workTime != null && viewModel.classification == Classification.GYM
            if (workTime != null && viewModel.classification == Classification.GYM) {
                text = getString(workTime.textId, workTime.args)
                val theme = requireActivity().theme
                background = ResourcesCompat.getDrawable(resources, workTime.bgId, theme)
                setCompoundDrawables(end = workTime.iconId)
                setOnClickListener {
                    viewModel.navigator.navigate(Navigation.TimetableInfo(item.timetable))
                }
            }
        }
        Glide.with(this).load(item.icon?.toImageUri()).into(binding.imageIcon)
    }

    private fun showGallery(items: List<String>?) {
        val gallery = items ?: emptyList()
        binding.indicators.isVisible = gallery.size > 1
        binding.pagerGallery.adapter = GalleryAdapter(gallery)
    }

    private fun showTags(items: List<String>?) {
        val parent = binding.chipGroupAmenities
        parent.removeAllViews()
        items.orEmpty().forEach {
            val binding = ItemAmenityBinding.inflate(layoutInflater, parent, false)
            binding.check.text = it
            parent.addView(binding.root)
        }
    }

    private fun showClasses(items: List<ClassEntity>) {
        val parent = binding.listClasses
        parent.removeAllViews()
        items.forEach { classEntity ->
            val binding = ItemFacilityClassBinding.inflate(layoutInflater, parent, false)
            binding.root.text = classEntity.name
            binding.root.setOnClickListener {
                classEntity.id?.let { classId -> navigateToCalendar(classId, classEntity) }
            }
            parent.addView(binding.root)
        }
    }

    private fun navigateToCalendar(classId: String, classEntity: ClassEntity) {
        viewModel.navigator.navigate(
            Navigation.BookingClassCalendar(
                classId,
                classEntity.name,
                viewModel.facility,
                false,
                null
            )
        )
    }

    private fun showGymAccess(hasGymAccess: Boolean) {
        binding.bottomBar.isVisible = hasGymAccess
    }

    private fun showItems(
        ptItems: List<TrainerCard>,
        expandedStates: Map<String, Boolean>,
        onExpandedClick: (String, Int) -> Unit
    ) {
        binding.listItems.adapter = FacilityPersonalTrainersAdapter(
            ptItems,
            expandedStates,
            ::onPtDetailsClicked,
            onExpandedClick
        )
    }

    private fun onPtDetailsClicked(item: TrainerCard) {
        viewModel.intent.value = FacilityDetailsIntent.PtDetailsTapped(item.id)
    }

    private fun onPtExpandedClicked(id: String, pos: Int) {
        viewModel.intent.value = FacilityDetailsIntent.UpdatePtExpandedState(id, pos)
    }

    private fun updatePtExpandedStates(expandedStates: Map<String, Boolean>, pos: Int) {
        try {
            (binding.listItems.adapter as FacilityPersonalTrainersAdapter).apply {
                setData(expandedStates)
                notifyItemChanged(pos)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkGymAccess()
    }

    override fun renderState(state: Any) {
        when (state) {
            is FacilityDetailsState.Loading -> showLoading(true)
            is FacilityDetailsState.GymAccess -> showGymAccess(state.hasAccess)
            is FacilityDetailsState.FacilitiesLoaded -> {
                showLoading(false)
                if(state.ptItems.isNotEmpty()) {
                    binding.facilityTrainerSections.isVisible = true
                    showItems(state.ptItems, state.expandedStates, ::onPtExpandedClicked)
                }
            }

            is FacilityDetailsState.UpdatePtExpandedStates -> updatePtExpandedStates(
                state.expandedStates,
                state.pos
            )

            is FacilityDetailsState.Error -> showError(state.error)
        }
    }
}