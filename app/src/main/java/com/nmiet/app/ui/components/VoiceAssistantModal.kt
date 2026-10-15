package com.nmiet.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nmiet.app.data.local.LanguageManager
import com.nmiet.app.data.remote.GeminiKeyRotator
import com.nmiet.app.domain.model.decodeBase64ToBitmap
import com.nmiet.app.speech.VoiceAssistantManager
import com.nmiet.app.speech.VoiceUiState
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber

@Composable
fun VoiceAssistantModal(
    manager: VoiceAssistantManager,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val voiceState by manager.uiState.collectAsState()
    val langConfig = remember(manager) { manager.getSpeechLanguageConfig() }

    // Automated navigation hook if action command is executed
    LaunchedEffect(voiceState) {
        val state = voiceState
        if (state is VoiceUiState.ActionExecuted && !state.targetRoute.isNullOrBlank()) {
            kotlinx.coroutines.delay(1000)
            onNavigate(state.targetRoute)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {
            manager.dismiss()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Scrim background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        manager.dismiss()
                        onDismiss()
                    }
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Modal Sheet Card
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(min = 360.dp, max = 560.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // prevent scrim click through
                    ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Drag Handle
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Header Row: Language Indicator Chip & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Language Indicator Chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PrimaryBlue.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                PrimaryBlue.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "🗣️",
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Listening in: ${langConfig.displayLabel}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlue
                                )
                            }
                        }

                        // Close / Dismiss Button (✖️)
                        IconButton(
                            onClick = {
                                manager.dismiss()
                                onDismiss()
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Voice Assistant",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Main Interactive Body
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        when (val state = voiceState) {
                            is VoiceUiState.Listening -> {
                                ListeningView(state = state)
                            }
                            is VoiceUiState.Thinking -> {
                                ThinkingView(state = state)
                            }
                            is VoiceUiState.Processing -> {
                                ThinkingView(state = VoiceUiState.Thinking(recognizedQuery = state.recognizedText))
                            }
                            is VoiceUiState.Speaking -> {
                                SpeakingView(
                                    state = state,
                                    onPause = { manager.pauseSpeaking() },
                                    onResume = { manager.resumeSpeaking() }
                                )
                            }
                            is VoiceUiState.Paused -> {
                                PausedView(state = state)
                            }
                            is VoiceUiState.ActionExecuted -> {
                                ActionExecutedView(state = state)
                            }
                            is VoiceUiState.Error -> {
                                ErrorView(
                                    errorMessage = state.errorMessage,
                                    onRetry = { manager.startListening() }
                                )
                            }
                            is VoiceUiState.Idle -> {
                                IdleView(onStart = { manager.startListening() })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Action Controls
                    BottomControlsBar(
                        voiceState = voiceState,
                        onPauseToggle = {
                            when (voiceState) {
                                is VoiceUiState.Speaking -> manager.pauseSpeaking()
                                is VoiceUiState.Paused -> manager.resumeSpeaking()
                                else -> {}
                            }
                        },
                        onStop = { manager.stopSpeaking() },
                        onRetryListening = { manager.startListening() },
                        onDismiss = {
                            manager.dismiss()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Interactive State Views
// -------------------------------------------------------------

@Composable
private fun ListeningView(state: VoiceUiState.Listening) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎙️ Listening...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ask any science, math, or textbook question",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Live soundwave pulse animation
        LiveSoundWaveVisualizer(soundLevelRms = state.soundLevelRms)

        Spacer(modifier = Modifier.height(24.dp))

        // Display partial recognized words
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text = if (state.partialText.isNotBlank()) "\"${state.partialText}\"" else "\"Speak now...\"",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                color = if (state.partialText.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
fun AnimatedThreeDots(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    dotSize: androidx.compose.ui.unit.Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "threeDots")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, delayMillis = index * 160, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun ThinkingView(state: VoiceUiState.Thinking) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = "Thinking...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            AnimatedThreeDots()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Query Card
        if (state.recognizedQuery.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Question:",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${state.recognizedQuery}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeakingView(
    state: VoiceUiState.Speaking,
    onPause: () -> Unit,
    onResume: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Speaking",
                tint = SuccessGreen,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = if (state.isPaused) "⏸️ Narration Paused" else "🔊 Speaking Answer...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (state.isPaused) WarningAmber else SuccessGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Speaker Wave Visualizer
        SpeakerWaveVisualizer(isPlaying = !state.isPaused)

        Spacer(modifier = Modifier.height(16.dp))

        // Generated Diagram (if present)
        state.generatedDiagramBase64?.let { b64 ->
            val bitmap = remember(b64) { decodeBase64ToBitmap(b64) }
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Generated Diagram",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Spoken Academic Response Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (state.isPaused) WarningAmber.copy(alpha = 0.3f) else SuccessGreen.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (state.query.isNotBlank()) {
                    Text(
                        text = "Q: ${state.query}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = state.answerText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
private fun PausedView(state: VoiceUiState.Paused) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⏸️ Audio Paused",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = WarningAmber
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Static frozen bars
        SpeakerWaveVisualizer(isPlaying = false)

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (state.query.isNotBlank()) {
                    Text(
                        text = "Q: ${state.query}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = state.answerText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
private fun ActionExecutedView(state: VoiceUiState.ActionExecuted) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = "Action",
            tint = PrimaryBlue,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = state.actionTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (state.actionOdia.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = state.actionOdia,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorView(errorMessage: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        FilledTonalButton(onClick = onRetry) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Try Speaking Again")
        }
    }
}

@Composable
private fun IdleView(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = onStart,
            modifier = Modifier
                .size(72.dp)
                .background(PrimaryBlue, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Start Speaking",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tap to Ask Voice Assistant",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Speak in English, Odia, or Hindi",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// -------------------------------------------------------------
// Soundwave & Audio Visualizers
// -------------------------------------------------------------

@Composable
private fun LiveSoundWaveVisualizer(soundLevelRms: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "soundwavePulse")
    val barCount = 9

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(60.dp)
    ) {
        for (i in 0 until barCount) {
            val baseHeight = 16f
            val dynamicBoost = (soundLevelRms * 4f).coerceIn(4f, 40f)
            val animatedHeight by infiniteTransition.animateFloat(
                initialValue = baseHeight + (i % 3) * 6f,
                targetValue = baseHeight + dynamicBoost + ((barCount - i) % 4) * 8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(350 + (i * 45), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar$i"
            )

            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(animatedHeight.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                PrimaryBlue,
                                Color(0xFF00B4D8)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun SpeakerWaveVisualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "speakerWave")
    val barCount = 7

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(48.dp)
    ) {
        for (i in 0 until barCount) {
            val animatedHeight = if (isPlaying) {
                val heightVal by infiniteTransition.animateFloat(
                    initialValue = 12f + (i % 3) * 8f,
                    targetValue = 38f - ((i + 2) % 3) * 6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(320 + (i * 60), easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "speakerBar$i"
                )
                heightVal
            } else {
                14f
            }

            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(animatedHeight.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPlaying) {
                            Brush.verticalGradient(
                                listOf(SuccessGreen, Color(0xFF80ED99))
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                            )
                        }
                    )
            )
        }
    }
}

// -------------------------------------------------------------
// Bottom Controls Bar (Pause, Stop, Resume, Mic, Dismiss)
// -------------------------------------------------------------

@Composable
private fun BottomControlsBar(
    voiceState: VoiceUiState,
    onPauseToggle: () -> Unit,
    onStop: () -> Unit,
    onRetryListening: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pause / Resume Button (⏸️ / ▶️)
        val isPaused = voiceState is VoiceUiState.Paused || (voiceState is VoiceUiState.Speaking && voiceState.isPaused)
        val canPause = voiceState is VoiceUiState.Speaking || voiceState is VoiceUiState.Paused

        IconButton(
            onClick = onPauseToggle,
            enabled = canPause,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (canPause) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                contentColor = if (canPause) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPaused) "Resume" else "Pause",
                modifier = Modifier.size(24.dp)
            )
        }

        // Central Mic / Speak Button (🎙️)
        IconButton(
            onClick = onRetryListening,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ),
            modifier = Modifier
                .size(62.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Speak to Assistant",
                modifier = Modifier.size(30.dp)
            )
        }

        // Stop Button (⏹️)
        val canStop = voiceState is VoiceUiState.Speaking || voiceState is VoiceUiState.Listening || voiceState is VoiceUiState.Thinking || voiceState is VoiceUiState.Paused
        IconButton(
            onClick = onStop,
            enabled = canStop,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (canStop) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                contentColor = if (canStop) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// VoiceAssistantOverlay Facade for AppNavHost & Root Scaffold
// -------------------------------------------------------------

@Composable
fun VoiceAssistantOverlay(
    onDismiss: () -> Unit,
    languageManager: LanguageManager,
    geminiKeyRotator: GeminiKeyRotator = GeminiKeyRotator.getInstance(),
    manager: VoiceAssistantManager? = null,
    onNavigate: (String) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val actualManager = manager ?: remember(languageManager, geminiKeyRotator) {
        val app = context.applicationContext as? com.nmiet.app.MainActivity
        // If repository is accessible or create fallback
        val db = com.nmiet.app.data.local.AppDatabase.getInstance(context.applicationContext)
        val repo = com.nmiet.app.data.repository.ContentRepositoryImpl(db.learningDao())
        VoiceAssistantManager(
            context = context.applicationContext,
            repository = repo,
            languageManager = languageManager,
            geminiKeyRotator = geminiKeyRotator
        )
    }

    VoiceAssistantModal(
        manager = actualManager,
        onDismiss = onDismiss,
        onNavigate = onNavigate
    )
}
