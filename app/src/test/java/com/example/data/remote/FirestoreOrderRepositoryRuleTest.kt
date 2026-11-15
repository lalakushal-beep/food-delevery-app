package com.example.data.remote

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Order
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class FirestoreOrderRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private fun sampleOrder(orderId: String = "MFD-${UUID.randomUUID().toString().take(8)}"): Order {
        return Order(
            orderId = orderId,
            placedAtMillis = System.currentTimeMillis(),
            formattedDate = "14 Nov, 08:45 PM",
            status = OrderStatus.PENDING,
            items = listOf(
                OrderLineItem(
                    menuItemId = "thali_maharaja",
                    name = "Maharaja Special Veg Thali",
                    quantity = 1,
                    unitPriceInr = 349,
                    isVeg = true,
                    customization = "Garlic Naan",
                )
            ),
            totalInr = 386,
            discountInr = 0,
            deliveryAddress = "24 MG Road, Indiranagar, Bengaluru",
            recipientName = "Rohan Verma",
            phoneNumber = "9876543210",
            deliveryInstruction = "Ring bell on arrival",
            kitchenNote = "Extra papad",
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            estimatedMinutes = 30,
        )
    }

    @Test
    fun createOrder_authenticatedOwner_createsDocumentAndObservesLiveUpdates() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = FirestoreOrderRepository(firestore, auth)
        val newOrder = sampleOrder()

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createOrder(newOrder)
        }
        assertTrue("Expected createOrder success, got $createResult", createResult.isSuccess)
        assertEquals(newOrder.orderId, createResult.getOrThrow())

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeOrders(isAdminScope = false).first { list ->
                list.any { it.orderId == newOrder.orderId }
            }
        }
        val fetched = observed.first { it.orderId == newOrder.orderId }
        assertEquals(OrderStatus.PENDING, fetched.status)
        assertEquals("Rohan Verma", fetched.recipientName)

        // Advance order status and observe live update
        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateOrderStatus(newOrder.orderId, OrderStatus.PREPARING)
        }
        assertTrue("Expected updateOrderStatus success, got $updateResult", updateResult.isSuccess)

        val updatedObserved = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeOrders(isAdminScope = false).first { list ->
                list.any { it.orderId == newOrder.orderId && it.status == OrderStatus.PREPARING }
            }
        }
        assertEquals(
            OrderStatus.PREPARING,
            updatedObserved.first { it.orderId == newOrder.orderId }.status,
        )
    }

    @Test
    fun getOrderById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestoreOrderRepository(firestore, auth)
        val aliceOrder = sampleOrder()
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createOrder(aliceOrder).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = FirestoreOrderRepository(firestore, auth)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getOrderById(aliceOrder.orderId)
        }
        assertTrue(result.isFailure)
        val firestoreException = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }

    @Test
    fun observeOrders_unauthenticatedCaller_throwsAndRejectsStream() = runBlocking {
        auth.signOut()
        val repository = FirestoreOrderRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeOrders(isAdminScope = false).first()
            }
            fail("Expected IllegalStateException or PERMISSION_DENIED when unauthenticated")
        } catch (expected: Exception) {
            val fsEx = expected.unwrapFirestoreException()
            assertTrue(
                expected is IllegalStateException ||
                    fsEx?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED,
            )
        }
    }

    @Test
    fun listAllOrders_nonAdminUser_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = FirestoreOrderRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeOrders(isAdminScope = true).first()
            }
            fail("Expected PERMISSION_DENIED for non-admin unfiltered collection query")
        } catch (expected: Exception) {
            val fsEx = expected.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $expected", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_orders@test.com"
        const val BOB_EMAIL = "bob_orders@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
