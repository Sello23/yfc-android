package com.yourfitness.coach.ui.pt_role

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import com.yourfitness.coach.databinding.FragmentDashboardBinding
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.pt.domain.values.CONFIRMED
import com.yourfitness.pt.domain.values.SUCCESS
import com.yourfitness.pt.ui.features.dashboard.DashboardIntent
import com.yourfitness.pt.ui.features.dashboard.PtDashboardFragment
import com.yourfitness.pt.ui.features.dashboard.inductions.ConfirmCompleteInductionDialog
import com.yourfitness.pt.ui.features.dashboard.inductions.InductionListIntent
import com.yourfitness.pt.ui.features.training_calendar.actions.UserConfirmActionDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private val binding: FragmentDashboardBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFragmentListener()
        setupInductionConfirmListener()
    }

    private fun setupFragmentListener() {
        setFragmentResultListener(UserConfirmActionDialog.RESULT) { _, bundle ->
            if (bundle.getBoolean(SUCCESS)) binding.dashboard.getFragment<PtDashboardFragment>().refresh()
            clearFragmentResultListener(UserConfirmActionDialog.RESULT)
            setupFragmentListener()
        }
    }

    private fun setupInductionConfirmListener() {
        setFragmentResultListener(ConfirmCompleteInductionDialog.RESULT) { _, bundle ->
            if (bundle.getBoolean(CONFIRMED)) binding.dashboard.getFragment<PtDashboardFragment>().refresh()
            clearFragmentResultListener(ConfirmCompleteInductionDialog.RESULT)
            setupInductionConfirmListener()
        }
    }
}
