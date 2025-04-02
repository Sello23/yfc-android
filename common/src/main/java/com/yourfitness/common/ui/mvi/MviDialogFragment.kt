package com.yourfitness.common.ui.mvi

import android.app.Dialog
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
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yourfitness.common.ui.utils.hideKeyboard
import com.yourfitness.common.R
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.setupWithNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class MviDialogFragment<I, S, VM : MviViewModel<I, S>> : BottomSheetDialogFragment() {

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

    open fun showLoading(isLoading: Boolean, contentId: Int) {
        if (isLoading) {
            hideKeyboard()
        }
        val progress = binding.root.findViewById<View>(R.id.progress)
        val content = binding.root.findViewById<View>(contentId)
        progress?.isVisible = isLoading
        content?.isVisible = !isLoading

    }

    open fun showMessage(message: String?) {
        Toast.makeText(requireActivity(), message ?: "Unknown Error", Toast.LENGTH_SHORT).show()
    }

    open fun showError(error: Throwable, contentId: Int? = null) {
        contentId?.let { showLoading(false, it) }
        val message = error.message ?: getString(R.string.unknown_error)
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    open fun setupBottomNavigation(bottomNavigation: BottomNavigationView, navGraph: Int) {
        val navController = findNavController()
        bottomNavigation.setupWithNavController(navController, navGraph)
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

    protected open fun Dialog.configureDialogView() {
        setOnShowListener { dialogInterface ->
            setupBottomInsets(binding.root)
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            lifecycleScope.launch {
                delay(200)
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }
}