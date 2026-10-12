package com.nmiet.app.data.remote

import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.CurriculumNode
import com.nmiet.app.domain.model.KeyTerm
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.LessonData
import com.nmiet.app.domain.model.LessonSection
import com.nmiet.app.domain.model.QuizQuestion

object PilotCurriculumCatalog {

    fun getPilotLearningPack(): LearningPack {
        return LearningPack(
            packId = "relaynet_class9_science_photosynthesis_v1",
            titleOdia = "Chapter 4: Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)",
            titleEnglish = "Chapter 4: Photosynthesis & Plant Biochemistry",
            board = Board.ODISHA_STATE,
            classGrade = 9,
            subject = "Science (ବିଜ୍ଞାନ)",
            chapterOdia = "ଅଧ୍ୟାୟ ୪: ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା",
            chapterEnglish = "Chapter 4: Photosynthesis",
            topicOdia = "ଆଲୋକ ଶ୍ଳେଷଣର କାର୍ଯ୍ୟପ୍ରଣାଳୀ, ଫୋଟୋଲିସିସ୍ ଓ ରାସାୟନିକ ପ୍ରକ୍ରିୟା",
            topicEnglish = "Mechanism of Photosynthesis, Photolysis & Chemical Reactions",
            version = 1,
            sizeBytes = 1887436L, // 1.8 MB
            checksumSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            isDownloaded = true,
            isReceivedViaMesh = false,
            sourceAttribution = "Odisha State Board of Secondary Education (BSE) Class 9 Science Curriculum & NCERT",
            lesson = LessonData(
                summaryOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ହେଉଛି ଏକ ଜୈବ-ରାସାୟନିକ ପ୍ରକ୍ରିୟା ଯେଉଁଥିରେ ସବୁଜ ଉଦ୍ଭିଦ ସୂର୍ଯ୍ୟାଲୋକ, କ୍ଲୋରୋଫିଲ୍, ବାୟୁମଣ୍ଡଳର ଅଙ୍ଗାରକାମ୍ଳ (CO₂) ଏବଂ ମାଟିରୁ ଜଳ (H₂O) ବ୍ୟବହାର କରି ଶର୍କରା (ଗ୍ଲୁକୋଜ୍) ପ୍ରସ୍ତୁତ କରେ ଏବଂ ଅମ୍ଳଜାନ (O₂) ଉପ-ଉତ୍ପାଦ ଭାବରେ ଛାଡିଥାଏ।",
                summaryEnglish = "Photosynthesis is the biochemical process by which green plants synthesize organic food (glucose) from carbon dioxide (CO₂) and water (H₂O) using solar energy trapped by chlorophyll, releasing oxygen (O₂) as a byproduct.",
                equationOrFormula = "6CO₂ + 12H₂O ──[Chlorophyll / Sunlight]──> C₆H₁₂O₆ + 6O₂ + 6H₂O",
                sections = listOf(
                    LessonSection(
                        titleOdia = "୧. ଆଲୋକ ଶ୍ଳେଷଣର ଆବଶ୍ୟକ ଉପାଦାନ",
                        titleEnglish = "1. Essential Raw Materials & Solar Absorption",
                        contentOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ସମ୍ପାଦନ ପାଇଁ ଚାରୋଟି ମୁଖ୍ୟ ଉପାଦାନ ଅପରିହାର୍ଯ୍ୟ:\n" +
                                "• ସୂର୍ଯ୍ୟାଲୋକ (Solar Radiation): ଶକ୍ତିର ମୁଖ୍ୟ ଉତ୍ସ ଭାବରେ କାର୍ଯ୍ୟ କରେ।\n" +
                                "• କ୍ଲୋରୋଫିଲ୍ (Chlorophyll): ପତ୍ରର ହରିତକଣିକା (Chloroplast) ରେ ଥିବା ସବୁଜ ରଙ୍ଗକଣିକା ଯାହା ଆଲୋକ ଶୋଷଣ କରେ।\n" +
                                "• ଅଙ୍ଗାରକାମ୍ଳ ବାଷ୍ପ (CO₂): ପତ୍ରରେ ଥିବା ଷ୍ଟୋମାଟା (Stomata) ମାଧ୍ୟମରେ ବାୟୁମଣ୍ଡଳରୁ ପ୍ରବେଶ କରେ।\n" +
                                "• ଜଳ (H₂O): ମୂଳରୋମ ଦ୍ୱାରା ମାଟିରୁ ଶୋଷିତ ହୋଇ ଜାଇଲେମ (Xylem) ତନ୍ତୁ ଦେଇ ପତ୍ରକୁ ପହଞ୍ଚେ।",
                        contentEnglish = "Four essential raw materials are required for photosynthesis:\n" +
                                "• Solar Radiation: Provides radiant photon energy to drive biochemical synthesis.\n" +
                                "• Chlorophyll: The green pigment inside chloroplast thylakoids that traps light.\n" +
                                "• Carbon Dioxide (CO₂): Absorbed from atmosphere via microscopic stomatal pores.\n" +
                                "• Water (H₂O): Absorbed by root hairs and transported to leaves via xylem vessels.",
                        keyTerms = listOf(
                            KeyTerm(
                                termOdia = " Photolysis / ଜଳ ବିଘଟନ",
                                termEnglish = "Photolysis of Water",
                                definitionOdia = "ଆଲୋକ ପ୍ରକ୍ରିୟା ସମୟରେ କ୍ଲୋରୋଫିଲ୍ ଶକ୍ତି ଦ୍ୱାରା ଜଳ ଅଣୁ (H₂O) ର ଭାଙ୍ଗିବା ଯାହାଦ୍ୱାରା ଅମ୍ଳଜାନ (O₂) ନିର୍ଗତ ହୁଏ।",
                                definitionEnglish = "Light-driven splitting of water molecules in thylakoids, which releases oxygen gas and free electrons."
                            ),
                            KeyTerm(
                                termOdia = "Stomata / ସ୍ତୋମ",
                                termEnglish = "Stomata & Guard Cells",
                                definitionOdia = "ପତ୍ରର ତଳ ପୃଷ୍ଠରେ ଥିବା କ୍ଷୁଦ୍ର ରନ୍ଧ୍ର ଯାହା ବାଷ୍ପୀୟ ଆଦାନ-ପ୍ରଦାନ (CO₂ & O₂) ଏବଂ ପ୍ରସ୍ୱେଦନ ନିୟନ୍ତ୍ରଣ କରେ।",
                                definitionEnglish = "Microscopic epidermal pores flanked by guard cells regulating photosynthetic gas exchange and transpiration."
                            ),
                            KeyTerm(
                                termOdia = "Chlorophyll / କ୍ଲୋରୋଫିଲ୍",
                                termEnglish = "Chlorophyll Pigment",
                                definitionOdia = "ପତ୍ରର କ୍ଲୋରୋପ୍ଲାଷ୍ଟ ଗ୍ରାନାରେ ଥିବା ପ୍ରାଥମିକ ସବୁଜ ରଙ୍ଗକଣିକା ଯାହା ସୌର ଆଲୋକ ଫୋଟନ୍ ଗ୍ରହଣ କରେ।",
                                definitionEnglish = "Primary photosynthetic pigment located in thylakoid membranes that absorbs blue and red spectrum photons."
                            )
                        )
                    ),
                    LessonSection(
                        titleOdia = "୨. ଦୁଇଟି ମୁଖ୍ୟ ପର୍ଯ୍ୟାୟ: ଆଲୋକ ଓ ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା",
                        titleEnglish = "2. Two Stages: Light Reaction & Calvin Cycle",
                        contentOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ମୁଖ୍ୟତଃ ଦୁଇଟି କ୍ରମିକ ପର୍ଯ୍ୟାୟରେ ଘଟିଥାଏ:\n" +
                                "କ) ଆଲୋକ ପ୍ରକ୍ରିୟା (Light Reaction / Hill Reaction):\n" +
                                "କ୍ଲୋରୋପ୍ଲାଷ୍ଟର ଗ୍ରାନା (Grana) ରେ ଘଟେ। ଏଠାରେ ଆଲୋକ ଶକ୍ତି ରାସାୟନିକ ଶକ୍ତି (ATP ଓ NADPH) ରେ ରୂପାନ୍ତରିତ ହୁଏ ଏବଂ ଜଳର ଫୋଟୋଲିସିସ୍ (Photolysis) ଘଟି ଅମ୍ଳଜାନ (O₂) ନିର୍ଗତ ହୁଏ।\n\n" +
                                "ଖ) ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା (Dark Reaction / Calvin Cycle):\n" +
                                "କ୍ଲୋରୋପ୍ଲାଷ୍ଟର ଷ୍ଟ୍ରୋମା (Stroma) ରେ ଘଟେ। ଏଥିପାଇଁ ସିଧାସଳଖ ଆଲୋକ ଆବଶ୍ୟକ ହୁଏ ନାହିଁ। ଏଠାରେ CO₂ ବିଜାରିତ ହୋଇ ଗ୍ଲୁକୋଜ୍ (C₆H₁₂O₆) ଶର୍କରା ଉତ୍ପନ୍ନ ହୁଏ।",
                        contentEnglish = "Photosynthesis operates in two coupled sequential phases:\n" +
                                "A) Light-Dependent Reaction (Grana):\n" +
                                "Occurs in chloroplast thylakoid stacks (Grana). Photons excite electrons, generating ATP & NADPH, accompanied by photolysis of water which splits H₂O and liberates O₂.\n\n" +
                                "B) Light-Independent Reaction (Calvin Cycle / Stroma):\n" +
                                "Takes place in chloroplast stroma matrix. Uses assimilation power (ATP & NADPH) to reduce atmospheric CO₂ into carbohydrates (glucose)."
                    )
                )
            ),
            quizQuestions = listOf(
                QuizQuestion(
                    id = "q1_photosynthesis",
                    questionOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟାରେ ଅମ୍ଳଜାନ କେଉଁଥିରୁ ନିର୍ଗତ ହୁଏ?",
                    questionEnglish = "In photosynthesis, oxygen is released from which compound?",
                    optionsOdia = listOf(
                        "A) CO₂ (ଅଙ୍ଗାରକାମ୍ଳ)",
                        "B) H₂O (ଜଳ)",
                        "C) Chlorophyll (କ୍ଲୋରୋଫିଲ୍)",
                        "D) Glucose (ଗ୍ଲୁକୋଜ୍)"
                    ),
                    optionsEnglish = listOf(
                        "A) CO₂",
                        "B) H₂O",
                        "C) Chlorophyll",
                        "D) Glucose"
                    ),
                    correctIndex = 1,
                    explanationOdia = "ଜଳର ଆଲୋକ ବିଭାଜନ (Photolysis of water) ଫଳରେ ଅମ୍ଳଜାନ ଗ୍ୟାସ୍ ନିର୍ଗତ ହୁଏ, ଅଙ୍ଗାରକାମ୍ଳରୁ ନୁହେଁ।",
                    explanationEnglish = "Photolysis of water molecules during the light-dependent reaction splits H₂O into protons, electrons, and releases O₂ gas.",
                    conceptTag = "photolysis_water_source",
                    misconceptionHintOdia = "⚠️ Concept Gap Detected: You selected CO₂. However, photolysis of water (H₂O) during light reactions is what splits and releases O₂, not CO₂.",
                    misconceptionHintEnglish = "⚠️ Concept Gap Detected: You selected CO₂. However, photolysis of water (H₂O) during light reactions is what splits and releases O₂, not CO₂."
                ),
                QuizQuestion(
                    id = "q2_chloroplast_site",
                    questionOdia = "କ୍ଲୋରୋପ୍ଲାଷ୍ଟର କେଉଁ ଭାଗରେ ଆଲୋକ ପ୍ରକ୍ରିୟା (Light reaction) ସମ୍ପନ୍ନ ହୁଏ?",
                    questionEnglish = "In which region of the chloroplast does the light-dependent reaction occur?",
                    optionsOdia = listOf(
                        "A) ଷ୍ଟ୍ରୋମା (Stroma)",
                        "B) ଗ୍ରାନା (Grana / Thylakoids)",
                        "C) କୋଷଜୀବକ (Cytoplasm)",
                        "D) ମାଇଟୋକଣ୍ଡ୍ରିଆ"
                    ),
                    optionsEnglish = listOf(
                        "A) Stroma",
                        "B) Grana (Thylakoids)",
                        "C) Cytoplasm",
                        "D) Mitochondria"
                    ),
                    correctIndex = 1,
                    explanationOdia = "କ୍ଲୋରୋପ୍ଲାଷ୍ଟର ଥାଇଲାକଏଡ୍ ଗଦା ବା ଗ୍ରାନା (Grana) ରେ ଆଲୋକ ପ୍ରକ୍ରିୟା ଘଟେ। ଷ୍ଟ୍ରୋମାରେ ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା ଘଟେ।",
                    explanationEnglish = "The light reaction occurs in thylakoid stacks called Grana where chlorophyll is embedded. Dark reactions occur in Stroma.",
                    conceptTag = "chloroplast_anatomy_site",
                    misconceptionHintOdia = "⚠️ Concept Gap Detected: You selected Stroma. Stroma is where dark reactions/Calvin cycle take place; light reactions occur in Grana.",
                    misconceptionHintEnglish = "⚠️ Concept Gap Detected: You selected Stroma. Stroma is where dark reactions/Calvin cycle take place; light reactions occur in Grana."
                ),
                QuizQuestion(
                    id = "q3_gas_exchange",
                    questionOdia = "ପତ୍ରରେ ଗ୍ୟାସୀୟ ଆଦାନପ୍ରଦାନ କାହା ଦ୍ୱାରା ନିୟନ୍ତ୍ରିତ ହୁଏ?",
                    questionEnglish = "Which structure regulates photosynthetic gaseous exchange in plant leaves?",
                    optionsOdia = listOf(
                        "A) ଜାଇଲେମ (Xylem)",
                        "B) ଷ୍ଟୋମାଟା / ସ୍ତୋମ (Stomata and Guard Cells)",
                        "C) ଫ୍ଲୋଏମ୍ (Phloem)",
                        "D) ମୂଳରୋମ (Root Hairs)"
                    ),
                    optionsEnglish = listOf(
                        "A) Xylem",
                        "B) Stomata and Guard Cells",
                        "C) Phloem",
                        "D) Root Hairs"
                    ),
                    correctIndex = 1,
                    explanationOdia = "ଷ୍ଟୋମାଟା ରନ୍ଧ୍ରର ଖୋଲିବା ଓ ବନ୍ଦ ହେବା ରକ୍ଷୀ କୋଷ (Guard cells) ଦ୍ୱାରା ନିୟନ୍ତ୍ରିତ ହୋଇ ବାୟୁମଣ୍ଡଳ ସହ CO₂ ଏବଂ O₂ ବିନିମୟ ହୁଏ।",
                    explanationEnglish = "Stomatal pores open and close via turgor pressure changes in surrounding guard cells to regulate CO₂ intake and transpiration.",
                    conceptTag = "stomata_regulation",
                    misconceptionHintOdia = "⚠️ Concept Gap Detected: Xylem and phloem are vascular transport tissues; gas exchange is regulated by stomata.",
                    misconceptionHintEnglish = "⚠️ Concept Gap Detected: Xylem and phloem are vascular transport tissues; gas exchange is regulated by stomata."
                ),
                QuizQuestion(
                    id = "q4_end_product",
                    questionOdia = "ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା (Calvin Cycle) ର ମୁଖ୍ୟ ସ୍ଥାୟୀ ଉତ୍ପାଦ କ'ଣ?",
                    questionEnglish = "What is the primary organic product synthesized in the Calvin cycle?",
                    optionsOdia = listOf(
                        "A) ସୁକ୍ରୋଜ୍ (Sucrose)",
                        "B) ଗ୍ଲୁକୋଜ୍ (C₆H₁₂O₆ Glucose)",
                        "C) ଆମିନୋ ଏସିଡ୍ (Amino Acid)",
                        "D) ସେଲୁଲୋଜ୍ (Cellulose)"
                    ),
                    optionsEnglish = listOf(
                        "A) Sucrose",
                        "B) Glucose (C₆H₁₂O₆)",
                        "C) Amino Acid",
                        "D) Cellulose"
                    ),
                    correctIndex = 1,
                    explanationOdia = "କ୍ୟାଲଭିନ୍ ଚକ୍ରରେ CO₂ ର ସ୍ଥିରୀକରଣ ଘଟି ପ୍ରାଥମିକ ଭାବରେ ଗ୍ଲୁକୋଜ୍ ଶର୍କରା ସୃଷ୍ଟି ହୁଏ, ଯାହା ପରେ ଷ୍ଟାର୍ଚରେ ସଞ୍ଚିତ ହୁଏ।",
                    explanationEnglish = "In the Calvin cycle, CO₂ fixation utilizes ATP and NADPH to synthesize glucose, which later condenses into storage starch.",
                    conceptTag = "calvin_cycle_product",
                    misconceptionHintOdia = "⚠️ Concept Gap Detected: Plants synthesize simple glucose monomers first during Calvin cycle before storing as starch.",
                    misconceptionHintEnglish = "⚠️ Concept Gap Detected: Plants synthesize simple glucose monomers first during Calvin cycle before storing as starch."
                ),
                QuizQuestion(
                    id = "q5_energy_transfer",
                    questionOdia = "ଆଲୋକ ଶ୍ଳେଷଣରେ ସୌରଶକ୍ତି କେଉଁ ଶକ୍ତିକୁ ରୂପାନ୍ତରିତ ହୁଏ?",
                    questionEnglish = "During photosynthesis, solar energy is converted into which form of energy?",
                    optionsOdia = listOf(
                        "A) ଯାନ୍ତ୍ରିକ ଶକ୍ତି (Mechanical Energy)",
                        "B) ତାପ ଶକ୍ତି (Thermal Energy)",
                        "C) ରାସାୟନିକ ଶକ୍ତି (Chemical Energy)",
                        "D) ଚୁମ୍ବକୀୟ ଶକ୍ତି (Magnetic Energy)"
                    ),
                    optionsEnglish = listOf(
                        "A) Mechanical Energy",
                        "B) Thermal Energy",
                        "C) Chemical Energy",
                        "D) Magnetic Energy"
                    ),
                    correctIndex = 2,
                    explanationOdia = "ସୂର୍ଯ୍ୟଙ୍କ ଆଲୋକ ଶକ୍ତି ଗ୍ଲୁକୋଜ୍ ଅଣୁର ରାସାୟନିକ ବନ୍ଧନ (Chemical bonds) ରେ ରାସାୟନିକ ଶକ୍ତି ଭାବେ ଆବଦ୍ଧ ହୁଏ।",
                    explanationEnglish = "Solar radiant energy is transduced and stored in covalent chemical bonds of organic carbohydrate molecules.",
                    conceptTag = "energy_transduction",
                    misconceptionHintOdia = "⚠️ Concept Gap Detected: Solar photon energy is converted into biochemical potential energy stored in covalent bonds.",
                    misconceptionHintEnglish = "⚠️ Concept Gap Detected: Solar photon energy is converted into biochemical potential energy stored in covalent bonds."
                )
            )
        )
    }

    fun getCurriculumCatalogue(): List<CurriculumNode> {
        return listOf(
            CurriculumNode(
                id = "curr-node-1",
                board = Board.ODISHA_STATE,
                classGrade = 9,
                subject = "Science (ବିଜ୍ଞାନ)",
                chapterTitleOdia = "ଅଧ୍ୟାୟ ୪: ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା",
                chapterTitleEnglish = "Chapter 4: Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)",
                topicTitleOdia = "ଆଲୋକ ଶ୍ଳେଷଣର କାର୍ଯ୍ୟପ୍ରଣାଳୀ, ଫୋଟୋଲିସିସ୍ ଓ ରାସାୟନିକ ପ୍ରକ୍ରିୟା",
                topicTitleEnglish = "Mechanism of Photosynthesis & Chemical Reactions",
                learningObjectiveOdia = "ସବୁଜ ଉଦ୍ଭିଦରେ ଆଲୋକ ଓ ଅନ୍ଧକାର ପ୍ରକ୍ରିୟାର ସମୀକରଣ ଓ କାର୍ଯ୍ୟପ୍ରଣାଳୀ ବୁଝିବା।",
                learningObjectiveEnglish = "Master the biochemical pathways of light and dark reactions.",
                packId = "relaynet_class9_science_photosynthesis_v1",
                isAvailableOffline = true
            ),
            CurriculumNode(
                id = "curr-node-2",
                board = Board.CBSE,
                classGrade = 9,
                subject = "Science (ବିଜ୍ଞାନ)",
                chapterTitleOdia = "ଅଧ୍ୟାୟ ୯: ବଳ ଏବଂ ଗତିର ନିୟମ",
                chapterTitleEnglish = "Chapter 9: Force and Laws of Motion",
                topicTitleOdia = "ନ୍ୟୁଟନଙ୍କ ତିନୋଟି ଗତି ନିୟମ ଓ ସଂବେଗ ସଂରକ୍ଷଣ",
                topicTitleEnglish = "Newton's Laws of Motion & Momentum",
                learningObjectiveOdia = "ନ୍ୟୁଟନଙ୍କ ଗତି ନିୟମର ପ୍ରୟୋଗ ଓ ଗାଣିତିକ ସମୀକରଣ ବୁଝିବା।",
                learningObjectiveEnglish = "Understand inertial mass, acceleration, and action-reaction pairs.",
                packId = "relaynet_class9_science_motion_v1",
                isAvailableOffline = false
            ),
            CurriculumNode(
                id = "curr-node-3",
                board = Board.ODISHA_STATE,
                classGrade = 10,
                subject = "Science (ବିଜ୍ଞାନ)",
                chapterTitleOdia = "ଅଧ୍ୟାୟ ୨: ଶ୍ୱସନ ଓ ରକ୍ତ ସଞ୍ଚାଳନ",
                chapterTitleEnglish = "Chapter 2: Respiration and Circulation",
                topicTitleOdia = "ମାନବ ଶ୍ୱସନ ତନ୍ତ୍ର ଓ ମାନବ ହୃତ୍‌ପିଣ୍ଡର ଗଠନ",
                topicTitleEnglish = "Human Respiratory System & Heart Anatomy",
                learningObjectiveOdia = "ମାନବ ଶରୀରରେ ଅମ୍ଳଜାନ ବିନିମୟ ଏବଂ ଦ୍ୱୈତ ରକ୍ତ ସଞ୍ଚାଳନ ବୁଝିବା।",
                learningObjectiveEnglish = "Trace systemic and pulmonary double circulation.",
                packId = "relaynet_class10_science_respiration_v1",
                isAvailableOffline = false
            ),
            CurriculumNode(
                id = "curr-node-4",
                board = Board.CISCE_ICSE,
                classGrade = 9,
                subject = "Mathematics",
                chapterTitleOdia = "ଅଧ୍ୟାୟ ୪: ରୈଖିକ ସମୀକରଣ",
                chapterTitleEnglish = "Chapter 4: Simultaneous Linear Equations",
                topicTitleOdia = "ଦୁଇ ଅଜ୍ଞାତ ରାଶି ବିଶିଷ୍ଟ ଯୁଗପତ ସମୀକରଣ ସମାଧାନ",
                topicTitleEnglish = "Elimination & Substitution Methods",
                learningObjectiveOdia = "ବୀଜଗାଣିତିକ ସମାଧାନ କୌଶଳ ଆୟତ୍ତ କରିବା।",
                learningObjectiveEnglish = "Solve simultaneous equations using elimination and cross-multiplication.",
                packId = "relaynet_class9_math_equations_v1",
                isAvailableOffline = false
            )
        )
    }

    fun getOfflineTutorResponse(question: String): Pair<String, String> {
        val qLower = question.lowercase()
        return when {
            qLower.contains("photosynthesis") || qLower.contains("ଆଲୋକ ଶ୍ଳେଷଣ") || qLower.contains("କଣ") || qLower.contains("explain") || qLower.contains("ବୁଝାନ୍ତୁ") -> {
                Pair(
                    "ଆଲୋକ ଶ୍ଳେଷଣ ହେଉଛି ସବୁଜ ଉଦ୍ଭିଦଙ୍କର ଖାଦ୍ୟ ପ୍ରସ୍ତୁତି ପ୍ରଣାଳୀ। ସବୁଜ ପତ୍ରରେ ଥିବା କ୍ଲୋରୋଫିଲ୍ ସୂର୍ଯ୍ୟାଲୋକ ଶୋଷଣ କରେ, ଚେର ମାଟିରୁ ଜଳ (H₂O) ଆଣେ ଏବଂ ଷ୍ଟୋମାଟା ବାୟୁରୁ CO₂ ଗ୍ରହଣ କରେ। ଏହି ପ୍ରକ୍ରିୟାରେ ଗ୍ଲୁକୋଜ୍ (ଖାଦ୍ୟ) ତିଆରି ହୁଏ ଏବଂ ଜୀବଜଗତ ପାଇଁ ଅମ୍ଳଜାନ (O₂) ବାୟୁମଣ୍ଡଳକୁ ମୁକ୍ତ ହୁଏ।\n\nସନ୍ତୁଳିତ ସମୀକରଣ:\n6CO₂ + 12H₂O ──[Chlorophyll / Sunlight]──> C₆H₁₂O₆ + 6O₂ + 6H₂O",
                    "Photosynthesis is the process by which green plants manufacture glucose from CO₂ and H₂O using solar energy absorbed by chlorophyll, releasing vital O₂ as a byproduct.\n\nBalanced Equation:\n6CO₂ + 12H₂O ──[Chlorophyll / Sunlight]──> C₆H₁₂O₆ + 6O₂ + 6H₂O"
                )
            }
            qLower.contains("oxygen") || qLower.contains("ଅମ୍ଳଜାନ") || qLower.contains("come from") -> {
                Pair(
                    "ଆଲୋକ ଶ୍ଳେଷଣରେ ନିର୍ଗତ ହେଉଥିବା ଅମ୍ଳଜାନ (O₂) ର ପ୍ରକୃତ ଉତ୍ସ ହେଉଛି ଜଳ (H₂O)। ଆଲୋକ ପ୍ରକ୍ରିୟାରେ କ୍ଲୋରୋଫିଲ୍ ଶକ୍ତି ଦ୍ୱାରା ଜଳର ଫୋଟୋଲିସିସ୍ (Photolysis of water) ଘଟି H₂O ଅଣୁ ଭାଙ୍ଗି ଅମ୍ଳଜାନ ଗ୍ୟାସ୍ ବାୟୁମଣ୍ଡଳକୁ ମୁକ୍ତ ହୁଏ, ଅଙ୍ଗାରକାମ୍ଳରୁ ନୁହେଁ।",
                    "In photosynthesis, oxygen (O₂) is derived entirely from the photolysis of water (H₂O). During the light reaction, photons split water molecules into protons, electrons, and O₂ gas."
                )
            }
            qLower.contains("translate") || qLower.contains("odia") || qLower.contains("ଓଡ଼ିଆରେ") -> {
                Pair(
                    "ଓଡ଼ିଆ ଅନୁବାଦ: ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟାରେ ସୌରଶକ୍ତି ରାସାୟନିକ ଶକ୍ତିରେ ପରିଣତ ହୁଏ। ଜଳର ଆଲୋକ ବିଭାଜନ ଦ୍ୱାରା ଅମ୍ଳଜାନ ବାୟୁମଣ୍ଡଳରେ ମୁକ୍ତ ହୁଏ।",
                    "Odia Translation: In photosynthesis, solar light energy transduces into chemical energy. Photolysis of water liberates oxygen gas into the atmosphere."
                )
            }
            else -> {
                Pair(
                    "ଆପଣଙ୍କ ପ୍ରଶ୍ନ ପାଇଁ ଧନ୍ୟବାଦ। କ୍ଲାସ୍ ୯ ବିଜ୍ଞାନ 'Chapter 4: Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)' ଅନୁଯାୟୀ ଉଦ୍ଭିଦ ମାଟିରୁ ଜଳ ଏବଂ ବାୟୁରୁ CO₂ ନେଇ ସୂର୍ଯ୍ୟାଲୋକରେ ଗ୍ଲୁକୋଜ୍ ପ୍ରସ୍ତୁତ କରେ। ଅଧିକ ଜାଣିବା ପାଇଁ ପାଠ୍ୟପୁସ୍ତକ ପଢ଼ନ୍ତୁ କିମ୍ବା କୁଇଜ୍ ଅଭ୍ୟାସ କରନ୍ତୁ।",
                    "According to Class 9 Science Chapter 4: Photosynthesis, plants synthesize glucose from atmospheric CO₂ and soil H₂O under solar radiation. Read the lesson summary and take the practice quiz to verify your conceptual mastery!"
                )
            }
        }
    }
}
