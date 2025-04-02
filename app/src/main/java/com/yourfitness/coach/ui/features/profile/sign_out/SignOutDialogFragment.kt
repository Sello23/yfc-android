package com.yourfitness.coach.ui.features.profile.sign_out

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogSignOutBinding
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignOutDialogFragment : MviDialogFragment<Any, Any, SignOutViewModel>() {

    override val binding: DialogSignOutBinding by viewBinding(createMethod = CreateMethod.BIND)
    override val viewModel: SignOutViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_sign_out, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.toolbar.toolbar.title = getString(R.string.sign_out_tapping_out)
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            onMenuItemClicked(it)
            return@setOnMenuItemClickListener true
        }
        binding.buttonSignOut.setOnClickListener { onSignOutClicked() }
    }

    private fun onSignOutClicked() {
        viewModel.intent.postValue(SignOutIntent.SignOut)
    }

    private fun onMenuItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
    }

    override fun renderState(state: Any) {
        when (state) {
            is SignOutState.Error -> showError(state.error, R.id.content)
        }
    }
}