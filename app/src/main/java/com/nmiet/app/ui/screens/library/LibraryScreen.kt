package com.nmiet.app.ui.screens.library

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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.ui.components.AppSearchBar
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.components.CategoryChips
import com.nmiet.app.ui.components.ContentCard
import com.nmiet.app.ui.components.EmptyView
import com.nmiet.app.ui.theme.SuccessGreen

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onItemClick: (String) -> Unit,
    onNavigateToFeed: () -> Unit
) {
    val savedItems by viewModel.savedItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Offline Library",
                isOnline = syncStatus.isOnline
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Offline Library Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SuccessGreen.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Offline Persistence Verified",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SuccessGreen
                        )
                        Text(
                            text = "${savedItems.size} document(s) cached locally in Room database for offline reading.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Search bar
            AppSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                placeholder = "Search saved offline documents..."
            )

            // Category filter chips
            CategoryChips(
                categories = viewModel.categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.onCategorySelect(it) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (savedItems.isEmpty()) {
                EmptyView(
                    title = if (searchQuery.isNotEmpty()) "No matching saved items" else "No saved content yet",
                    subtitle = if (searchQuery.isNotEmpty())
                        "Try a different search query."
                    else
                        "Bookmark items on the Campus Feed or receive them over Nearby Share to read them offline.",
                    icon = Icons.Default.BookmarkBorder,
                    actionButtonText = if (searchQuery.isEmpty()) "Browse Campus Feed" else "Clear Search",
                    onActionClick = {
                        if (searchQuery.isNotEmpty()) {
                            viewModel.onSearchQueryChange("")
                            viewModel.onCategorySelect("All")
                        } else {
                            onNavigateToFeed()
                        }
                    }
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(savedItems, key = { "lib-${it.id}" }) { item ->
                        ContentCard(
                            item = item,
                            onClick = { onItemClick(item.id) },
                            onToggleSave = { viewModel.toggleSave(item.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
