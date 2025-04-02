package com.yourfitness.community.ui.features.likes

import android.os.Bundle
import android.view.View
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.community.ui.navigation.CommunityNavigation
import com.yourfitness.community.ui.navigation.CommunityNavigator
import com.yourfitness.comunity.R
import com.yourfitness.comunity.databinding.DialogHowGetLikesBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HowGetLikesDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogHowGetLikesBinding by viewBinding()

    @Inject
    lateinit var navigator: CommunityNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.header.text = getString(R.string.get_likes_title)
        binding.message.text = getString(R.string.get_likes_msg)
        binding.buttonRetry.text = getString(R.string.get_likes_action_main)
        binding.buttonRetry.setOnClickListener {
            navigator.navigate(CommunityNavigation.FriendsList)
        }
        binding.buttonBack.text = getString(R.string.get_likes_action_secondary)
        binding.buttonBack.setOnClickListener { dismiss() }
        binding.close.setOnClickListener { dismiss() }
    }
}
