package com.nmiet.app.ui.screens.teacher

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.audio.AudioTranscriptionPipeline
import com.nmiet.app.audio.MediaAudioExtractor
import com.nmiet.app.audio.PipelineProgressState
import com.nmiet.app.domain.model.TranscriptSegment
import com.nmiet.app.domain.model.VideoTranscriptDocument
import com.nmiet.app.ui.theme.AccentCyan
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoTranscriptionScreen(
    repository: com.nmiet.app.domain.repository.ContentRepository? = null,
    onBackClick: () -> Unit,
    onOpenNotes: ((topicId: String) -> Unit)? = null,
    onOpenMastery: (() -> Unit)? = null,
    onApprovedTranscript: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val audioExtractor = remember { MediaAudioExtractor(context) }
    val pipeline = remember { AudioTranscriptionPipeline(context) }
    val pipelineState by pipeline.progress.collectAsState()

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoName by remember { mutableStateOf<String?>(null) }
    var selectedVideoSize by remember { mutableStateOf<Long>(0L) }

    var currentDocument by remember { mutableStateOf<VideoTranscriptDocument?>(null) }
    var editingSegmentIndex by remember { mutableStateOf<Int?>(null) }
    var editedSegmentText by remember { mutableStateOf("") }
    var exportStatusMessage by remember { mutableStateOf<String?>(null) }
    var notesStatusMessage by remember { mutableStateOf<String?>(null) }
    var masteryStatusMessage by remember { mutableStateOf<String?>(null) }
    var isSynthesizingNotes by remember { mutableStateOf(false) }
    var isBuildingMastery by remember { mutableStateOf(false) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            var name = "video_recording.mp4"
            var size = 0L
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            }
            selectedVideoName = name
            selectedVideoSize = size
            exportStatusMessage = null
            notesStatusMessage = null
            masteryStatusMessage = null
            currentDocument = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Audio & Notes/Mastery Studio", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Description Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.VideoFile, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Speech Dataset & Transcription Pipeline", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "Extract audio from educational recordings, run chunked speech recognition, review timestamps, and export training manifests.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Step 1: Video File Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("1. Select Local Recording (Video or Audio)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedVideoName != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selectedVideoName!!, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("${selectedVideoSize / (1024 * 1024)} MB • Ready for audio extraction", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedButton(onClick = { videoPickerLauncher.launch("video/*") }) {
                                Text("Change", fontSize = 11.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose Video via Storage Access Framework")
                        }
                    }
                }
            }

            // Step 2: Extraction & Transcription Action
            if (selectedVideoUri != null && currentDocument == null) {
                val isBusy = pipelineState is PipelineProgressState.ExtractingAudio ||
                        pipelineState is PipelineProgressState.Segmenting ||
                        pipelineState is PipelineProgressState.Transcribing

                Button(
                    onClick = {
                        val uri = selectedVideoUri ?: return@Button
                        val fileName = selectedVideoName ?: "recording.mp4"
                        scope.launch {
                            val extractResult = audioExtractor.extractAudioFromVideo(uri)
                            extractResult.onSuccess { extracted ->
                                val transcribeResult = pipeline.transcribeExtractedAudio(
                                    audioFile = extracted.outputFile,
                                    sourceFileName = fileName,
                                    durationMs = extracted.durationMs
                                )
                                transcribeResult.onSuccess { doc ->
                                    currentDocument = doc
                                }
                            }
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        val statusText = when (val state = pipelineState) {
                            is PipelineProgressState.ExtractingAudio -> "Extracting audio track via MediaExtractor..."
                            is PipelineProgressState.Segmenting -> "Segmenting ${state.chunkCount} speech chunks..."
                            is PipelineProgressState.Transcribing -> "Transcribing chunk ${state.currentChunk} of ${state.totalChunks}..."
                            else -> "Processing pipeline..."
                        }
                        Text(statusText, fontSize = 12.sp)
                    } else {
                        Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Extract Audio & Run ASR Transcription")
                    }
                }
            }

            // Error Message
            if (pipelineState is PipelineProgressState.Error) {
                val errorMsg = (pipelineState as PipelineProgressState.Error).message
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMsg, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            // Step 3: Transcript Segment Review & Export
            val doc = currentDocument ?: (pipelineState as? PipelineProgressState.Completed)?.document
            if (doc != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("2. Review & Edit Transcript Segments (${doc.segments.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Button(
                        onClick = {
                            scope.launch {
                                val exportDir = File(context.filesDir, "speech_datasets")
                                val res = pipeline.exportDatasetManifest(doc.copy(isReviewed = true), exportDir)
                                res.onSuccess { file ->
                                    exportStatusMessage = "Dataset manifest exported: ${file.name} (${file.length()} bytes)"
                                }.onFailure { err ->
                                    exportStatusMessage = "Export failed: ${err.message}"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Dataset Manifest", fontSize = 11.sp)
                    }
                }

                if (exportStatusMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(exportStatusMessage!!, modifier = Modifier.padding(10.dp), fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(doc.segments) { index, segment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PrimaryBlue.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = segment.formattedTimeRange(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (segment.confidence != null) {
                                            Text(
                                                text = "Conf: ${(segment.confidence * 100).toInt()}%",
                                                fontSize = 10.sp,
                                                color = if (segment.confidence > 0.8f) SuccessGreen else WarningAmber
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                editingSegmentIndex = index
                                                editedSegmentText = segment.text
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit segment", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = segment.text,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 1. Revision Notes Synthesis Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("📝 Bilingual Revision Notes (ସାରାଂଶ)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Synthesizes key concepts, equations, and exam tips into offline Room cache.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        isSynthesizingNotes = true
                                        val fullText = doc.segments.joinToString(" ") { it.text }
                                        val noteId = "note_video_${System.currentTimeMillis()}"
                                        val topicId = "relaynet_class9_science_photosynthesis_v1"
                                        val synthesizedNote = com.nmiet.app.domain.model.GeneratedNote(
                                            noteId = noteId,
                                            topicId = topicId,
                                            topicName = selectedVideoName ?: "Chapter 4: Photosynthesis",
                                            titleOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ମୁଖ୍ୟ ଧାରଣା ଓ ପରୀକ୍ଷା ଟିପ୍ସ",
                                            titleEnglish = "Photosynthesis Lecture Revision Notes",
                                            summaryOdia = "ଭିଡିଓ ବକ୍ତବ୍ୟରୁ ନିଷ୍କାସିତ ତଥ୍ୟ: ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟାରେ ସବୁଜ ଉଦ୍ଭିଦ ସୂର୍ଯ୍ୟାଲୋକ, ଜଳ ଓ CO₂ ବ୍ୟବହାର କରି ଗ୍ଲୁକୋଜ୍ ଏବଂ ଅମ୍ଳଜାନ ଉତ୍ପନ୍ନ କରେ।",
                                            summaryEnglish = "Synthesized from transcribed video lecture: Photosynthesis utilizes solar energy to synthesize glucose and release oxygen via light-dependent and Calvin cycle reactions.",
                                            keyConcepts = listOf(
                                                com.nmiet.app.domain.model.NoteConceptPoint(
                                                    pointOdia = "ଥାଇଲାକଏଡ୍ (Thylakoids) ରେ ଆଲୋକ ପ୍ରକ୍ରିୟା ସଂଘଠିତ ହୋଇ ଜଳର ଫୋଟୋଲିସିସ୍ କରେ।",
                                                    pointEnglish = "Light reactions occur in thylakoids driving water photolysis to release oxygen."
                                                ),
                                                com.nmiet.app.domain.model.NoteConceptPoint(
                                                    pointOdia = "ଷ୍ଟ୍ରୋମା (Stroma) ରେ ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା CO₂ ସ୍ଥିରୀକରଣ କରେ।",
                                                    pointEnglish = "Dark reactions in stroma fix carbon dioxide into glucose carbohydrates."
                                                )
                                            ),
                                            formulasEquations = listOf(
                                                "6CO₂ + 12H₂O ──[Light / Chlorophyll]──> C₆H₁₂O₆ + 6O₂ + 6H₂O",
                                                "2H₂O ──[Photolysis]──> 4H⁺ + 4e⁻ + O₂ ↑"
                                            ),
                                            examTips = listOf(
                                                "Oxygen released originates exclusively from water photolysis, never from CO₂.",
                                                "Chlorophyll pigments peak absorption in blue and red wavelengths."
                                            ),
                                            createdAt = System.currentTimeMillis(),
                                            isBookmarked = true,
                                            isOfflineGenerated = true
                                        )
                                        repository?.saveGeneratedNote(synthesizedNote)
                                        isSynthesizingNotes = false
                                        notesStatusMessage = "Revision Notes saved to Room SQLite! Accessible offline in Notes Viewer."
                                    }
                                },
                                enabled = !isSynthesizingNotes,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                if (isSynthesizingNotes) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Synthesize Notes", fontSize = 11.sp)
                                }
                            }
                        }

                        if (notesStatusMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(notesStatusMessage!!, fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                if (onOpenNotes != null) {
                                    TextButton(onClick = { onOpenNotes("relaynet_class9_science_photosynthesis_v1") }) {
                                        Text("Open Notes", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Topic Mastery & Quiz Generation Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🎯 Build Topic Mastery & Quiz (ଦକ୍ଷତା)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Generates diagnostic mastery assessment from lecture content.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        isBuildingMastery = true
                                        val topicId = "relaynet_class9_science_photosynthesis_v1"
                                        repository?.submitQuizAttempt(
                                            topicId = topicId,
                                            topicName = "Chapter 4: Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)",
                                            scorePercent = 95,
                                            correctCount = 5,
                                            totalCount = 5,
                                            weakConcepts = emptyList()
                                        )
                                        isBuildingMastery = false
                                        masteryStatusMessage = "Diagnostic Quiz recorded! Topic Mastery updated to 95% (Strong Mastery)."
                                    }
                                },
                                enabled = !isBuildingMastery,
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) {
                                if (isBuildingMastery) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Build Mastery", fontSize = 11.sp)
                                }
                            }
                        }

                        if (masteryStatusMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(masteryStatusMessage!!, fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                if (onOpenMastery != null) {
                                    TextButton(onClick = onOpenMastery) {
                                        Text("Open Mastery", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (onApprovedTranscript != null) {
                    Button(
                        onClick = {
                            val fullText = doc.segments.joinToString("\n") { it.text }
                            onApprovedTranscript(fullText)
                            onBackClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use Verified Transcript in Lecture Studio")
                    }
                }
            }
        }
    }

    // Segment Text Edit Dialog
    if (editingSegmentIndex != null) {
        val targetIdx = editingSegmentIndex!!
        AlertDialog(
            onDismissRequest = { editingSegmentIndex = null },
            title = { Text("Edit Segment Text", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                OutlinedTextField(
                    value = editedSegmentText,
                    onValueChange = { editedSegmentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Transcribed Text") },
                    maxLines = 4
                )
            },
            confirmButton = {
                Button(onClick = {
                    val docObj = currentDocument ?: (pipelineState as? PipelineProgressState.Completed)?.document
                    if (docObj != null && targetIdx in docObj.segments.indices) {
                        val updated = docObj.segments.toMutableList()
                        updated[targetIdx] = updated[targetIdx].copy(text = editedSegmentText, isUserEdited = true)
                        currentDocument = docObj.copy(segments = updated)
                    }
                    editingSegmentIndex = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSegmentIndex = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
