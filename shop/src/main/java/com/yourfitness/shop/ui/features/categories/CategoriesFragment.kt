package com.yourfitness.shop.ui.features.categories

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.FragmentCategoriesBinding
import com.yourfitness.shop.domain.model.CategoryCard
import com.yourfitness.shop.ui.utils.setupCoinBalanceCard
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CategoriesFragment : MviFragment<CategoriesIntent, Any, CategoriesViewModel>() {
    override val binding: FragmentCategoriesBinding by viewBinding()
    override val viewModel: CategoriesViewModel by viewModels()
    private val adapter by lazy { CategoriesAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading(true)
        viewModel.handleIntent(CategoriesIntent.ScreenOpened)
        setupRecyclerView()
        setupCard()
        setupViews()
        setupToolbar(binding.toolbar.root)
        binding.toolbar.root.title = getString(R.string.yfc_shop)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            try {
                val progress: View? = requireActivity().findViewById(com.yourfitness.common.R.id.progress)
                if (progress?.isVisible == false) {
                    findNavController().navigateUp()
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun renderState(state: Any) {
        when (state) {
            is CategoriesState.CartSizeLoaded -> {
                setupShopBucket(state.size)
                setupCoinBalanceCard(state.coins, state.coinsCost, state.currency)
                showLoading(false)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter.setData(setupCategoryCards())
        binding.recyclerView.adapter = adapter
    }

    private fun setupCategoryCards(): List<CategoryCard> {
        return mutableListOf(
            CategoryCard(
                getString(R.string.card_apparel),
                R.drawable.image_apparel
            ) { cardTitle -> viewModel.handleIntent(CategoriesIntent.Apparel(cardTitle)) },
            CategoryCard(
                getString(R.string.card_services),
                R.drawable.image_services,
            ) { cardTitle -> viewModel.handleIntent(CategoriesIntent.Services(cardTitle)) },
            CategoryCard(
                getString(R.string.card_equipment),
                R.drawable.image_equipment,
            ) { cardTitle -> viewModel.handleIntent(CategoriesIntent.Equipment(cardTitle)) },
            CategoryCard(
                getString(R.string.card_accessories),
                R.drawable.image_accessories,
            ) { cardTitle -> viewModel.handleIntent(CategoriesIntent.Accessories(cardTitle)) },
        )
    }

    private fun setupCard() {
        binding.viewOrders.textTitle.text = getString(R.string.my_orders)
        binding.viewOrders.textSubtitle.text = getString(R.string.my_orders_msg)
        binding.viewOrders.image.setImageResource(R.drawable.image_orders)
        binding.viewOrders.textButton.text = getString(R.string.my_orders_action)
    }

    private fun setupViews() {
        binding.shopBucket.root.setOnClickListener {
            viewModel.handleIntent(CategoriesIntent.Cart)
        }

        binding.viewOrders.root.setOnClickListener {
            viewModel.handleIntent(CategoriesIntent.OrderHistory)
        }
    }

    private fun setupShopBucket(size: Int) {
        binding.shopBucket.root.isVisible = size > 0
        binding.shopBucket.bucketItems.text = size.toString()
    }

    private fun setupCoinBalanceCard(coins: Long, coinsCost: Double, currency: String) {
        binding.coinsBalanceContainer.setupCoinBalanceCard(coins, coinsCost, currency) {
            viewModel.intent.value = CategoriesIntent.NavigateTo(it)
        }
    }
}