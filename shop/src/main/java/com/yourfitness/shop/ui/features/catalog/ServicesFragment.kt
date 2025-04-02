package com.yourfitness.shop.ui.features.catalog

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.yourfitness.common.ui.utils.EndlessRecyclerViewScrollListener
import com.yourfitness.shop.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ServicesFragment : CatalogFragment() {
    override val viewModel: ServicesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.catalogTitle.text = getString(R.string.card_services)
    }

    override fun renderState(state: CatalogState) {
        when (state) {
            is CatalogState.FavoriteAdded -> showFavoriteAddedMessage()
        }

        super.renderState(state)
    }

    override fun onCardClick(productId: String) {
        super.onCardClick(productId)
        viewModel.intent.value = CatalogIntent.Details(getString(R.string.card_services), productId)
    }

    override fun updateToolbarTitle(isFavorites: Boolean) {
        binding.toolbar.catalogTitle.text =
            getString(if (isFavorites) R.string.services_favorites else R.string.card_services)
    }

    override fun setupLayoutManager() {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        endlessListener = object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.intent.value = CatalogIntent.OnNewPage(page, totalItemsCount)
            }
        }
        binding.catalogRecyclerView.apply {
            this.layoutManager = layoutManager
            addOnScrollListener(endlessListener)
        }
    }

    private fun showFavoriteAddedMessage() {
        val snackbar = Snackbar.make(
            binding.snackbarContainer,
            R.string.favorite_added_to_cart,
            Snackbar.LENGTH_SHORT
        )
        val customSnackView: View = layoutInflater.inflate(com.yourfitness.common.R.layout.snackbar_cart, null)
        customSnackView.findViewById<MaterialTextView>(R.id.message).text = getString(R.string.favorite_added_to_cart)
        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.addView(customSnackView, 0)
        snackbar.show()
    }
}
