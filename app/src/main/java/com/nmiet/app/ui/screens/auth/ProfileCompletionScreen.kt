package com.nmiet.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.nmiet.app.R
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.Board
import com.nmiet.app.ui.theme.LightBackground
import com.nmiet.app.ui.theme.LightSurface
import com.nmiet.app.ui.theme.MeshCardBorder
import com.nmiet.app.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCompletionScreen(
    viewModel: AuthViewModel,
    onProfileCompleted: () -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current

    val name by viewModel.name.collectAsState()
    val selectedGrade by viewModel.selectedGrade.collectAsState()
    val selectedBoard by viewModel.selectedBoard.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val schoolName by viewModel.schoolName.collectAsState()
    val selectedInterests by viewModel.selectedInterests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val availableInterests = listOf(
        "Photosynthesis",
        "Cell Biology",
        "Plant Physiology",
        "Physics & Motion",
        "Chemistry & Acids",
        "Mathematics",
        "Diagram Solvers",
        "Odia Science Quizzes"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBackground
                )
            )
        },
        containerColor = LightBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Small Brand Label & Progress Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryBlue.copy(alpha = 0.12f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_relaynet_logo),
                            contentDescription = "RelayNet",
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RelayNet Profile Setup",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Avatar Placeholder with Student Initial
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.12f))
                    .border(2.dp, PrimaryBlue.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initial = if (name.isNotBlank()) name.trim().first().uppercase() else "R"
                Text(
                    text = initial,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Heading
            Text(
                text = "Let's make it yours",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202B3D),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 4. Short supporting text
            Text(
                text = "Personalize your learning syllabus, grade, and preferred language.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error Banner
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.dismissError() },
                            modifier = Modifier.size(24.dp)
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
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 5. Full Name Field (Required)
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Full Name * (ଛାତ୍ରଙ୍କ ସମ୍ପୂର୍ଣ୍ଣ ନାମ)") },
                placeholder = { Text("e.g. Soumya Ranjan Sahoo") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryBlue
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MeshCardBorder,
                    focusedContainerColor = LightSurface,
                    unfocusedContainerColor = LightSurface
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Class / Grade Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Class / Grade Level *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF202B3D)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(6, 7, 8, 9, 10).forEach { grade ->
                        FilterChip(
                            selected = selectedGrade == grade,
                            onClick = { viewModel.onGradeChange(grade) },
                            label = { Text("Class $grade", fontWeight = FontWeight.Medium) },
                            leadingIcon = if (selectedGrade == grade) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 7. Education Board Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Educational Board *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF202B3D)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Board.ODISHA_STATE to "Odisha State (BSE)",
                        Board.CBSE to "CBSE",
                        Board.CISCE_ICSE to "ICSE"
                    ).forEach { (b, label) ->
                        FilterChip(
                            selected = selectedBoard == b,
                            onClick = { viewModel.onBoardChange(b) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            leadingIcon = if (selectedBoard == b) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 8. Preferred Language Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Preferred Language *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF202B3D)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        AppLanguage.BILINGUAL_EN_OR to "Bilingual (English + ଓଡ଼ିଆ)",
                        AppLanguage.ODIA_ONLY to "ଓଡ଼ିଆ (Odia Medium)",
                        AppLanguage.ENGLISH_ONLY to "English",
                        AppLanguage.BILINGUAL_EN_HI to "Bilingual (English + हिंदी)",
                        AppLanguage.HINDI_ONLY to "हिंदी"
                    ).forEach { (lang, label) ->
                        FilterChip(
                            selected = selectedLanguage == lang,
                            onClick = { viewModel.onLanguageChange(lang) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            leadingIcon = if (selectedLanguage == lang) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 9. Optional School / College Field
            OutlinedTextField(
                value = schoolName,
                onValueChange = { viewModel.onSchoolChange(it) },
                label = { Text("School / College Name (Optional)") },
                placeholder = { Text("e.g. Govt High School, Dhenkanal") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = PrimaryBlue
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MeshCardBorder,
                    focusedContainerColor = LightSurface,
                    unfocusedContainerColor = LightSurface
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 10. Optional Learning Interest Chips
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Learning Interests (Optional)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF202B3D)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableInterests.forEach { interest ->
                        val isSelected = selectedInterests.contains(interest)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleInterest(interest) },
                            label = { Text(interest, fontSize = 11.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 11. Full-width Primary Blue CTA Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.saveCompletedProfile(onSuccess = onProfileCompleted)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Start Learning",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 12. Note that details can be updated later
            Text(
                text = "You can update syllabus, board, and medium anytime in your profile settings.",
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
