package com.example.data.model

data class CartItem(
    val id: String,
    val menuItem: MenuItem,
    val quantity: Int,
    val customization: String,
) {
    val lineTotalInr: Int
        get() = menuItem.priceInr * quantity
}

data class BillSummary(
    val itemTotalInr: Int,
    val packagingFeeInr: Int,
    val deliveryFeeInr: Int,
    val discountInr: Int,
    val gstInr: Int,
    val grandTotalInr: Int,
    val appliedCouponCode: String?,
    val freeDeliveryUnlocked: Boolean,
)
