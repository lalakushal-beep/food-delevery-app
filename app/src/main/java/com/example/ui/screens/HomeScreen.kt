package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.auth.CloudSyncStatusCard
import com.example.data.model.FoodCategory
import com.example.data.model.MenuItem
import com.example.data.model.OrderStatus
import com.example.ui.components.FeaturedDishCard
import com.example.ui.components.MenuDishCard
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.VegIndicatorGreen
import com.example.ui.viewmodel.FoodDeliveryUiState

@Composable
fun HomeScreen(
    uiState: FoodDeliveryUiState,
    onSearchChange: (String) -> Unit,
    onVegOnlyChange: (Boolean) -> Unit,
    onCategorySelected: (FoodCategory) -> Unit,
    onSeeAllMenu: () -> Unit,
    onDishClick: (MenuItem) -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    onIncrementInCart: (String) -> Unit,
    onDecrementFromCart: (String) -> Unit,
    onOrderHeroBanner: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // Restaurant Identity & Delivery Location Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                MaterialTheme.colorScheme.background,
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Delivery address",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Delivering to ${uiState.checkoutForm.addressLine.substringBefore(",")}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Restaurant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = stringResource(id = R.string.kitchen_name),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Restaurant rating",
                                    tint = SaffronGold,
                                    modifier = Modifier.size(15.dp),
                                )
                                Text(
                                    text = "4.8",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = "25m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                // Clearly visible Restaurant Owner Admin Panel shortcut
                val pendingOrdersCount = uiState.orders.count { it.status == OrderStatus.PENDING }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Restaurant Owner",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = if (pendingOrdersCount > 0) {
                                    "$pendingOrdersCount pending • ${uiState.orders.size} total orders"
                                } else {
                                    "Manage ${uiState.orders.size} customer orders"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (pendingOrdersCount > 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        FilledTonalButton(
                            onClick = onOpenAdminPanel,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier.testTag("home_admin_panel_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = "Open Admin Panel",
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.kitchen_admin_button),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                            )
                        }
                    }
                }

                CloudSyncStatusCard(
                    signedInEmail = uiState.signedInEmail,
                    signedInName = uiState.signedInName,
                    isAdminAccount = uiState.isAdminAccount,
                    syncError = uiState.cloudSyncError,
                )

                // Search bar + Veg Only Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.search_hint),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search food",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear search",
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_search_input"),
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (uiState.vegOnly) VegIndicatorGreen else MaterialTheme.colorScheme.outlineVariant,
                        ),
                        modifier = Modifier.clickable { onVegOnlyChange(!uiState.vegOnly) },
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "VEG",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.vegOnly) VegIndicatorGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                            )
                            Switch(
                                checked = uiState.vegOnly,
                                onCheckedChange = onVegOnlyChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = VegIndicatorGreen,
                                ),
                                modifier = Modifier.testTag("home_veg_switch"),
                            )
                        }
                    }
                }
            }
        }

        // Featured Maharaja Thali Promo Banner (hidden while actively searching)
        if (uiState.searchQuery.isBlank()) {
            item {
                Card(
                    onClick = onOrderHeroBanner,
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(192.dp)
                        .testTag("hero_promo_banner"),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_thali),
                            contentDescription = "Maharaja Special Thali Offer",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xE61F0D05),
                                            Color(0x991F0D05),
                                            Color.Transparent,
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    text = "FLAT ₹100 OFF • CODE: NAMASTE100",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = "Royal Indian Feast,\nDelivered Piping Hot",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = onOrderHeroBanner,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("hero_order_now_button"),
                                ) {
                                    Text(
                                        text = "Explore Thali & Menu",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Food Categories Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(id = R.string.categories_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        TextButton(
                            onClick = onSeeAllMenu,
                            modifier = Modifier.testTag("see_all_categories_button"),
                        ) {
                            Text(stringResource(id = R.string.see_all))
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(FoodCategory.selectableCategories, key = { it.id }) { category ->
                            val isSelected = uiState.selectedCategory == category
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(78.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onCategorySelected(category) }
                                    .padding(vertical = 4.dp)
                                    .testTag("home_category_${category.id}"),
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    border = if (isSelected) {
                                        BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
                                    } else {
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    },
                                    shadowElevation = if (isSelected) 4.dp else 1.dp,
                                    modifier = Modifier.size(68.dp),
                                ) {
                                    Image(
                                        painter = painterResource(id = category.imageRes),
                                        contentDescription = category.label,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = category.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onBackground
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            // Kitchen Bestsellers Horizontal Shelf
            if (uiState.bestsellers.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(id = R.string.bestsellers_title),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = "Chef's Picks",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.bestsellers, key = { it.id }) { item ->
                                FeaturedDishCard(
                                    item = item,
                                    quantityInCart = uiState.itemQuantities[item.id] ?: 0,
                                    onCardClick = { onDishClick(item) },
                                    onAdd = { onAddToCart(item) },
                                    onIncrement = { onIncrementInCart(item.id) },
                                    onDecrement = { onDecrementFromCart(item.id) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Popular / Filtered Menu List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (uiState.searchQuery.isNotBlank()) {
                        "Results for \"${uiState.searchQuery}\""
                    } else {
                        stringResource(id = R.string.popular_title)
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
                TextButton(onClick = onSeeAllMenu) {
                    Text("Full Menu")
                }
            }
        }

        if (uiState.filteredMenuItems.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "No matching dishes found",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Try searching for Pizza, Biryani, Dosa, Paneer Tikka, or Thali.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(uiState.filteredMenuItems, key = { "home_${it.id}" }) { item ->
                MenuDishCard(
                    item = item,
                    quantityInCart = uiState.itemQuantities[item.id] ?: 0,
                    onCardClick = { onDishClick(item) },
                    onAdd = { onAddToCart(item) },
                    onIncrement = { onIncrementInCart(item.id) },
                    onDecrement = { onDecrementFromCart(item.id) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
