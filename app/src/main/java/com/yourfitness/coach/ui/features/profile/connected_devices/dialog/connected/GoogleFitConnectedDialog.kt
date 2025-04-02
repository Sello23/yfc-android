package com.yourfitness.coach.ui.features.profile.connected_devices.dialog.connected

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
import com.yourfitness.coach.databinding.DialogGoogleFitConnectedBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoogleFitConnectedDialog : BottomSheetDialogFragment() {

    private val binding: DialogGoogleFitConnectedBinding by viewBinding(createMethod = CreateMethod.BIND)
    private val viewModel: GoogleFitConnectedViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_google_fit_connected, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val providerName = viewModel.providerInfo?.name?.let { getString(it) } ?: ""

        binding.toolbar.toolbar.title = getString(R.string.connected_devices_screen_google_fit_connected_text, providerName)
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
        observeIntent()
        binding.buttonGetStarted.setOnClickListener { dialog?.dismiss() }
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