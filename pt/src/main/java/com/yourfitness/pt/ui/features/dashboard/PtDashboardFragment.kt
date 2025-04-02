package com.yourfitness.pt.ui.features.dashboard

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.features.fitness_calendar.toPx
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.getColorCompat
import com.yourfitness.common.ui.utils.getTopInsets
import com.yourfitness.common.ui.utils.setShowSideItems
import com.yourfitness.common.ui.utils.setupTopInsets
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.FragmentPtDashboardBinding
import com.yourfitness.pt.databinding.ViewCardsPagerBinding
import com.yourfitness.pt.domain.models.ClientInductionData
import com.yourfitness.pt.ui.features.training_calendar.adapters.CalendarCardBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PtDashboardFragment : MviFragment<DashboardIntent, DashboardState, PtDashboardViewModel>(),
    CalendarCardBuilder {

    override val binding: FragmentPtDashboardBinding by viewBinding()
    override val viewModel: PtDashboardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading(true)
        setupTopInsets(binding.root)
        binding.root.setColorSchemeColors(requireContext().getColorCompat(com.yourfitness.common.R.color.main_active))
        binding.root.setOnRefreshListener {
            viewModel.intent.value = DashboardIntent.Refresh
        }
        binding.root.setProgressViewOffset(false, 0, getTopInsets() + 20.toPx())
    }

    override fun renderState(state: DashboardState) {
        when (state) {
            is DashboardState.Loading -> showLoading(true)
            is DashboardState.Error -> showLoading(false)
            is DashboardState.DashboardDataLoaded -> {
                showLoading(false)
                binding.root.isRefreshing = false
                setupCards(state.data)
                setupFacilityCard(
                    binding.latestRequests,
                    state.latestRequests,
                    R.string.latest_requests,
                    R.string.latest_requests_empty_state
                )
                setupFacilityCard(
                    binding.nextAppointment,
                    state.nextAppointments,
                    R.string.next_appointment,
                    R.string.next_appointment_empty_state
                )
                setupPager(
                    binding.inductions,
                    state.inductions,
                    R.string.inductions_label,
                    R.string.inductions_empty_state,
                    DashboardIntent.SeeALllInductionsClicked
                ) { client -> DashboardIntent.InductionCardClicked(client) }
                setupPager(
                    binding.clients,
                    state.clients,
                    R.string.list_of_clients,
                    R.string.list_of_clients_empty_state,
                    DashboardIntent.SeeALllClientsClicked,
                ) { client -> DashboardIntent.ClientCardClicked(client) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.intent.value = DashboardIntent.Refresh
    }

    override fun showLoading(isLoading: Boolean) {
        binding.loading.root.isVisible = isLoading
    }

        private fun setupCards(data: List<Pair<Int, String>>) {
        binding.totalNumbers.adapter = PtCartAdapter(data)
    }

    private fun setupFacilityCard(
        blocBinding: ViewCardsPagerBinding,
        data: List<CalendarView.CalendarItem>,
        @StringRes title: Int,
        @StringRes emptyStateMsg: Int
    ) {
        blocBinding.apply {
            setupHeader(data.size, emptyStateMsg, title)
            header.goToFitnessCalendar.setOnClickListener {
                if (data.isNotEmpty()) {
                    viewModel.intent.value = DashboardIntent.GoToCalendarClicked(data.first().objectId)
                }
            }

            cardPager.isVisible = false

            card.root.apply {
                isVisible = data.isNotEmpty()
                if (isVisible) {
                    card.buildCard(data.first(), ::onSessionClick)
                }
            }
        }
    }

    private fun setupPager(
        blocBinding: ViewCardsPagerBinding,
        data: List<ClientInductionData>,
        @StringRes title: Int,
        @StringRes emptyStateMsg: Int,
        onClickIntent: DashboardIntent,
        onItemClickIntent: (ClientInductionData) -> DashboardIntent
    ) {
        blocBinding.apply {
            setupHeader(data.size, emptyStateMsg, title)
            header.goToFitnessCalendar.apply {
                text = getString(com.yourfitness.common.R.string.filters_see_all)
                setOnClickListener {
                    if (data.isNotEmpty()) {
                        viewModel.intent.value = onClickIntent
                    }
                }
            }
            cardPager.apply {
                isVisible = data.isNotEmpty()
                if (isVisible) {
                    setupPager(cardPager, data, onItemClickIntent)
                }
            }

            card.root.isVisible = false
        }
    }

    private fun ViewCardsPagerBinding.setupHeader(
        dataSize: Int,
        emptyStateMsg: Int,
        title: Int
    ) {
        emptyState.isVisible = dataSize == 0
        emptyState.text = getString(emptyStateMsg)

        header.apply {
            root.isVisible = true
            headerText.text = getString(title)
            label.isVisible = dataSize > 1
            label.text = getString(R.string.more_items_label, dataSize - 1)
            goToFitnessCalendar.isVisible = dataSize > 0
        }
    }

    private fun setupPager(
        pager: ViewPager2,
        data: List<ClientInductionData>,
        onItemClickIntent: (ClientInductionData) -> DashboardIntent
    ) {
        pager.apply {
            adapter = ClientPagerAdapter(
                clients = ArrayList(data)
            ) { client -> viewModel.intent.value = onItemClickIntent(client) }
            setShowSideItems(0.toPx(), 12.toPx())
        }
    }

    private fun onSessionClick(sessionId: String, status: String) {
        viewModel.intent.value = DashboardIntent.OnSessionActionClicked(sessionId, status)
    }

    fun refresh() {
        viewModel.intent.postValue(DashboardIntent.Refresh)
    }
}
