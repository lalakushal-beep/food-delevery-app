package com.example.data.remote

import com.example.data.local.OrderConverters
import com.example.data.model.Order
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreOrderLineItem(
    val menuItemId: String = "",
    val name: String = "",
    val quantity: Long = 1L,
    val unitPriceInr: Long = 0L,
    val isVeg: Boolean = true,
    val customization: String = "Regular",
)

@IgnoreExtraProperties
data class FirestoreOrderDocument(
    @DocumentId val orderId: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val placedAtMillis: Long = 0L,
    val formattedDate: String = "",
    val status: String = OrderStatus.PENDING.name,
    val items: List<FirestoreOrderLineItem> = emptyList(),
    val totalInr: Long = 0L,
    val discountInr: Long = 0L,
    val deliveryAddress: String = "",
    val recipientName: String = "",
    val phoneNumber: String = "",
    val deliveryInstruction: String = "",
    val kitchenNote: String = "",
    val paymentMethod: String = PaymentMethod.CASH_ON_DELIVERY.name,
    val estimatedMinutes: Long = 25L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun toDomain(): Order = Order(
        orderId = orderId,
        placedAtMillis = placedAtMillis,
        formattedDate = formattedDate,
        status = OrderConverters.statusFromName(status),
        items = items.map { item ->
            OrderLineItem(
                menuItemId = item.menuItemId,
                name = item.name,
                quantity = item.quantity.toInt().coerceAtLeast(1),
                unitPriceInr = item.unitPriceInr.toInt().coerceAtLeast(0),
                isVeg = item.isVeg,
                customization = item.customization.ifBlank { "Regular" },
            )
        },
        totalInr = totalInr.toInt().coerceAtLeast(0),
        discountInr = discountInr.toInt().coerceAtLeast(0),
        deliveryAddress = deliveryAddress,
        recipientName = recipientName,
        phoneNumber = phoneNumber,
        deliveryInstruction = deliveryInstruction,
        kitchenNote = kitchenNote,
        paymentMethod = OrderConverters.paymentFromName(paymentMethod),
        estimatedMinutes = estimatedMinutes.toInt().coerceAtLeast(1),
    )
}

fun Order.toFirestorePayload(
    userId: String,
    userEmail: String?,
): Map<String, Any> {
    val serializedItems = items.map { line ->
        mapOf(
            "menuItemId" to line.menuItemId,
            "name" to line.name,
            "quantity" to line.quantity.toLong(),
            "unitPriceInr" to line.unitPriceInr.toLong(),
            "isVeg" to line.isVeg,
            "customization" to line.customization,
        )
    }

    return buildMap {
        put("userId", userId)
        if (!userEmail.isNullOrBlank()) {
            put("userEmail", userEmail)
        }
        put("placedAtMillis", placedAtMillis)
        put("formattedDate", formattedDate)
        put("status", status.name)
        put("items", serializedItems)
        put("totalInr", totalInr.toLong())
        put("discountInr", discountInr.toLong())
        put("deliveryAddress", deliveryAddress)
        put("recipientName", recipientName)
        put("phoneNumber", phoneNumber)
        put("deliveryInstruction", deliveryInstruction)
        put("kitchenNote", kitchenNote)
        put("paymentMethod", paymentMethod.name)
        put("estimatedMinutes", estimatedMinutes.toLong())
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }
}
