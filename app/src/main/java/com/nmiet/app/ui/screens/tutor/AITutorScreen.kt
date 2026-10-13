package com.nmiet.app.ui.screens.tutor

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.model.TutorMessage
import com.nmiet.app.domain.model.TutorSender
import com.nmiet.app.ui.components.AppTopBar
import com.nmiet.app.ui.components.VoiceInputBar
import com.nmiet.app.ui.theme.AccentCyan
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber

@Composable
fun AITutorScreen(
    viewModel: AITutorViewModel
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val attachedImage by viewModel.attachedImage.collectAsState()
    val isRecording by viewModel.isRecordingVoice.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // One-Tap Suggestion Chips
    val sampleQueries = listOf(
        "ଆଲୋକ ଶ୍ଳେଷଣ କ’ଣ ବୁଝାନ୍ତୁ",
        "Where does Oxygen come from in photosynthesis?",
        "Explain Light vs Dark Reactions in Odia",
        "Who is Salman Khan?" // Test guardrail refusal
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Bilingual AI Tutor",
                isOnline = syncStatus.isOnline
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mode Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (syncStatus.isOnline) PrimaryBlue.copy(alpha = 0.1f) else WarningAmber.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (syncStatus.isOnline) Icons.Default.AutoAwesome else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (syncStatus.isOnline) PrimaryBlue else WarningAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (syncStatus.isOnline)
                            "Online Mode: [Cloud AI] Gemini Vision & Study Guardrails active"
                        else
                            "Offline Mode: [Offline Grounded] using local Room SQLite FTS cache",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (syncStatus.isOnline) PrimaryBlue else WarningAmber
                    )
                }
            }

            // Chat Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    TutorMessageBubble(message = msg, isOnlineContext = syncStatus.isOnline)
                }

                if (isGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RelayNet AI Tutor is solving with academic grounding...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Animated Audio-Wave indicator when microphone press-to-speak is active
            if (isRecording) {
                VoiceWaveformIndicator()
            }

            // One-tap suggestion chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SuggestionChip(
                    onClick = { viewModel.attachSampleTextbookPhoto() },
                    label = { Text("📸 Attach Diagram (ଚିତ୍ର)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue) }
                )
                sampleQueries.forEach { query ->
                    SuggestionChip(
                        onClick = { viewModel.sendMessage(query) },
                        label = { Text(query, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Voice-First & Vision Multimodal Input Bar
            VoiceInputBar(
                inputText = inputText,
                onInputTextChanged = { viewModel.onInputTextChanged(it) },
                attachedImage = attachedImage,
                onImageSelected = { viewModel.onImageCaptured(it) },
                onClearImage = { viewModel.clearAttachedImage() },
                isRecordingVoice = isRecording,
                onMicClick = { viewModel.simulateVoiceInput() },
                onSendClick = { viewModel.sendMessage() },
                isGenerating = isGenerating
            )
        }
    }
}

@Composable
fun TutorMessageBubble(message: TutorMessage, isOnlineContext: Boolean) {
    val isUser = message.sender == TutorSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        // Amber warning card if query was out of syllabus & rejected by guardrail
        if (!message.isStudyRelated) {
            Card(
                modifier = Modifier.fillMaxWidth(0.92f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Study-Only Guardrail Triggered",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ମୁଁ କେବଳ ପାଠପଢ଼ା ଏବଂ ପାଠ୍ୟକ୍ରମ ସମ୍ବନ୍ଧୀୟ ପ୍ରଶ୍ନର ଉତ୍ତର ଦେଇପାରିବି। (I can only answer academic curriculum questions).",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                    if (message.textOdia.isNotBlank() && !message.textOdia.contains("ମୁଁ କେବଳ ପାଠପଢ଼ା")) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = message.textOdia,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
            return@Row
        }

        // Standard Chat Message Bubble
        Card(
            modifier = Modifier.fillMaxWidth(0.88f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) PrimaryBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isUser) 0.dp else 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RelayNet AI Tutor", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }

                        // Origin Badge: [Offline Grounded] vs [Cloud AI]
                        val isOfflineGrounded = message.isOfflineGrounded || !isOnlineContext
                        val badgeText = if (isOfflineGrounded) "[Offline Grounded]" else "[Cloud AI]"
                        val badgeBg = if (isOfflineGrounded) SuccessGreen.copy(alpha = 0.15f) else AccentCyan.copy(alpha = 0.15f)
                        val badgeTextColor = if (isOfflineGrounded) SuccessGreen else AccentCyan

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // If user attached an image, render preview inside bubble
                if (isUser && message.imageBitmap != null) {
                    Image(
                        bitmap = message.imageBitmap.asImageBitmap(),
                        contentDescription = "User submitted photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Odia text
                Text(
                    text = message.textOdia,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // English text for Tutor messages (Bilingual support)
                if (!isUser && message.textEnglish != message.textOdia) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("English Translation:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = message.textEnglish,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Concept grounding tags if present
                if (!isUser && message.sourceTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        message.sourceTags.take(3).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("#$tag", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceWaveformIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = waveAlpha),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Listening to Odia/English voice input... Speak now",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun animateMicPulse(isRecording: Boolean): androidx.compose.runtime.State<Float> {
    if (!isRecording) return androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1.0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    return infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
}
