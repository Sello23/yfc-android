package com.yourfitness.shop.ui.features.catalog

import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.EndlessRecyclerViewScrollListener
import com.yourfitness.common.ui.utils.formatCoins
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.FragmentApparelBinding
import com.yourfitness.shop.domain.cart_service.SortType
import com.yourfitness.shop.domain.products.ProductFilters
import com.yourfitness.shop.domain.products.isEmpty
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import reactivecircus.flowbinding.android.widget.queryTextChanges
import com.yourfitness.common.R as common

const val REQUEST_CODE_PRODUCT_FILTERS = "98"
const val REQUEST_CODE_PRODUCT_DETAILS = "97"
const val REQUEST_CODE_CART_CLOSED = "96"

@AndroidEntryPoint
open class CatalogFragment : MviFragment<CatalogIntent, CatalogState, CatalogViewModel>() {

    override val binding: FragmentApparelBinding by viewBinding()
    override val viewModel: CatalogViewModel by viewModels()
    open val adapter by lazy {
        CatalogAdapter(
            ::onCardClick,
            resources.getColor(com.yourfitness.common.R.color.pale_blue),
            ::onFavoriteClick,
            viewModel.productTypeId
        )
    }
    protected lateinit var endlessListener: EndlessRecyclerViewScrollListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupViews()
        setupToolbar(binding.toolbar.root)
        setupSearchView()
        showFiltersButton()
    }

    override fun onPause() {
        super.onPause()

        viewModel.intent.value = CatalogIntent.UploadFavorites
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater
        inflater.inflate(R.menu.catalog_sort, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.low_to_high_price -> {
                viewModel.intent.value = CatalogIntent.SortTypeChanged(SortType.LOW_TO_HIGH)
                true
            }
            R.id.high_to_low_price -> {
                viewModel.intent.value = CatalogIntent.SortTypeChanged(SortType.HIGH_TO_LOW)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        setupLayoutManager()
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.catalogRecyclerView.adapter = adapter
        binding.catalogRecyclerView.itemAnimator = null
        binding.catalogRecyclerView.recycledViewPool.clear()
    }

    protected open fun setupLayoutManager() {
        val layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        endlessListener = object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.intent.value = CatalogIntent.OnNewPage(page, totalItemsCount)
            }
        }
        binding.catalogRecyclerView.apply {
            this.layoutManager = layoutManager
            addOnScrollListener(endlessListener)
            addItemDecoration(SpacesItemDecoration())
        }
    }

    private fun setupSearchView() {
        val favoritesItem: MenuItem = binding.toolbar.toolbar.menu.findItem(R.id.favorites)
        favoritesItem.apply {
            isChecked = viewModel.isFavoriteChecked
            icon = ContextCompat.getDrawable(
                requireContext(),
                if (viewModel.isFavoriteChecked) com.yourfitness.common.R.drawable.ic_favorites_checked else R.drawable.ic_favorites
            )
            updateToolbarTitle(viewModel.isFavoriteChecked)
        }

        binding.toolbar.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.search) {
                viewModel.isSearchActive = true
                binding.toolbar.root.menu.findItem(R.id.search).isVisible = false
                binding.toolbar.root.menu.findItem(R.id.favorites).isVisible = false
                binding.toolbar.root.invalidateMenu()
                binding.toolbar.searchView.isVisible = true
                with(binding.toolbar.searchView) {
                    requestFocus()
                    isIconifiedByDefault = true
                    isFocusable = true
                    isIconified = false
                    requestFocusFromTouch()
                }
                binding.toolbar.container.isVisible = false
            } else if (it.itemId == R.id.favorites) {
                viewModel.intent.value = CatalogIntent.FilterFavoriteChanged(!it.isChecked)
                it.isChecked = !it.isChecked
                it.icon = ContextCompat.getDrawable(
                    requireContext(),
                    if (it.isChecked) com.yourfitness.common.R.drawable.ic_favorites_checked else R.drawable.ic_favorites
                )
                updateToolbarTitle(it.isChecked)
            }

            true
        }

        binding.toolbar.searchView.setOnCloseListener {
            viewModel.isSearchActive = false
            binding.toolbar.root.menu.findItem(R.id.search).isVisible = true
            binding.toolbar.root.menu.findItem(R.id.favorites).isVisible = true
            binding.toolbar.root.invalidateMenu()
            binding.toolbar.searchView.isVisible = false
            binding.toolbar.container.isVisible = true
            onQueryChanged("")
            true
        }

        lifecycleScope.launch {
            binding.toolbar.searchView.queryTextChanges().debounce(100).collect { onQueryChanged(it) }
        }
    }

    private fun setupViews() {
        binding.toolbar.coins.root.setOnClickListener {
            viewModel.intent.value = CatalogIntent.CoinsInfo
        }

        binding.shopBucket.root.setOnClickListener {
            onCartClick()
        }

        binding.configuration.filter.setOnClickListener {
            openFilters()
        }

        registerForContextMenu(binding.configuration.sort)
        binding.configuration.sort.setOnClickListener {
            requireActivity().openContextMenu(it)
        }
    }

    override fun renderState(state: CatalogState) {
        when (state) {
            is CatalogState.CartSizeLoaded -> setupShopBucket(state.size)
            is CatalogState.Loading -> showLoading(true)
            is CatalogState.Loaded -> onDataLoaded(state)
            is CatalogState.Error -> showError(state.error)
            is CatalogState.ProductsUpdated -> {
                setupToolbar(state.coins)
                setupShopBucket(state.cartSize)
                endlessListener.resetState()
                showFiltersButton()
                binding.body.isVisible = state.products.isNotEmpty()
                binding.emptyState.isVisible = state.products.isEmpty()
                if (state.products.isEmpty() && state.emptyState != null) {
                    binding.emptyState.text = getString(state.emptyState.string)
                }

                lifecycleScope.launch {
                    val scrollState = binding.catalogRecyclerView.layoutManager?.onSaveInstanceState()
                    binding.catalogRecyclerView.post {
                        adapter.setData(state.products, state.query)
                        adapter.notifyDataSetChanged()
                        binding.catalogRecyclerView.layoutManager?.onRestoreInstanceState(scrollState)
                    }
                }
                showLoading(false)
            }
            is CatalogState.PageLoaded -> {
                setupToolbar(state.coins)
                lifecycleScope.launch {
                    val scrollState = binding.catalogRecyclerView.layoutManager?.onSaveInstanceState()
                    binding.catalogRecyclerView.post {
                        val count = binding.catalogRecyclerView.adapter?.itemCount ?: 0
                        adapter.addData(state.productList)
                        adapter.notifyItemRangeChanged(count, state.productList.size - 1, Unit)
                        binding.catalogRecyclerView.layoutManager?.onRestoreInstanceState(scrollState)
                    }
                }
            }
            is CatalogState.FavoritesUpdated -> {
                setupToolbar(state.coins)
                setupShopBucket(state.cartSize)
                lifecycleScope.launch {
                    val scrollState = binding.catalogRecyclerView.layoutManager?.onSaveInstanceState()
                    binding.catalogRecyclerView.post {
                        adapter.setData(state.products)
                        adapter.notifyItemChanged(state.index)
                        binding.catalogRecyclerView.layoutManager?.onRestoreInstanceState(scrollState)
                    }
                }
            }
            is CatalogState.CartSizeUpdated -> {
                setupShopBucket(state.cartSize)
                setupToolbar(state.coins)
            }
        }
    }

    private fun onDataLoaded(state: CatalogState.Loaded) {
        setupShopBucket(state.cartSize)
        lifecycleScope.launch {
            binding.catalogRecyclerView.post {
                adapter.setData(state.productList)
                adapter.notifyDataSetChanged()
            }
        }
        setupToolbar(state.coins)
        binding.body.isVisible = state.productList.isNotEmpty()

        binding.emptyState.apply {
            isVisible = state.productList.isEmpty()
            if (state.productList.isEmpty()) {
                text = getString(EmptyState.NO_SEARCH_ITEMS.string)
            }
        }
        showLoading(false)
    }

    private fun setupToolbar(coins: Long) {
        binding.toolbar.coins.textCoins.text =
            resources.getQuantityString(
                common.plurals.profile_screen_format_coins,
                coins.toInt(),
                coins.formatCoins()
            )
    }

    private fun openFilters() {
        setFragmentResultListener(REQUEST_CODE_PRODUCT_FILTERS) { key, bundle ->
            if (key == REQUEST_CODE_PRODUCT_FILTERS) {
                val filters = bundle["filters"] as ProductFilters
                viewModel.intent.postValue(CatalogIntent.FilterApplied(filters))
            }
            clearFragmentResultListener(REQUEST_CODE_PRODUCT_FILTERS)
        }
        viewModel.intent.value = CatalogIntent.FilterTapped
    }

    private fun setupShopBucket(size: Int) {
        binding.shopBucket.root.isVisible = size > 0
        binding.shopBucket.bucketItems.text = size.toString()
    }

    private fun onQueryChanged(query: CharSequence) {
        if (!viewModel.isSearchActive) return
        viewModel.intent.postValue(CatalogIntent.Search(query.toString()))
    }

    private fun showFiltersButton() {
        val filterApplied = !viewModel.filters.isEmpty()
        val icon = if (filterApplied) common.drawable.ic_toolbar_filter_selected else common.drawable.ic_toolbar_filter
        binding.configuration.filter.setCompoundDrawables(start = icon)
    }

    protected open fun onFavoriteClick(dbId: Int, productId: String, checked: Boolean) {
        viewModel.intent.value = CatalogIntent.FavoriteChanged(dbId, productId, checked)
    }

    protected open fun onCardClick(productId: String) {
        setFragmentResultListener(REQUEST_CODE_PRODUCT_DETAILS) { key, bundle ->
            if (key == REQUEST_CODE_PRODUCT_DETAILS) {
                val dbId = bundle.getInt("dbId")
                val id = bundle.getString("productId").orEmpty()
                val checked = bundle.getBoolean("checked")
                viewModel.intent.postValue(CatalogIntent.FavoriteChanged(dbId, id, checked))
            }
            clearFragmentResultListener(REQUEST_CODE_PRODUCT_DETAILS)
        }
    }

    private fun onCartClick() {
        viewModel.intent.postValue(CatalogIntent.Cart)
        setFragmentResultListener(REQUEST_CODE_CART_CLOSED) { _, _ ->
            viewModel.intent.postValue(CatalogIntent.CartClosed)
            clearFragmentResultListener(REQUEST_CODE_CART_CLOSED)
        }
    }
    protected open fun updateToolbarTitle(isFavorites: Boolean) {}
}

enum class EmptyState(val string: Int) {
    NO_FAVORITE_ITEMS(R.string.no_favorites_found),
    NO_SEARCH_ITEMS(R.string.no_items_found),
}
