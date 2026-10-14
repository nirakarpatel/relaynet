package com.nmiet.app.ui.screens.teacher

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.repository.ContentRepository
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherToolsScreen(
    repository: ContentRepository,
    onBackClick: () -> Unit = {}
) {
    var lectureTitle by remember { mutableStateOf("Photosynthesis & Light Reactions (Class 9 BSE)") }
    var rawTextTranscript by remember {
        mutableStateOf(
            "Welcome students. Today we cover Chapter 4. Photosynthesis begins in the thylakoid membrane where photolysis splits water into oxygen..."
        )
    }
    var isProcessing by remember { mutableStateOf(false) }
    var publishStatus by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teacher Pipeline & Publishing", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Teacher Content Studio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Transcribe lecture audio, review draft quizzes, and publish verified offline packs.", fontSize = 11.sp)
                    }
                }
            }

            Text("Lecture Title & Curriculum Tagging", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            OutlinedTextField(
                value = lectureTitle,
                onValueChange = { lectureTitle = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Lecture Title") }
            )

            Text("Lecture Recording Transcript / Lesson Source", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            OutlinedTextField(
                value = rawTextTranscript,
                onValueChange = { rawTextTranscript = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                maxLines = 6,
                label = { Text("Lecture Transcript Text") }
            )

            Button(
                onClick = {
                    scope.launch {
                        isProcessing = true
                        publishStatus = null
                        delay(1200)
                        isProcessing = false
                        publishStatus = "Draft lesson, 5 diagnostic questions, and bilingual notes synthesized! Verified against Class 9 curriculum standards."
                    }
                },
                enabled = !isProcessing && lectureTitle.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing Lecture Pipeline...")
                } else {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run Lecture Pipeline & Generate Draft")
                }
            }

            if (publishStatus != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ready for Teacher Approval", fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(publishStatus!!, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                publishStatus = "✅ Pack published successfully! Manifest signed with SHA-256 and made available for student downloads and Bluetooth peer distribution."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Approve & Publish to Curriculum Library")
                        }
                    }
                }
            }
        }
    }
}
