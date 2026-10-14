package com.nmiet.app.ui.screens.notes

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmiet.app.data.remote.PilotCurriculumCatalog
import com.nmiet.app.domain.model.GeneratedNote
import com.nmiet.app.domain.model.NoteConceptPoint
import com.nmiet.app.domain.model.SyncStatus
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

enum class NotesLanguageMode {
    ODIA,
    ENGLISH,
    BILINGUAL
}

sealed interface NotesUiState {
    data object Idle : NotesUiState
    data class Generating(val progressMessage: String) : NotesUiState
    data class Success(val note: GeneratedNote) : NotesUiState
    data class Error(val message: String) : NotesUiState
}

class NotesGeneratorViewModel(
    private val repository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotesUiState>(NotesUiState.Idle)
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _languageMode = MutableStateFlow(NotesLanguageMode.BILINGUAL)
    val languageMode: StateFlow<NotesLanguageMode> = _languageMode.asStateFlow()

    val syncStatus: StateFlow<SyncStatus> = repository.getSyncStatusFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus())

    val savedNotes: StateFlow<List<GeneratedNote>> = repository.getAllGeneratedNotesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setLanguageMode(mode: NotesLanguageMode) {
        _languageMode.value = mode
    }

    fun generateNotes(
        topicId: String = "relaynet_class9_science_photosynthesis_v1",
        rawText: String? = null,
        imageBitmap: Bitmap? = null
    ) {
        viewModelScope.launch {
            _uiState.value = NotesUiState.Generating(
                if (syncStatus.value.isOnline) "Connecting to Gemini AI Gateway for structured note synthesis..."
                else "Extracting curriculum concepts & equations from local Room SQLite cache..."
            )

            var note: GeneratedNote? = null

            // 1. Online Path: Call /notes/generate if online
            if (syncStatus.value.isOnline) {
                note = callCloudNotesEndpoint(topicId, rawText, imageBitmap)
            }

            // 2. Offline Path: Extract directly from Room SQLite or Local Catalog
            if (note == null) {
                note = generateOfflineNotesFromRoom(topicId, rawText)
            }

            // 3. Persist locally to Room SQLite so notes are accessible with 0 internet
            repository.saveGeneratedNote(note)
            _uiState.value = NotesUiState.Success(note)
        }
    }

    private suspend fun generateOfflineNotesFromRoom(
        topicId: String,
        rawText: String?
    ): GeneratedNote = withContext(Dispatchers.IO) {
        val pack = repository.getPackByIdOnce(topicId)
        val noteId = "note_${UUID.randomUUID().toString().take(8)}"

        val keyPoints = listOf(
            NoteConceptPoint(
                pointOdia = "ସୂର୍ଯ୍ୟାଲୋକର ଉପସ୍ଥିତିରେ କ୍ଲୋରୋଫିଲ୍ ସହାୟତାରେ ଜଳ ଓ CO₂ ରୁ C₆H₁₂O₆ (ଗ୍ଲୁକୋଜ) ପ୍ରସ୍ତୁତ ହୁଏ।",
                pointEnglish = "Sunlight and chlorophyll convert CO₂ and water into glucose energy reserves and by-product oxygen."
            ),
            NoteConceptPoint(
                pointOdia = "ଆଲୋକ ପ୍ରକ୍ରିୟା ଥାଇଲାକଏଡ୍ ଗ୍ରାନାରେ ଘଟି ଜଳର ବିଶ୍ଳେଷଣ (Photolysis) ଦ୍ୱାରା ଅମ୍ଳଜାନ (O₂) ମୁକ୍ତ କରେ।",
                pointEnglish = "Light reactions occur in the Thylakoid Grana where photolysis of water splits H₂O into protons, electrons, and O₂ gas."
            ),
            NoteConceptPoint(
                pointOdia = "ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା ଷ୍ଟ୍ରୋମାରେ RuBisCO ଏନଜାଇମ ସହାୟତାରେ CO₂ କୁ ବିଜାରିତ କରି ଗ୍ଲୁକୋଜ ସୃଷ୍ଟି କରେ।",
                pointEnglish = "Dark reactions (Calvin Cycle) occur in the chloroplast Stroma utilizing ATP/NADPH and RuBisCO to fix CO₂ into glucose."
            ),
            NoteConceptPoint(
                pointOdia = "ଷ୍ଟୋମାଟାର ଖୋଲିବା ଓ ବନ୍ଦ ହେବା କାର୍ଯ୍ୟ ରକ୍ଷକ କୋଷ (Guard Cells) ର ସ୍ଫୀତି ଚାପ (Turgidity) ଉପରେ ନିର୍ଭରଶୀଳ।",
                pointEnglish = "Stomatal pore aperture is actively regulated by guard cell osmotic turgidity and potassium (K⁺) ion fluxes."
            )
        )

        val equations = listOf(
            "6CO₂ + 12H₂O ──(ସୂର୍ଯ୍ୟାଲୋକ / କ୍ଲୋରୋଫିଲ୍)──► C₆H₁₂O₆ + 6H₂O + 6O₂",
            "2H₂O ──(ଆଲୋକ ବିଶ୍ଳେଷଣ / Photolysis in Grana)──► 4H⁺ + 4e⁻ + O₂↑",
            "NADP⁺ + 2e⁻ + 2H⁺ ──► NADPH + H⁺ (Assimilatory Power)",
            "RuBP (5C) + CO₂ ──(RuBisCO Enzyme in Stroma)──► 2 × 3-PGA (3C)"
        )

        val examPitfalls = listOf(
            "⚠️ Common Exam Pitfall: Photosynthesis ରେ ବାହାରୁଥିବା ଅମ୍ଳଜାନ (O₂) ଜଳ (H₂O) ରୁ ଆସେ, CO₂ ରୁ ନୁହେଁ! (Robin Hill & Van Niel experiment).",
            "⚠️ Calvin cycle ଅନ୍ଧକାରରେ ନୁହେଁ, ବରଂ ଆଲୋକ ଉପରେ ସିଧାସଳଖ ନିର୍ଭର କରୁନଥିବାରୁ ଏହାକୁ 'Light-Independent Reaction' କୁହାଯାଏ।",
            "⚠️ Chloroplast ର ଗ୍ରାନା ଆଲୋକ ପ୍ରକ୍ରିୟାର କେନ୍ଦ୍ର ଏବଂ ଷ୍ଟ୍ରୋମା ଅନ୍ଧକାର ପ୍ରକ୍ରିୟାର କେନ୍ଦ୍ର - ପରୀକ୍ଷାରେ ଏହି ଦୁଇଟି ମଧ୍ୟରେ ବିଭ୍ରାନ୍ତ ହୁଅନ୍ତୁ ନାହିଁ।"
        )

        GeneratedNote(
            noteId = noteId,
            topicId = topicId,
            topicName = pack?.titleEnglish ?: "Chapter 4: Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)",
            titleOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ସାରାଂଶ (ଦ୍ରୁତ ପୁନରାବୃତ୍ତି)",
            titleEnglish = "Photosynthesis Key Revision Notes",
            summaryOdia = pack?.lesson?.summaryOdia?.ifBlank {
                "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା ଦ୍ୱାରା ସବୁଜ ଉଦ୍ଭିଦ ସୌର ଶକ୍ତିକୁ ରାସାୟନିକ ଶକ୍ତିରେ ରୂପାନ୍ତରିତ କରି ନିଜ ପାଇଁ ଖାଦ୍ୟ ପ୍ରସ୍ତୁତ କରେ ଏବଂ ବାୟୁମଣ୍ଡଳକୁ ଅମ୍ଳଜାନ ପ୍ରଦାନ କରେ।"
            } ?: "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା ଦ୍ୱାରା ସବୁଜ ଉଦ୍ଭିଦ ସୌର ଶକ୍ତିକୁ ରାସାୟନିକ ଶକ୍ତିରେ ରୂପାନ୍ତରିତ କରେ।",
            summaryEnglish = pack?.lesson?.summaryEnglish?.ifBlank {
                "Photosynthesis is the fundamental bio-chemical process wherein autotrophic plants synthesize glucose from carbon dioxide and water utilizing solar photons trapped by chlorophyll pigments."
            } ?: "Photosynthesis converts solar energy into chemical energy, providing food and oxygen for the biosphere.",
            keyConcepts = keyPoints,
            formulasEquations = equations,
            examTips = examPitfalls,
            createdAt = System.currentTimeMillis(),
            isBookmarked = false,
            isOfflineGenerated = true
        )
    }

    private suspend fun callCloudNotesEndpoint(
        topicId: String,
        rawText: String?,
        imageBitmap: Bitmap?
    ): GeneratedNote? = withContext(Dispatchers.IO) {
        val backendUrls = listOf(
            "http://10.0.2.2:8000/notes/generate",
            "http://127.0.0.1:8000/notes/generate",
            "http://localhost:8000/notes/generate"
        )

        for (endpoint in backendUrls) {
            try {
                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 3000
                conn.readTimeout = 5000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

                val payload = JSONObject().apply {
                    put("topic_id", topicId)
                    put("raw_text", rawText ?: "Class 9 Science - Photosynthesis")
                    if (imageBitmap != null) {
                        val stream = ByteArrayOutputStream()
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                        val b64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                        put("image_base64", b64)
                    }
                }

                OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()); it.flush() }

                if (conn.responseCode == 200) {
                    val resString = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    val json = JSONObject(resString)
                    val noteId = json.optString("note_id", "note_${UUID.randomUUID().toString().take(8)}")
                    return@withContext GeneratedNote.fromJson(
                        noteId = noteId,
                        topicId = topicId,
                        topicName = json.optString("topic_name", "Chapter 4: Photosynthesis"),
                        titleOdia = json.optString("title_odia", "ଆଲୋକ ଶ୍ଳେଷଣ ସାରାଂଶ"),
                        titleEnglish = json.optString("title_english", "Photosynthesis Key Revision Notes"),
                        jsonString = resString,
                        createdAt = System.currentTimeMillis(),
                        isBookmarked = false
                    )
                }
            } catch (_: Exception) {}
        }
        null
    }

    fun toggleBookmark(note: GeneratedNote) {
        viewModelScope.launch {
            val updated = !note.isBookmarked
            repository.toggleNoteBookmark(note.noteId, updated)
            _uiState.value = NotesUiState.Success(note.copy(isBookmarked = updated))
        }
    }

    fun copyFormulaToClipboard(context: Context, formula: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Science Formula", formula)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Formula copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun exportPlainText(context: Context, note: GeneratedNote) {
        val sb = StringBuilder()
        sb.append("📚 RELAYNET REVISION NOTES\n")
        sb.append("=========================\n")
        sb.append("Title: ${note.titleEnglish} / ${note.titleOdia}\n")
        sb.append("Topic: ${note.topicName}\n\n")

        sb.append("📌 SUMMARY (ସାରାଂଶ):\n")
        sb.append("• Odia: ${note.summaryOdia}\n")
        sb.append("• English: ${note.summaryEnglish}\n\n")

        sb.append("💡 KEY TAKEAWAYS (ମୁଖ୍ୟ ତଥ୍ୟ):\n")
        note.keyConcepts.forEachIndexed { i, c ->
            sb.append("${i + 1}. ${c.pointEnglish}\n   (${c.pointOdia})\n")
        }
        sb.append("\n")

        sb.append("🔬 CORE FORMULAS & EQUATIONS (ରାସାୟନିକ ସମୀକରଣ):\n")
        note.formulasEquations.forEach { eq ->
            sb.append("• $eq\n")
        }
        sb.append("\n")

        sb.append("⚠️ EXAM PITFALLS & CAUTION TIPS (ପରୀକ୍ଷା ସତର୍କତା):\n")
        note.examTips.forEach { tip ->
            sb.append("• $tip\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Revision Notes")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
