package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY id ASC")
    fun observeCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE id = :id LIMIT 1")
    suspend fun getCartItemById(id: String): CartItemEntity?

    @Query("SELECT * FROM cart_items WHERE menuItemId = :menuItemId")
    suspend fun getCartItemsForMenu(menuItemId: String): List<CartItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY placedAtMillis DESC")
    fun observeOrders(): Flow<List<OrderEntity>>

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun countOrders(): Int

    @Query("SELECT COUNT(*) FROM orders WHERE orderId = :orderId")
    suspend fun countByOrderId(orderId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: OrderEntity)

    @Query("UPDATE orders SET statusName = :statusName WHERE orderId = :orderId")
    suspend fun updateStatus(orderId: String, statusName: String)
}
