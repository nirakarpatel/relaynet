@file:OptIn(
    androidx.media3.common.util.UnstableApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.nmiet.app.ui.screens.lesson

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.nmiet.app.data.local.entities.BrainPackSeed
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.ui.theme.AccentCyan
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber
import java.io.File
 
@Composable
fun BrainVideoLessonScreen(
    onBackClick: () -> Unit,
    onTakeDiagnostics: (packId: String) -> Unit,
    onShareMesh: (packId: String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }

    // Offline-Resilient ExoPlayer Initialization
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            val localVideoFile = File(context.filesDir, BrainPackSeed.VIDEO_LOCAL_ASSET)
            val mediaItem = if (localVideoFile.exists() && localVideoFile.length() > 0) {
                MediaItem.fromUri(Uri.fromFile(localVideoFile))
            } else {
                MediaItem.fromUri("asset:///${BrainPackSeed.VIDEO_LOCAL_ASSET}")
            }
            try {
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = false
            } catch (_: Exception) {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "The Human Brain",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "ମାନବ ମସ୍ତିଷ୍କ: ମୁଖ୍ୟ ନିୟନ୍ତ୍ରଣ କେନ୍ଦ୍ର",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Action Floating Footer with persistent buttons
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onShareMesh(BrainPackSeed.PACK_ID) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share via ShikshaMesh", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }

                    Button(
                        onClick = { onTakeDiagnostics(BrainPackSeed.PACK_ID) },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Take Brain Diagnostics", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Embedded Video Player View with Offline Fallback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            setShowFastForwardButton(true)
                            setShowRewindButton(true)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Controls & Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dr. Binocs Biology Series", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SuccessGreen.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = "OFFLINE CACHED",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 2. Tab Navigation: Core Notes vs. Bilingual Transcript
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Core Notes & Flowchart", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bilingual Transcript", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Tab Content Panes
            when (selectedTab) {
                0 -> CoreNotesAndFlowchartTab()
                1 -> BilingualTranscriptTab()
            }
        }
    }
}

@Composable
private fun CoreNotesAndFlowchartTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Executive Summary Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Executive Summary: The Body's Master Boss", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "The human brain processes sensory inputs and coordinates both voluntary actions and involuntary survival rhythms faster than modern supercomputers. It produces 12–25 W of bioelectrical energy continuously.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Comparison Table: 4 Brain Structures
        Text("Anatomical Architecture & Functional Division", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        val structures = listOf(
            BrainStructureItem(
                name = "Cerebrum",
                proportion = "≈ 85% of total brain mass; folded hemispheres",
                functions = "Conscious thought, intelligence, memory, voluntary muscle locomotion (walking, dancing, reading).",
                dysfunction = "Inability to formulate thoughts, loss of voluntary movement control, cognitive failure.",
                color = PrimaryBlue
            ),
            BrainStructureItem(
                name = "Cerebellum",
                proportion = "Compact structure tucked beneath posterior cerebrum",
                functions = "Vestibular balance, equilibrium, posture, and fine motor coordination.",
                dysfunction = "Loss of balance, uncoordinated stumbling, ataxia (\"falling all over the place\").",
                color = AccentCyan
            ),
            BrainStructureItem(
                name = "Brainstem",
                proportion = "Columnar stalk connecting brain directly to spinal cord",
                functions = "Autonomic life support: cardiac rhythm, respiratory cadence, gastrointestinal peristalsis.",
                dysfunction = "Immediate cessation of involuntary life functions (breathing and heartbeat).",
                color = Color(0xFFD32F2F)
            ),
            BrainStructureItem(
                name = "Amygdala",
                proportion = "Small, almond-shaped subcortical limbic structure",
                functions = "Emotional arousal (fear, excitement), survival instincts (\"fight-or-flight\"), event memory.",
                dysfunction = "Impaired emotional regulation, loss of basic survival awareness.",
                color = Color(0xFF7B1FA2)
            )
        )

        structures.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = item.color)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = item.color.copy(alpha = 0.12f)
                        ) {
                            Text(item.proportion, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = item.color, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Primary Functions: ${item.functions}", fontSize = 12.sp, lineHeight = 17.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Observable Failure: ${item.dysfunction}",
                        fontSize = 11.sp,
                        color = Color(0xFFC62828),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 16:9 Diagram: Bioelectrical Output (12-25 W) & Exercise Neuroplasticity
        Text("Neurophysiology & Metabolic Activity Flowchart", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("NEURAL BIOELECTRICITY & PLASTICITY", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("12 – 25 WATT POWER", color = WarningAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Visual Representation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Wattage & LED Lightbulb
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(WarningAmber.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("12–25 Watts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Lights an LED Bulb", color = Color.LightGray, fontSize = 10.sp)
                        }

                        // Arrow
                        Text("⚡", fontSize = 20.sp)

                        // Right: Exercise & Neuroplasticity
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Aerobic Exercise", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("BDNF Synaptic Growth", color = Color.LightGray, fontSize = 10.sp)
                        }
                    }

                    // Bottom Flow Note
                    Text(
                        text = "Aerobic Exercise ──> BDNF Secretion ──> Synaptic Plasticity ──> Faster Problem Solving",
                        color = Color.Cyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BilingualTranscriptTab() {
    val transcriptItems = listOf(
        TranscriptPair(
            timeTag = "00:00 - 00:25",
            speaker = "Dr. Binocs",
            english = "Hello friends! Ever wondered who the real boss of your body is? It's your brain! Running faster than any supercomputer, it controls everything you think, feel, and do.",
            odia = "ନମସ୍କାର ସାଙ୍ଗମାନେ! କେବେ ଭାବିଛନ୍ତି କି ଆମ ଶରୀରର ପ୍ରକୃତ ବସ୍ କିଏ? ଏହା ହେଉଛି ଆମ ମସ୍ତିଷ୍କ! ଯେକୌଣସି ସୁପର କମ୍ପ୍ୟୁଟରଠାରୁ ଦ୍ରୁତ ଚାଲି ଏହା ଆମର ସମସ୍ତ ଚିନ୍ତା, ଭାବନା ଓ କାର୍ଯ୍ୟକୁ ନିୟନ୍ତ୍ରଣ କରେ।",
            hindi = "नमस्ते दोस्तों! क्या आपने कभी सोचा है कि आपके शरीर का असली बॉस कौन है? वह आपका दिमाग है! किसी भी सुपरकंप्यूटर से तेज चलकर यह आपके सोचने, महसूस करने और कार्य करने को नियंत्रित करता है।"
        ),
        TranscriptPair(
            timeTag = "00:26 - 00:55",
            speaker = "Dr. Binocs",
            english = "First up is the Cerebrum! It makes up about 85% of your brain's weight. It controls conscious thoughts, memories, learning mathematics, and voluntary movements like walking and dancing.",
            odia = "ପ୍ରଥମେ ଆସୁଛି ସେରେବ୍ରମ୍ (Cerebrum)! ଏହା ମସ୍ତିଷ୍କର ପ୍ରାୟ ୮୫% ଓଜନ ଗଠନ କରେ। ଏହା ଚିନ୍ତାଶକ୍ତି, ସ୍ମୃତି, ଗଣିତ ଶିଖିବା ଏବଂ ଚାଲିବା ଓ ନାଚିବା ଭଳି ଐଚ୍ଛିକ ଗତିବିଧିକୁ ପରିଚାଳନା କରେ।",
            hindi = "सबसे पहले है सेरेब्रम! यह मस्तिष्क के वजन का लगभग 85% बनाता है। यह सचेत सोच, यादें, पढ़ाई और चलने व नाचने जैसी ऐच्छिक गतियों को नियंत्रित करता है।"
        ),
        TranscriptPair(
            timeTag = "00:56 - 01:25",
            speaker = "Dr. Binocs",
            english = "Tucked right beneath the cerebrum is the Cerebellum! It is small but mighty—it coordinates balance and posture. Without it, you wouldn't be able to stand upright or coordinate smooth motions without falling over!",
            odia = "ସେରେବ୍ରମ୍ ର ଠିକ୍ ତଳେ ଅଛି ସେରେବେଲମ୍ (Cerebellum)! ଏହା ଆକାରରେ ଛୋଟ ମାତ୍ର ଭାରସାମ୍ୟ ଓ ଶାରୀରିକ ସନ୍ତୁଳନ ରକ୍ଷା କରେ। ଏହା ବିନା ଆପଣ ସିଧା ଠିଆ ହୋଇପାରିବେ ନାହିଁ ଏବଂ ବାରମ୍ବାର ଝୁଣ୍ଟି ପଡିବେ।",
            hindi = "सेरेब्रम के ठीक नीचे सेरेबेलम है! यह छोटा है किंतु संतुलन और मुद्रा को नियंत्रित करता है। इसके बिना आप सीधे खड़े नहीं हो सकते और बार-बार गिर पड़ेंगे।"
        ),
        TranscriptPair(
            timeTag = "01:26 - 01:50",
            speaker = "Dr. Binocs",
            english = "Connecting your brain to the spinal cord is the Brainstem. It handles involuntary vital functions—pumping your heart, breathing air, and digesting food, even while you are fast asleep!",
            odia = "ମସ୍ତିଷ୍କକୁ ମେରୁଦଣ୍ଡ ସହ ଯୋଡୁଛି ବ୍ରେନ୍-ଷ୍ଟେମ୍ (Brainstem)। ଏହା ଅନୈଚ୍ଛିକ ଜୀବନରକ୍ଷକ କାର୍ଯ୍ୟ ଯଥା ହୃଦସ୍ପନ୍ଦନ, ଶ୍ୱାସକ୍ରିୟା ଓ ହଜମ କ୍ରିୟା ପରିଚାଳନା କରେ—ଯେତେବେଳେ ଆପଣ ଶୋଇଥାନ୍ତି ସେତେବେଳେ ମଧ୍ୟ!",
            hindi = "मस्तिष्क को रीढ़ की हड्डी से जोड़ता है ब्रेनस्टेम। यह अनैच्छिक जीवन रक्षक कार्यों जैसे दिल की धड़कन, सांस लेना और पाचन को नियंत्रित करता है—तब भी जब आप सो रहे होते हैं!"
        ),
        TranscriptPair(
            timeTag = "01:51 - 02:20",
            speaker = "Dr. Binocs",
            english = "Deep inside is the almond-shaped Amygdala, regulating emotions like fear and excitement, and survival fight-or-flight reactions.",
            odia = "ଭିତରେ ରହିଛି ବାଦାମ ଆକାରର ଆମିଗଡାଲା (Amygdala), ଯାହା ଭୟ, ଉତ୍ସାହ ଏବଂ 'Fight-or-Flight' ଜୀବନ ରକ୍ଷାକାରୀ ଭାବନା ନିୟନ୍ତ୍ରଣ କରେ।",
            hindi = "अंदर बादाम के आकार का अमिग्डाला है, जो भय, उत्साह और जीवन-रक्षक 'लड़ो या भागो' जैसी भावनाओं को नियंत्रित करता है।"
        ),
        TranscriptPair(
            timeTag = "02:21 - 02:50",
            speaker = "Dr. Binocs",
            english = "Fun trivia! Your brain generates 12 to 25 Watts of electricity—enough to light up an LED bulb! And doing physical exercise stimulates neuroplasticity, helping your brain master tough homework much faster!",
            odia = "ମଜାଦାର ତଥ୍ୟ! ଆପଣଙ୍କ ମସ୍ତିଷ୍କ ୧୨ ରୁ ୨୫ ୱାଟ୍ ବିଦ୍ୟୁତ୍ ଶକ୍ତି ଉତ୍ପନ୍ନ କରେ—ଏକ ଏଲ୍.ଇ.ଡି ବଲ୍ବ ଜଳାଇବା ପାଇଁ ଯଥେଷ୍ଟ! ଏବଂ ବ୍ୟାୟାମ ନ୍ୟୁରୋପ୍ଲାଷ୍ଟିସିଟି ବୃଦ୍ଧି କରି କଠିନ ହୋମୱାର୍କ ସହଜ କରିଥାଏ!",
            hindi = "मजेदार तथ्य! आपका दिमाग 12 से 25 वाट बिजली पैदा करता है—एक एलईडी बल्ब जलाने के लिए पर्याप्त! और व्यायाम न्यूरोप्लास्टिसिटी को बढ़ाता है, जिससे कठिन गृहकार्य हल करना आसान हो जाता है!"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        transcriptItems.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PrimaryBlue.copy(alpha = 0.1f)
                        ) {
                            Text(item.timeTag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Text(item.speaker, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.english,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "ଓଡ଼ିଆ: ${item.odia}",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF1E3A8A)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "हिंदी: ${item.hindi}",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF4A148C)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

private data class BrainStructureItem(
    val name: String,
    val proportion: String,
    val functions: String,
    val dysfunction: String,
    val color: Color
)

private data class TranscriptPair(
    val timeTag: String,
    val speaker: String,
    val english: String,
    val odia: String,
    val hindi: String
)
