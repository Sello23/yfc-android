package com.yourfitness.common.ui.mvi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.yourfitness.common.R
import com.yourfitness.common.domain.analytics.CrashlyticsManager
import com.yourfitness.common.ui.utils.hideKeyboard
import com.yourfitness.common.ui.utils.setupWithNavController
import timber.log.Timber

abstract class MviFragment<I, S, VM : MviViewModel<I, S>> : Fragment() {

    abstract val binding: ViewBinding
    abstract val viewModel: VM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeState()
        observeIntent()
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) {
            if (it != null) {
                renderState(it)
            }
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

    open fun renderState(state: S) {
    }

    open fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            hideKeyboard()
        }
        try {
            val progress: View? = requireActivity().findViewById(R.id.progress)
            progress?.isVisible = isLoading
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    open fun showMessage(message: String?) {
        Toast.makeText(requireActivity(), message ?: "Unknown Error", Toast.LENGTH_SHORT).show()
    }

    open fun showError(error: Throwable) {
        CrashlyticsManager.trackException(error)
        showLoading(false)
        val message = error.message ?: getString(R.string.unknown_error)
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    open fun setupBottomNavigation(bottomNavigation: BottomNavigationView, navGraph: Int) {
        val navController = findNavController()
        bottomNavigation.setupWithNavController(navController, navGraph)
        bottomNavigation.setOnItemReselectedListener {}
    }

    open fun setupToolbar(toolbar: Toolbar) {
        val navController = findNavController()
        toolbar.setNavigationOnClickListener { navController.navigateUp() }
        toolbar.setNavigationIcon(R.drawable.ic_back_button)
    }

    open fun setupOptionsMenu(toolbar: Toolbar, @MenuRes menuRes: Int, onItemSelected: (MenuItem) -> Unit) {
        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(menuRes, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                onItemSelected(menuItem)
                return true
            }
        }
        toolbar.addMenuProvider(menuProvider)
    }

    open fun showConnectionErrorMsg() {
        val text = getString(R.string.connection_issue)
        val snackbar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
        snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_active))
        snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.white))
        snackbar.show()
    }
}

inline fun <reified T: ViewBinding> Fragment.viewBinding() = viewBinding<T>(CreateMethod.INFLATE)