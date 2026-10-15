package com.nmiet.app.ui.screens.tutor

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
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
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.TutorChatMessage
import com.nmiet.app.domain.model.TutorMessage
import com.nmiet.app.domain.model.TutorSender
import com.nmiet.app.domain.model.decodeBase64ToBitmap
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
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val attachedImage by viewModel.attachedImage.collectAsState()
    val isRecording by viewModel.isRecordingVoice.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isGeneratingVisual by viewModel.isGeneratingVisual.collectAsState()
    val isScanningImage by viewModel.isScanningImage.collectAsState()
    val offlineNotice by viewModel.offlineNotice.collectAsState()
    val currentlySpeakingMessageId by viewModel.currentlySpeakingMessageId.collectAsState()
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val isTtsPaused by viewModel.isTtsPaused.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initTts(context)
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecognition(context)
        } else {
            viewModel.simulateVoiceInput()
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Dynamic Screen Header
    val headerTitle = when (currentLanguage) {
        AppLanguage.ENGLISH_ONLY -> "AI Academic Tutor"
        AppLanguage.ODIA_ONLY -> "AI ଶିକ୍ଷା ସାଥୀ"
        AppLanguage.HINDI_ONLY -> "AI अध्ययन शिक्षक"
        AppLanguage.BILINGUAL_EN_HI, AppLanguage.BILINGUAL_EN_OR -> "Bilingual AI Tutor"
    }

    // One-Tap Suggestion Chips based on Language Mode
    val sampleQueries = when (currentLanguage) {
        AppLanguage.ENGLISH_ONLY -> listOf(
            "Explain Photosynthesis",
            "Where does Oxygen come from in photosynthesis?"
        )
        AppLanguage.ODIA_ONLY -> listOf(
            "ଆଲୋକ ଶ୍ଳେଷଣ କ’ଣ ବୁଝାନ୍ତୁ",
            "ଆଲୋକ ଶ୍ଳେଷଣରେ ଅମ୍ଳଜାନ କେଉଁଠାରୁ ଆସେ?"
        )
        AppLanguage.HINDI_ONLY -> listOf(
            "प्रकाश संश्लेषण समझाइए",
            "प्रकाश संश्लेषण में ऑक्सीजन कहाँ से निकलती है?"
        )
        AppLanguage.BILINGUAL_EN_HI -> listOf(
            "Explain Photosynthesis",
            "प्रकाश संश्लेषण में ऑक्सीजन कहाँ से निकलती है?"
        )
        AppLanguage.BILINGUAL_EN_OR -> listOf(
            "ଆଲୋକ ଶ୍ଳେଷଣ କ’ଣ ବୁଝାନ୍ତୁ",
            "Where does Oxygen come from in photosynthesis?"
        )
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopBar(
                title = headerTitle,
                isOnline = syncStatus.isOnline
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding(), bottom = 2.dp)
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
                            "Online Mode: [Cloud AI] Academic Solver & Smart Visuals Active"
                        else
                            "Offline Mode: [Offline Grounded] using local Room SQLite FTS cache",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (syncStatus.isOnline) PrimaryBlue else WarningAmber
                    )
                }
            }

            // Offline Notification Banner for Photo Scan / Visual Gen
            AnimatedVisibility(
                visible = offlineNotice != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (offlineNotice != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = offlineNotice!!,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f),
                                lineHeight = 16.sp
                            )
                            IconButton(
                                onClick = { viewModel.clearOfflineNotice() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Chat Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    TutorMessageCard(
                        message = msg,
                        isCurrentlySpeaking = (currentlySpeakingMessageId == msg.id && isTtsSpeaking),
                        isCurrentMessagePaused = (currentlySpeakingMessageId == msg.id && isTtsPaused),
                        onToggleSpeak = { text -> viewModel.toggleSpeakMessage(msg.id, text, context) }
                    )
                }

                if (isGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isGeneratingVisual -> "Synthesizing textbook diagram with Smart Visuals..."
                                    isScanningImage -> "Scanning textbook problem and extracting equations..."
                                    else -> "RelayNet AI Tutor is solving with academic grounding..."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Animated Audio-Wave indicator with smooth visibility transition to prevent content jump
            AnimatedVisibility(
                visible = isRecording,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                VoiceWaveformIndicator()
            }

            // Floating TTS Audio Control Bar (with Pause / Resume / Stop options)
            AnimatedVisibility(
                visible = isTtsSpeaking || isTtsPaused,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (isTtsSpeaking) Icons.Default.VolumeUp else Icons.Default.Pause,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTtsSpeaking) "Tutor reading answer aloud..." else "Tutor voice paused",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (isTtsSpeaking) {
                                        viewModel.pauseTts()
                                    } else {
                                        val activeMsg = messages.find { it.id == currentlySpeakingMessageId }
                                        val text = activeMsg?.text ?: ""
                                        viewModel.speakResponse(text, context, currentlySpeakingMessageId)
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTtsSpeaking) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isTtsSpeaking) "Pause" else "Resume",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.stopTts() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // One-tap suggestion chips
            val attachChipLabel = when (currentLanguage) {
                AppLanguage.ENGLISH_ONLY -> "📸 Attach Diagram"
                AppLanguage.ODIA_ONLY -> "📸 ଚିତ୍ର ସଂଲଗ୍ନ କରନ୍ତୁ"
                AppLanguage.HINDI_ONLY -> "📸 चित्र संलग्न करें"
                AppLanguage.BILINGUAL_EN_HI -> "📸 Attach Diagram (चित्र)"
                AppLanguage.BILINGUAL_EN_OR -> "📸 Attach Diagram (ଚିତ୍ର)"
            }
            val drawChipLabel = when (currentLanguage) {
                AppLanguage.ENGLISH_ONLY -> "🎨 Draw Photosynthesis Diagram"
                AppLanguage.ODIA_ONLY -> "🎨 ଆଲୋକ ଶ୍ଳେଷଣ ଚିତ୍ର ଆଙ୍କନ୍ତୁ"
                AppLanguage.HINDI_ONLY -> "🎨 प्रकाश संश्लेषण चित्र बनाएं"
                AppLanguage.BILINGUAL_EN_HI -> "🎨 Draw Diagram (चित्र)"
                AppLanguage.BILINGUAL_EN_OR -> "🎨 Draw Diagram (ଚିତ୍ର)"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Attach Textbook Photo Chip
                SuggestionChip(
                    onClick = { viewModel.attachSampleTextbookPhoto() },
                    label = { Text(attachChipLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue) }
                )

                // Scan, Draw, & Solve Visual Synthesis Chip
                SuggestionChip(
                    onClick = {
                        val drawPrompt = when (currentLanguage) {
                            AppLanguage.ENGLISH_ONLY -> "Draw a labeled scientific diagram of Photosynthesis light reactions and chloroplast structure"
                            AppLanguage.ODIA_ONLY -> "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା ଏବଂ ହରିତକଣାର ଚିତ୍ର ଆଙ୍କନ୍ତୁ"
                            AppLanguage.HINDI_ONLY -> "प्रकाश संश्लेषण प्रक्रिया और हरितलवक का नामांकित चित्र बनाएं"
                            else -> "Draw a labeled scientific diagram of Photosynthesis light reactions and chloroplast structure"
                        }
                        viewModel.requestVisualDiagram(drawPrompt)
                    },
                    label = { Text(drawChipLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan) }
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
                onMicClick = {
                    val hasAudioPerm = androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.RECORD_AUDIO
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    if (hasAudioPerm) {
                        viewModel.toggleVoiceRecognition(context)
                    } else {
                        audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    }
                },
                onSendClick = { viewModel.sendMessage() },
                isGenerating = isGenerating
            )
        }
    }
}

@Composable
fun TutorMessageCard(
    message: TutorChatMessage,
    isCurrentlySpeaking: Boolean = false,
    isCurrentMessagePaused: Boolean = false,
    onToggleSpeak: ((String) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (message.isFromUser) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (message.isFromUser) "You" else "RelayNet AI Tutor",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!message.isFromUser && onToggleSpeak != null && !message.text.isNullOrBlank()) {
                        IconButton(
                            onClick = { onToggleSpeak(message.text) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    isCurrentlySpeaking -> Icons.Default.Pause
                                    isCurrentMessagePaused -> Icons.Default.PlayArrow
                                    else -> Icons.Default.VolumeUp
                                },
                                contentDescription = if (isCurrentlySpeaking) "Pause Voice" else "Listen to Tutor",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = message.sourceTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User Uploaded Photo (if present)
            message.imageBitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "User submitted photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Generated Diagram (if present)
            message.generatedDiagramBase64?.let { b64 ->
                val bitmap = remember(b64) { decodeBase64ToBitmap(b64) }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Generated Diagram",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Message Text (Guaranteed visible styling)
            message.text?.let { textContent ->
                Text(
                    text = textContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun TutorMessageCard(message: TutorChatMessage) {
    TutorMessageCard(message = message, isCurrentlySpeaking = false, isCurrentMessagePaused = false, onToggleSpeak = null)
}

@Composable
fun TutorMessageBubble(
    message: TutorMessage,
    isOnlineContext: Boolean,
    currentLanguage: AppLanguage = AppLanguage.BILINGUAL_EN_OR,
    isCurrentlySpeaking: Boolean = false,
    isCurrentMessagePaused: Boolean = false,
    onToggleSpeak: ((String) -> Unit)? = null,
    isScanningActive: Boolean = false
) {
    val isUser = message.sender == TutorSender.USER

    val primaryText = if (isUser) {
        message.textEnglish.ifBlank { message.textOdia }
    } else {
        when (currentLanguage) {
            AppLanguage.ENGLISH_ONLY -> message.textEnglish
            AppLanguage.ODIA_ONLY -> message.textOdia.ifBlank { message.textEnglish }
            AppLanguage.HINDI_ONLY -> if (message.textHindi.isNotBlank()) message.textHindi else message.textEnglish
            AppLanguage.BILINGUAL_EN_HI -> if (message.textHindi.isNotBlank()) message.textHindi else message.textEnglish
            AppLanguage.BILINGUAL_EN_OR -> message.textOdia.ifBlank { message.textEnglish }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        // Red alert card if query was out of syllabus & rejected by academic guardrail
        if (!message.isStudyRelated) {
            val guardrailTitle = when (currentLanguage) {
                AppLanguage.ENGLISH_ONLY -> "Curriculum Guardrail Notice"
                AppLanguage.ODIA_ONLY -> "ପାଠ୍ୟକ୍ରମ ସୁରକ୍ଷା ନିୟମ"
                AppLanguage.HINDI_ONLY -> "पाठ्यक्रम सुरक्षा नियम"
                AppLanguage.BILINGUAL_EN_HI -> "Curriculum Guardrail (पाठ्यक्रम सुरक्षा)"
                AppLanguage.BILINGUAL_EN_OR -> "Curriculum Guardrail (ପାଠ୍ୟକ୍ରମ ସୁରକ୍ଷା)"
            }
            val guardrailMsg = when {
                message.textEnglish.isNotBlank() && (message.textEnglish.contains("assistant") || message.textEnglish.contains("sketch")) -> message.textEnglish
                message.textOdia.isNotBlank() && message.textOdia.contains("ଶିକ୍ଷା") -> message.textOdia
                currentLanguage == AppLanguage.ODIA_ONLY -> "ଜଣେ ଶିକ୍ଷା ସାଥୀ ଭାବରେ, ମୁଁ ପାଠ୍ୟକ୍ରମ ବାହାରର ଚିତ୍ର ତିଆରି କରିପାରିବି ନାହିଁ। ଆସନ୍ତୁ ବିଜ୍ଞାନ ଏବଂ ଗଣିତ ବିଷୟ ଉପରେ ଧ୍ୟାନ ଦେବା!"
                currentLanguage == AppLanguage.HINDI_ONLY -> "एक सहायक के रूप में, मैं गैर-पाठ्यचर्या संबंधी चित्र नहीं बना सकता। आइए विज्ञान और गणित पर ध्यान दें!"
                else -> "As your assistant, I cannot sketch non-curricular things. Let's focus on Science & Math!"
            }
            Card(
                modifier = Modifier.fillMaxWidth(0.92f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = guardrailTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = guardrailMsg,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        lineHeight = 20.sp
                    )
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

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onToggleSpeak != null) {
                                IconButton(
                                    onClick = { onToggleSpeak(primaryText) },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isCurrentlySpeaking) PrimaryBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isCurrentlySpeaking -> Icons.Default.Pause
                                            isCurrentMessagePaused -> Icons.Default.PlayArrow
                                            else -> Icons.Default.VolumeUp
                                        },
                                        contentDescription = if (isCurrentlySpeaking) "Pause Voice" else "Listen to Tutor",
                                        tint = if (isCurrentlySpeaking) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            // Origin Badge: [Cloud AI] Smart Visuals vs [Cloud AI] Academic Solver vs [Offline Grounded] vs [Cloud AI] RelayNet Tutor
                            val isOfflineGrounded = message.isOfflineGrounded || !isOnlineContext
                            val badgeText = when {
                                !message.imageBase64.isNullOrBlank() -> "[Cloud AI] Smart Visuals"
                                message.isNemotronReasoning -> "[Cloud AI] Academic Solver"
                                isOfflineGrounded -> "[Offline Grounded]"
                                else -> "[Cloud AI] RelayNet Tutor"
                            }
                            val badgeBg = when {
                                !message.imageBase64.isNullOrBlank() -> AccentCyan.copy(alpha = 0.2f)
                                message.isNemotronReasoning -> Color(0xFF6B21A8).copy(alpha = 0.15f)
                                isOfflineGrounded -> SuccessGreen.copy(alpha = 0.15f)
                                else -> PrimaryBlue.copy(alpha = 0.12f)
                            }
                            val badgeTextColor = when {
                                !message.imageBase64.isNullOrBlank() -> PrimaryBlue
                                message.isNemotronReasoning -> Color(0xFF9333EA)
                                isOfflineGrounded -> SuccessGreen
                                else -> PrimaryBlue
                            }

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
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // If user attached an image, render preview inside bubble
                if (isUser && message.imageBitmap != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            bitmap = message.imageBitmap.asImageBitmap(),
                            contentDescription = "User submitted photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        if (isScanningActive) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.72f))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Scanning textbook problem and extracting equations...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // If tutor returned a synthesized educational diagram (FLUX.2 Klein 16:9 illustration or Base64)
                val tutorImageBitmap = remember(message.imageBase64, message.imageBitmap) {
                    if (!isUser) {
                        if (message.imageBitmap != null) {
                            message.imageBitmap
                        } else if (!message.imageBase64.isNullOrBlank()) {
                            try {
                                val cleanB64 = message.imageBase64.substringAfter("base64,")
                                val decodedBytes = android.util.Base64.decode(cleanB64, android.util.Base64.DEFAULT)
                                android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            } catch (_: Exception) {
                                null
                            }
                        } else null
                    } else null
                }

                if (!isUser && tutorImageBitmap != null) {
                    var showFullScreenZoom by remember { mutableStateOf(false) }

                    // High-quality 16:9 diagram card with full-screen zoom trigger
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                    .background(Color.White)
                            ) {
                            Image(
                                bitmap = tutorImageBitmap.asImageBitmap(),
                                contentDescription = "Generated Educational Diagram",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { showFullScreenZoom = true },
                                contentScale = ContentScale.Fit
                            )

                            // Zoom-in full-screen trigger overlay badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .clickable { showFullScreenZoom = true }
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Zoom Diagram Full Screen",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Explanatory label below diagram card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PrimaryBlue.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Synthesized Textbook Diagram (Smart Visuals • 16:9 HD)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }

                    // Full-screen Zoom Dialog with interactive pinch-to-zoom
                    if (showFullScreenZoom) {
                        FullScreenZoomableImageDialog(
                            bitmap = tutorImageBitmap,
                            title = "Smart Visuals Textbook Diagram",
                            onDismiss = { showFullScreenZoom = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                val secondaryText = if (!isUser) {
                    when (currentLanguage) {
                        AppLanguage.BILINGUAL_EN_HI -> if (message.textHindi.isNotBlank() && message.textEnglish.isNotBlank()) message.textEnglish else null
                        AppLanguage.BILINGUAL_EN_OR -> if (message.textOdia.isNotBlank() && message.textOdia != message.textEnglish) message.textEnglish else null
                        else -> null
                    }
                } else null

                // Primary Message Text
                Text(
                    text = primaryText,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // English text translation block beneath for Bilingual modes
                if (secondaryText != null) {
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
                                text = secondaryText,
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

@Composable
fun FullScreenZoomableImageDialog(
    bitmap: Bitmap,
    title: String = "Textbook Diagram (16:9 HD)",
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 5f)
            val maxOffset = 500f * (scale - 1f)
            offset = Offset(
                x = (offset.x + panChange.x).coerceIn(-maxOffset, maxOffset),
                y = (offset.y + panChange.y).coerceIn(-maxOffset, maxOffset)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Pinch to zoom (1x - 5x) • Drag to pan",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (scale > 1.05f) {
                        TextButton(onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        }) {
                            Text("Reset Zoom", color = AccentCyan)
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 70.dp, horizontal = 12.dp)
                    .transformable(state = transformableState),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Full Screen Visual Blueprint",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
