package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FoodCategory
import com.example.data.model.MenuItem
import com.example.data.model.MenuSortOption
import com.example.ui.components.MenuDishCard
import com.example.ui.theme.VegIndicatorGreen
import com.example.ui.viewmodel.FoodDeliveryUiState

@Composable
fun MenuScreen(
    uiState: FoodDeliveryUiState,
    onCategorySelected: (FoodCategory) -> Unit,
    onSearchChange: (String) -> Unit,
    onVegOnlyChange: (Boolean) -> Unit,
    onSortOptionSelected: (MenuSortOption) -> Unit,
    onResetFilters: () -> Unit,
    onDishClick: (MenuItem) -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    onIncrementInCart: (String) -> Unit,
    onDecrementFromCart: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("menu_screen"),
    ) {
        val columns = if (maxWidth >= 600.dp) 2 else 1

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = 24.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("menu_grid"),
        ) {
            item(span = { GridItemSpan(columns) }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Food Categories & Menu",
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Text(
                                text = "${uiState.filteredMenuItems.size} freshly cooked dishes • All prices in INR",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search pizza, burger, biryani, dosa, thali…") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search menu",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
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
                            .fillMaxWidth()
                            .testTag("menu_search_input"),
                    )

                    // Category Pill Cards
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.testTag("menu_category_row"),
                    ) {
                        items(FoodCategory.entries, key = { it.id }) { category ->
                            val isSelected = uiState.selectedCategory == category
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                contentColor = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                border = if (isSelected) {
                                    null
                                } else {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onCategorySelected(category) }
                                    .testTag("menu_category_${category.id}"),
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 8.dp,
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Image(
                                        painter = painterResource(id = category.imageRes),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape),
                                    )
                                    Column {
                                        Text(
                                            text = category.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                        )
                                        Text(
                                            text = category.tagline,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Veg Only + Sort Option Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.vegOnly,
                                onClick = { onVegOnlyChange(!uiState.vegOnly) },
                                label = { Text("Pure Veg") },
                                leadingIcon = if (uiState.vegOnly) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VegIndicatorGreen.copy(alpha = 0.16f),
                                    selectedLabelColor = VegIndicatorGreen,
                                    selectedLeadingIconColor = VegIndicatorGreen,
                                ),
                                modifier = Modifier.testTag("menu_veg_chip"),
                            )
                        }

                        items(MenuSortOption.entries, key = { it.name }) { option ->
                            val selected = uiState.sortOption == option
                            FilterChip(
                                selected = selected,
                                onClick = { onSortOptionSelected(option) },
                                label = {
                                    Text(
                                        text = option.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.SwapVert,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                    )
                                },
                                modifier = Modifier.testTag("menu_sort_${option.name}"),
                            )
                        }
                    }
                }
            }

            if (uiState.filteredMenuItems.isEmpty()) {
                item(span = { GridItemSpan(columns) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "No dishes match your filters",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Reset filters to explore all Thalis, Biryanis, Dosas, Pizzas & Burgers.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = onResetFilters) {
                            Text("Show All Dishes")
                        }
                    }
                }
            } else {
                items(uiState.filteredMenuItems, key = { it.id }) { item ->
                    MenuDishCard(
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
