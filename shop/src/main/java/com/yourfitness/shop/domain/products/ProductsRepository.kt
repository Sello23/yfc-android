package com.yourfitness.shop.domain.products

import com.yourfitness.common.ui.utils.getSortWeight
import com.yourfitness.common.ui.utils.toLocalGenderValue
import com.yourfitness.shop.data.ShopStorage
import com.yourfitness.shop.data.dao.ProductsDao
import com.yourfitness.shop.data.entity.ProductDataEntity
import com.yourfitness.shop.data.entity.FavoritesEntity
import com.yourfitness.shop.data.entity.ProductEntity
import com.yourfitness.shop.data.entity.ProductTypeId
import com.yourfitness.shop.data.mapper.toEntity
import com.yourfitness.shop.data.mapper.toImageEntity
import com.yourfitness.shop.network.ShopRestApi
import com.yourfitness.shop.network.dto.Favorites
import timber.log.Timber
import javax.inject.Inject

class ProductsRepository @Inject constructor(
    private val dao: ProductsDao,
    private val restApi: ShopRestApi,
    private val shopStorage: ShopStorage,
) {

    suspend fun downloadProducts(productTypeId: ProductTypeId) {
        var syncedAt: Long? = null
        try {
            syncedAt =  when (productTypeId) {
                ProductTypeId.APPAREL -> shopStorage.apparelLastSyncedAt
                ProductTypeId.EQUIPMENT -> shopStorage.equipmentLastSyncedAt
                ProductTypeId.ACCESSORIES -> shopStorage.accessoriesLastSyncedAt
                ProductTypeId.SERVICES -> shopStorage.servicesLastSyncedAt
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        val updatedAtData =  downloadProducts(productTypeId, syncedAt)
        val newSyncedAt = updatedAtData.maxOrNull()
        newSyncedAt?.let {
            when (productTypeId) {
                ProductTypeId.APPAREL -> shopStorage.apparelLastSyncedAt = newSyncedAt
                ProductTypeId.EQUIPMENT -> shopStorage.equipmentLastSyncedAt = newSyncedAt
                ProductTypeId.ACCESSORIES -> shopStorage.accessoriesLastSyncedAt = newSyncedAt
                ProductTypeId.SERVICES -> shopStorage.servicesLastSyncedAt = newSyncedAt
            }
        }
    }

    private suspend fun downloadProducts(
        productTypeId: ProductTypeId,
        now: Long?
    ): MutableSet<Long> {
        val updatedAtData = mutableSetOf<Long>()
        val response = when (productTypeId) {
            ProductTypeId.APPAREL -> restApi.getApparelList(now)
            ProductTypeId.EQUIPMENT -> restApi.getEquipmentList(now)
            ProductTypeId.ACCESSORIES -> restApi.getAccessoriesList(now)
            else -> restApi.getServicesList(now)
        }
        response.forEach { product ->
            product.updatedAt?.let { updatedAtData.add(it) }
            val apparelEntity = product.toEntity(productTypeId)
            dao.saveProduct(apparelEntity)
            val apparelId =
                dao.getProductId(product.id, productTypeId.value) ?: return updatedAtData

            if (productTypeId == ProductTypeId.APPAREL) {
                product.colors?.forEach { color ->
                    val colorEntity = color.toEntity(apparelId)
                    dao.saveColorItem(colorEntity)
                    val colorId = dao.getColorId(color.color, apparelId) ?: return updatedAtData

                    color.images?.forEach { image ->
                        val imageEntity = image.toImageEntity(colorId)
                        dao.saveImageItem(imageEntity)
                    }

                    val sizes = color.sizes?.sortedBy { it.sequence }
                    sizes?.forEach { size ->
                        val sizeEntity = size.toEntity(colorId)
                        dao.saveSizeItem(sizeEntity)
                    }
                }
            }
        }

        return updatedAtData
    }

    suspend fun getProductId(productId: String, productTypeId: Int): Int? {
        return dao.getProductId(productId, productTypeId)
    }

    suspend fun downloadFavorites() {
        try {
            val favorites = restApi.getFavorites()
            favorites?.map {
                val id = dao.getProductId(it) ?: -1
                dao.saveToFavorites(
                    FavoritesEntity(
                        apparelId = it,
                        id = id
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    suspend fun searchProducts(query: String, productId: Int): List<ProductDataEntity> {
        val normalizedQuery = query.trim().lowercase()
        return getProductList(productId).filter {
                it.product.name.lowercase().contains(normalizedQuery)
            }
    }

    suspend fun getProductList(productTypeId: Int): List<ProductDataEntity> = dao.readAllProducts(productTypeId)

    suspend fun getProductListSimple(productTypeId: Int): List<ProductEntity> = dao.readAllProductsSimple(productTypeId)

    suspend fun getProductDetails(id: String, productId: Int): ProductDataEntity = dao.getProductById(id, productId)

    suspend fun saveFavorite(checked: Boolean, id: Int, productId: String) {
        val favorite = FavoritesEntity(apparelId = productId, id = id)
        if (checked) {
            dao.saveToFavorites(favorite)
        } else {
            dao.deleteFromFavorites(productId)
        }
    }

    suspend fun syncFavorites() {
        try {
            val favoritesToUpload = dao.readAllFavorites().orEmpty()
            restApi.saveFavorites(Favorites(
                favoritesToUpload.map { it.apparelId }
            ))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    suspend fun prepareFilterData(
        filters: ProductFilters,
        productTypeId: Int,
        getMaxProductsPrice: (suspend (list: List<ProductDataEntity>) -> Long)? = null,
        getMinProductsPrice: (suspend (list: List<ProductDataEntity>) -> Long)? = null,
        sortSelected: Boolean = false
    ): ProductFilters? {
        if (filters.allTypes.isEmpty() || filters.allBrands.isEmpty() || filters.isPriceRangeEmpty() || sortSelected) {
            val productsList = getProductList(productTypeId)
            return filters.copy(
                allTypes = productsList.getAllSubCategories(sortSelected, filters.types),
                allBrands = productsList.getAllBrands(sortSelected, filters.brands),
                allGenders = productsList.getAllGenders(),
                defaultMaxPrice = getMaxProductsPrice?.invoke(productsList) ?: filters.defaultMaxPrice,
                defaultMinPrice = getMinProductsPrice?.invoke(productsList) ?: filters.defaultMinPrice
            )
        }

        return null
    }

    private fun List<ProductDataEntity>.getAllSubCategories(
        sortSelected: Boolean,
        categories: List<String>
    ): List<String> {
        val allItems = mapNotNull { it.product.subcategory }
            .distinct()
        return if (sortSelected) {
            val checkedItems = allItems.filter { categories.contains(it) }
                .sortedBy { it.lowercase() }
            val uncheckedItems = allItems.filter { !categories.contains(it) }
                .sortedBy { it.lowercase() }
            checkedItems + uncheckedItems
        } else {
            allItems.sortedBy { it.lowercase() }
        }
    }

    private fun List<ProductDataEntity>.getAllBrands(
        sortSelected: Boolean,
        brands: List<String>
    ): List<Pair<String, String?>> {
        val allItems = mapNotNull { it.product.brandName?.let { brandName -> brandName to it.product.brandImage } }
            .distinct()
         return if (sortSelected) {
            val checkedItems = allItems.filter { brands.contains(it.first) }
                .sortedBy { it.first.lowercase() }
            val uncheckedItems = allItems.filter { !brands.contains(it.first) }
                .sortedBy { it.first.lowercase() }
            checkedItems + uncheckedItems
        } else {
            allItems.sortedBy { it.first.lowercase() }
        }
    }

    private fun List<ProductDataEntity>.getAllGenders(): List<String> {
        return asSequence().map { it.product.gender }
            .map { it.toLocalGenderValue() }
            .distinct()
            .filter { it.isNotBlank() }
            .sortedBy { it.getSortWeight() }
            .toList()
    }
}
