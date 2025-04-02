package com.yourfitness.shop.domain.model

data class CategoryCard(
    val title: String,
    val backgroundImage: Int,
    val onCardClick: (cardTitle: String) -> Unit
)
