package com.example

import com.example.data.model.FoodCategory
import com.example.data.model.OrderStatus
import com.example.data.repository.MenuCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun catalog_containsAllRequestedIndianFoodCategories() {
        val categoriesPresent = MenuCatalog.items.map { it.category }.toSet()
        assertTrue(FoodCategory.PIZZA in categoriesPresent)
        assertTrue(FoodCategory.BURGER in categoriesPresent)
        assertTrue(FoodCategory.BIRYANI in categoriesPresent)
        assertTrue(FoodCategory.DOSA in categoriesPresent)
        assertTrue(FoodCategory.PANEER_TIKKA in categoriesPresent)
        assertTrue(FoodCategory.THALI in categoriesPresent)
        assertTrue(MenuCatalog.items.all { it.priceInr > 0 })
    }

    @Test
    fun orderStatus_progressesInExpectedSequence() {
        assertEquals(OrderStatus.ACCEPTED, OrderStatus.PENDING.next())
        assertEquals(OrderStatus.PREPARING, OrderStatus.ACCEPTED.next())
        assertEquals(OrderStatus.READY, OrderStatus.PREPARING.next())
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.READY.next())
        assertEquals(OrderStatus.DELIVERED, OrderStatus.OUT_FOR_DELIVERY.next())
        assertEquals(OrderStatus.DELIVERED, OrderStatus.DELIVERED.next())
        assertEquals(OrderStatus.REJECTED, OrderStatus.REJECTED.next())
        assertEquals(false, OrderStatus.REJECTED.isActive)
    }
}
