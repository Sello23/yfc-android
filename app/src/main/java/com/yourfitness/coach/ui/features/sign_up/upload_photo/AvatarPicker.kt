package com.yourfitness.coach.ui.features.sign_up.upload_photo

import android.net.Uri
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.canhub.cropper.*
import com.yourfitness.coach.R
import timber.log.Timber
import com.yourfitness.common.R as common

class AvatarPicker(
    private val fragment: Fragment,
    private val onImageSelected: (Uri) -> Unit = {}
) {

    private lateinit var popupMenu: PopupMenu
    var uri: Uri? = null

    private val cropLauncher = fragment.registerForActivityResult(CropImageContract()) {
        if (it.isSuccessful) {
            uri = it.uriContent
            onImageSelected(uri!!)
        } else {
            Timber.e(it.error)
        }
    }

    fun init(imagePhoto: ImageView) {
        createMenu(imagePhoto)
        imagePhoto.setOnClickListener { popupMenu.show() }
    }

    fun openMenu() {
        popupMenu.show()
    }

    private fun createMenu(imagePhoto: ImageView) {
        popupMenu = PopupMenu(fragment.requireContext(), imagePhoto)
        popupMenu.inflate(R.menu.upload_photo)
        popupMenu.setOnMenuItemClickListener { handleMenu(it) }
    }

    private fun handleMenu(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.gallery -> openGallery()
            R.id.camera -> openCamera()
        }
        return false
    }

    private fun openGallery() {
        val options = createOptions(false)
        cropLauncher.launch(options)
    }

    private fun openCamera() {
        val options = createOptions(true)
        cropLauncher.launch(options)
    }

    private fun createOptions(includeCamera: Boolean): CropImageContractOptions {
        return CropImageContractOptions(
            null, CropImageOptions(
                outputCompressQuality = 60,
                fixAspectRatio = true,
                allowFlipping = false,
                allowRotation = false,
                aspectRatioX = 1,
                aspectRatioY = 1,
                cropShape = CropImageView.CropShape.OVAL,
                imageSourceIncludeCamera = includeCamera,
                imageSourceIncludeGallery = !includeCamera,
                progressBarColor = ContextCompat.getColor(fragment.requireContext(), common.color.black),
                activityBackgroundColor = ContextCompat.getColor(fragment.requireContext(), common.color.black)
            )
        )
    }
}