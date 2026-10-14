package com.nmiet.app.ui.screens.playground

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.ai.llm.LocalLlmState
import com.nmiet.app.ai.llm.ModelInstallationState
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalAiPlaygroundScreen(
    viewModel: LocalAiPlaygroundViewModel,
    onNavigateBack: () -> Unit
) {
    val modelInfo by viewModel.selectedModel.collectAsState()
    val installState by viewModel.installState.collectAsState()
    val engineState by viewModel.engineState.collectAsState()
    val memoryGuidance by viewModel.memoryGuidance.collectAsState()
    val promptInput by viewModel.promptInput.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, (engineState as? LocalLlmState.Generating)?.streamedResponse) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Local AI Playground",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "LiteRT-LM On-Device LLM (CPU)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(10.dp)) }

                // 1. Dynamic Engine & Installation Banner
                item {
                    LocalModelStatusCard(
                        modelInfo = modelInfo,
                        installState = installState,
                        engineState = engineState,
                        memoryGuidance = memoryGuidance,
                        onDownload = { viewModel.startModelDownload() },
                        onCancelDownload = { viewModel.cancelModelDownload() },
                        onLoad = { viewModel.loadModel() },
                        onUnload = { viewModel.unloadModel() },
                        onDelete = { viewModel.deleteModel() }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 2. Hardware & License Details Card
                item {
                    ModelSpecificationCard(
                        modelInfo = modelInfo,
                        memoryGuidance = memoryGuidance
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 3. Test Prompts Strip
                item {
                    Text(
                        text = "Quick Evaluation Prompts (English · Odia · Hindi):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.selectTestPrompt("Explain photosynthesis in simple terms.") },
                            label = { Text("English", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.selectTestPrompt("ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା କଣ ବୁଝାନ୍ତୁ?") },
                            label = { Text("ଓଡ଼ିଆ (Odia)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.selectTestPrompt("प्रकाश संश्लेषण क्या है? समझाइए।") },
                            label = { Text("हिंदी (Hindi)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 4. Chat Messages List
                if (messages.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Standalone Edge AI Session",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Send a prompt above to verify local CPU inference using LiteRT-LM. All tokens are computed strictly on-device without remote servers.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        PlaygroundMessageBubble(message = msg)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // 5. Bottom Input Bar with Generation Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val isGenerating = engineState is LocalLlmState.Generating
                    val isModelReady = viewModel.isModelLoaded

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { viewModel.updatePromptInput(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    text = if (isModelReady) "Ask on-device Qwen3-0.6B..." else "Load model first to chat...",
                                    fontSize = 13.sp
                                )
                            },
                            enabled = isModelReady && !isGenerating,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        if (isGenerating) {
                            IconButton(
                                onClick = { viewModel.cancelActiveGeneration() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = Color.White
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.sendPrompt() },
                                enabled = isModelReady && promptInput.isNotBlank(),
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isModelReady && promptInput.isNotBlank()) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (isModelReady && promptInput.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocalModelStatusCard(
    modelInfo: com.nmiet.app.ai.llm.LocalModelInfo,
    installState: ModelInstallationState,
    engineState: LocalLlmState,
    memoryGuidance: com.nmiet.app.ai.llm.DeviceMemoryGuidance,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onLoad: () -> Unit,
    onUnload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                engineState is LocalLlmState.Loaded || engineState is LocalLlmState.Generating -> SuccessGreen.copy(alpha = 0.12f)
                installState is ModelInstallationState.Installed -> PrimaryBlue.copy(alpha = 0.08f)
                installState is ModelInstallationState.Failed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when {
                        engineState is LocalLlmState.Loaded || engineState is LocalLlmState.Generating -> Icons.Default.Speed
                        installState is ModelInstallationState.Installed -> Icons.Default.CheckCircle
                        installState is ModelInstallationState.Downloading -> Icons.Default.CloudDownload
                        else -> Icons.Default.SmartToy
                    }
                    val iconTint = when {
                        engineState is LocalLlmState.Loaded || engineState is LocalLlmState.Generating -> SuccessGreen
                        installState is ModelInstallationState.Installed -> PrimaryBlue
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = modelInfo.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (engineState is LocalLlmState.Loaded || engineState is LocalLlmState.Generating) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SuccessGreen)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "⚡ 100% OFFLINE (LiteRT-LM)",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body message based on installation / engine state
            when (installState) {
                is ModelInstallationState.NotInstalled -> {
                    Text(
                        text = "Model weights (~331 MB) are stored outside the APK. Download is required to enable offline on-device inference.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onDownload,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download Model (331 MB)", fontSize = 12.sp)
                        }
                    }
                }
                is ModelInstallationState.CheckingStorage -> {
                    Text("Checking private storage headroom...", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is ModelInstallationState.Downloading -> {
                    val mbDownloaded = (installState.bytesDownloaded / 1_000_000.0).let { "%.1f".format(it) }
                    val mbTotal = (installState.totalBytes / 1_000_000.0).let { "%.1f".format(it) }

                    Text(
                        text = "Downloading to app-private storage: $mbDownloaded / $mbTotal MB (${installState.progressPercent}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { installState.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onCancelDownload,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel Download", fontSize = 12.sp)
                    }
                }
                is ModelInstallationState.VerifyingChecksum -> {
                    Text("Verifying SHA-256 hash against pinned release...", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is ModelInstallationState.Installed -> {
                    when (val eng = engineState) {
                        is LocalLlmState.Unloaded -> {
                            Text(
                                text = "Weights verified on disk (SHA-256 confirmed). Ready to load into memory for CPU inference.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onLoad,
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Load Model (CPU)", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = onDelete
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        is LocalLlmState.Loading -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Loading model weights into memory...", fontSize = 12.sp)
                            }
                        }
                        is LocalLlmState.Loaded -> {
                            Text(
                                text = "Model loaded in ${eng.loadDurationMs} ms. Engine initialized with CPU backend.",
                                fontSize = 12.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onUnload,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Unload Model From Memory", fontSize = 12.sp)
                            }
                        }
                        is LocalLlmState.Generating -> {
                            Text(
                                text = "Streaming response on-device (${eng.tokenCount} tokens · ${eng.speedTokensPerSec?.let { "%.1f tok/s".format(it) } ?: "computing..."})",
                                fontSize = 12.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        is LocalLlmState.Error -> {
                            Text(
                                text = "Engine Error: ${eng.errorMessage}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onLoad) {
                                Text("Retry Load", fontSize = 12.sp)
                            }
                        }
                    }
                }
                is ModelInstallationState.Failed -> {
                    Text(
                        text = "Installation Failed: ${installState.errorMessage}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onDownload) {
                        Text("Retry Download", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ModelSpecificationCard(
    modelInfo: com.nmiet.app.ai.llm.LocalModelInfo,
    memoryGuidance: com.nmiet.app.ai.llm.DeviceMemoryGuidance
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Model Specifications & Device Compatibility", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Text("• Format: ${modelInfo.format} (Google LiteRT-LM)", fontSize = 11.sp)
            Text("• Quantization: ${modelInfo.variant}", fontSize = 11.sp)
            Text("• Pinned Revision: ${modelInfo.pinnedRevision.take(10)}... (Apache 2.0)", fontSize = 11.sp)
            Text("• Hardware Guidance: ${memoryGuidance.guidanceMessage}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                Text(
                    text = "⚠️ ${modelInfo.academicGuidance}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun PlaygroundMessageBubble(message: PlaygroundMessage) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) PrimaryBlue else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isUser) 0.dp else 2.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Qwen3 0.6B (Local CPU)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SuccessGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("⚡ ON-DEVICE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )

                if (!isUser && message.tokenCount != null && message.tokenCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${message.tokenCount} tokens · ${message.latencyMs?.let { "${it}ms" } ?: ""} ${message.tokensPerSec?.let { "• %.1f tok/s".format(it) } ?: ""}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
