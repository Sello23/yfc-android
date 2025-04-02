package com.yourfitness.coach.ui.features.bottom_menu

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentBottomMenuBinding
import com.yourfitness.coach.ui.features.profile.profile.UpdateListener
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomMenuFragment: BottomMenuConfigFragment<BottomMenuIntent, BottomMenuState, BottomMenuViewModel>() {
    override val viewModel: BottomMenuViewModel by viewModels()

    override val binding: FragmentBottomMenuBinding by viewBinding()

    private var updateListener: UpdateListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading(true)
        setupBottomInsets(binding.bottomNavView)
        setupBottomNavView(binding.bottomNavView)
        setupBottomNavigation(binding.bottomNavView, R.id.nav_graph)
        getAppPermissions()
        binding.buttonLock.setOnClickListener {
            showLoading(true)
            viewModel.intent.value = BottomMenuIntent.ProcessQuickAccessTap //getBookedClasses() // TODO temporary removed Studios
        }
    }

    override fun onResume() {
        super.onResume()
        setupButtonLockBg(viewModel.subscriptionActive)
    }

    override fun renderState(state: BottomMenuState) {
        when (state) {
            is BottomMenuState.ConfigsLoaded -> {
                setupButtonLockBg(state.subscriptionActive)
                updateListener?.onSubscriptionUpdated(state.subscriptionActive)
            }
            is BottomMenuState.SavedSubscriptionLoaded -> setupButtonLockBg(state.subscriptionActive)
            is BottomMenuState.GymsLoaded -> showLoading(false)
            is BottomMenuState.Error -> {
                setupButtonLockBg(false)
                updateListener?.onSubscriptionUpdated(false)
                showError(state.error)
            }
//            is BottomMenuState.BookedClassLoaded -> showBookedClass(state)
            is BottomMenuState.LoadSubscriptionState -> getGymAccess(state)
            is BottomMenuState.Loading -> showLoading(state.active)
            else -> {}
        }
    }

    private fun setupButtonLockBg(isSubscriptionActive: Boolean) {
        binding.buttonLock.background = ContextCompat.getDrawable(
            requireContext(),
            if (isSubscriptionActive) R.drawable.button_lock else R.drawable.button_lock_red
        )
    }

    private fun getGymAccess(state: BottomMenuState.LoadSubscriptionState) {
        if (state.isActive) {
            if (checkSystemLocationPermission()) {
                if (isLocationEnabled()) {
                    viewModel.intent.value = BottomMenuIntent.FindGym
                } else {
                    showLoading(false)
                }
            } else {
                showLoading(false)
                if (canAskForPermission()) getAppPermissions()
                else showNoPermissionDialog()
            }
        } else {
            showLoading(false)
            viewModel.navigator.navigate(Navigation.SubscriptionError)
        }
    }

    /*private fun showBookedClass(state: BottomMenuState.BookedClassLoaded) {
        binding.buttonLock.isClickable = true
        if (state.bookedClass != null) {
            viewModel.navigator.navigate(Navigation.YouHaveUpcomingClass(state.bookedClass, state.profile))
        } else {
            checkLocationState()
            if (isLocationEnabled()) {
                viewModel.findNearestGym()
            } else {
                showLoading(false)
            }
        }
    }*/

    private fun getAppPermissions() {
        val permissions = mutableListOf<String>()
        if (!checkSystemLocationPermission()) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (!checkSystemCameraPermission()) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !checkSystemNotificationPermission()) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissions.isEmpty()) return

        ActivityCompat.requestPermissions(
            requireActivity(), permissions.toTypedArray(),
            PERMISSIONS_REQUEST_NOTIFICATION
        )
    }

    private fun canAskForPermission() =
        ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkSystemNotificationPermission() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    private fun checkSystemLocationPermission() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun checkSystemCameraPermission() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            viewModel.navigator.navigate(Navigation.WhatYourPositionDialog)
            false
        } else {
            true
        }
    }

    private fun showNoPermissionDialog() {
        viewModel.navigator.navigate(Navigation.NoPermission)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.buttonLock.isEnabled = !isLoading
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }

            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    fun registerUpdateListener(listener: UpdateListener) {
        updateListener = listener
    }

    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 12
        const val PERMISSIONS_REQUEST_NOTIFICATION = 14
        const val CAMERA_PERMISSION_CODE = 1010101
    }
}

fun Fragment.showBottomNavigationItem(@IdRes selectedItemId: Int) {
    val bottomMenuFragment = childFragmentManager.findFragmentById(R.id.bottom_bar) as BottomMenuFragment
    val bottomNavigation = bottomMenuFragment.binding.bottomNavView
    bottomNavigation.setOnItemSelectedListener(null)
    bottomNavigation.selectedItemId = selectedItemId
    bottomNavigation.setupWithNavController(findNavController(), R.id.nav_graph)
}