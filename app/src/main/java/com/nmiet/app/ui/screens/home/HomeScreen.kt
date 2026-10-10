package com.nmiet.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.ui.components.AppSearchBar
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.components.CategoryChips
import com.nmiet.app.ui.components.ContentCard
import com.nmiet.app.ui.components.EmptyView
import com.nmiet.app.ui.theme.WarningAmber

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onItemClick: (String) -> Unit
) {
    val items by viewModel.items.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "NMIET Connect",
                isOnline = syncStatus.isOnline,
                isRefreshing = syncStatus.isSyncing,
                onRefreshClick = { viewModel.refreshFeed() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Offline alert strip if not online
            if (!syncStatus.isOnline) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarningAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline indicator",
                            tint = WarningAmber
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline Mode: Browsing locally cached campus updates",
                            fontSize = 12.sp,
                            color = WarningAmber
                        )
                    }
                }
            }

            // Search bar
            AppSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) }
            )

            // Category filter chips
            CategoryChips(
                categories = viewModel.categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.onCategorySelect(it) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Feed list or empty view
            if (items.isEmpty()) {
                EmptyView(
                    title = if (searchQuery.isNotEmpty()) "No matching content" else "Feed is empty",
                    subtitle = if (searchQuery.isNotEmpty()) "Try adjusting your search terms or category filter." else "Tap the refresh button in the top bar to fetch campus updates.",
                    icon = Icons.Default.SearchOff,
                    actionButtonText = if (searchQuery.isNotEmpty()) "Clear Search" else "Refresh Feed",
                    onActionClick = {
                        if (searchQuery.isNotEmpty()) {
                            viewModel.onSearchQueryChange("")
                            viewModel.onCategorySelect("All")
                        } else {
                            viewModel.refreshFeed()
                        }
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items, key = { it.id }) { item ->
                        ContentCard(
                            item = item,
                            onClick = { onItemClick(item.id) },
                            onToggleSave = { viewModel.toggleSave(item.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav
                    }
                }
            }
        }
    }
}
