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
import com.nmiet.app.domain.model.AppLanguage
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
    val currentLanguage by viewModel.currentLanguage.collectAsState()
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
                    TutorMessageBubble(
                        message = msg,
                        isOnlineContext = syncStatus.isOnline,
                        currentLanguage = currentLanguage
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
            val attachChipLabel = when (currentLanguage) {
                AppLanguage.ENGLISH_ONLY -> "📸 Attach Diagram"
                AppLanguage.ODIA_ONLY -> "📸 ଚିତ୍ର ସଂଲଗ୍ନ କରନ୍ତୁ"
                AppLanguage.HINDI_ONLY -> "📸 चित्र संलग्न करें"
                AppLanguage.BILINGUAL_EN_HI -> "📸 Attach Diagram (चित्र)"
                AppLanguage.BILINGUAL_EN_OR -> "📸 Attach Diagram (ଚିତ୍ର)"
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SuggestionChip(
                    onClick = { viewModel.attachSampleTextbookPhoto() },
                    label = { Text(attachChipLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue) }
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
fun TutorMessageBubble(
    message: TutorMessage,
    isOnlineContext: Boolean,
    currentLanguage: AppLanguage = AppLanguage.BILINGUAL_EN_OR
) {
    val isUser = message.sender == TutorSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        // Amber warning card if query was out of syllabus & rejected by guardrail
        if (!message.isStudyRelated) {
            val guardrailTitle = when (currentLanguage) {
                AppLanguage.ENGLISH_ONLY -> "Study-Only Guardrail Triggered"
                AppLanguage.ODIA_ONLY -> "କେବଳ ପାଠ୍ୟକ୍ରମ ସୁରକ୍ଷା ନିୟମ"
                AppLanguage.HINDI_ONLY -> "केवल अध्ययन सुरक्षा नियम"
                AppLanguage.BILINGUAL_EN_HI -> "Study Guardrail (अध्ययन सुरक्षा)"
                AppLanguage.BILINGUAL_EN_OR -> "Study Guardrail (ପାଠ୍ୟକ୍ରମ ସୁରକ୍ଷା)"
            }
            val guardrailMsg = when (currentLanguage) {
                AppLanguage.ENGLISH_ONLY -> "I can only answer academic curriculum questions."
                AppLanguage.ODIA_ONLY -> "ମୁଁ କେବଳ ପାଠପଢ଼ା ଏବଂ ପାଠ୍ୟକ୍ରମ ସମ୍ବନ୍ଧୀୟ ପ୍ରଶ୍ନର ଉତ୍ତର ଦେଇପାରିବି।"
                AppLanguage.HINDI_ONLY -> "मैं केवल शैक्षणिक पाठ्यक्रम से संबंधित प्रश्नों के उत्तर दे सकता हूँ।"
                AppLanguage.BILINGUAL_EN_HI -> "मैं केवल शैक्षणिक पाठ्यक्रम से संबंधित प्रश्नों के उत्तर दे सकता हूँ। (I can only answer academic curriculum questions)."
                AppLanguage.BILINGUAL_EN_OR -> "ମୁଁ କେବଳ ପାଠପଢ଼ା ଏବଂ ପାଠ୍ୟକ୍ରମ ସମ୍ବନ୍ଧୀୟ ପ୍ରଶ୍ନର ଉତ୍ତର ଦେଇପାରିବି। (I can only answer academic curriculum questions)."
            }
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
                            text = guardrailTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = guardrailMsg,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
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
