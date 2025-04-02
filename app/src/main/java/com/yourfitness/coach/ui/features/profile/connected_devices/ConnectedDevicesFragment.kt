package com.yourfitness.coach.ui.features.profile.connected_devices

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.spikeapi.SpikeConnection
import com.yourfitness.coach.BuildConfig.SPIKE_CLIENT_ID
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentConnectedDevicesBinding
import com.yourfitness.coach.ui.features.profile.connected_devices.dialog.disconnected.GoogleFitDisconnectedDialog.Companion.BOOLEAN_PAIR
import com.yourfitness.coach.ui.features.profile.connected_devices.dialog.disconnected.GoogleFitDisconnectedDialog.Companion.REQUEST_CODE
import com.yourfitness.coach.ui.features.profile.connected_devices.dialog.need_permission.NeedPermissionDialog
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupTopInsets
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ConnectedDevicesFragment :
    MviFragment<ConnectedDevicesIntent, Any, ConnectedDevicesViewModel>() {

    override val binding: FragmentConnectedDevicesBinding by viewBinding()
    override val viewModel: ConnectedDevicesViewModel by viewModels()

    private val contentAdapter by lazy { ProvidersAdapter(::onSwitchClick) }
    private var permissionLauncher: ActivityResultLauncher<Set<String>> = registerForActivityResult(SpikeConnection.requestReadAuthorization()) { result ->
        viewModel.intent.value = ConnectedDevicesIntent.PermissionsGranted
    }
    private var settingsLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK || result.resultCode == RESULT_CANCELED) {
            viewModel.intent.value = ConnectedDevicesIntent.PermissionsGranted
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTopInsets(binding.toolbar.toolbar)

        setupDisconnectionListener()
        setupToolbar(binding.toolbar.root)
        setupToolbar()

        binding.providersRecyclerView.adapter = contentAdapter
    }

    override fun renderState(state: Any) {
        when (state) {
            is ConnectedDevicesState.Loading -> showLoading(state.loading)
            is ConnectedDevicesState.ProvidersLoaded -> {
                showLoading(false)
                contentAdapter.setData(state.providers)
                contentAdapter.notifyDataSetChanged()
            }
            is ConnectedDevicesState.AskSpikePermissions -> {
                state.permissions.forEach {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it)) {
                        viewModel.intent.value = ConnectedDevicesIntent.PermissionDescriptionRequired
                        setupOpenSettingsListener()
                        return
                    }
                }
                permissionLauncher.launch(state.permissions)
            }
            is ConnectedDevicesState.RequestSpikeApiConnection -> openSpikeAuth(state.userId, state.provider)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (viewModel.selectedProviderType != null) {
            setFragmentResult(GOOGLE_FIT_REQUEST_CODE, bundleOf(BOOLEAN to true))
        } else {
            setFragmentResult(GOOGLE_FIT_REQUEST_CODE, bundleOf(BOOLEAN to false))
        }
    }

    private fun setupToolbar() {
        binding.toolbar.toolbar.title =
            getString(R.string.connected_devices_screen_connected_device_text)
    }

    private fun onSwitchClick(type: ProviderType, selected: Boolean) {
        viewModel.intent.value = ConnectedDevicesIntent.ProviderEnabled(type, selected)
    }

    private fun setupDisconnectionListener() {
        setFragmentResultListener(REQUEST_CODE) { _, bundle ->
            val status = bundle.getBoolean(BOOLEAN_PAIR)

            if (!status) {
                viewModel.intent.value = ConnectedDevicesIntent.DisconnectApproved
            }

            clearFragmentResultListener(REQUEST_CODE)
            setupDisconnectionListener()
        }
    }

    private fun setupOpenSettingsListener() {
        setFragmentResultListener(NeedPermissionDialog.REQUEST_CODE) { _, bundle ->
            val status = bundle.getBoolean(NeedPermissionDialog.BOOLEAN_PAIR)

            if (!status) {
                openAppPermissionSettings()
            }

            clearFragmentResultListener(NeedPermissionDialog.REQUEST_CODE)
        }
    }

    fun onSpikeApiInited(userId: String?) {
        if (userId != null) {
            viewModel.intent.postValue(ConnectedDevicesIntent.SpikeConnected(userId))
        }
    }

    private fun openSpikeAuth(userId: String, provider: String) {
        val url = "https://api.spikeapi.com/init-user-integration/?provider=$provider&user_id=$userId&client_id=$SPIKE_CLIENT_ID"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        val chooser = Intent.createChooser(intent, "Choose browser")
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            try {
                requireActivity().startActivity(chooser)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun openAppPermissionSettings() {
        val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireActivity().packageName, null)
        }
        settingsLauncher.launch(intent)
    }

    companion object {
        const val GOOGLE_FIT_REQUEST_CODE = "google_fit_request_code"
        const val BOOLEAN = "state"
    }
}
