package com.yourfitness.coach.ui.features.profile.connected_devices.dialog.disconnected

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogGoogleFitDisconnectedBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoogleFitDisconnectedDialog : BottomSheetDialogFragment() {

    private val binding: DialogGoogleFitDisconnectedBinding by viewBinding(createMethod = CreateMethod.BIND)
    private val viewModel: GoogleFitDisconnectedViewModel by viewModels()
    private var isDisconnectButtonClicked = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_google_fit_disconnected, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val providerName = viewModel.providerInfo?.name?.let { getString(it) } ?: ""

        binding.toolbar.toolbar.title = getString(R.string.connected_devices_screen_google_fit_disconnected_text, providerName)
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }

        viewModel.providerInfo?.logo?.let { binding.imageCheck.setImageResource(it) }
        binding.textAreYourSure.text = getString(R.string.connected_devices_screen_are_you_sure_text, providerName)

        observeIntent()

        binding.buttonDisconnect.text = getString(R.string.connected_devices_screen_disconnect_google_fit_text, providerName)
        binding.buttonDisconnect.setOnClickListener { disconnectGoogleFit() }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (isDisconnectButtonClicked) {
            setFragmentResult(REQUEST_CODE, bundleOf(BOOLEAN_PAIR to false))
        } else {
            setFragmentResult(REQUEST_CODE, bundleOf(BOOLEAN_PAIR to true))
        }
    }

    private fun disconnectGoogleFit() {
        isDisconnectButtonClicked = true
        dismiss()
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

    companion object {
        const val REQUEST_CODE = "2"
        const val BOOLEAN_PAIR = "state"
    }
}