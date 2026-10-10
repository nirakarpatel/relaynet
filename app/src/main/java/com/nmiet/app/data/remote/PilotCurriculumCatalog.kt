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
            titleOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା (Photosynthesis)",
            titleEnglish = "Photosynthesis & Plant Nutrition",
            board = Board.ODISHA_STATE,
            classGrade = 9,
            subject = "Science (Biology)",
            chapterOdia = "ଅଧ୍ୟାୟ ୧: ଉଦ୍ଭିଦରେ ପୋଷଣ (Nutrition in Plants)",
            chapterEnglish = "Chapter 1: Nutrition in Plants",
            topicOdia = "ଆଲୋକ ଶ୍ଳେଷଣର କାର୍ଯ୍ୟପ୍ରଣାଳୀ ଓ ରାସାୟନିକ ପ୍ରକ୍ରିୟା",
            topicEnglish = "Mechanism of Photosynthesis & Chemical Reactions",
            version = 1,
            sizeBytes = 148500,
            checksumSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            isDownloaded = true,
            isReceivedViaMesh = false,
            sourceAttribution = "Odisha State Board of Secondary Education (BSE) Class 9 Science Curriculum & NCERT",
            lesson = LessonData(
                summaryOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ହେଉଛି ଏକ ଜୈବ-ରାସାୟନିକ ପ୍ରକ୍ରିୟା ଯେଉଁଥିରେ ସବୁଜ ଉଦ୍ଭିଦ ସୂର୍ଯ୍ୟାଲୋକ, କ୍ଲୋରୋଫିଲ୍, ବାୟୁମଣ୍ଡଳର ଅଙ୍ଗାରକାମ୍ଳ (CO₂) ଏବଂ ମାଟିରୁ ଜଳ (H₂O) ବ୍ୟବହାର କରି ଶର୍କରା (ଗ୍ଲୁକୋଜ୍) ପ୍ରସ୍ତୁତ କରେ ଏବଂ ଅମ୍ଳଜାନ (O₂) ଉପ-ଉତ୍ପାଦ ଭାବରେ ଛାଡିଥାଏ।",
                summaryEnglish = "Photosynthesis is the biochemical process by which green plants synthesize organic food (glucose) from carbon dioxide (CO₂) and water (H₂O) using solar energy trapped by chlorophyll, releasing oxygen (O₂) as a byproduct.",
                equationOrFormula = "6CO₂ + 12H₂O ──[ସୂର୍ଯ୍ୟାଲୋକ / କ୍ଲୋରୋଫିଲ୍]──► C₆H₁₂O₆ + 6H₂O + 6O₂ ↑",
                sections = listOf(
                    LessonSection(
                        titleOdia = "୧. ଆଲୋକ ଶ୍ଳେଷଣର ଆବଶ୍ୟକ ଉପାଦାନ",
                        titleEnglish = "1. Essential Components of Photosynthesis",
                        contentOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ସମ୍ପାଦନ ପାଇଁ ଚାରୋଟି ମୁଖ୍ୟ ଉପାଦାନ ଅପରିହାର୍ଯ୍ୟ:\n" +
                                "• ସୂର୍ଯ୍ୟାଲୋକ (Solar Energy): ଶକ୍ତିର ମୁଖ୍ୟ ଉତ୍ସ ଭାବରେ କାର୍ଯ୍ୟ କରେ।\n" +
                                "• କ୍ଲୋରୋଫିଲ୍ (Chlorophyll): ପତ୍ରର ହରିତକଣିକା (Chloroplast) ରେ ଥିବା ସବୁଜ ରଙ୍ଗକଣିକା ଯାହା ଆଲୋକ ଶୋଷଣ କରେ।\n" +
                                "• ଅଙ୍ଗାରକାମ୍ଳ ବାଷ୍ପ (CO₂): ପତ୍ରରେ ଥିବା ଷ୍ଟୋମାଟା (Stomata) ମାଧ୍ୟମରେ ବାୟୁମଣ୍ଡଳରୁ ପ୍ରବେଶ କରେ।\n" +
                                "• ଜଳ (H₂O): ମୂଳରୋମ ଦ୍ୱାରା ମାଟିରୁ ଶୋଷିତ ହୋଇ ଜାଇଲେମ (Xylem) ତନ୍ତୁ ଦେଇ ପତ୍ରକୁ ପହଞ୍ଚେ।",
                        contentEnglish = "Four essential raw materials are required for photosynthesis:\n" +
                                "• Solar Light: Provides radiant energy to drive biochemical synthesis.\n" +
                                "• Chlorophyll: The green pigment inside chloroplasts that traps photons.\n" +
                                "• Carbon Dioxide (CO₂): Absorbed from atmosphere via microscopic stomata pores.\n" +
                                "• Water (H₂O): Absorbed by root hair and transported to leaves via xylem vessels.",
                        keyTerms = listOf(
                            KeyTerm(
                                termOdia = "କ୍ଲୋରୋଫିଲ୍ (Chlorophyll)",
                                termEnglish = "Chlorophyll",
                                definitionOdia = "ପତ୍ରର କ୍ଲୋରୋପ୍ଲାଷ୍ଟରେ ଥିବା ସବୁଜ ରଙ୍ଗକଣିକା ଯାହା ସୂର୍ଯ୍ୟାଲୋକ ଫୋଟନ୍ କଣିକା ଶୋଷଣ କରେ।",
                                definitionEnglish = "The green pigment located in thylakoid membranes of chloroplasts that absorbs light energy."
                            ),
                            KeyTerm(
                                termOdia = "ଷ୍ଟୋମାଟା (Stomata)",
                                termEnglish = "Stomata",
                                definitionOdia = "ପତ୍ରର ତଳ ପୃଷ୍ଠରେ ଥିବା କ୍ଷୁଦ୍ର ରନ୍ଧ୍ର ଯାହା ବାଷ୍ପୀୟ ଆଦାନ-ପ୍ରଦାନ ଓ ପ୍ରସ୍ୱେଦନ ନିୟନ୍ତ୍ରଣ କରେ।",
                                definitionEnglish = "Microscopic pores on leaf epidermis regulated by guard cells for gaseous exchange."
                            )
                        )
                    ),
                    LessonSection(
                        titleOdia = "୨. ଦୁଇଟି ମୁଖ୍ୟ ପର୍ଯ୍ୟାୟ: ଆଲୋକ ଓ ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା",
                        titleEnglish = "2. Two Stages: Light & Dark Reactions",
                        contentOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ମୁଖ୍ୟତଃ ଦୁଇଟି କ୍ରମିକ ପର୍ଯ୍ୟାୟରେ ଘଟିଥାଏ:\n" +
                                "କ) ଆଲୋକ ପ୍ରକ୍ରିୟା (Light Reaction / Hill Reaction):\n" +
                                "କ୍ଲୋରୋପ୍ଲାଷ୍ଟର ଗ୍ରାନା (Grana) ରେ ଘଟେ। ଏଠାରେ ଆଲୋକ ଶକ୍ତି ରାସାୟନିକ ଶକ୍ତି (ATP ଓ NADPH) ରେ ରୂପାନ୍ତରିତ ହୁଏ ଏବଂ ଜଳର ଫୋଟୋଲିସିସ୍ ଘଟି ଅମ୍ଳଜାନ (O₂) ନିର୍ଗତ ହୁଏ।\n\n" +
                                "ଖ) ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା (Dark Reaction / Calvin Cycle):\n" +
                                "କ୍ଲୋରୋପ୍ଲାଷ୍ଟର ଷ୍ଟ୍ରୋମା (Stroma) ରେ ଘଟେ। ଏଥିପାଇଁ ସିଧାସଳଖ ଆଲୋକ ଆବଶ୍ୟକ ହୁଏ ନାହିଁ। ଏଠାରେ CO₂ ବିଜାରିତ ହୋଇ ଗ୍ଲୁକୋଜ୍ ଶର୍କରା ଉତ୍ପନ୍ନ ହୁଏ।",
                        contentEnglish = "Photosynthesis operates in two coupled sequential phases:\n" +
                                "A) Light-Dependent Reaction (Grana):\n" +
                                "Occurs in chloroplast grana thylakoids. Photons excite electrons, producing ATP and NADPH, accompanied by photolysis of water which releases O₂.\n\n" +
                                "B) Light-Independent Reaction (Calvin Cycle / Stroma):\n" +
                                "Takes place in chloroplast stroma matrix. Uses ATP and NADPH to reduce CO₂ into carbohydrates (glucose)."
                    )
                )
            ),
            quizQuestions = listOf(
                QuizQuestion(
                    id = "q1_photosynthesis",
                    questionOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟାରେ ନିର୍ଗତ ହେଉଥିବା ଅମ୍ଳଜାନ (O₂) ର ପ୍ରକୃତ ଉତ୍ସ କ'ଣ?",
                    questionEnglish = "What is the actual source of oxygen (O₂) released during photosynthesis?",
                    optionsOdia = listOf(
                        "କ) ଅଙ୍ଗାରକାମ୍ଳ (CO₂)",
                        "ଖ) ଜଳ (H₂O)",
                        "ଗ) ଗ୍ଲୁକୋଜ୍ (C₆H₁₂O₆)",
                        "ଘ) କ୍ଲୋରୋଫିଲ୍"
                    ),
                    optionsEnglish = listOf(
                        "A) Carbon Dioxide (CO₂)",
                        "B) Water (H₂O)",
                        "C) Glucose (C₆H₁₂O₆)",
                        "D) Chlorophyll"
                    ),
                    correctIndex = 1,
                    explanationOdia = "ଜଳର ଆଲୋକ ବିଭାଜନ (Photolysis of water) ଫଳରେ ଅମ୍ଳଜାନ ଗ୍ୟାସ୍ ନିର୍ଗତ ହୁଏ, ଅଙ୍ଗାରକାମ୍ଳରୁ ନୁହେଁ।",
                    explanationEnglish = "Photolysis of water molecules during the light reaction breaks H₂O into protons, electrons, and releases O₂ gas.",
                    conceptTag = "photolysis_water_source",
                    misconceptionHintOdia = "ସାଧାରଣ ଭୁଲ୍ ଧାରଣା: ଅନେକ ଭାବନ୍ତି O₂ ଅଙ୍ଗାରକାମ୍ଳରୁ ଆସେ, କିନ୍ତୁ ପ୍ରକୃତରେ ଏହା ଜଳ (H₂O) ରୁ ହିଁ ନିର୍ଗତ ହୁଏ।",
                    misconceptionHintEnglish = "Common misconception: Many students incorrectly assume O₂ comes from CO₂, but experiments prove it originates entirely from H₂O photolysis."
                ),
                QuizQuestion(
                    id = "q2_chloroplast_site",
                    questionOdia = "କ୍ଲୋରୋପ୍ଲାଷ୍ଟର କେଉଁ ଭାଗରେ ଆଲୋକ ପ୍ରକ୍ରିୟା (Light reaction) ସମ୍ପନ୍ନ ହୁଏ?",
                    questionEnglish = "In which region of the chloroplast does the light-dependent reaction occur?",
                    optionsOdia = listOf(
                        "କ) ଷ୍ଟ୍ରୋମା (Stroma)",
                        "ଖ) ଗ୍ରାନା (Grana)",
                        "ଗ) କୋଷଜୀବକ (Cytoplasm)",
                        "ଘ) ମାଇଟୋକଣ୍ଡ୍ରିଆ"
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
                    misconceptionHintOdia = "ମନେରଖନ୍ତୁ: ଗ୍ରାନା = ଆଲୋକ (Light); ଷ୍ଟ୍ରୋମା = ଅନ୍ଧକାର/କ୍ୟାଲଭିନ୍ ଚକ୍ର (Dark).",
                    misconceptionHintEnglish = "Memory aid: Grana = Green/Light absorbing; Stroma = Soluble matrix for Dark/Calvin synthesis."
                ),
                QuizQuestion(
                    id = "q3_gas_exchange",
                    questionOdia = "ପତ୍ରରେ ଗ୍ୟାସୀୟ ଆଦାନପ୍ରଦାନ କାହା ଦ୍ୱାରା ନିୟନ୍ତ୍ରିତ ହୁଏ?",
                    questionEnglish = "Which structure regulates gaseous exchange in plant leaves?",
                    optionsOdia = listOf(
                        "କ) ଜାଇଲେମ (Xylem)",
                        "ଖ) ଷ୍ଟୋମାଟା ଏବଂ ତାହାର ରକ୍ଷୀ କୋଷ (Guard cells)",
                        "ଗ) ଫ୍ଲୋଏମ୍ (Phloem)",
                        "ଘ) ମୂଳରୋମ"
                    ),
                    optionsEnglish = listOf(
                        "A) Xylem",
                        "B) Stomata and its Guard Cells",
                        "C) Phloem",
                        "D) Root Hairs"
                    ),
                    correctIndex = 1,
                    explanationOdia = "ଷ୍ଟୋମାଟା ରନ୍ଧ୍ରର ଖୋଲିବା ଓ ବନ୍ଦ ହେବା ରକ୍ଷୀ କୋଷ (Guard cells) ଦ୍ୱାରା ନିୟନ୍ତ୍ରିତ ହୋଇ ବାୟୁମଣ୍ଡଳ ସହ CO₂ ଏବଂ O₂ ବିନିମୟ ହୁଏ।",
                    explanationEnglish = "Stomatal pores open and close via turgor pressure changes in surrounding guard cells to regulate CO₂ intake and transpiration.",
                    conceptTag = "stomata_regulation",
                    misconceptionHintOdia = "ଜାଇଲେମ୍ ଓ ଫ୍ଲୋଏମ୍ ପରିବହନ ତନ୍ତୁ; କିନ୍ତୁ ଗ୍ୟାସ ବିନିମୟ ଷ୍ଟୋମାଟା ଦ୍ୱାରା ହିଁ ହୁଏ।",
                    misconceptionHintEnglish = "Xylem and phloem conduct sap; gas exchange is strictly governed by stomata."
                ),
                QuizQuestion(
                    id = "q4_end_product",
                    questionOdia = "ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା (Calvin Cycle) ର ମୁଖ୍ୟ ସ୍ଥାୟୀ ଉତ୍ପାଦ କ'ଣ?",
                    questionEnglish = "What is the primary organic product synthesized in the Calvin cycle?",
                    optionsOdia = listOf(
                        "କ) ସୁକ୍ରୋଜ୍",
                        "ଖ) ଗ୍ଲୁକୋଜ୍ ଶର୍କରା (Glucose)",
                        "ଗ) ଆମିନୋ ଏସିଡ୍",
                        "ଘ) ସେଲୁଲୋଜ୍"
                    ),
                    optionsEnglish = listOf(
                        "A) Sucrose",
                        "B) Glucose (Simple Carbohydrate)",
                        "C) Amino Acid",
                        "D) Cellulose"
                    ),
                    correctIndex = 1,
                    explanationOdia = "କ୍ୟାଲଭିନ୍ ଚକ୍ରରେ CO₂ ର ସ୍ଥିରୀକରଣ ଘଟି ପ୍ରାଥମିକ ଭାବରେ ଗ୍ଲୁକୋଜ୍ ଶର୍କରା ସୃଷ୍ଟି ହୁଏ, ଯାହା ପରେ ଷ୍ଟାର୍ଚରେ ସଞ୍ଚିତ ହୁଏ।",
                    explanationEnglish = "In the Calvin cycle, CO₂ fixation utilizes ATP and NADPH to synthesize glucose, which later condenses into storage starch.",
                    conceptTag = "calvin_cycle_product",
                    misconceptionHintOdia = "ଉଦ୍ଭିଦ ପ୍ରଥମେ ଗ୍ଲୁକୋଜ୍ ତିଆରି କରେ ଏବଂ ପରେ ତାହାକୁ ଷ୍ଟାର୍ଚ (ଶ୍ୱେତସାର) ଭାବରେ ସାଇତି ରଖେ।",
                    misconceptionHintEnglish = "Plants first synthesize simple glucose monomers before storing them as starch polysaccharides."
                ),
                QuizQuestion(
                    id = "q5_energy_transfer",
                    questionOdia = "ଆଲୋକ ଶ୍ଳେଷଣରେ ସୌରଶକ୍ତି କେଉଁ ଶକ୍ତିକୁ ରୂପାନ୍ତରିତ ହୁଏ?",
                    questionEnglish = "During photosynthesis, solar energy is converted into which form of energy?",
                    optionsOdia = listOf(
                        "କ) ଯାନ୍ତ୍ରିକ ଶକ୍ତି",
                        "ଖ) ତାପ ଶକ୍ତି",
                        "ଗ) ରାସାୟନିକ ଶକ୍ତି (Chemical Energy)",
                        "ଘ) ଚୁମ୍ବକୀୟ ଶକ୍ତି"
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
                    misconceptionHintOdia = "ଶକ୍ତି ନଷ୍ଟ ହୁଏ ନାହିଁ; ସୌରଶକ୍ତି ଜୈବ-ରାସାୟନିକ ଶକ୍ତିରେ ପରିଣତ ହୁଏ।",
                    misconceptionHintEnglish = "Radiant energy is conserved by transduction into biochemical potential energy."
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
                subject = "Science (Biology)",
                chapterTitleOdia = "ଅଧ୍ୟାୟ ୧: ଉଦ୍ଭିଦରେ ପୋଷଣ (Nutrition in Plants)",
                chapterTitleEnglish = "Chapter 1: Nutrition in Plants",
                topicTitleOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା (Photosynthesis)",
                topicTitleEnglish = "Mechanism of Photosynthesis",
                learningObjectiveOdia = "ସବୁଜ ଉଦ୍ଭିଦରେ ଆଲୋକ ଓ ଅନ୍ଧକାର ପ୍ରକ୍ରିୟାର ସମୀକରଣ ଓ କାର୍ଯ୍ୟପ୍ରଣାଳୀ ବୁଝିବା।",
                learningObjectiveEnglish = "Master the biochemical pathways of light and dark reactions.",
                packId = "relaynet_class9_science_photosynthesis_v1",
                isAvailableOffline = true
            ),
            CurriculumNode(
                id = "curr-node-2",
                board = Board.CBSE,
                classGrade = 9,
                subject = "Science (Physics)",
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
                subject = "Science (Life Science)",
                chapterTitleOdia = "ଅଧ୍ୟାୟ ୨: ଶ୍ୱସନ ଓ ରକ୍ତ ସଞ୍ଚାଳନ (Respiration & Circulation)",
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
                chapterTitleOdia = "ଅଧ୍ୟାୟ ୪: ରୈଖିକ ସମୀକରଣ (Linear Equations)",
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
            qLower.contains("photosynthesis") || qLower.contains("ଆଲୋକ ଶ୍ଳେଷଣ") || qLower.contains("କଣ") || qLower.contains("what") -> {
                Pair(
                    "ଆଲୋକ ଶ୍ଳେଷଣ ହେଉଛି ସବୁଜ ଉଦ୍ଭିଦଙ୍କର ଖାଦ୍ୟ ପ୍ରସ୍ତୁତି ପ୍ରଣାଳୀ। ସବୁଜ ପତ୍ରରେ ଥିବା କ୍ଲୋରୋଫିଲ୍ ସୂର୍ଯ୍ୟାଲୋକ ଶୋଷଣ କରେ, ଚେର ମାଟିରୁ ଜଳ (H₂O) ଆଣେ ଏବଂ ଷ୍ଟୋମାଟା ବାୟୁରୁ CO₂ ଗ୍ରହଣ କରେ। ଏହି ପ୍ରକ୍ରିୟାରେ ଗ୍ଲୁକୋଜ୍ (ଖାଦ୍ୟ) ତିଆରି ହୁଏ ଏବଂ ଜୀବଜଗତ ପାଇଁ ଅମ୍ଳଜାନ (O₂) ବାୟୁମଣ୍ଡଳକୁ ମୁକ୍ତ ହୁଏ।\n\nରାସାୟନିକ ସମୀକରଣ:\n6CO₂ + 12H₂O ──► C₆H₁₂O₆ + 6H₂O + 6O₂ ↑",
                    "Photosynthesis is the process by which green plants manufacture glucose from CO₂ and H₂O using solar energy absorbed by chlorophyll, releasing vital O₂ as a byproduct.\n\nBalanced Equation:\n6CO₂ + 12H₂O ──► C₆H₁₂O₆ + 6H₂O + 6O₂ ↑"
                )
            }
            qLower.contains("equation") || qLower.contains("ସମୀକରଣ") || qLower.contains("formula") -> {
                Pair(
                    "ଆଲୋକ ଶ୍ଳେଷଣର ସନ୍ତୁଳିତ ରାସାୟନିକ ସମୀକରଣ:\n6CO₂ (ଅଙ୍ଗାରକାମ୍ଳ) + 12H₂O (ଜଳ) ──[ସୂର୍ଯ୍ୟାଲୋକ / କ୍ଲୋରୋଫିଲ୍]──► C₆H₁₂O₆ (ଗ୍ଲୁକୋଜ୍) + 6H₂O + 6O₂ (ଅମ୍ଳଜାନ)",
                    "The balanced chemical equation of photosynthesis:\n6CO₂ + 12H₂O ──[Solar Light / Chlorophyll]──► C₆H₁₂O₆ (Glucose) + 6H₂O + 6O₂ ↑"
                )
            }
            qLower.contains("chlorophyll") || qLower.contains("କ୍ଲୋରୋଫିଲ") || qLower.contains("ହରିତକଣିକା") -> {
                Pair(
                    "କ୍ଲୋରୋଫିଲ୍ ହେଉଛି ପତ୍ରର ହରିତକଣିକା (Chloroplast) ରେ ଥିବା ସବୁଜ ରଙ୍ଗକଣିକା। ଏହାର ପ୍ରମୁଖ କାର୍ଯ୍ୟ ହେଉଛି ସୂର୍ଯ୍ୟାଲୋକର ଫୋଟନ୍ ଶକ୍ତି ଶୋଷଣ କରି ରାସାୟନିକ ଶକ୍ତି (ATP ଓ NADPH) ରେ ପରିଣତ କରିବା।",
                    "Chlorophyll is the green pigment in plant chloroplasts. Its primary function is trapping solar photons and converting radiant energy into chemical energy carriers (ATP & NADPH)."
                )
            }
            else -> {
                Pair(
                    "ଆପଣଙ୍କ ପ୍ରଶ୍ନ ପାଇଁ ଧନ୍ୟବାଦ। କ୍ଲାସ୍ ୯ ବିଜ୍ଞାନ ସିଲାବସ୍ ଅନୁଯାୟୀ ଆଲୋକ ଶ୍ଳେଷଣରେ ଉଦ୍ଭିଦ ମାଟିରୁ ଜଳ ଏବଂ ବାୟୁରୁ CO₂ ନେଇ ସୂର୍ଯ୍ୟାଲୋକରେ ଗ୍ଲୁକୋଜ୍ ପ୍ରସ୍ତୁତ କରେ। ଅଧିକ ଜାଣିବା ପାଇଁ ଅଧ୍ୟାୟ ୧ ପଢ଼ନ୍ତୁ କିମ୍ବା କୁଇଜ୍ ଅଭ୍ୟାସ କରନ୍ତୁ।",
                    "According to the Class 9 Science curriculum, plants synthesize carbohydrates using atmospheric CO₂ and soil water under solar radiation. Check the Chapter 1 lesson and attempt the diagnostic quiz to test your mastery!"
                )
            }
        }
    }
}
