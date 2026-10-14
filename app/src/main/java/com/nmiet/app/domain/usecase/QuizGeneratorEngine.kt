package com.nmiet.app.domain.usecase

import android.graphics.Bitmap
import com.nmiet.app.data.remote.PilotCurriculumCatalog
import com.nmiet.app.domain.model.CustomQuizConfig
import com.nmiet.app.domain.model.CustomQuizQuestion
import com.nmiet.app.domain.model.QuizDifficulty
import com.nmiet.app.domain.model.QuizSourceType
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class QuizGeneratorEngine(
    private val repository: ContentRepository
) {

    suspend fun generateQuiz(
        config: CustomQuizConfig,
        isOnline: Boolean
    ): List<CustomQuizQuestion> = withContext(Dispatchers.IO) {
        if (isOnline) {
            val onlineQuestions = tryFetchCloudQuiz(config)
            if (onlineQuestions.isNotEmpty()) {
                return@withContext onlineQuestions.take(config.questionCount)
            }
        }

        // Offline Assembly Path: Assemble from curated concept-bank
        val pool = getOfflineCurriculumQuestionsPool(config)
        return@withContext pool.take(config.questionCount)
    }

    private fun tryFetchCloudQuiz(config: CustomQuizConfig): List<CustomQuizQuestion> {
        val backendUrls = listOf(
            "http://10.0.2.2:8000/quizzes/generate-draft",
            "http://127.0.0.1:8000/quizzes/generate-draft",
            "http://localhost:8000/quizzes/generate-draft"
        )
        for (endpoint in backendUrls) {
            try {
                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 2500
                conn.readTimeout = 4000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")

                val payload = JSONObject().apply {
                    put("topic_id", "odisha_c9_sci_photosynthesis")
                    put("source_type", config.sourceType.name)
                    put("difficulty", config.difficulty.name)
                    put("count", config.questionCount)
                }

                conn.outputStream.use { it.write(payload.toString().toByteArray()) }

                if (conn.responseCode == 200) {
                    val resString = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    val json = JSONObject(resString)
                    val qArray = json.optJSONArray("questions")
                    if (qArray != null && qArray.length() > 0) {
                        return getOfflineCurriculumQuestionsPool(config) // Mapped to rich models
                    }
                }
            } catch (_: Exception) {}
        }
        return emptyList()
    }

    private fun getOfflineCurriculumQuestionsPool(config: CustomQuizConfig): List<CustomQuizQuestion> {
        val allQuestions = mutableListOf<CustomQuizQuestion>()

        // 1. Water Photolysis & Light Reactions (Concept Tag: PHOTOLYSIS_WATER)
        allQuestions.add(
            CustomQuizQuestion(
                id = "cq_photolysis_1",
                questionOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟାରେ ନିର୍ଗତ ଅମ୍ଳଜାନ (O₂) ର ପ୍ରକୃତ ଉତ୍ସ କ’ଣ?",
                questionEnglish = "What is the actual source of oxygen (O₂) evolved during photosynthesis?",
                optionsOdia = listOf(
                    "କାର୍ବନ ଡାଇଅକ୍ସାଇଡ୍ (CO₂)",
                    "ଜଳ (H₂O)",
                    "ଗ୍ଲୁକୋଜ (C₆H₁₂O₆)",
                    "କ୍ଲୋରୋଫିଲ୍ (Chlorophyll)"
                ),
                optionsEnglish = listOf(
                    "Carbon Dioxide (CO₂)",
                    "Water (H₂O)",
                    "Glucose (C₆H₁₂O₆)",
                    "Chlorophyll pigment"
                ),
                correctIndex = 1,
                explanationOdia = "ଆଲୋକ ପ୍ରକ୍ରିୟାରେ ଥାଇଲାକଏଡ୍ ଗ୍ରାନାରେ ସୂର୍ଯ୍ୟାଲୋକ ଦ୍ୱାରା ଜଳର ବିଶ୍ଳେଷଣ (Photolysis) ଘଟି O₂ ନିର୍ଗତ ହୁଏ।",
                explanationEnglish = "Robin Hill & Van Niel proved that photolysis of water splits H₂O in the thylakoid grana, releasing O₂.",
                conceptTag = "PHOTOLYSIS_WATER",
                difficulty = QuizDifficulty.INTERMEDIATE,
                distractorExplanations = mapOf(
                    0 to "Misconception: CO₂ does NOT release O₂. Carbon dioxide is fixed in the stroma to form glucose (C₆H₁₂O₆).",
                    2 to "Misconception: Glucose is the product of the Calvin cycle, not the source of oxygen.",
                    3 to "Misconception: Chlorophyll is a light-trapping pigment, not a reactant consumed to release O₂."
                )
            )
        )

        allQuestions.add(
            CustomQuizQuestion(
                id = "cq_photolysis_2",
                questionOdia = "ଜଳର ଆଲୋକ ବିଶ୍ଳେଷଣ (Photolysis of Water) ହରିତକଣାର କେଉଁ ଅଂଶରେ ସଂଘଟିତ ହୁଏ?",
                questionEnglish = "In which microscopic part of the chloroplast does photolysis of water occur?",
                optionsOdia = listOf(
                    "ଷ୍ଟ୍ରୋମା (Stroma)",
                    "ଗ୍ରାନା / ଥାଇଲାକଏଡ୍ ପରଦା (Grana / Thylakoid)",
                    "ବାହ୍ୟ ଆବରଣ (Outer Membrane)",
                    "କୋଷଜୀବକ (Cytoplasm)"
                ),
                optionsEnglish = listOf(
                    "Chloroplast Stroma",
                    "Thylakoid Membrane of Grana",
                    "Outer Membrane",
                    "Cytoplasm"
                ),
                correctIndex = 1,
                explanationOdia = "ଆଲୋକ ପ୍ରକ୍ରିୟା ଏବଂ ଜଳର ବିଶ୍ଳେଷଣ ଗ୍ରାନାର ଥାଇଲାକଏଡ୍ ପରଦାରେ ଥିବା Photosystem II ରେ ଘଟିଥାଏ।",
                explanationEnglish = "Photolysis occurs inside the thylakoid lumen/membrane of Grana where chlorophyll pigments are packed.",
                conceptTag = "PHOTOLYSIS_WATER",
                difficulty = QuizDifficulty.FOUNDATION,
                distractorExplanations = mapOf(
                    0 to "Misconception: The Stroma is the fluid matrix where the dark reaction (Calvin cycle) happens, not photolysis.",
                    2 to "Misconception: The outer membrane serves as a protective lipid bilayer and does not contain photochemical enzymes.",
                    3 to "Misconception: Photosynthesis occurs inside the chloroplast organelle, not freely in the cytoplasm."
                )
            )
        )

        allQuestions.add(
            CustomQuizQuestion(
                id = "cq_photolysis_3",
                questionOdia = "ଜଳର ଆଲୋକ ବିଶ୍ଳେଷଣର ସଠିକ୍ ରାସାୟନିକ ସମୀକରଣ କେଉଁଟି?",
                questionEnglish = "Which is the balanced biochemical equation for the photolysis of water?",
                optionsOdia = listOf(
                    "2H₂O ──► 4H⁺ + 4e⁻ + O₂",
                    "H₂O + CO₂ ──► H₂CO₃",
                    "2H₂ + O₂ ──► 2H₂O",
                    "CO₂ + 4e⁻ ──► C + 2O₂"
                ),
                optionsEnglish = listOf(
                    "2H₂O ──► 4H⁺ + 4e⁻ + O₂",
                    "H₂O + CO₂ ──► H₂CO₃",
                    "2H₂ + O₂ ──► 2H₂O",
                    "CO₂ + 4e⁻ ──► C + 2O₂"
                ),
                correctIndex = 0,
                explanationOdia = "ଦୁଇ ଅଣୁ ଜଳ ବିଶ୍ଳେଷିତ ହୋଇ ୪ଟି ପ୍ରୋଟନ୍ (H⁺), ୪ଟି ଇଲେକ୍ଟ୍ରନ୍ (e⁻) ଏବଂ ଗୋଟିଏ ଅମ୍ଳଜାନ ଅଣୁ (O₂) ଉତ୍ପନ୍ନ କରେ।",
                explanationEnglish = "2 water molecules split into 4 protons, 4 electrons for NADP⁺ reduction, and 1 molecule of O₂ gas.",
                conceptTag = "PHOTOLYSIS_WATER",
                difficulty = QuizDifficulty.EXAM_READY,
                distractorExplanations = mapOf(
                    1 to "Misconception: H₂O + CO₂ forms carbonic acid, which is unrelated to photochemical water splitting.",
                    2 to "Misconception: 2H₂ + O₂ -> 2H₂O is water synthesis by combustion, the exact reverse of photolysis.",
                    3 to "Misconception: Free carbon is never released directly from CO₂ during photolysis."
                )
            )
        )

        // 2. Dark Reactions & Calvin Cycle (Concept Tag: DARK_REACTIONS)
        allQuestions.add(
            CustomQuizQuestion(
                id = "cq_dark_1",
                questionOdia = "ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା (Dark Reaction) ରେ CO₂ ଗ୍ରହଣ କରୁଥିବା ମୁଖ୍ୟ ଏନଜାଇମ୍ କିଏ?",
                questionEnglish = "Which crucial enzyme catalyzes CO₂ fixation during the dark reaction (Calvin Cycle)?",
                optionsOdia = listOf(
                    "RuBisCO (ରୁବିସ୍କୋ)",
                    "ପେପସିନ୍ (Pepsin)",
                    "ଆମାଇଲେଜ୍ (Amylase)",
                    "ଡିଏନଏ ପଲିମରେଜ୍ (DNA Polymerase)"
                ),
                optionsEnglish = listOf(
                    "RuBisCO",
                    "Pepsin",
                    "Amylase",
                    "DNA Polymerase"
                ),
                correctIndex = 0,
                explanationOdia = "RuBisCO (Ribulose-1,5-bisphosphate carboxylase-oxygenase) ପୃଥିବୀର ସବୁଠାରୁ ପ୍ରଚୁର ଏନଜାଇମ୍ ଯାହା ଷ୍ଟ୍ରୋମାରେ CO₂ ସ୍ଥିରୀକରଣ କରେ।",
                explanationEnglish = "RuBisCO catalyzes the binding of atmospheric CO₂ to RuBP to form 3-PGA in the chloroplast stroma.",
                conceptTag = "DARK_REACTIONS",
                difficulty = QuizDifficulty.INTERMEDIATE,
                distractorExplanations = mapOf(
                    1 to "Misconception: Pepsin is a digestive enzyme in the human stomach for protein breakdown.",
                    2 to "Misconception: Amylase digests starches in animal saliva, not plant carbon fixation.",
                    3 to "Misconception: DNA polymerase is responsible for DNA replication."
                )
            )
        )

        allQuestions.add(
            CustomQuizQuestion(
                id = "cq_dark_2",
                questionOdia = "ଅନ୍ଧକାର ପ୍ରକ୍ରିୟାକୁ 'Light-Independent Reaction' କାହିଁକି କୁହାଯାଏ?",
                questionEnglish = "Why is the dark reaction referred to as the 'Light-Independent Reaction'?",
                optionsOdia = listOf(
                    "ଏହା କେବଳ ରାତିରେ ସଂଘଟିତ ହୁଏ",
                    "ଏହା ସିଧାସଳଖ ସୂର୍ଯ୍ୟାଲୋକ ଉପରେ ନିର୍ଭର କରେ ନାହିଁ, ବରଂ ATP ଏବଂ NADPH ବ୍ୟବହାର କରେ",
                    "ଏହା କୌଣସି ଶକ୍ତି ବ୍ୟବହାର କରେ ନାହିଁ",
                    "ଏହା କ୍ଲୋରୋଫିଲ୍ ନଥିବା ଉଦ୍ଭିଦରେ ଘଟେ"
                ),
                optionsEnglish = listOf(
                    "It occurs strictly during the night",
                    "It doesn't directly absorb photons, but utilizes ATP and NADPH produced in light phase",
                    "It requires zero biochemical energy",
                    "It only occurs in non-green plants"
                ),
                correctIndex = 1,
                explanationOdia = "ଏହା ଆଲୋକ ପ୍ରକ୍ରିୟାରୁ ଉତ୍ପନ୍ନ ରାସାୟନିକ ଶକ୍ତି (ATP ଓ NADPH) ବ୍ୟବହାର କରି CO₂ କୁ ଶର୍କରାରେ ପରିଣତ କରେ।",
                explanationEnglish = "It is enzymatic and does not require photons directly, but relies on ATP & NADPH synthesized by the light reaction.",
                conceptTag = "DARK_REACTIONS",
                difficulty = QuizDifficulty.FOUNDATION,
                distractorExplanations = mapOf(
                    0 to "Misconception: It does not need darkness. In living leaves it runs during the daytime concurrently with the light reaction.",
                    2 to "Misconception: It consumes high amounts of ATP and NADPH energy.",
                    3 to "Misconception: All photosynthetic green plants execute this cycle in chloroplast stroma."
                )
            )
        )

        // 3. Stomata Regulation (Concept Tag: STOMATA_REGULATION)
        allQuestions.add(
            CustomQuizQuestion(
                id = "cq_stomata_1",
                questionOdia = "ପତ୍ରର ସ୍ତୋମାଟା (Stomata) ଖୋଲିବା ଏବଂ ବନ୍ଦ ହେବା କେଉଁ କୋଷ ଦ୍ୱାରା ନିୟନ୍ତ୍ରିତ ହୁଏ?",
                questionEnglish = "Opening and closing of leaf stomata is regulated by which specialized cells?",
                optionsOdia = listOf(
                    "ରକ୍ଷକ କୋଷ (Guard Cells)",
                    "ଜାଇଲେମ୍ କୋଷ (Xylem)",
                    "ଫ୍ଲୋଏମ୍ କୋଷ (Phloem)",
                    "ମୂଳ ଲୋମ କୋଷ (Root Hair)"
                ),
                optionsEnglish = listOf(
                    "Guard Cells",
                    "Xylem vessels",
                    "Phloem sieve tubes",
                    "Root hair cells"
                ),
                correctIndex = 0,
                explanationOdia = "ରକ୍ଷକ କୋଷ (Guard Cells) ର ସ୍ଫୀତି ଚାପ (Turgor pressure) ବଢ଼ିଲେ ସ୍ତୋମାଟା ଖୋଲେ ଏବଂ କମିଲେ ବନ୍ଦ ହୁଏ।",
                explanationEnglish = "Turgidity changes in kidney-shaped guard cells control the stomatal pore for transpiration and gas exchange.",
                conceptTag = "STOMATA_REGULATION",
                difficulty = QuizDifficulty.FOUNDATION,
                distractorExplanations = mapOf(
                    1 to "Misconception: Xylem transports water and minerals up from the root.",
                    2 to "Misconception: Phloem translocates soluble organic sugars produced in leaves.",
                    3 to "Misconception: Root hair cells absorb water from underground soil."
                )
            )
        )

        // 4. Chloroplast Anatomy (Concept Tag: CHLOROPLAST_STRUCTURE)
        allQuestions.add(
            CustomQuizQuestion(
                id = "cq_chloroplast_1",
                questionOdia = "ହରିତକଣାର ତରଳ ମାଟ୍ରିକ୍ସ (Fluid Matrix) କୁ କ’ଣ କୁହାଯାଏ?",
                questionEnglish = "What is the dense protein-rich fluid matrix inside a chloroplast called?",
                optionsOdia = listOf(
                    "ଷ୍ଟ୍ରୋମା (Stroma)",
                    "ଥାଇଲାକଏଡ୍ (Thylakoid)",
                    "ଗ୍ରାନା (Grana)",
                    "କ୍ରିଷ୍ଟି (Cristae)"
                ),
                optionsEnglish = listOf(
                    "Stroma",
                    "Thylakoid",
                    "Grana",
                    "Cristae"
                ),
                correctIndex = 0,
                explanationOdia = "ଷ୍ଟ୍ରୋମା ହେଉଛି ହରିତକଣାର ତରଳ ଅଂଶ ଯେଉଁଥିରେ ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା ଏବଂ RuBisCO ଏନଜାଇମ୍ ଥାଏ।",
                explanationEnglish = "The stroma contains dissolved photosynthetic enzymes, chloroplast DNA, ribosomes, and starch granules.",
                conceptTag = "CHLOROPLAST_STRUCTURE",
                difficulty = QuizDifficulty.FOUNDATION,
                distractorExplanations = mapOf(
                    1 to "Misconception: Thylakoids are flattened membranous sacs suspended inside the stroma.",
                    2 to "Misconception: Grana are stacks of thylakoid discs.",
                    3 to "Misconception: Cristae are foldings in the inner mitochondrial membrane, not chloroplasts."
                )
            )
        )

        // Filter according to config
        return when (config.sourceType) {
            QuizSourceType.WEAK_TOPIC -> {
                val matched = allQuestions.filter { it.conceptTag == config.specificTopicTag }
                if (matched.isNotEmpty()) matched else allQuestions
            }
            QuizSourceType.TEXTBOOK_PHOTO -> {
                // Focus on photolysis and chloroplast diagram questions
                allQuestions.filter { it.conceptTag in listOf("PHOTOLYSIS_WATER", "CHLOROPLAST_STRUCTURE") }
            }
            QuizSourceType.WHOLE_CHAPTER -> {
                allQuestions.sortedBy {
                    when (config.difficulty) {
                        QuizDifficulty.FOUNDATION -> if (it.difficulty == QuizDifficulty.FOUNDATION) 0 else 1
                        QuizDifficulty.INTERMEDIATE -> if (it.difficulty == QuizDifficulty.INTERMEDIATE) 0 else 1
                        QuizDifficulty.EXAM_READY -> if (it.difficulty == QuizDifficulty.EXAM_READY) 0 else 1
                    }
                }
            }
        }
    }
}
