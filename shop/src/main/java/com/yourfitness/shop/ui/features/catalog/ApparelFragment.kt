package com.yourfitness.shop.ui.features.catalog

import android.os.Bundle
import android.view.View
import com.yourfitness.shop.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ApparelFragment : CatalogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.catalogTitle.text = getString(R.string.card_apparel)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCardClick(productId: String) {
        super.onCardClick(productId)
        viewModel.intent.value = CatalogIntent.Details(getString(R.string.card_apparel), productId)
    }

    override fun updateToolbarTitle(isFavorites: Boolean) {
        binding.toolbar.catalogTitle.text =
            getString(if (isFavorites) R.string.apparel_favorites else R.string.card_apparel)
    }
}
