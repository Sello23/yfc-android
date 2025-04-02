package com.yourfitness.coach.ui.features.facility.map

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.maps.android.SphericalUtil.computeDistanceBetween
import com.yourfitness.coach.R
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.classTypes
import com.yourfitness.coach.data.entity.isAvailable
import com.yourfitness.coach.data.entity.toLatLng
import com.yourfitness.coach.data.entity.workTimeData
import com.yourfitness.coach.databinding.FragmentMapBinding
import com.yourfitness.coach.databinding.ItemFacilityDetailsCardBinding
import com.yourfitness.coach.databinding.ItemMarkerBinding
import com.yourfitness.coach.databinding.ItemMarkerSelectedBinding
import com.yourfitness.coach.domain.facility.Filters
import com.yourfitness.coach.domain.facility.isEmpty
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.utils.CurrentLocation
import com.yourfitness.coach.ui.utils.setupMapInsets
import com.yourfitness.coach.ui.utils.toBitmap
import com.yourfitness.coach.ui.utils.toPx
import com.yourfitness.common.databinding.ViewWorkTimeBinding
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.addOnTabSelectionListener
import com.yourfitness.common.ui.utils.formatDistance
import com.yourfitness.common.ui.utils.selectTab
import com.yourfitness.common.ui.utils.setShowSideItems
import com.yourfitness.common.ui.utils.setupTopInsets
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.databinding.ItemPtDetailsCardBinding
import com.yourfitness.pt.databinding.ViewPtShortcutBinding
import com.yourfitness.pt.databinding.ViewPtShortcutOtherBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.math.min
import com.yourfitness.common.R as common
import com.yourfitness.pt.R as pt
import java.util.Timer
import kotlin.concurrent.schedule

const val REQUEST_CODE_FILTERS = "0"
const val REQUEST_CODE_COMMON = "1"
const val REQUEST_CODE_DETAILS = "2"
private val DEFAULT_MAP_PADDING = 128.toPx()

@AndroidEntryPoint
class MapFragment : MviFragment<Any, Any, MapViewModel>() {

    override val binding: FragmentMapBinding by viewBinding()
    override val viewModel: MapViewModel by viewModels()

    private val markers = mutableMapOf<Marker, FacilityEntity>()
    private var map: GoogleMap? = null

    private var selectedMarker: Marker? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        setupScreenInsets()
        showTab(viewModel.classification)
        showFiltersButton(viewModel.filters)
        setupListeners()
        binding.buttonList.isVisible = false
        binding.buttonLocation.isVisible = hasLocationPermissions()
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
    }

    override fun onStart() {
        super.onStart()
        if (!binding.buttonList.isVisible) {
            viewModel.refreshListButton()
        }
    }

    override fun onStop() {
        super.onStop()
        val cameraBounds = map?.projection?.visibleRegion?.latLngBounds
        val currentMapPosition = map?.cameraPosition
        if (markers.any { cameraBounds?.contains(it.key.position) == true }) {
            CurrentLocation.currentLocation = currentMapPosition
            CurrentLocation.currentTab = binding.toolbar.tabLayout.selectedTabPosition
        } else {
            CurrentLocation.currentLocation = null
            CurrentLocation.currentTab = null
        }
    }

    private fun onMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.filters -> openFilters()
        }
    }

    private fun openFilters() {
        setFragmentResultListener(REQUEST_CODE_FILTERS) { key, bundle ->
            if (key == REQUEST_CODE_FILTERS) {
                val filters = bundle["filters"] as Filters
                viewModel.intent.postValue(MapIntent.UpdateFilters(filters))
                showFiltersButton(filters)
                showCard(null)
                if (!binding.buttonList.isVisible) {
                    viewModel.refreshListButton()
                }
            }
            clearFragmentResultListener(REQUEST_CODE_FILTERS)
        }
        viewModel.navigator.navigate(Navigation.Filter(selectedTab(), viewModel.filters))
    }

    private fun showFiltersButton(filters: Filters = Filters()) {
        val icon = if (filters.isEmpty()) common.drawable.ic_toolbar_filter else common.drawable.ic_toolbar_filter_selected
        val filtersItem = binding.toolbar.toolbar.menu.findItem(R.id.filters)
        filtersItem.setIcon(icon)
    }

    @SuppressLint("PotentialBehaviorOverride", "MissingPermission")
    private fun onMapReady(map: GoogleMap) {
        this.map = map
        map.isMyLocationEnabled = hasLocationPermissions()
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.setOnMarkerClickListener {
            markers[it]?.let { marker -> onMarkerClicked(it, marker) } ?: false
        }
        map.setOnMapClickListener { onMapClicked() }
    }

    private fun onMapClicked() {
        val ieAvailable = viewModel.selectedFacility?.isAvailable ?: false
        showMarker(null, null, ieAvailable)
        showCard(null)
    }

    private fun onMarkerClicked(marker: Marker, item: FacilityEntity): Boolean {
        onMapClicked()
        showMarker(marker, item)
        if (viewModel.classification != Classification.TRAINER) {
            showCard(item)
        }
        viewModel.intent.value = MapIntent.LoadFacilityTrainers(item)
        return false
    }

    private fun onCardClicked() {
        val item = viewModel.selectedFacility
        if (item != null) {
            registerResultListener()
            viewModel.state.value = null
            viewModel.navigator.navigate(Navigation.FacilityDetails(item, selectedTab()))
        }
    }

    private fun onLocationClicked() {
        viewModel.intent.postValue(MapIntent.MyLocation)
    }

    private fun onTabSelected(tab: TabLayout.Tab) {
        showCard(null)
        showFiltersButton()
        when (tab.position) {
            0 -> {
                viewModel.intent.postValue(MapIntent.Load(Classification.GYM))
//                binding.groupBannerCreditsMap.isVisible = false // TODO temporary removed Studios
//                viewModel.getSubscription()
            }
            1 -> {
                viewModel.intent.postValue(MapIntent.Load(Classification.TRAINER))
                binding.groupBannerSubscribeMap.isVisible = false
                viewModel.getCredits()
            }
            2 -> {
                viewModel.intent.postValue(MapIntent.Load(Classification.STUDIO))
                binding.groupBannerSubscribeMap.isVisible = false
            }
        }
        CurrentLocation.currentLocation = null
    }

    override fun renderState(state: Any) {
        when (state) {
            is MapState.Loading -> showLoading(true)
            is MapState.FacilitiesLoaded -> showItems(state.items, state.zoomAll, state.selectedFacility)
            is MapState.MyLocation -> showMyLocation(state.location)
            is MapState.Error -> showError(state.error)
            is MapState.SubscriptionValue -> showSubscribeBanner(state)
            is MapState.SubscriptionError -> showSubscriptionError()
            is MapState.Credits -> showCreditsBanner(state)
            is MapState.FacilityTrainersLoaded -> {
                showLoading(false)
                val marker = markers.entries.find { it.value.id == state.facility.id }?.key
                showMarker(marker, state.facility)
                showCard(state.facility, state.trainers)
            }
        }
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(::onMapReady)
    }

    private fun setupScreenInsets() {
        setupTopInsets(binding.toolbar.toolbar)
        val fromNotification = arguments?.getBoolean("notification") ?: false
        if (!fromNotification) {
            binding.rootContainer.setupMapInsets(binding.toolbar.toolbar)
        }
    }

    private fun showItems(items: List<FacilityEntity>, zoomAll: Boolean, selectedFacility: FacilityEntity?) {
        showLoading(false)
        val trainersCount = items.mapNotNull { it.personalTrainersIds }.flatten().toSet().size
        showViewButton(items.size, trainersCount)
        showCard(selectedFacility)
        replaceMarkers(items)
        if (CurrentLocation.currentLocation != null && CurrentLocation.currentTab == binding.toolbar.tabLayout.selectedTabPosition) {
            CurrentLocation.currentLocation?.let {
                map?.moveCamera(CameraUpdateFactory.newCameraPosition(it))
            }
        } else {
            zoomToShowAll(markers, zoomAll)
        }
        selectedMarker = null
        if (items.isEmpty()) {
            viewModel.navigator.navigate(Navigation.NoResult(selectedTab()))
        }
    }

    private fun showTab(classification: Classification) {
        val position = when (classification) {
            Classification.GYM -> 0
            Classification.TRAINER -> 1
            Classification.STUDIO -> 2
        }
        binding.toolbar.tabLayout.selectTab(position)
    }

    private fun showViewButton(count: Int, ptCount: Int) {
        val selectedTab = binding.toolbar.tabLayout.selectedTabPosition
        val itemCountFormat = when (selectedTab) {
            0 -> R.string.map_screen_gyms_count_format
            1 -> R.string.map_screen_trainers_count_format
            else -> R.string.map_screen_studios_count_format
        }
        binding.buttonList.isVisible = true
        val countStringFormatted = if (selectedTab == 1) {
            if (ptCount == 0) {
                binding.buttonList.text = getString(pt.string.no_trainers_msg)
                return
            }
            getString(itemCountFormat, count, ptCount)
        } else {
            getString(itemCountFormat, count)
        }
        binding.buttonList.text = getString(
            R.string.map_screen_view_button_format,
            getString(R.string.map_screen_list_view),
            countStringFormatted
        )
    }

    private fun showCard(item: FacilityEntity?, trainers: PtShortcut = mapOf()) {
        val binding =
            if (viewModel.classification == Classification.TRAINER) binding.cardPt
            else binding.cardFacility

        if (item != null) {
            if (viewModel.classification == Classification.TRAINER) {
                configurePtCard(item, trainers, binding as ItemPtDetailsCardBinding)
            } else {
                configureFacilityCard(item, binding as ItemFacilityDetailsCardBinding)
            }
            BottomSheetBehavior.from(binding.root).state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            BottomSheetBehavior.from(binding.root).state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun configureFacilityCard(item: FacilityEntity, binding: ItemFacilityDetailsCardBinding) {
        binding.root.isVisible = true
        binding.textName.text = item.name?.trim()
        binding.textAddress.text = item.address
        binding.textDistance.text = computeDistanceBetween(item.toLatLng(), viewModel.location).formatDistance()
        binding.textLadies.isVisible = item.femaleOnly ?: false
        binding.textDescription.text = when (selectedTab()) {
            Classification.GYM -> item.types?.take(3)?.joinToString(", ")
            else -> item.classTypes.sorted().take(3).joinToString(", ")
        }
        Glide.with(this).load(item.icon?.toImageUri()).into(binding.imageIcon)
        Glide.with(this).load(item.gallery?.first()?.toImageUri()).into(binding.imageFacility)
        configureWorkTime(item, binding.workTimeContainer)
    }

    private fun configureWorkTime(item: FacilityEntity, binding: ViewWorkTimeBinding) {
        val workTime = item.workTimeData
        binding.root.apply {
            isVisible = workTime != null && selectedTab() != Classification.STUDIO
            if (workTime != null && selectedTab() != Classification.STUDIO) {
                text = getString(workTime.textId, workTime.args)
                val theme = requireActivity().theme
                background = ResourcesCompat.getDrawable(resources, workTime.bgId, theme)
            }
        }
    }

    private fun configurePtCard(
        item: FacilityEntity,
        trainers: PtShortcut,
        binding: ItemPtDetailsCardBinding
    ) {
        val ptLogos = trainers.values.map { it.first }
        binding.root.isVisible = true
        binding.textLadies.isVisible = item.femaleOnly ?: false
        binding.textName.text = item.name?.trim()
        binding.textAddress.text = item.address
        binding.textDistance.text = computeDistanceBetween(item.toLatLng(), viewModel.location).formatDistance()
        binding.textDescription.text =
            resources.getQuantityString(R.plurals.trainers_amount, ptLogos.size, ptLogos.size)
        Glide.with(this).load(item.gallery?.first()?.toImageUri()).into(binding.imageFacility)
        var startMargin = 0
        configureWorkTime(item, binding.workTimePt)

        binding.trainersContainer.removeAllViews()
        ptLogos.shuffled().take(3).map {
             val res = addTrainerShortcut(binding, it, startMargin)
            startMargin = res.first
            binding.trainersContainer.addView(res.second, 0)
        }
        if (ptLogos.size > 3) {
            val shortcut = ViewPtShortcutOtherBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.root,
                false
            )
            shortcut.root.text = "+${ptLogos.size - 3}"
            val layoutParams: FrameLayout.LayoutParams =
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            layoutParams.setMargins((22 * 3).toPx(), 0, 0, 0)
            shortcut.root.layoutParams = layoutParams
            binding.trainersContainer.addView(shortcut.root, 0)
        }

        binding.viewPager.apply {
            adapter = PtPagerAdapter(
                trainers,
                { page -> binding.viewPager.setCurrentItem(page, true) },
                { ptId -> viewModel.intent.value = MapIntent.PtDetailsTapped(ptId) }
            )
            setShowSideItems(0.toPx(), 12.toPx())
        }
    }


    private fun addTrainerShortcut(
        binding: ItemPtDetailsCardBinding,
        logo: String,
        startMargin: Int,
    ): Pair<Int, View> {
        val shortcut = ViewPtShortcutBinding.inflate(
            LayoutInflater.from(requireContext()),
            binding.root,
            false
        )
        Glide.with(this).load(logo.toImageUri()).into(shortcut.imageProfile)
        shortcut.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = startMargin.toPx()
        }
        return Pair(startMargin + 22, shortcut.root)
    }

    private fun showCreditsBanner(state: MapState.Credits) {
     /*   binding.groupBannerSubscribeMap.isVisible = false // TODO temporary removed Studios
        if (state.credits == 0) {
            binding.groupBannerCreditsMap.isVisible = true
            binding.viewBannerCreditsMap.textBuyCredits.setOnClickListener {
                registerResultListener()
                viewModel.navigator.navigate(Navigation.BuyCredits(flow = PaymentFlow.BUY_CREDITS_FROM_MAP))
            }
            binding.viewBannerCreditsMap.icClose.setOnClickListener {
                binding.groupBannerCreditsMap.isVisible = false
                viewModel.setCreditsBannerStateClose()
            }
            val text = getString(R.string.map_screen_buy_credits_to_attend_text)
            binding.viewBannerCreditsMap.textAttendClasses.text = SpannableString(text).apply {
                setCustomFont(
                    requireContext(),
                    0,
                    11,
                    common.font.work_sans_bold
                )
            }
        } else {
            binding.groupBannerCreditsMap.isVisible = false
        }*/
    }

    private fun showSubscribeBanner(state: MapState.SubscriptionValue) {
//        binding.groupBannerCreditsMap.isVisible = false // TODO temporary removed Studios
        if (state.subscription.autoRenewal || state.isActive) {
            binding.groupBannerSubscribeMap.isVisible = false
        } else {
            showSubscriptionError()
        }
    }

    private fun showSubscriptionError() {
        binding.groupBannerSubscribeMap.isVisible = true
        binding.viewBannerSubscribeMap.textSubscribeNow.setOnClickListener {
            registerResultListener()
            viewModel.navigator.navigate(Navigation.Subscription(PaymentFlow.BUY_SUBSCRIPTION_FROM_MAP))
        }
        binding.viewBannerSubscribeMap.icClose.setOnClickListener {
            binding.groupBannerSubscribeMap.isVisible = false
            viewModel.setSubscriptionBannerStateClose()
        }
    }

    private fun replaceMarkers(items: List<FacilityEntity>) {
        val newMarkers = items.map(::addMarker)
        markers.forEach { it.key.remove() }
        markers.clear()
        markers.putAll(newMarkers)
    }

    private fun addMarker(item: FacilityEntity): Pair<Marker, FacilityEntity> {
        val marker = map?.addMarker(item.toMarker()) as Marker
        loadMarkerPin(marker, item.icon, item.isAvailable)
        return marker to item
    }

    private fun showMarker(marker: Marker?, item: FacilityEntity?, isAvailableDefault: Boolean = false) {
        loadMarkerPin(selectedMarker, viewModel.selectedFacility?.icon, item?.isAvailable ?: isAvailableDefault)
        loadSelectedMarkerPin(marker, item?.icon, item?.isAvailable ?: false)
        viewModel.selectedFacility = item
        selectedMarker = marker
    }

    private fun loadMarkerPin(marker: Marker?, imageUri: String?, isAvailable: Boolean) {
        if (marker == null || imageUri == null) return

        try {
            val binding = ItemMarkerBinding.inflate(layoutInflater, this.binding.root, false)
            binding.exclamationIcon.isVisible =
                !isAvailable && selectedTab() != Classification.STUDIO
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(binding.root.toBitmap()))
            Glide.with(this)
                .load(imageUri.toImageUri())
                .circleCrop()
                .listener {
                    binding.imageIcon.setImageDrawable(it)
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(binding.root.toBitmap()))
                }
                .into(binding.imageIcon)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun loadSelectedMarkerPin(marker: Marker?, imageUri: String?, isAvailable: Boolean) {
        if (marker == null || imageUri == null) return

        try {
            val binding =
                ItemMarkerSelectedBinding.inflate(layoutInflater, this.binding.root, false)
            binding.exclamationIcon.isVisible =
                !isAvailable && selectedTab() != Classification.STUDIO
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(binding.root.toBitmap()))
            Glide.with(this)
                .load(imageUri.toImageUri())
                .circleCrop()
                .listener {
                    binding.imageIcon.setImageDrawable(it)
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(binding.root.toBitmap()))
                }
                .into(binding.imageIcon)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun showMyLocation(latLng: LatLng) {
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.5f))
    }

    private fun zoomToShowAll(markers: Map<Marker, *>, zoomAll: Boolean) {
        val cameraBounds = map?.projection?.visibleRegion?.latLngBounds
        val isAnyVisible = markers.any { cameraBounds?.contains(it.key.position) == true }
        if ((!isAnyVisible || zoomAll) && markers.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            markers.forEach { builder.include(it.key.position) }
            val bounds = builder.build()
            val update = CameraUpdateFactory.newLatLngBounds(bounds, DEFAULT_MAP_PADDING)
            map?.animateCamera(update)
        }
    }

    private fun FacilityEntity.toMarker(): MarkerOptions {
        val marker = MarkerOptions()
        val position = this.toLatLng()
        if (position != null) marker.position(position)
        return marker
    }

    private fun selectedTab(): Classification {
        return when (binding.toolbar.tabLayout.selectedTabPosition) {
            0 -> Classification.GYM
            1 -> Classification.TRAINER
            else -> Classification.STUDIO
        }
    }

    private fun setupListeners() {
        binding.toolbar.tabLayout.addOnTabSelectionListener { onTabSelected(it) }
        binding.toolbar.toolbar.setNavigationOnClickListener {
            registerResultListener()
            viewModel.selectedFacility = null
            viewModel.navigator.navigate(Navigation.Search(selectedTab()))
        }
        binding.buttonList.setOnClickListener {
            viewModel.navigator.navigate(Navigation.List(viewModel.classification, viewModel.filters))
        }
        binding.buttonLocation.setOnClickListener { onLocationClicked() }
        binding.cardFacility.root.setOnClickListener { onCardClicked() }
        binding.cardPt.root.setOnClickListener { onCardClicked() }
    }

    private fun registerResultListener() {
        setFragmentResultListener(REQUEST_CODE_COMMON) { key, _ ->
            if (key == REQUEST_CODE_COMMON) {
                val fromNotification = arguments?.getBoolean("notification") ?: false
                if (fromNotification) {
                    binding.rootContainer.setupMapInsets(binding.toolbar.toolbar)
                }
            }
            clearFragmentResultListener(REQUEST_CODE_COMMON)
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return checkSelfPermission(requireActivity(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
    }
}

fun RequestBuilder<Drawable?>.listener(block: (Drawable?) -> Unit): RequestBuilder<Drawable?> {
    val listener = object : RequestListener<Drawable?> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable?>?,
            isFirstResource: Boolean
        ): Boolean {
            return true
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable?>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            try {
                block(resource)
            } catch (error: IllegalArgumentException) {
                Timber.e(error)
            }
            return true
        }
    }
    return this.listener(listener)
}
