package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.auth.CloudSyncStatusCard
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.ui.components.VegNonVegBadge
import com.example.ui.components.formatInr
import com.example.ui.viewmodel.FoodDeliveryUiState

private enum class OrdersTabFilter(val label: String) {
    ALL("All Orders"),
    ACTIVE("Active"),
    PAST("Past"),
}

@Composable
fun OrdersScreen(
    uiState: FoodDeliveryUiState,
    onAdvanceOrderStatus: (Order) -> Unit,
    onReorder: (Order) -> Unit,
    onBrowseMenu: () -> Unit,
    onOpenAdminOrders: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(OrdersTabFilter.ALL) }
    val filteredOrders = remember(uiState.orders, selectedTab) {
        when (selectedTab) {
            OrdersTabFilter.ALL -> uiState.orders
            OrdersTabFilter.ACTIVE -> uiState.orders.filter { it.status.isActive }
            OrdersTabFilter.PAST -> uiState.orders.filter { !it.status.isActive }
        }
    }
    val bottomExtraSpace = if (uiState.totalCartCount > 0) 96.dp else 24.dp

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("orders_screen"),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = bottomExtraSpace,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.my_orders_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    FilledTonalButton(
                        onClick = onOpenAdminOrders,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("open_admin_orders_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = "Open Admin Orders",
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.kitchen_admin_button),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OrdersTabFilter.entries.forEach { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag("orders_filter_${tab.name.lowercase()}"),
                        )
                    }
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp),
                    )
                    Text(
                        text = stringResource(id = R.string.empty_orders_title),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(id = R.string.empty_orders_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(onClick = onBrowseMenu) {
                        Icon(Icons.Filled.RestaurantMenu, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.browse_menu))
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.orderId }) { order ->
                OrderHistoryCard(
                    order = order,
                    onAdvanceStatus = { onAdvanceOrderStatus(order) },
                    onReorder = { onReorder(order) },
                )
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
    }
}

@Composable
private fun OrderHistoryCard(
    order: Order,
    onAdvanceStatus: () -> Unit,
    onReorder: () -> Unit,
) {
    val isActive = order.status.isActive
    val isRejected = order.status == OrderStatus.REJECTED
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp),
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = when {
                isRejected -> MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
                isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                else -> MaterialTheme.colorScheme.outlineVariant
            },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderId}"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = order.formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isRejected -> MaterialTheme.colorScheme.errorContainer
                        isActive -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    },
                    modifier = Modifier.testTag("order_status_${order.orderId}"),
                ) {
                    Text(
                        text = order.status.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isRejected -> MaterialTheme.colorScheme.onErrorContainer
                            isActive -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }

            if (isActive || isRejected) {
                OrderProgressTracker(currentStatus = order.status)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                order.items.forEach { item ->
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
                            VegNonVegBadge(isVeg = item.isVeg)
                            Text(
                                text = "${item.quantity}x ${item.name}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(
                            text = formatInr(item.unitPriceInr * item.quantity),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "${order.recipientName} • ${order.phoneNumber} • ${order.deliveryAddress}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.paymentMethod.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatInr(order.totalInr),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isActive) {
                        OutlinedButton(
                            onClick = onAdvanceStatus,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("advance_status_${order.orderId}"),
                        ) {
                            Icon(Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Next Stage")
                        }
                    }
                    Button(
                        onClick = onReorder,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("reorder_${order.orderId}"),
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(id = R.string.reorder))
                    }
                }
            }
        }
    }
}
