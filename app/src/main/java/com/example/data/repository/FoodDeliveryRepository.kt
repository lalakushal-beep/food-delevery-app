package com.example.data.repository

import com.example.data.local.CartDao
import com.example.data.local.CartItemEntity
import com.example.data.local.OrderConverters
import com.example.data.local.OrderDao
import com.example.data.local.OrderEntity
import com.example.data.model.BillSummary
import com.example.data.model.CartItem
import com.example.data.model.MenuItem
import com.example.data.model.Order
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.remote.FirestoreOrderRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class FoodDeliveryRepository(
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val firestoreOrderRepository: FirestoreOrderRepository? = null,
) {
    val menuItems: List<MenuItem> = MenuCatalog.items

    fun observeCartItems(): Flow<List<CartItem>> =
        cartDao.observeCartItems().map { entities ->
            entities.mapNotNull { entity ->
                val menuItem = MenuCatalog.findById(entity.menuItemId) ?: return@mapNotNull null
                CartItem(
                    id = entity.id,
                    menuItem = menuItem,
                    quantity = entity.quantity,
                    customization = entity.customization,
                )
            }
        }

    fun observeOrders(): Flow<List<Order>> =
        orderDao.observeOrders().map { entities ->
            entities.map { entity ->
                Order(
                    orderId = entity.orderId,
                    placedAtMillis = entity.placedAtMillis,
                    formattedDate = entity.formattedDate,
                    status = OrderConverters.statusFromName(entity.statusName),
                    items = OrderConverters.decodeItems(entity.itemsJson),
                    totalInr = entity.totalInr,
                    discountInr = entity.discountInr,
                    deliveryAddress = entity.deliveryAddress,
                    recipientName = entity.recipientName,
                    phoneNumber = entity.phoneNumber,
                    deliveryInstruction = entity.deliveryInstruction,
                    kitchenNote = entity.kitchenNote,
                    paymentMethod = OrderConverters.paymentFromName(entity.paymentMethodName),
                    estimatedMinutes = entity.estimatedMinutes,
                )
            }
        }

    suspend fun seedSampleOrdersIfNeeded() {
        if (orderDao.countOrders() > 0) return

        val now = System.currentTimeMillis()
        val yesterday = now - 24 * 60 * 60 * 1000L
        val threeDaysAgo = now - 3 * 24 * 60 * 60 * 1000L

        val sampleOrders = listOf(
            OrderEntity(
                orderId = "MFD-2041",
                placedAtMillis = yesterday,
                formattedDate = formatTimestamp(yesterday),
                statusName = OrderStatus.DELIVERED.name,
                itemsJson = OrderConverters.encodeItems(
                    listOf(
                        OrderLineItem(
                            menuItemId = "biryani_hyderabadi_chicken",
                            name = "Hyderabadi Chicken Dum Biryani",
                            quantity = 1,
                            unitPriceInr = 329,
                            isVeg = false,
                            customization = "Authentic Dum",
                        ),
                        OrderLineItem(
                            menuItemId = "paneer_tikka_smoky",
                            name = "Smoky Tandoori Paneer Tikka",
                            quantity = 1,
                            unitPriceInr = 269,
                            isVeg = true,
                            customization = "Classic Tandoori",
                        ),
                        OrderLineItem(
                            menuItemId = "dessert_gulab_jamun",
                            name = "Warm Saffron Gulab Jamun (3 Pcs)",
                            quantity = 1,
                            unitPriceInr = 129,
                            isVeg = true,
                            customization = "Serve Piping Hot",
                        ),
                    )
                ),
                totalInr = 684,
                discountInr = 100,
                deliveryAddress = "Flat 402, Lotus Residency, 12th Main Road, Indiranagar, Bengaluru",
                recipientName = "Aarav Sharma",
                phoneNumber = "9876543210",
                deliveryInstruction = "Ring bell on arrival",
                kitchenNote = "Extra mint chutney please",
                paymentMethodName = PaymentMethod.CASH_ON_DELIVERY.name,
                estimatedMinutes = 28,
            ),
            OrderEntity(
                orderId = "MFD-1987",
                placedAtMillis = threeDaysAgo,
                formattedDate = formatTimestamp(threeDaysAgo),
                statusName = OrderStatus.DELIVERED.name,
                itemsJson = OrderConverters.encodeItems(
                    listOf(
                        OrderLineItem(
                            menuItemId = "thali_maharaja",
                            name = "Maharaja Special Veg Thali",
                            quantity = 1,
                            unitPriceInr = 349,
                            isVeg = true,
                            customization = "Garlic Naan",
                        ),
                        OrderLineItem(
                            menuItemId = "dosa_ghee_masala",
                            name = "Ghee Roast Masala Dosa",
                            quantity = 1,
                            unitPriceInr = 179,
                            isVeg = true,
                            customization = "Crispy Ghee Roast",
                        ),
                    )
                ),
                totalInr = 574,
                discountInr = 0,
                deliveryAddress = "Floor 6, Horizon Tech Park, Outer Ring Road, Bellandur, Bengaluru",
                recipientName = "Aarav Sharma",
                phoneNumber = "9876543210",
                deliveryInstruction = "Leave at reception",
                kitchenNote = "",
                paymentMethodName = PaymentMethod.UPI_AT_DOORSTEP.name,
                estimatedMinutes = 25,
            ),
        )

        sampleOrders.forEach { orderDao.insert(it) }
    }

    suspend fun addToCart(menuItem: MenuItem, customization: String? = null) {
        val chosenCustomization = customization ?: menuItem.customizationChoices.firstOrNull() ?: "Regular"
        val key = "${menuItem.id}::$chosenCustomization"
        val existing = cartDao.getCartItemById(key)
        val newQty = (existing?.quantity ?: 0) + 1
        cartDao.upsert(
            CartItemEntity(
                id = key,
                menuItemId = menuItem.id,
                quantity = newQty,
                customization = chosenCustomization,
            )
        )
    }

    suspend fun incrementCartItem(cartItemId: String) {
        val existing = cartDao.getCartItemById(cartItemId) ?: return
        cartDao.upsert(existing.copy(quantity = existing.quantity + 1))
    }

    suspend fun decrementCartItem(cartItemId: String) {
        val existing = cartDao.getCartItemById(cartItemId) ?: return
        if (existing.quantity <= 1) {
            cartDao.deleteById(cartItemId)
        } else {
            cartDao.upsert(existing.copy(quantity = existing.quantity - 1))
        }
    }

    suspend fun incrementMenuItem(menuItemId: String) {
        val matches = cartDao.getCartItemsForMenu(menuItemId)
        val target = matches.lastOrNull()
        if (target != null) {
            cartDao.upsert(target.copy(quantity = target.quantity + 1))
        } else {
            val menuItem = MenuCatalog.findById(menuItemId) ?: return
            addToCart(menuItem)
        }
    }

    suspend fun decrementMenuItem(menuItemId: String) {
        val matches = cartDao.getCartItemsForMenu(menuItemId)
        val target = matches.lastOrNull() ?: return
        if (target.quantity <= 1) {
            cartDao.deleteById(target.id)
        } else {
            cartDao.upsert(target.copy(quantity = target.quantity - 1))
        }
    }

    suspend fun removeCartItem(cartItemId: String) {
        cartDao.deleteById(cartItemId)
    }

    suspend fun placeOrder(
        cartItems: List<CartItem>,
        bill: BillSummary,
        recipientName: String,
        phoneNumber: String,
        addressLine: String,
        landmark: String,
        deliveryInstruction: String,
        kitchenNote: String,
        paymentMethod: PaymentMethod,
    ): Order {
        val now = System.currentTimeMillis()
        val orderId = generateUniqueOrderId(now)
        val fullAddress = if (landmark.isNotBlank()) "$addressLine ($landmark)" else addressLine
        val formattedDate = formatTimestamp(now)
        val lineItems = cartItems.map {
            OrderLineItem(
                menuItemId = it.menuItem.id,
                name = it.menuItem.name,
                quantity = it.quantity,
                unitPriceInr = it.menuItem.priceInr,
                isVeg = it.menuItem.isVeg,
                customization = it.customization,
            )
        }
        val estimatedMins = (cartItems.maxOfOrNull { it.menuItem.prepTimeMins } ?: 20) + 10

        val entity = OrderEntity(
            orderId = orderId,
            placedAtMillis = now,
            formattedDate = formattedDate,
            statusName = OrderStatus.PENDING.name,
            itemsJson = OrderConverters.encodeItems(lineItems),
            totalInr = bill.grandTotalInr,
            discountInr = bill.discountInr,
            deliveryAddress = fullAddress,
            recipientName = recipientName.trim(),
            phoneNumber = phoneNumber.trim(),
            deliveryInstruction = deliveryInstruction,
            kitchenNote = kitchenNote.trim(),
            paymentMethodName = paymentMethod.name,
            estimatedMinutes = estimatedMins,
        )

        val confirmedOrder = Order(
            orderId = orderId,
            placedAtMillis = now,
            formattedDate = formattedDate,
            status = OrderStatus.PENDING,
            items = lineItems,
            totalInr = bill.grandTotalInr,
            discountInr = bill.discountInr,
            deliveryAddress = fullAddress,
            recipientName = recipientName.trim(),
            phoneNumber = phoneNumber.trim(),
            deliveryInstruction = deliveryInstruction,
            kitchenNote = kitchenNote.trim(),
            paymentMethod = paymentMethod,
            estimatedMinutes = estimatedMins,
        )
        orderDao.insert(entity)
        cartDao.clearCart()

        val hasAuthUser = runCatching { Firebase.auth.currentUser != null }.getOrDefault(false)
        if (hasAuthUser) {
            firestoreOrderRepository?.createOrder(confirmedOrder)
        }

        return confirmedOrder
    }

    private suspend fun generateUniqueOrderId(timestampMillis: Long): String {
        val timePart = (timestampMillis / 1000L).toString().takeLast(3)
        repeat(10) {
            val suffix = (100..999).random()
            val candidate = "MFD-$timePart$suffix"
            if (orderDao.countByOrderId(candidate) == 0) {
                return candidate
            }
        }
        return "MFD-${timestampMillis.toString().takeLast(6)}"
    }

    suspend fun advanceOrderStatus(order: Order): Result<Unit> {
        if (!order.status.isActive) return Result.success(Unit)
        return updateOrderStatus(order.orderId, order.status.next())
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        orderDao.updateStatus(orderId, status.name)
        val hasAuthUser = runCatching { Firebase.auth.currentUser != null }.getOrDefault(false)
        if (hasAuthUser && firestoreOrderRepository != null && !isDemoSampleOrder(orderId)) {
            return firestoreOrderRepository.updateOrderStatus(orderId, status)
        }
        return Result.success(Unit)
    }

    suspend fun upsertSyncedOrders(remoteOrders: List<Order>) {
        remoteOrders.forEach { order ->
            orderDao.insert(
                OrderEntity(
                    orderId = order.orderId,
                    placedAtMillis = order.placedAtMillis,
                    formattedDate = order.formattedDate,
                    statusName = order.status.name,
                    itemsJson = OrderConverters.encodeItems(order.items),
                    totalInr = order.totalInr,
                    discountInr = order.discountInr,
                    deliveryAddress = order.deliveryAddress,
                    recipientName = order.recipientName,
                    phoneNumber = order.phoneNumber,
                    deliveryInstruction = order.deliveryInstruction,
                    kitchenNote = order.kitchenNote,
                    paymentMethodName = order.paymentMethod.name,
                    estimatedMinutes = order.estimatedMinutes,
                )
            )
        }
    }

    private fun isDemoSampleOrder(orderId: String): Boolean =
        orderId == "MFD-2041" || orderId == "MFD-1987"

    suspend fun reorder(order: Order) {
        order.items.forEach { line ->
            val menuItem = MenuCatalog.findById(line.menuItemId) ?: return@forEach
            val key = "${menuItem.id}::${line.customization}"
            val existing = cartDao.getCartItemById(key)
            val newQty = (existing?.quantity ?: 0) + line.quantity
            cartDao.upsert(
                CartItemEntity(
                    id = key,
                    menuItemId = menuItem.id,
                    quantity = newQty,
                    customization = line.customization,
                )
            )
        }
    }

    fun calculateBill(cartItems: List<CartItem>, couponCode: String?): BillSummary {
        val itemTotal = cartItems.sumOf { it.lineTotalInr }
        if (itemTotal == 0) {
            return BillSummary(
                itemTotalInr = 0,
                packagingFeeInr = 0,
                deliveryFeeInr = 0,
                discountInr = 0,
                gstInr = 0,
                grandTotalInr = 0,
                appliedCouponCode = null,
                freeDeliveryUnlocked = false,
            )
        }

        val coupon = couponCode?.let { code ->
            MenuCatalog.coupons.find { it.code.equals(code, ignoreCase = true) }
        }
        val couponValid = coupon != null && itemTotal >= coupon.minOrderInr
        val activeCode = if (couponValid) coupon!!.code else null
        val discount = if (couponValid) coupon!!.discountAmountInr else 0
        val freeDeliveryFromThreshold = itemTotal >= 399
        val freeDeliveryFromCoupon = couponValid && coupon!!.isFreeDelivery
        val freeDelivery = freeDeliveryFromThreshold || freeDeliveryFromCoupon

        val packagingFee = 20
        val deliveryFee = if (freeDelivery) 0 else 35
        val taxable = (itemTotal - discount).coerceAtLeast(0)
        val gst = (taxable * 0.05).roundToInt()
        val grandTotal = taxable + packagingFee + deliveryFee + gst

        return BillSummary(
            itemTotalInr = itemTotal,
            packagingFeeInr = packagingFee,
            deliveryFeeInr = deliveryFee,
            discountInr = discount,
            gstInr = gst,
            grandTotalInr = grandTotal,
            appliedCouponCode = activeCode,
            freeDeliveryUnlocked = freeDelivery,
        )
    }

    private fun formatTimestamp(millis: Long): String {
        val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return formatter.format(Date(millis))
    }
}
