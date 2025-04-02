package com.yourfitness.shop.ui.features.catalog.filters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.yourfitness.common.domain.location.LocationRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.common.ui.utils.toLocalGenderValue
import com.yourfitness.shop.data.entity.ProductDataEntity
import com.yourfitness.shop.domain.products.*
import com.yourfitness.shop.ui.constants.Constants
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToLong

@HiltViewModel
class ServicesFiltersViewModel @Inject constructor(
    navigator: ShopNavigator,
    productsRepository: ProductsRepository,
    private val locationRepository: LocationRepository,
    savedState: SavedStateHandle
) : ProductFiltersViewModel(navigator, productsRepository, savedState) {

    private var currentLatLang: LatLng? = null

    override suspend fun getMinProductsPrice(list: List<ProductDataEntity>): Long {
        return list.minOf {
            calculateDistance(
                it.product.latitude,
                it.product.longitude
            ) ?: Long.MAX_VALUE
        }
    }

    override suspend fun getMaxProductsPrice(list: List<ProductDataEntity>): Long {
        return list.maxOf {
            calculateDistance(
                it.product.latitude,
                it.product.longitude
            ) ?: Long.MIN_VALUE
        }
    }

    override suspend fun filterProducts() {
        filteredProducts = products.filterByDistance().filterByCategory()
    }

    private suspend fun calculateDistance(latitude: Double?, longitude: Double?): Long? {
        return if (latitude != null && longitude != null) {
            if (currentLatLang == null) {
                currentLatLang = locationRepository.lastLocation()
            }
            SphericalUtil.computeDistanceBetween(LatLng(latitude, longitude), currentLatLang).roundToLong()
        } else {
            null
        }
    }

    private suspend fun List<ProductDataEntity>.filterByDistance(): List<ProductDataEntity> {
        return filter {
            val distance = calculateDistance(it.product.latitude, it.product.longitude) ?: -1
            distance in 0..filters.maxPrice
        }
    }
}

@HiltViewModel
open class ProductFiltersViewModel @Inject constructor(
    private val navigator: ShopNavigator,
    private val productsRepository: ProductsRepository,
    savedState: SavedStateHandle
) : MviViewModel<Any, Any>() {

    val currency = savedState.get<String>(Constants.CURRENCY).orEmpty()
    private val searchQuery = savedState.get<String>(Constants.SEARCH_QUERY).orEmpty()
    private val isFavorites = savedState.get<Boolean>(Constants.FAVORITES) ?: false
    private val productTypeId = savedState.get<Int>(Constants.PRODUCT_TYPE_ID) ?: 0
    var filters = savedState.get<ProductFilters>(Constants.FILTERS) ?: ProductFilters()
        protected set
    protected var products: List<ProductDataEntity> = listOf()
    var filteredProducts: List<ProductDataEntity> = listOf()
        protected set


    var showAll = false
        private set
    var showAllBrands = false
        private set
    var showAllGenders = false
        private set

    init {
        loadFilters()
    }

    override fun handleIntent(intent: Any) {
        when (intent) {
            is FiltersIntent.RangeChanged -> updateRange(intent.minRange, intent.maxRange)
            is FiltersIntent.Reset -> resetFilters()
            is FiltersIntent.InputRange -> navigator.navigate(
                ShopNavigation.EnterRange(
                    currency, filters.defaultMinPrice, filters.defaultMaxPrice
                )
            )
            is FiltersIntent.SeeAllCategories -> seeAllCategories()
            is FiltersIntent.SeeAllGenders -> seeAllGenders()
            is FiltersIntent.SeeAllBrands -> seeAllBrands()
            is FiltersIntent.TypesChanged -> updateTypes(intent.types)
            is FiltersIntent.BrandsChanged -> updateBrands(intent.types)
            is FiltersIntent.GendersChanged -> updateGenders(intent.types)
            is FiltersIntent.ResetSeeAll -> resetSeeAll()
        }
    }

    private fun loadFilters() {
        viewModelScope.launch {
            products = productsRepository.searchProducts(searchQuery, productTypeId)
            if (isFavorites) {
                products = products.filter { it.favourite != null }
            }
            prepareFilters()
            filterProducts()
            state.postValue(FiltersState.Loaded(filters, filteredProducts.size))
        }
    }

    private suspend fun prepareFilters() {
        productsRepository.prepareFilterData(
            filters,
            productTypeId,
            ::getMaxProductsPrice,
            ::getMinProductsPrice,
            true
        )?.let {
            filters = it
        }
    }

    private fun updateRange(minRange: Long, maxRange: Long) {
        filters = filters.copy(minPrice = minRange, maxPrice = maxRange)
        fetchProductsCount()
    }

    private fun updateTypes(types: List<String>) {
        filters = filters.copy(types = types)
        fetchProductsCount()
    }

    private fun updateBrands(types: List<String>) {
        filters = filters.copy(brands = types)
        fetchProductsCount()
    }

    private fun updateGenders(types: List<String>) {
        filters = filters.copy(genders = types)
        fetchProductsCount()
    }

    private fun resetFilters() {
        filters = filters.reset()
        filteredProducts = products
        state.postValue(FiltersState.DataReset(filters, filteredProducts.size))
    }

    private fun fetchProductsCount() {
        viewModelScope.launch {
            try {
                filterProducts()
                state.postValue(FiltersState.Updated(filteredProducts.size, filters))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(FiltersState.Error(error))
            }
        }
    }

    protected open suspend fun filterProducts() {
        filteredProducts = products.filterByPrice().filterByCategory().filterByBrand().filterByGender()
    }

    private fun List<ProductDataEntity>.filterByBrand(): List<ProductDataEntity> {
        return filter { item ->
            filters.brands.isEmpty() || filters.brands.contains(item.product.brandName.orEmpty())
        }
    }

    private fun List<ProductDataEntity>.filterByGender(): List<ProductDataEntity> {
        return filter { item ->
            filters.genders.isEmpty() || filters.genders.contains(item.product.gender.toLocalGenderValue())
        }
    }

    protected fun List<ProductDataEntity>.filterByCategory(): List<ProductDataEntity> {
        return filter { item ->
                filters.types.isEmpty() || filters.types.contains(item.product.subcategory.orEmpty())
            }
    }

    private fun List<ProductDataEntity>.filterByPrice(): List<ProductDataEntity> {
        val min = filters.minPrice
        val max = filters.maxPrice
        return filter { it.product.price in min..max }
    }

    private fun seeAllCategories() {
        showAll = true
        fetchProductsCount()
    }

    private fun seeAllGenders() {
        showAll = true
        fetchProductsCount()
    }

    private fun seeAllBrands() {
        showAllBrands = true
        fetchProductsCount()
    }

    private fun resetSeeAll() {
        showAll = false
        showAllBrands = false
        viewModelScope.launch {
            try {
                prepareFilters()
                fetchProductsCount()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    protected open suspend fun getMinProductsPrice(list: List<ProductDataEntity>): Long {
        return list.minOf { it.product.price }
    }

    protected open suspend fun getMaxProductsPrice(list: List<ProductDataEntity>): Long {
        return list.maxOf { it.product.price }
    }
}

open class FiltersIntent {
    data class RangeChanged(val minRange: Long, val maxRange: Long) : FiltersIntent()
    object Reset : FiltersIntent()
    object InputRange : FiltersIntent()
    object SeeAllCategories : FiltersIntent()
    object SeeAllGenders : FiltersIntent()
    object SeeAllBrands : FiltersIntent()
    data class TypesChanged(val types: List<String>) : FiltersIntent()
    data class BrandsChanged(val types: List<String>) : FiltersIntent()
    data class GendersChanged(val types: List<String>) : FiltersIntent()
    object ResetSeeAll : FiltersIntent()
}

open class FiltersState {
    object Loading : FiltersState()
    data class Loaded(
        val filters: ProductFilters,
        val count: Int
    ) : FiltersState()
    data class DataReset(
        val filters: ProductFilters,
        val count: Int
    ) : FiltersState()
    data class Updated(val count: Int, val filters: ProductFilters) : FiltersState()
    data class AllCategoriesLoaded(val count: Int) : FiltersState()
    data class Error(val error: Exception) : FiltersState()
}