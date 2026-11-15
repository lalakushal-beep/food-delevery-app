package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,
    val menuItemId: String,
    val quantity: Int,
    val customization: String,
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val placedAtMillis: Long,
    val formattedDate: String,
    val statusName: String,
    val itemsJson: String,
    val totalInr: Int,
    val discountInr: Int,
    val deliveryAddress: String,
    val recipientName: String,
    val phoneNumber: String,
    val deliveryInstruction: String,
    val kitchenNote: String,
    val paymentMethodName: String,
    val estimatedMinutes: Int,
)

object OrderConverters {
    fun encodeItems(items: List<OrderLineItem>): String {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("menuItemId", item.menuItemId)
                put("name", item.name)
                put("quantity", item.quantity)
                put("unitPriceInr", item.unitPriceInr)
                put("isVeg", item.isVeg)
                put("customization", item.customization)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun decodeItems(json: String): List<OrderLineItem> {
        val list = mutableListOf<OrderLineItem>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                OrderLineItem(
                    menuItemId = obj.getString("menuItemId"),
                    name = obj.getString("name"),
                    quantity = obj.getInt("quantity"),
                    unitPriceInr = obj.getInt("unitPriceInr"),
                    isVeg = obj.getBoolean("isVeg"),
                    customization = obj.optString("customization", "Regular"),
                )
            )
        }
        return list
    }

    @TypeConverter
    fun statusFromName(name: String): OrderStatus = when (name) {
        "CONFIRMED" -> OrderStatus.ACCEPTED
        else -> runCatching { OrderStatus.valueOf(name) }.getOrDefault(OrderStatus.PENDING)
    }

    @TypeConverter
    fun paymentFromName(name: String): PaymentMethod =
        runCatching { PaymentMethod.valueOf(name) }.getOrDefault(PaymentMethod.CASH_ON_DELIVERY)
}
