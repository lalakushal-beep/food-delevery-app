package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.auth.CloudSyncStatusCard
import com.example.data.model.DeliveryAddress
import com.example.data.model.PaymentMethod
import com.example.ui.components.VegNonVegBadge
import com.example.ui.components.formatInr
import com.example.ui.viewmodel.FoodDeliveryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    uiState: FoodDeliveryUiState,
    onBack: () -> Unit,
    onSelectSavedAddress: (DeliveryAddress) -> Unit,
    onRecipientNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onAddressLineChange: (String) -> Unit,
    onLandmarkChange: (String) -> Unit,
    onDeliveryInstructionSelect: (String) -> Unit,
    onPaymentMethodSelect: (PaymentMethod) -> Unit,
    onPlaceOrder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val form = uiState.checkoutForm
    val bill = uiState.billSummary
    val instructionChoices = listOf("Ring bell on arrival", "Leave at door", "Call on arrival")

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
            .fillMaxSize()
            .testTag("checkout_screen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.checkout_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("checkout_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to cart",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            val activeValidationError = form.nameError ?: form.phoneError ?: form.addressError
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (activeValidationError != null) {
                        Text(
                            text = activeValidationError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 10.dp)
                                .testTag("checkout_validation_error"),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                        ) {
                            Text(
                                text = formatInr(bill.grandTotalInr),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                text = form.paymentMethod.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                        Button(
                            onClick = onPlaceOrder,
                            enabled = !form.isPlacingOrder && uiState.cartItems.isNotEmpty(),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                            modifier = Modifier.testTag("place_order_button"),
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${stringResource(id = R.string.place_order)} • ${formatInr(bill.grandTotalInr)}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .testTag("checkout_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Delivery Time Banner
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                        Column {
                            Text(
                                text = "Express Kitchen Delivery • 25–30 mins",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = "Prepared fresh at Saffron & Ember Kitchen",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                CloudSyncStatusCard(
                    signedInEmail = uiState.signedInEmail,
                    signedInName = uiState.signedInName,
                    isAdminAccount = uiState.isAdminAccount,
                    syncError = uiState.cloudSyncError,
                )
            }

            // Saved Addresses + Editable Delivery Form
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Delivery Details",
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            uiState.savedAddresses.forEach { addr ->
                                val selected = form.selectedAddressId == addr.id
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectSavedAddress(addr) },
                                    label = { Text(addr.label) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (addr.label == "Home") Icons.Filled.Home else Icons.Filled.Work,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                    modifier = Modifier.testTag("address_chip_${addr.label.lowercase()}"),
                                )
                            }
                        }

                        OutlinedTextField(
                            value = form.recipientName,
                            onValueChange = onRecipientNameChange,
                            label = { Text("Customer Full Name") },
                            placeholder = { Text("Enter customer's full name") },
                            isError = form.nameError != null,
                            supportingText = form.nameError?.let { err -> { Text(err) } },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_name_input"),
                        )

                        OutlinedTextField(
                            value = form.phoneNumber,
                            onValueChange = onPhoneNumberChange,
                            label = { Text("10-Digit Indian Mobile Number") },
                            prefix = { Text("+91 ") },
                            isError = form.phoneError != null,
                            supportingText = {
                                val msg = form.phoneError
                                    ?: if (form.phoneNumber.length == 10) {
                                        "10/10 digits valid"
                                    } else {
                                        "Enter 10-digit Indian mobile number"
                                    }
                                Text(msg)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_phone_input"),
                        )

                        OutlinedTextField(
                            value = form.addressLine,
                            onValueChange = onAddressLineChange,
                            label = { Text("Complete Delivery Address") },
                            placeholder = { Text("Flat / House No., Building, Street, Area, City") },
                            isError = form.addressError != null,
                            supportingText = form.addressError?.let { err -> { Text(err) } },
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_address_input"),
                        )

                        OutlinedTextField(
                            value = form.landmark,
                            onValueChange = onLandmarkChange,
                            label = { Text("Landmark") },
                            placeholder = { Text("Nearby metro, gate, or building (Optional)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_landmark_input"),
                        )

                        Text(
                            text = "Delivery Instructions",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(instructionChoices) { choice ->
                                FilterChip(
                                    selected = form.deliveryInstruction == choice,
                                    onClick = { onDeliveryInstructionSelect(choice) },
                                    label = { Text(choice) },
                                )
                            }
                        }
                    }
                }
            }

            // Payment Method Selection (No Payment Gateway)
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Payment on Delivery",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Online payment gateway is disabled in this version. Pay conveniently when your order arrives.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        PaymentMethod.entries.forEach { method ->
                            val selected = form.paymentMethod == method
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                                border = BorderStroke(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onPaymentMethodSelect(method) }
                                    .testTag("payment_option_${method.name}"),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(
                                        imageVector = if (method == PaymentMethod.CASH_ON_DELIVERY) {
                                            Icons.Filled.Payments
                                        } else {
                                            Icons.Filled.QrCode2
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = method.title,
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Text(
                                            text = method.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    RadioButton(
                                        selected = selected,
                                        onClick = { onPaymentMethodSelect(method) },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Order Review Summary
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Order Summary (${uiState.totalCartCount} items)",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        uiState.cartItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    VegNonVegBadge(isVeg = item.menuItem.isVeg)
                                    Text(
                                        text = "${item.quantity}x ${item.menuItem.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                                Text(
                                    text = formatInr(item.lineTotalInr),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        CheckoutSummaryRow("Item Total", formatInr(bill.itemTotalInr))
                        CheckoutSummaryRow("Packaging & Kitchen", formatInr(bill.packagingFeeInr))
                        CheckoutSummaryRow(
                            "Delivery Fee",
                            if (bill.deliveryFeeInr == 0) "FREE" else formatInr(bill.deliveryFeeInr),
                        )
                        if (bill.discountInr > 0) {
                            CheckoutSummaryRow(
                                "Discount (${bill.appliedCouponCode})",
                                "-${formatInr(bill.discountInr)}",
                            )
                        }
                        CheckoutSummaryRow("GST (5%)", formatInr(bill.gstInr))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Total Amount (INR)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = formatInr(bill.grandTotalInr),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckoutSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
