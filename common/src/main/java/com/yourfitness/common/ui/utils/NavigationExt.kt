package com.yourfitness.common.ui.utils

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.core.view.forEach
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavOptions
import com.google.android.material.navigation.NavigationBarView
import java.lang.ref.WeakReference

fun NavigationBarView.setupWithNavController(navController: NavController, navGraph: Int) {
    setOnItemSelectedListener { item ->
        onNavDestinationSelected(
            item,
            navController,
            navGraph
        )
    }
    val weakReference = WeakReference(this)
    navController.addOnDestinationChangedListener(
        object : NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                val view = weakReference.get()
                if (view == null) {
                    navController.removeOnDestinationChangedListener(this)
                    return
                }
                view.menu.forEach { item ->
                    if (destination.matchDestination(item.itemId)) {
                        item.isChecked = true
                    }
                }
            }
        })
}

private fun NavDestination.matchDestination(@IdRes destId: Int): Boolean =
    hierarchy.any { it.id == destId }

private fun onNavDestinationSelected(item: MenuItem, navController: NavController, navGraph: Int): Boolean {
    val builder = NavOptions.Builder().setLaunchSingleTop(true).setRestoreState(true)
    if (
        navController.currentDestination!!.parent!!.findNode(item.itemId)
                is ActivityNavigator.Destination
    ) {
        builder.setEnterAnim(androidx.navigation.ui.R.anim.nav_default_enter_anim)
            .setExitAnim(androidx.navigation.ui.R.anim.nav_default_exit_anim)
            .setPopEnterAnim(androidx.navigation.ui.R.anim.nav_default_pop_enter_anim)
            .setPopExitAnim(androidx.navigation.ui.R.anim.nav_default_pop_exit_anim)
    } else {
        builder.setEnterAnim(androidx.navigation.ui.R.animator.nav_default_enter_anim)
            .setExitAnim(androidx.navigation.ui.R.animator.nav_default_exit_anim)
            .setPopEnterAnim(androidx.navigation.ui.R.animator.nav_default_pop_enter_anim)
            .setPopExitAnim(androidx.navigation.ui.R.animator.nav_default_pop_exit_anim)
    }
    if (item.order and Menu.CATEGORY_SECONDARY == 0) {
        builder.setPopUpTo(
            navGraph,
            inclusive = true,
            saveState = true
        )
    }
    val options = builder.build()
    return try {
        navController.navigate(item.itemId, null, options)
        navController.currentDestination?.matchDestination(item.itemId) == true
    } catch (e: IllegalArgumentException) {
        false
    }
}

fun NavController.isFragmentInBackStack(destinationId: Int) =
    try {
        getBackStackEntry(destinationId)
        true
    } catch (e: Exception) {
        false
    }