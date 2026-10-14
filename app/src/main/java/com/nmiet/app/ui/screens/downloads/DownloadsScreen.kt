package com.nmiet.app.ui.screens.downloads

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nmiet.app.data.remote.R2ContentConfig
import com.nmiet.app.data.remote.R2ContentManager
import com.nmiet.app.domain.model.ContentItem
import com.nmiet.app.domain.model.ContentMediaType
import com.nmiet.app.domain.model.DownloadStatus
import com.nmiet.app.domain.repository.ContentRepository
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber
import kotlinx.coroutines.launch
import java.io.File

enum class ContentFilter {
    ALL,
    ZIP_PACKS,
    DIAGRAMS,
    NOTES_AND_JSON
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DownloadsScreen(
    repository: ContentRepository,
    onBackClick: () -> Unit = {},
    onOpenLesson: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val contentManager = remember { R2ContentManager.getInstance(context, repository) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val items by contentManager.contentItems.collectAsState()
    val catalogStatus by contentManager.catalogStatusMessage.collectAsState()
    val isRefreshing by contentManager.isRefreshing.collectAsState()
    val activeBaseUrl by contentManager.activeBaseUrl.collectAsState()

    var activeFilter by remember { mutableStateOf(ContentFilter.ALL) }
    var viewingTextItem by remember { mutableStateOf<Pair<ContentItem, String>?>(null) }
    var viewingImageItem by remember { mutableStateOf<Pair<ContentItem, File>?>(null) }
    var showUrlConfigDialog by remember { mutableStateOf(false) }

    val filteredItems = remember(items, activeFilter) {
        when (activeFilter) {
            ContentFilter.ALL -> items
            ContentFilter.ZIP_PACKS -> items.filter { it.isZip }
            ContentFilter.DIAGRAMS -> items.filter { it.isImage }
            ContentFilter.NOTES_AND_JSON -> items.filter { it.isText }
        }
    }

    LaunchedEffect(Unit) {
        contentManager.refreshCatalogue()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Content Library & Downloads", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Cloudflare R2 Object Storage",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { contentManager.refreshCatalogue() }, enabled = !isRefreshing) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Catalogue")
                        }
                    }
                    IconButton(onClick = { showUrlConfigDialog = true }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Configure URL")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Cloudflare R2 Connection Banner
            item {
                Spacer(modifier = Modifier.height(4.dp))
                R2ConnectionHeaderCard(
                    statusMessage = catalogStatus,
                    baseUrl = activeBaseUrl,
                    isRefreshing = isRefreshing,
                    onRefresh = { contentManager.refreshCatalogue() }
                )
            }

            // 2. Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeFilter == ContentFilter.ALL,
                        onClick = { activeFilter = ContentFilter.ALL },
                        label = { Text("All (${items.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                            selectedLabelColor = PrimaryBlue
                        )
                    )
                    FilterChip(
                        selected = activeFilter == ContentFilter.ZIP_PACKS,
                        onClick = { activeFilter = ContentFilter.ZIP_PACKS },
                        label = { Text("Packs (${items.count { it.isZip }})", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = activeFilter == ContentFilter.DIAGRAMS,
                        onClick = { activeFilter = ContentFilter.DIAGRAMS },
                        label = { Text("Diagrams (${items.count { it.isImage }})", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = activeFilter == ContentFilter.NOTES_AND_JSON,
                        onClick = { activeFilter = ContentFilter.NOTES_AND_JSON },
                        label = { Text("Notes/JSON (${items.count { it.isText }})", fontSize = 12.sp) }
                    )
                }
            }

            // 3. Catalogue Items
            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No resources found matching filter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    R2ContentItemCard(
                        item = item,
                        onDownloadClick = { contentManager.startDownload(item) },
                        onCancelClick = { contentManager.cancelDownload(item.id) },
                        onDeleteClick = { contentManager.deleteDownloadedItem(item) },
                        onOpenClick = {
                            when {
                                item.isImage -> {
                                    val file = contentManager.getLocalImageFile(item)
                                    if (file != null) {
                                        viewingImageItem = Pair(item, file)
                                    } else {
                                        scope.launch { snackbarHostState.showSnackbar("Image file not accessible on disk.") }
                                    }
                                }
                                item.isText -> {
                                    scope.launch {
                                        val textResult = contentManager.readTextContent(item)
                                        textResult.onSuccess { content ->
                                            viewingTextItem = Pair(item, content)
                                        }.onFailure { err ->
                                            snackbarHostState.showSnackbar("Failed to read text: ${err.message}")
                                        }
                                    }
                                }
                                item.isZip -> {
                                    scope.launch {
                                        val importResult = contentManager.importLearningPackZip(item)
                                        importResult.onSuccess { pack ->
                                            snackbarHostState.showSnackbar("✅ Pack verified & imported: ${pack.titleEnglish}")
                                        }.onFailure { err ->
                                            snackbarHostState.showSnackbar("❌ ZIP Import failed: ${err.message}")
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Text / JSON Resource Viewer
    viewingTextItem?.let { (item, content) ->
        TextViewerDialog(
            item = item,
            content = content,
            onDismiss = { viewingTextItem = null }
        )
    }

    // Modal: Image / Diagram Viewer
    viewingImageItem?.let { (item, file) ->
        ImageViewerDialog(
            item = item,
            file = file,
            onDismiss = { viewingImageItem = null }
        )
    }

    // Modal: Configurable Base URL Dialog
    if (showUrlConfigDialog) {
        UrlConfigDialog(
            currentUrl = activeBaseUrl,
            onDismiss = { showUrlConfigDialog = false },
            onApply = { newUrl ->
                contentManager.refreshCatalogue(newUrl)
                showUrlConfigDialog = false
            }
        )
    }
}

@Composable
private fun R2ConnectionHeaderCard(
    statusMessage: String,
    baseUrl: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Cloud, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = R2ContentConfig.CONFIG_LABEL,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = R2ContentConfig.CONFIG_NOTE,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                if (isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                Text(
                    text = statusMessage,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun R2ContentItemCard(
    item: ContentItem,
    onDownloadClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onOpenClick: () -> Unit
) {
    val (icon, typeBadgeColor, typeLabel) = when (item.mediaType) {
        ContentMediaType.LEARNING_PACK_ZIP -> Triple(Icons.Default.FolderZip, Color(0xFF7C3AED), "ZIP PACK")
        ContentMediaType.IMAGE_DIAGRAM -> Triple(Icons.Default.Image, Color(0xFF0D9488), "PNG DIAGRAM")
        ContentMediaType.REVISION_NOTES -> Triple(Icons.AutoMirrored.Filled.MenuBook, Color(0xFF2563EB), "TXT REVISION")
        ContentMediaType.JSON_RESOURCE -> Triple(Icons.Default.Code, Color(0xFFD97706), "JSON RESOURCE")
        ContentMediaType.PDF_TEXTBOOK -> Triple(Icons.Default.PictureAsPdf, Color(0xFFDC2626), "PDF TEXTBOOK")
        ContentMediaType.VIDEO_LESSON -> Triple(Icons.Default.PlayCircle, Color(0xFFE11D48), "VIDEO LESSON")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Icon, Title, and Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(typeBadgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = typeBadgeColor, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Media Type Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(typeBadgeColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = typeLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = typeBadgeColor)
                        }

                        // Language Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = item.language, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }

                        // Size
                        Text(
                            text = item.formattedSize,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            // SHA-256 Badge if available
            if (item.sha256Checksum.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "SHA-256: ${item.sha256Checksum.take(16)}...",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action / Status Area
            when (item.downloadStatus) {
                DownloadStatus.DOWNLOADING -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Downloading & verifying SHA-256...",
                                fontSize = 11.sp,
                                color = WarningAmber,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "${item.downloadProgressPercent}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { item.downloadProgressPercent / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = WarningAmber
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onCancelClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancel Download", fontSize = 12.sp)
                        }
                    }
                }

                DownloadStatus.COMPLETED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Verified Offline",
                                fontSize = 11.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = onOpenClick,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.isZip) Icons.Default.FolderZip else Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (item.isZip) "Import Pack" else "Open Offline", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = onDeleteClick) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                DownloadStatus.NOT_DOWNLOADED -> {
                    Button(
                        onClick = onDownloadClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download from R2", fontSize = 12.sp)
                    }
                }

                DownloadStatus.FAILED -> {
                    Column {
                        item.errorMessage?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = msg,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        OutlinedButton(
                            onClick = onDownloadClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Download", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextViewerDialog(
    item: ContentItem,
    content: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Offline Document Viewer • ${item.language}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 360.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = content,
                        fontSize = 12.sp,
                        fontFamily = if (item.mediaType == ContentMediaType.JSON_RESOURCE) FontFamily.Monospace else FontFamily.Default,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Close Document")
                }
            }
        }
    }
}

@Composable
private fun ImageViewerDialog(
    item: ContentItem,
    file: File,
    onDismiss: () -> Unit
) {
    val bitmap = remember(file) {
        try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Offline Diagram Viewer", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 320.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = item.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    } else {
                        Text("Unable to render image bitmap.", color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Close Diagram")
                }
            }
        }
    }
}

@Composable
private fun UrlConfigDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit
) {
    var textUrl by remember { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cloudflare R2 Endpoint", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Configurable public base URL for educational content catalogue and objects.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = textUrl,
                    onValueChange = { textUrl = it },
                    label = { Text("Base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "⚠️ Testing configuration only. Never enter secret keys or production admin credentials.",
                    fontSize = 11.sp,
                    color = WarningAmber
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(textUrl.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Apply & Sync")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
