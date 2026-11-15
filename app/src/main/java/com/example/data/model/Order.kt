package com.example.data.model

enum class OrderStatus(
    val title: String,
    val subtitle: String,
    val stepIndex: Int,
) {
    PENDING("Pending", "Order placed, waiting for restaurant confirmation", 0),
    ACCEPTED("Accepted", "Restaurant accepted your order", 1),
    PREPARING("Preparing", "Chef is freshly cooking your meal", 2),
    READY("Ready", "Packed and ready for rider pickup", 3),
    OUT_FOR_DELIVERY("Out for Delivery", "Rider is on the way to your doorstep", 4),
    DELIVERED("Delivered", "Enjoy your fresh hot meal!", 5),
    REJECTED("Rejected", "Order was declined by the restaurant", -1);

    fun next(): OrderStatus = when (this) {
        PENDING -> ACCEPTED
        ACCEPTED -> PREPARING
        PREPARING -> READY
        READY -> OUT_FOR_DELIVERY
        OUT_FOR_DELIVERY -> DELIVERED
        DELIVERED -> DELIVERED
        REJECTED -> REJECTED
    }

    val isActive: Boolean
        get() = this != DELIVERED && this != REJECTED

    companion object {
        val workflowSteps: List<OrderStatus> = listOf(
            PENDING,
            ACCEPTED,
            PREPARING,
            READY,
            OUT_FOR_DELIVERY,
            DELIVERED,
        )
    }
}

enum class PaymentMethod(val title: String, val subtitle: String) {
    CASH_ON_DELIVERY("Cash on Delivery (COD)", "Pay with cash when your order arrives"),
    UPI_AT_DOORSTEP("UPI QR at Doorstep", "Scan rider's UPI QR upon delivery (no gateway needed)"),
}

data class DeliveryAddress(
    val id: String,
    val label: String,
    val recipientName: String,
    val phoneNumber: String,
    val addressLine: String,
    val landmark: String,
)

data class OrderLineItem(
    val menuItemId: String,
    val name: String,
    val quantity: Int,
    val unitPriceInr: Int,
    val isVeg: Boolean,
    val customization: String,
)

data class Order(
    val orderId: String,
    val placedAtMillis: Long,
    val formattedDate: String,
    val status: OrderStatus,
    val items: List<OrderLineItem>,
    val totalInr: Int,
    val discountInr: Int,
    val deliveryAddress: String,
    val recipientName: String,
    val phoneNumber: String,
    val deliveryInstruction: String,
    val kitchenNote: String,
    val paymentMethod: PaymentMethod,
    val estimatedMinutes: Int,
)

data class Coupon(
    val code: String,
    val title: String,
    val description: String,
    val minOrderInr: Int,
    val discountAmountInr: Int,
    val isFreeDelivery: Boolean = false,
)
