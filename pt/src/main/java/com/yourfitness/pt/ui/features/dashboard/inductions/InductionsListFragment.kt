package com.yourfitness.pt.ui.features.dashboard.inductions

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.yourfitness.pt.R
import com.yourfitness.pt.domain.models.ClientInductionData
import com.yourfitness.pt.domain.models.InductionInfo
import com.yourfitness.pt.domain.values.CONFIRMED
import com.yourfitness.pt.ui.features.dashboard.ClientPagerAdapter
import com.yourfitness.pt.ui.features.dashboard.PtDashboardFragment
import com.yourfitness.pt.ui.features.dashboard.base.ClientInductionBaseFragment
import com.yourfitness.pt.ui.features.training_calendar.adapters.CalendarCardBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InductionsListFragment : ClientInductionBaseFragment<InductionInfo>(), CalendarCardBuilder {

    override val viewModel: InductionsListViewModel by viewModels()

    override val adapter by lazy { ClientPagerAdapter<InductionInfo>(false, onInductionClick = ::onClientClick) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFragmentListener()
        binding.toolbar.root.title = getString(R.string.inductions_label)
    }

    override fun setupSearchView() {
        binding.toolbar.root.menu.clear()
    }

    override fun setAdapterData(clients: List<ClientInductionData>) {
        adapter.setData(clients.filterIsInstance<InductionInfo>())
    }

    override fun setupViewsVisibility(clients: List<ClientInductionData>) {
        binding.textResults.isVisible = false
    }

    private fun onClientClick(client: ClientInductionData) {
        viewModel.intent.value = InductionListIntent.OnInductionClick(client)
    }

    private fun setupFragmentListener() {
        setFragmentResultListener(ConfirmCompleteInductionDialog.RESULT) { _, bundle ->
            if (bundle.getBoolean(CONFIRMED)) {
                viewModel.intent.postValue(InductionListIntent.Refresh)
            }
            clearFragmentResultListener(ConfirmCompleteInductionDialog.RESULT)
            setupFragmentListener()
        }
    }
}
