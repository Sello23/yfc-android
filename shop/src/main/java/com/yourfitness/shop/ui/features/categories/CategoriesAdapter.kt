package com.yourfitness.shop.ui.features.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yourfitness.shop.domain.model.CategoryCard
import com.yourfitness.shop.databinding.ItemCategoryBinding

class CategoriesAdapter(
) : RecyclerView.Adapter<CategoryViewHolder>() {

    private val categoryCardList = arrayListOf<CategoryCard>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categoryCardList[position])
    }

    override fun getItemCount(): Int = categoryCardList.size

    fun setData(categoryCard: List<CategoryCard>) {
        this.categoryCardList.clear()
        this.categoryCardList.addAll(categoryCard)
    }
}

class CategoryViewHolder(
    binding: ItemCategoryBinding,
) : RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemCategoryBinding = ItemCategoryBinding.bind(itemView)
    fun bind(categoryCard: CategoryCard) {
        itemView.setOnClickListener { categoryCard.onCardClick(categoryCard.title) }
        binding.textTitle.text = categoryCard.title
        binding.backgroundImage.setImageResource(categoryCard.backgroundImage)
    }
}
