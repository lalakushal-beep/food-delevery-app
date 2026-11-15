package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.R

enum class FoodCategory(
    val id: String,
    val label: String,
    val tagline: String,
    @DrawableRes val imageRes: Int,
) {
    ALL("all", "All", "Full Kitchen", R.drawable.img_thali),
    THALI("thali", "Thali", "Royal Platters", R.drawable.img_thali),
    BIRYANI("biryani", "Biryani", "Slow-Dum Pots", R.drawable.img_biryani),
    DOSA("dosa", "Dosa", "Crispy & Golden", R.drawable.img_dosa),
    PANEER_TIKKA("paneer_tikka", "Paneer Tikka", "Charred Tandoor", R.drawable.img_paneer_tikka),
    PIZZA("pizza", "Pizza", "Desi Wood-Fired", R.drawable.img_pizza),
    BURGER("burger", "Burger", "Masala Brioche", R.drawable.img_burger),
    DESSERTS("desserts", "Desserts", "Sweet Saffron", R.drawable.img_gulab_jamun);

    companion object {
        val selectableCategories: List<FoodCategory> = entries.filter { it != ALL }
    }
}

enum class SpiceLevel(val label: String) {
    NONE("Sweet / Mild"),
    MILD("Mild Spice"),
    MEDIUM("Medium Spice"),
    SPICY("Fiery Tandoor"),
}

enum class MenuSortOption(val label: String) {
    RECOMMENDED("Recommended"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    TOP_RATED("Top Rated"),
}
