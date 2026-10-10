package com.nmiet.app.ui.screens.contentdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.repository.ContentRepository
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.components.CategoryBadge
import com.nmiet.app.ui.components.EmptyView
import com.nmiet.app.ui.components.ReceivedViaNearbyBadge
import com.nmiet.app.ui.components.StoredOfflineBadge
import com.nmiet.app.ui.components.formatRelativeTime
import com.nmiet.app.ui.theme.SuccessGreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ContentDetailScreen(
    itemId: String,
    repository: ContentRepository,
    onBackClick: () -> Unit,
    onNavigateToNearbyShare: (String) -> Unit
) {
    val item by repository.getContentByIdFlow(itemId).collectAsState(initial = null)
    val syncStatus by repository.getSyncStatusFlow().collectAsState(initial = com.nmiet.app.domain.model.SyncStatus())

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Notice Detail",
                isOnline = syncStatus.isOnline,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        if (item == null) {
            EmptyView(
                title = "Content Not Found",
                subtitle = "The requested article could not be loaded from local storage.",
                actionButtonText = "Go Back",
                onActionClick = onBackClick,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            val contentItem = item!!
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Header tags row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryBadge(category = contentItem.category)
                    if (contentItem.isSavedOffline) {
                        StoredOfflineBadge()
                    }
                    if (contentItem.isReceivedViaNearby) {
                        ReceivedViaNearbyBadge(peerName = contentItem.receivedFromPeerName)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = contentItem.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata: Source & Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Source: ${contentItem.source}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatRelativeTime(contentItem.publishedAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Full Body text
                Text(
                    text = contentItem.body,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Integrity & Cryptographic Hash info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Content Hash",
                            modifier = Modifier.padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Integrity Hash (SHA-256 snippet):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = contentItem.contentHash,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Offline status callout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SuccessGreen.copy(alpha = 0.12f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = if (contentItem.isSavedOffline) {
                                "Available completely offline. Verified in device local SQLite cache."
                            } else {
                                "Cached in session. Tap 'Save Offline' to ensure permanent local retention."
                            },
                            fontSize = 12.sp,
                            color = SuccessGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                repository.toggleSavedOffline(contentItem.id)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (contentItem.isSavedOffline) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (contentItem.isSavedOffline) "Saved" else "Save Offline")
                    }

                    Button(
                        onClick = { onNavigateToNearbyShare(contentItem.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Nearby Share")
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
