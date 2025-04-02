package com.yourfitness.common.ui.navigation

import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavGraphNavigator
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.yourfitness.common.R

class CommonNavigationHandler constructor(
    private val navController: NavController,
    private val navigator: CommonNavigator,
    private val logoutRoute: Int
) {

    fun observeNavigation(owner: LifecycleOwner) {
        navigator.navigation.observe(owner) {
            if (it != null) {
                navigator.navigation.value = null
                navigate(it)
            }
        }
    }

    private fun navigate(node: CommonNavigation) {
        when (node) {
            is CommonNavigation.WelcomeBack -> {
                navController.popBackStack()
                navController.navigate(logoutRoute)
            }
            is CommonNavigation.AddCreditCard -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.common/dialog_add_credit_card".toUri())
                    .build()

                navController.navigate(request)
            }
            is CommonNavigation.ConfiguredRoute -> {
                val options = NavOptions.Builder()
                    .setPopUpTo(node.popUpToId, true)
                    .build()
                navController.navigate(node.destinationId, null, options)
            }
            is CommonNavigation.CommonError -> openCommonErrorDialog()
            is CommonNavigation.CommonError2Pop -> {
                navController.popBackStack()
                navController.popBackStack()
                openCommonErrorDialog()
            }
        }
    }

    private fun openCommonErrorDialog() {
        val request = NavDeepLinkRequest.Builder
            .fromUri("android-app://com.yourfitness.common/dialog_common_error".toUri())
            .build()

        navController.navigate(request)
    }
}