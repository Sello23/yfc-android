package com.yourfitness.coach.ui.features.facility.no_result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogNoResultBinding
import com.yourfitness.coach.ui.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoResultDialogFragment : BottomSheetDialogFragment() {

    private val binding: DialogNoResultBinding by viewBinding(createMethod = CreateMethod.BIND)
    private val viewModel: NoResultViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_no_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbar.title = getString(R.string.map_screen_no_matches_found)
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
        binding.buttonAction.setOnClickListener { viewModel.navigator.navigate(Navigation.Filter(viewModel.classification)) }
    }

    private fun onMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
    }
}