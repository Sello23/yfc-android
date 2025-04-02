package com.yourfitness.common.ui.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yourfitness.common.R
import com.yourfitness.common.ui.utils.hideKeyboard
import com.yourfitness.common.ui.utils.setupBottomInsets
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.yourfitness.common.R as common

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    abstract val binding: ViewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }

    protected open fun setupToolbar(toolbar: Toolbar, title: String = "", dismissId: Int) {
        if (title.isNotBlank()) {
            toolbar.title = title
        }
        toolbar.setOnMenuItemClickListener {
            onToolbarMenuItemClicked(it, dismissId)
            return@setOnMenuItemClickListener true
        }
    }

    open fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            hideKeyboard()
        }
        val progress = requireActivity().findViewById<View>(R.id.progress)
        progress.isVisible = isLoading
    }

    open fun showMessage(message: String?) {
        Toast.makeText(requireActivity(), message ?: "Unknown Error", Toast.LENGTH_SHORT).show()
    }

    open fun showError(error: Throwable) {
        showLoading(false)
        val message = error.message ?: getString(common.string.unknown_error)
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    protected open fun onToolbarMenuItemClicked(item: MenuItem, dismissId: Int) {
        when (item.itemId) {
            dismissId -> dismissDialog()
        }
    }

    protected open fun dismissDialog() = dismiss()

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