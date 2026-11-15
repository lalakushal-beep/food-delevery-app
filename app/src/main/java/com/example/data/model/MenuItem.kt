package com.example.data.model

import androidx.annotation.DrawableRes

data class MenuItem(
    val id: String,
    val name: String,
    val category: FoodCategory,
    val priceInr: Int,
    val originalPriceInr: Int? = null,
    val description: String,
    @DrawableRes val imageRes: Int,
    val isVeg: Boolean,
    val spiceLevel: SpiceLevel,
    val prepTimeMins: Int,
    val rating: Double,
    val reviewCount: Int,
    val isBestseller: Boolean = false,
    val servesLabel: String = "Serves 1",
    val customizationChoices: List<String> = listOf("Regular", "Medium Spice", "Extra Spicy"),
)
