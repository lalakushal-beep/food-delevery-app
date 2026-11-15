package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.authStateFlow
import com.example.data.local.FoodDeliveryDatabase
import com.example.data.model.BillSummary
import com.example.data.model.CartItem
import com.example.data.model.DeliveryAddress
import com.example.data.model.FoodCategory
import com.example.data.model.MenuItem
import com.example.data.model.MenuSortOption
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.remote.FirestoreOrderRepository
import com.example.data.repository.FoodDeliveryRepository
import com.example.data.repository.MenuCatalog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutFormState(
    val selectedAddressId: String = MenuCatalog.defaultAddresses.first().id,
    val recipientName: String = MenuCatalog.defaultAddresses.first().recipientName,
    val phoneNumber: String = MenuCatalog.defaultAddresses.first().phoneNumber,
    val addressLine: String = MenuCatalog.defaultAddresses.first().addressLine,
    val landmark: String = MenuCatalog.defaultAddresses.first().landmark,
    val deliveryInstruction: String = "Ring bell on arrival",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val nameError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val isPlacingOrder: Boolean = false,
)

data class FoodDeliveryUiState(
    val allMenuItems: List<MenuItem> = MenuCatalog.items,
    val filteredMenuItems: List<MenuItem> = MenuCatalog.items,
    val bestsellers: List<MenuItem> = MenuCatalog.items.filter { it.isBestseller },
    val selectedCategory: FoodCategory = FoodCategory.ALL,
    val searchQuery: String = "",
    val vegOnly: Boolean = false,
    val sortOption: MenuSortOption = MenuSortOption.RECOMMENDED,
    val cartItems: List<CartItem> = emptyList(),
    val itemQuantities: Map<String, Int> = emptyMap(),
    val totalCartCount: Int = 0,
    val billSummary: BillSummary = BillSummary(0, 0, 0, 0, 0, 0, null, false),
    val kitchenNote: String = "",
    val couponInput: String = "",
    val appliedCouponCode: String? = null,
    val couponMessage: String? = null,
    val savedAddresses: List<DeliveryAddress> = MenuCatalog.defaultAddresses,
    val checkoutForm: CheckoutFormState = CheckoutFormState(),
    val orders: List<Order> = emptyList(),
    val selectedDishForDetail: MenuItem? = null,
    val lastConfirmedOrder: Order? = null,
    val signedInEmail: String? = null,
    val signedInName: String? = null,
    val isAdminAccount: Boolean = false,
    val isCloudSynced: Boolean = false,
    val cloudSyncError: String? = null,
)

private data class FilterState(
    val category: FoodCategory = FoodCategory.ALL,
    val searchQuery: String = "",
    val vegOnly: Boolean = false,
    val sortOption: MenuSortOption = MenuSortOption.RECOMMENDED,
)

class FoodDeliveryViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreOrderRepository: FirestoreOrderRepository? by lazy {
        runCatching { FirestoreOrderRepository(application) }.getOrNull()
    }

    private val repository: FoodDeliveryRepository by lazy {
        val db = FoodDeliveryDatabase.getInstance(application)
        FoodDeliveryRepository(db.cartDao(), db.orderDao(), firestoreOrderRepository)
    }

    private val filterState = MutableStateFlow(FilterState())
    private val couponCodeFlow = MutableStateFlow<String?>(null)
    private val couponInputFlow = MutableStateFlow("")
    private val couponMessageFlow = MutableStateFlow<String?>(null)
    private val kitchenNoteFlow = MutableStateFlow("")
    private val checkoutFormFlow = MutableStateFlow(CheckoutFormState())
    private val selectedDishFlow = MutableStateFlow<MenuItem?>(null)
    private val lastConfirmedOrderFlow = MutableStateFlow<Order?>(null)
    private val cloudAuthStateFlow = MutableStateFlow(CloudSyncUiMetadata())
    private var firestoreSyncJob: Job? = null

    private val metaState = combine(
        kitchenNoteFlow,
        couponInputFlow,
        couponMessageFlow,
        checkoutFormFlow,
        selectedDishFlow,
    ) { note, couponInput, couponMsg, checkout, dish ->
        MetaBundle(note, couponInput, couponMsg, checkout, dish)
    }

    val uiState: StateFlow<FoodDeliveryUiState> = combine(
        filterState,
        repository.observeCartItems(),
        couponCodeFlow,
        repository.observeOrders(),
        metaState,
        lastConfirmedOrderFlow,
        cloudAuthStateFlow,
    ) { values ->
        val filters = values[0] as FilterState
        @Suppress("UNCHECKED_CAST")
        val cart = values[1] as List<CartItem>
        val appliedCode = values[2] as String?
        @Suppress("UNCHECKED_CAST")
        val orders = values[3] as List<Order>
        val meta = values[4] as MetaBundle
        val lastConfirmed = values[5] as Order?
        val cloudMeta = values[6] as CloudSyncUiMetadata

        val filtered = applyFilters(MenuCatalog.items, filters)
        val quantities = cart.groupBy { it.menuItem.id }
            .mapValues { (_, list) -> list.sumOf { it.quantity } }
        val totalCount = cart.sumOf { it.quantity }
        val bill = repository.calculateBill(cart, appliedCode)

        FoodDeliveryUiState(
            allMenuItems = MenuCatalog.items,
            filteredMenuItems = filtered,
            bestsellers = MenuCatalog.items.filter { it.isBestseller && (!filters.vegOnly || it.isVeg) },
            selectedCategory = filters.category,
            searchQuery = filters.searchQuery,
            vegOnly = filters.vegOnly,
            sortOption = filters.sortOption,
            cartItems = cart,
            itemQuantities = quantities,
            totalCartCount = totalCount,
            billSummary = bill,
            kitchenNote = meta.kitchenNote,
            couponInput = meta.couponInput,
            appliedCouponCode = bill.appliedCouponCode,
            couponMessage = meta.couponMessage,
            savedAddresses = MenuCatalog.defaultAddresses,
            checkoutForm = meta.checkoutForm,
            orders = orders,
            selectedDishForDetail = meta.selectedDish,
            lastConfirmedOrder = lastConfirmed,
            signedInEmail = cloudMeta.signedInEmail,
            signedInName = cloudMeta.signedInName,
            isAdminAccount = cloudMeta.isAdminAccount,
            isCloudSynced = cloudMeta.isCloudSynced,
            cloudSyncError = cloudMeta.cloudSyncError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FoodDeliveryUiState(),
    )

    init {
        viewModelScope.launch {
            repository.seedSampleOrdersIfNeeded()
        }
        observeFirebaseAuthAndSync()
    }

    private fun observeFirebaseAuthAndSync() {
        val auth = runCatching { Firebase.auth }.getOrNull() ?: return
        viewModelScope.launch {
            auth.authStateFlow().collectLatest { user ->
                handleAuthUserChange(user)
            }
        }
    }

    private fun handleAuthUserChange(user: FirebaseUser?) {
        firestoreSyncJob?.cancel()
        firestoreSyncJob = null

        if (user == null) {
            cloudAuthStateFlow.value = CloudSyncUiMetadata()
            return
        }

        val remoteRepo = firestoreOrderRepository ?: return
        val isAdmin = remoteRepo.isBootstrappedAdmin()
        cloudAuthStateFlow.value = CloudSyncUiMetadata(
            signedInEmail = user.email,
            signedInName = user.displayName,
            isAdminAccount = isAdmin,
            isCloudSynced = true,
            cloudSyncError = null,
        )

        firestoreSyncJob = viewModelScope.launch {
            remoteRepo.observeOrders(isAdminScope = isAdmin)
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    Log.w(TAG, "Error observing Cloud Firestore orders", rootError)
                    cloudAuthStateFlow.update {
                        it.copy(cloudSyncError = rootError.message ?: "Failed to sync Cloud Firestore orders")
                    }
                }
                .collect { remoteOrders ->
                    repository.upsertSyncedOrders(remoteOrders)
                    cloudAuthStateFlow.update { it.copy(cloudSyncError = null) }
                }
        }
    }

    fun selectCategory(category: FoodCategory) {
        filterState.update { it.copy(category = category) }
    }

    fun updateSearchQuery(query: String) {
        filterState.update { it.copy(searchQuery = query) }
    }

    fun toggleVegOnly(vegOnly: Boolean) {
        filterState.update { it.copy(vegOnly = vegOnly) }
    }

    fun selectSortOption(option: MenuSortOption) {
        filterState.update { it.copy(sortOption = option) }
    }

    fun resetFilters() {
        filterState.value = FilterState()
    }

    fun openDishDetail(menuItem: MenuItem) {
        selectedDishFlow.value = menuItem
    }

    fun dismissDishDetail() {
        selectedDishFlow.value = null
    }

    fun addToCart(menuItem: MenuItem, customization: String? = null) {
        viewModelScope.launch {
            repository.addToCart(menuItem, customization)
        }
    }

    fun incrementCartItem(cartItemId: String) {
        viewModelScope.launch {
            repository.incrementCartItem(cartItemId)
        }
    }

    fun decrementCartItem(cartItemId: String) {
        viewModelScope.launch {
            repository.decrementCartItem(cartItemId)
        }
    }

    fun incrementMenuItem(menuItemId: String) {
        viewModelScope.launch {
            repository.incrementMenuItem(menuItemId)
        }
    }

    fun decrementMenuItem(menuItemId: String) {
        viewModelScope.launch {
            repository.decrementMenuItem(menuItemId)
        }
    }

    fun removeCartItem(cartItemId: String) {
        viewModelScope.launch {
            repository.removeCartItem(cartItemId)
        }
    }

    fun updateKitchenNote(note: String) {
        kitchenNoteFlow.value = note
    }

    fun updateCouponInput(input: String) {
        couponInputFlow.value = input.uppercase()
        couponMessageFlow.value = null
    }

    fun applyCoupon(code: String) {
        val clean = code.trim().uppercase()
        val coupon = MenuCatalog.coupons.find { it.code == clean }
        val itemTotal = uiState.value.billSummary.itemTotalInr
        when {
            clean.isEmpty() -> {
                couponMessageFlow.value = "Enter a coupon code to apply."
            }
            coupon == null -> {
                couponMessageFlow.value = "Coupon \"$clean\" is invalid. Try NAMASTE100 or FREEDEL."
            }
            itemTotal < coupon.minOrderInr -> {
                couponMessageFlow.value =
                    "Add ₹${coupon.minOrderInr - itemTotal} more to unlock ${coupon.code}."
            }
            else -> {
                couponCodeFlow.value = coupon.code
                couponInputFlow.value = coupon.code
                couponMessageFlow.value = "${coupon.code} applied! (${coupon.title})"
            }
        }
    }

    fun removeCoupon() {
        couponCodeFlow.value = null
        couponInputFlow.value = ""
        couponMessageFlow.value = null
    }

    fun selectSavedAddress(address: DeliveryAddress) {
        checkoutFormFlow.update {
            it.copy(
                selectedAddressId = address.id,
                recipientName = address.recipientName,
                phoneNumber = address.phoneNumber,
                addressLine = address.addressLine,
                landmark = address.landmark,
                nameError = null,
                phoneError = null,
                addressError = null,
            )
        }
    }

    fun updateRecipientName(name: String) {
        checkoutFormFlow.update { it.copy(recipientName = name, nameError = null) }
    }

    fun updatePhoneNumber(phone: String) {
        val digitsOnly = phone.filter { it.isDigit() }.take(10)
        val liveError = if (digitsOnly.isNotEmpty() && digitsOnly.length < 10) {
            "Enter all 10 digits (${digitsOnly.length}/10)"
        } else {
            null
        }
        checkoutFormFlow.update { it.copy(phoneNumber = digitsOnly, phoneError = liveError) }
    }

    fun updateAddressLine(line: String) {
        checkoutFormFlow.update { it.copy(addressLine = line, selectedAddressId = "custom", addressError = null) }
    }

    fun updateLandmark(landmark: String) {
        checkoutFormFlow.update { it.copy(landmark = landmark) }
    }

    fun selectDeliveryInstruction(instruction: String) {
        checkoutFormFlow.update { it.copy(deliveryInstruction = instruction) }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        checkoutFormFlow.update { it.copy(paymentMethod = method) }
    }

    fun placeOrder(onOrderConfirmed: (Order) -> Unit) {
        val currentState = uiState.value
        val form = currentState.checkoutForm
        if (currentState.cartItems.isEmpty()) return

        val nameErr = if (form.recipientName.trim().length < 2) {
            "Please enter the customer's full name."
        } else null
        val phoneErr = if (form.phoneNumber.length != 10 || !form.phoneNumber.all { it.isDigit() }) {
            "Mobile number must be exactly 10 digits."
        } else null
        val addrErr = if (form.addressLine.trim().length < 10) {
            "Please enter your complete delivery address (flat, street, city)."
        } else null

        if (nameErr != null || phoneErr != null || addrErr != null) {
            checkoutFormFlow.update {
                it.copy(
                    nameError = nameErr,
                    phoneError = phoneErr,
                    addressError = addrErr,
                )
            }
            return
        }

        viewModelScope.launch {
            checkoutFormFlow.update {
                it.copy(
                    isPlacingOrder = true,
                    nameError = null,
                    phoneError = null,
                    addressError = null,
                )
            }
            val confirmed = repository.placeOrder(
                cartItems = currentState.cartItems,
                bill = currentState.billSummary,
                recipientName = form.recipientName,
                phoneNumber = form.phoneNumber,
                addressLine = form.addressLine,
                landmark = form.landmark,
                deliveryInstruction = form.deliveryInstruction,
                kitchenNote = currentState.kitchenNote,
                paymentMethod = form.paymentMethod,
            )
            lastConfirmedOrderFlow.value = confirmed
            couponCodeFlow.value = null
            couponInputFlow.value = ""
            couponMessageFlow.value = null
            kitchenNoteFlow.value = ""
            checkoutFormFlow.update { it.copy(isPlacingOrder = false) }
            onOrderConfirmed(confirmed)
        }
    }

    fun advanceOrderStatus(order: Order) {
        viewModelScope.launch {
            val result = repository.advanceOrderStatus(order)
            result.onFailure { err ->
                cloudAuthStateFlow.update { it.copy(cloudSyncError = err.message ?: "Failed to sync status update") }
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            val result = repository.updateOrderStatus(orderId, status)
            result.onFailure { err ->
                cloudAuthStateFlow.update { it.copy(cloudSyncError = err.message ?: "Failed to sync status update") }
            }
        }
    }

    fun reorder(order: Order, onAddedToCart: () -> Unit) {
        viewModelScope.launch {
            repository.reorder(order)
            onAddedToCart()
        }
    }

    private fun applyFilters(items: List<MenuItem>, filters: FilterState): List<MenuItem> {
        val query = filters.searchQuery.trim().lowercase()
        val base = items.filter { item ->
            val matchesCategory =
                filters.category == FoodCategory.ALL || item.category == filters.category
            val matchesVeg = !filters.vegOnly || item.isVeg
            val matchesSearch = query.isEmpty() ||
                item.name.lowercase().contains(query) ||
                item.category.label.lowercase().contains(query) ||
                item.description.lowercase().contains(query)
            matchesCategory && matchesVeg && matchesSearch
        }

        return when (filters.sortOption) {
            MenuSortOption.RECOMMENDED -> base.sortedByDescending { it.isBestseller }
            MenuSortOption.PRICE_LOW_TO_HIGH -> base.sortedBy { it.priceInr }
            MenuSortOption.TOP_RATED -> base.sortedByDescending { it.rating }
        }
    }

    private data class MetaBundle(
        val kitchenNote: String,
        val couponInput: String,
        val couponMessage: String?,
        val checkoutForm: CheckoutFormState,
        val selectedDish: MenuItem?,
    )

    private data class CloudSyncUiMetadata(
        val signedInEmail: String? = null,
        val signedInName: String? = null,
        val isAdminAccount: Boolean = false,
        val isCloudSynced: Boolean = false,
        val cloudSyncError: String? = null,
    )

    private companion object {
        const val TAG = "FoodDeliveryVM"
    }
}
