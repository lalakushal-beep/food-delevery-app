package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreOrderRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth,
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun isBootstrappedAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.isEmailVerified && user.email == BOOTSTRAPPED_ADMIN_EMAIL
    }

    fun observeOrders(isAdminScope: Boolean): Flow<List<Order>> = flow {
        val uid = requireUserId()
        val query: Query = if (isAdminScope) {
            db.collection(COLLECTION_ORDERS)
                .orderBy("placedAtMillis", Query.Direction.DESCENDING)
        } else {
            db.collection(COLLECTION_ORDERS)
                .whereEqualTo("userId", uid)
        }

        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        FirestoreOrderDocument::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    )
                        .map { it.toDomain() }
                        .sortedByDescending { it.placedAtMillis }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, COLLECTION_ORDERS, auth)
                    }
                    throw firestoreError ?: error
                }
        )
    }

    suspend fun createOrder(order: Order): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        require(order.orderId.isNotBlank()) { "Order ID must not be blank" }
        val docRef = db.collection(COLLECTION_ORDERS).document(order.orderId)
        val payload = order.toFirestorePayload(
            userId = uid,
            userEmail = auth.currentUser?.email,
        )
        return try {
            docRef.set(payload).await()
            Result.success(order.orderId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun getOrderById(orderId: String): Result<Order?> {
        try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = db.collection(COLLECTION_ORDERS).document(orderId)
        return try {
            val snapshot = docRef.get().await()
            val domain = snapshot.toObject(
                FirestoreOrderDocument::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )?.toDomain()
            Result.success(domain)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = db.collection(COLLECTION_ORDERS).document(orderId)
        return try {
            docRef.update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    companion object {
        const val COLLECTION_ORDERS = "orders"
        const val BOOTSTRAPPED_ADMIN_EMAIL = "lala.kushal@gmail.com"
    }
}
