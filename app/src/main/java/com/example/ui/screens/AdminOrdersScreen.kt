package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.auth.CloudSyncStatusCard
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.ui.components.VegNonVegBadge
import com.example.ui.components.formatInr
import com.example.ui.viewmodel.FoodDeliveryUiState
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    uiState: FoodDeliveryUiState,
    onBack: () -> Unit,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    var statusFilter by remember { mutableStateOf<OrderStatus?>(null) }

    val filteredOrders = remember(uiState.orders, statusFilter) {
        if (statusFilter == null) {
            uiState.orders
        } else {
            uiState.orders.filter { it.status == statusFilter }
        }
    }

    val startOfTodayMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val pendingCount = remember(uiState.orders) { uiState.orders.count { it.status == OrderStatus.PENDING } }
    val activeCount = remember(uiState.orders) { uiState.orders.count { it.status.isActive } }
    val todayValidOrders = remember(uiState.orders, startOfTodayMillis) {
        uiState.orders.filter {
            it.placedAtMillis >= startOfTodayMillis && it.status != OrderStatus.REJECTED
        }
    }
    val dailySalesInr = remember(todayValidOrders) { todayValidOrders.sumOf { it.totalInr } }
    val totalSalesInr = remember(uiState.orders) {
        uiState.orders.filter { it.status != OrderStatus.REJECTED }.sumOf { it.totalInr }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_orders_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.admin_orders_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(id = R.string.admin_orders_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("admin_back_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = if (uiState.isCloudSynced) "Cloud Sync" else "Local Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("admin_orders_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                CloudSyncStatusCard(
                    signedInEmail = uiState.signedInEmail,
                    signedInName = uiState.signedInName,
                    isAdminAccount = uiState.isAdminAccount,
                    syncError = uiState.cloudSyncError,
                )
            }

            // Admin Sync Notice
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = if (uiState.isCloudSynced) {
                                if (uiState.isAdminAccount) {
                                    "Admin Console synced live via Cloud Firestore. Status updates reflect immediately in Customer My Orders."
                                } else {
                                    "Synced to Cloud Firestore for your signed-in account. Sign in with the Admin Google account to manage all customers' orders."
                                }
                            } else {
                                "Showing local orders. Sign in with Google above to view online customer orders and sync status updates via Cloud Firestore."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            // Admin Dashboard KPI Grid: Total Orders, Pending/Active, Daily Sales & Total Sales
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AdminMetricCard(
                            label = "Total Orders",
                            value = "${uiState.orders.size}",
                            sublabel = "$activeCount active",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_kpi_total_orders"),
                        )
                        AdminMetricCard(
                            label = "Pending Approval",
                            value = "$pendingCount",
                            sublabel = if (pendingCount > 0) "Needs action" else "All reviewed",
                            highlight = pendingCount > 0,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_kpi_pending_orders"),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AdminMetricCard(
                            label = "Daily Sales",
                            value = formatInr(dailySalesInr),
                            sublabel = "${todayValidOrders.size} valid ${if (todayValidOrders.size == 1) "order" else "orders"} today",
                            highlight = dailySalesInr > 0,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_kpi_daily_sales"),
                        )
                        AdminMetricCard(
                            label = "Total Sales",
                            value = formatInr(totalSalesInr),
                            sublabel = "Excludes rejected",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_kpi_total_sales"),
                        )
                    }
                }
            }

            // Status Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("admin_filter_row"),
                ) {
                    item {
                        FilterChip(
                            selected = statusFilter == null,
                            onClick = { statusFilter = null },
                            label = { Text("All (${uiState.orders.size})") },
                            modifier = Modifier.testTag("admin_filter_all"),
                        )
                    }
                    items(OrderStatus.entries, key = { it.name }) { status ->
                        val count = uiState.orders.count { it.status == status }
                        FilterChip(
                            selected = statusFilter == status,
                            onClick = { statusFilter = status },
                            label = { Text("${status.title} ($count)") },
                            modifier = Modifier.testTag("admin_filter_${status.name.lowercase()}"),
                        )
                    }
                }
            }

            if (filteredOrders.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp),
                        )
                        Text(
                            text = "No orders in this status",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Customer orders placed at checkout appear here immediately.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(filteredOrders, key = { "admin_${it.orderId}" }) { order ->
                    AdminCustomerOrderCard(
                        order = order,
                        onUpdateStatus = { newStatus ->
                            onUpdateOrderStatus(order.orderId, newStatus)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    label: String,
    value: String,
    sublabel: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        border = BorderStroke(
            1.dp,
            if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AdminCustomerOrderCard(
    order: Order,
    onUpdateStatus: (OrderStatus) -> Unit,
) {
    val isActive = order.status.isActive
    val isPending = order.status == OrderStatus.PENDING
    val isRejected = order.status == OrderStatus.REJECTED

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = when {
                isRejected -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                isActive -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_order_card_${order.orderId}"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Order Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "#${order.orderId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
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
                        isActive -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    },
                    modifier = Modifier.testTag("admin_order_status_${order.orderId}"),
                ) {
                    Text(
                        text = order.status.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isRejected -> MaterialTheme.colorScheme.onErrorContainer
                            isActive -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }

            // Customer Info Block (Name, Phone, Delivery Address)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = order.recipientName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = order.phoneNumber,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = order.deliveryAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (order.deliveryInstruction.isNotBlank() || order.kitchenNote.isNotBlank()) {
                        val notes = listOfNotNull(
                            order.deliveryInstruction.takeIf { it.isNotBlank() }?.let { "Drop: $it" },
                            order.kitchenNote.takeIf { it.isNotBlank() }?.let { "Kitchen Note: $it" },
                        ).joinToString(" • ")
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Ordered Food Items & Quantities
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
                                text = "${item.quantity}x ${item.name} (${item.customization})",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Text(
                            text = formatInr(item.unitPriceInr * item.quantity),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Total Amount & Payment Method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = order.paymentMethod.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Total: ${formatInr(order.totalInr)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            // Primary Action Area: Accept / Reject for Pending, or 1-tap Move to Next Stage
            if (isPending) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = { onUpdateStatus(OrderStatus.REJECTED) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_reject_${order.orderId}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reject", maxLines = 1)
                    }
                    Button(
                        onClick = { onUpdateStatus(OrderStatus.ACCEPTED) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_accept_${order.orderId}"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Accept", maxLines = 1)
                    }
                }
            } else if (isActive) {
                val nextStatus = order.status.next()
                Button(
                    onClick = { onUpdateStatus(nextStatus) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_advance_${order.orderId}"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Move to: ${nextStatus.title}")
                }
            }

            // Direct Status Update Chips (Pending, Accepted, Preparing, Ready, Out for Delivery, Delivered, Rejected)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Update Order Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.testTag("admin_status_row_${order.orderId}"),
                ) {
                    items(OrderStatus.entries, key = { it.name }) { status ->
                        val selected = order.status == status
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdateStatus(status) },
                            label = {
                                Text(
                                    text = status.title,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (status == OrderStatus.REJECTED) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                },
                                selectedLabelColor = if (status == OrderStatus.REJECTED) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                },
                            ),
                            modifier = Modifier.testTag("admin_status_${order.orderId}_${status.name}"),
                        )
                    }
                }
            }
        }
    }
}
