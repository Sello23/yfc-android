package com.yourfitness.shop.ui.utils

import androidx.recyclerview.widget.DiffUtil
import com.yourfitness.shop.domain.model.CatalogCard

class GoodsDiffCallback(
    private val oldList: List<CatalogCard>,
    private val newList: List<CatalogCard>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}