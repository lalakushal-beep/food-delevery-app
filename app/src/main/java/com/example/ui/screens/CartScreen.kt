package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CartItem
import com.example.data.repository.MenuCatalog
import com.example.ui.components.VegNonVegBadge
import com.example.ui.components.formatInr
import com.example.ui.viewmodel.FoodDeliveryUiState

@Composable
fun CartScreen(
    uiState: FoodDeliveryUiState,
    onIncrementItem: (String) -> Unit,
    onDecrementItem: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onKitchenNoteChange: (String) -> Unit,
    onCouponInputChange: (String) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onRemoveCoupon: () -> Unit,
    onBrowseMenu: () -> Unit,
    onProceedToCheckout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.cartItems.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(28.dp)
                .testTag("empty_cart_state"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                modifier = Modifier.size(96.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize(),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(id = R.string.empty_cart_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.empty_cart_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBrowseMenu,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                modifier = Modifier.testTag("empty_cart_browse_button"),
            ) {
                Icon(Icons.Filled.RestaurantMenu, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.browse_menu),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        return
    }

    val bill = uiState.billSummary
    val freeDelThreshold = 399
    val progress = (bill.itemTotalInr.toFloat() / freeDelThreshold).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("cart_screen"),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("cart_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Your Cart",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "${uiState.totalCartCount} ${if (uiState.totalCartCount == 1) "item" else "items"} from Saffron & Ember Kitchen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Free Delivery Banner
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (bill.freeDeliveryUnlocked) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = if (bill.freeDeliveryUnlocked) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = if (bill.freeDeliveryUnlocked) {
                                    "Free delivery unlocked on this order!"
                                } else {
                                    "Add ${formatInr(freeDelThreshold - bill.itemTotalInr)} more for FREE delivery"
                                },
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (bill.freeDeliveryUnlocked) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                        )
                    }
                }
            }

            // Cart Items List
            items(uiState.cartItems, key = { it.id }) { cartItem ->
                CartItemRowCard(
                    cartItem = cartItem,
                    onIncrement = { onIncrementItem(cartItem.id) },
                    onDecrement = { onDecrementItem(cartItem.id) },
                    onRemove = { onRemoveItem(cartItem.id) },
                )
            }

            // Add More Items Link
            item {
                OutlinedButton(
                    onClick = onBrowseMenu,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add More Dishes")
                }
            }

            // Kitchen Note
            item {
                OutlinedTextField(
                    value = uiState.kitchenNote,
                    onValueChange = onKitchenNoteChange,
                    label = { Text("Cooking Instructions (Optional)") },
                    placeholder = { Text("e.g., Less spicy, extra raita, no onions…") },
                    maxLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cart_kitchen_note_input"),
                )
            }

            // Coupons Section
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalOffer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Offers & Coupons",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = uiState.couponInput,
                                onValueChange = onCouponInputChange,
                                placeholder = { Text("Enter coupon code") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cart_coupon_input"),
                            )
                            if (uiState.appliedCouponCode != null) {
                                TextButton(onClick = onRemoveCoupon) {
                                    Text("Remove")
                                }
                            } else {
                                Button(
                                    onClick = { onApplyCoupon(uiState.couponInput) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("cart_apply_coupon_button"),
                                ) {
                                    Text("Apply")
                                }
                            }
                        }

                        if (!uiState.couponMessage.isNullOrBlank()) {
                            Text(
                                text = uiState.couponMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.appliedCouponCode != null) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(MenuCatalog.coupons, key = { it.code }) { coupon ->
                                val isApplied = uiState.appliedCouponCode == coupon.code
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isApplied) {
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (isApplied) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant,
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onApplyCoupon(coupon.code) }
                                        .testTag("coupon_pill_${coupon.code}"),
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(
                                            text = "${coupon.code} • ${coupon.title}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = coupon.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bill Summary Card
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
                            text = "Bill Summary",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        BillRow("Item Total", formatInr(bill.itemTotalInr))
                        BillRow("Kitchen & Packaging", formatInr(bill.packagingFeeInr))
                        BillRow(
                            label = "Delivery Fee",
                            value = if (bill.deliveryFeeInr == 0) "FREE" else formatInr(bill.deliveryFeeInr),
                            highlight = bill.deliveryFeeInr == 0,
                        )
                        if (bill.discountInr > 0) {
                            BillRow(
                                label = "Coupon Discount (${bill.appliedCouponCode})",
                                value = "-${formatInr(bill.discountInr)}",
                                highlight = true,
                            )
                        }
                        BillRow("GST (5%)", formatInr(bill.gstInr))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Grand Total",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = formatInr(bill.grandTotalInr),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }

        // Sticky Checkout CTA
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        text = "Inclusive of all taxes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(
                    onClick = onProceedToCheckout,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    modifier = Modifier.testTag("proceed_to_checkout_button"),
                ) {
                    Text(
                        text = stringResource(id = R.string.proceed_checkout),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemRowCard(
    cartItem: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(id = cartItem.menuItem.imageRes),
                contentDescription = cartItem.menuItem.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.small),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    VegNonVegBadge(isVeg = cartItem.menuItem.isVeg)
                    Text(
                        text = cartItem.menuItem.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = cartItem.customization,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatInr(cartItem.lineTotalInr),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("cart_dec_${cartItem.menuItem.id}"),
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "${cartItem.quantity}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .testTag("cart_qty_${cartItem.menuItem.id}"),
                        )
                        IconButton(
                            onClick = onIncrement,
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("cart_inc_${cartItem.menuItem.id}"),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp))
                        }
                    }
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Remove item",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BillRow(
    label: String,
    value: String,
    highlight: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (highlight) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
        )
    }
}
