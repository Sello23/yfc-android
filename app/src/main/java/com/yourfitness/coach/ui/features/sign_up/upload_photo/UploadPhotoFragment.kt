package com.yourfitness.coach.ui.features.sign_up.upload_photo

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentUploadPhotoBinding
import com.yourfitness.coach.domain.entity.fullName
import com.yourfitness.coach.domain.entity.nameInitials
import com.yourfitness.coach.ui.features.sign_up.setupStepIndicator
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadPhotoFragment : MviFragment<Any, Any, UploadPhotoViewModel>() {

    override val binding: FragmentUploadPhotoBinding by viewBinding()
    override val viewModel: UploadPhotoViewModel by viewModels()

    private val avatarPicker = AvatarPicker(this, ::onAvatarSelected)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.root)
        setupStepIndicator(binding.toolbar.progressStep)
        showPhoto(null, viewModel.user.fullName)
        binding.imagePlaceholder.setOnClickListener { avatarPicker.openMenu() }
        binding.buttonNext.setOnClickListener {
            binding.buttonNext.isClickable = false
            onNextClicked()
        }
        avatarPicker.init(binding.imagePhoto)
    }

    private fun onAvatarSelected(uri: Uri) {
        showPhoto(uri, viewModel.user.fullName)
    }

    private fun onNextClicked() {
        viewModel.intent.postValue(UploadPhotoIntent.Next(avatarPicker.uri!!))
    }

    override fun renderState(state: Any) {
        when (state) {
            is UploadPhotoState.Loading -> showLoading(true)
            is UploadPhotoState.Success -> showLoading(false)
            is UploadPhotoState.Error -> {
                binding.buttonNext.isClickable = true
                showError(state.error)
            }
        }
    }

    private fun showPhoto(photo: Uri?, name: String) {
        val photoAdded = photo != null
        if (photoAdded) {
            binding.imagePhoto.setImageURI(photo)
        } else {
            binding.imagePlaceholder.text = name.nameInitials
        }
        binding.imagePlaceholder.isInvisible = photoAdded
        binding.imagePhoto.isInvisible = !photoAdded
        validateInput(photoAdded)
    }

    private fun validateInput(photoAdded: Boolean) {
        val photoIcon = if (photoAdded) R.drawable.ic_photo_edit else R.drawable.ic_photo_upload
        binding.imagePhotoIcon.setImageResource(photoIcon)
        binding.buttonNext.isEnabled = photoAdded
    }
}