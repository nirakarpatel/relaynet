package com.nmiet.app.data.remote

import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.CurriculumNode
import com.nmiet.app.domain.model.KeyTerm
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.LessonData
import com.nmiet.app.domain.model.LessonSection
import com.nmiet.app.domain.model.QuizQuestion
import com.nmiet.app.ui.components.MultilingualString

data class LocalizedLessonContent(
    val title: MultilingualString,
    val summary: MultilingualString,
    val sections: List<MultilingualString>,
    val balancedEquation: String = "6CO₂ + 12H₂O ──[Chlorophyll / Sunlight]──> C₆H₁₂O₆ + 6O₂ + 6H₂O"
)

object PilotCurriculumCatalog {

    val localizedPhotosynthesisContent = LocalizedLessonContent(
        title = MultilingualString(
            english = "Chapter 4: Photosynthesis & Plant Biochemistry",
            odia = "ଅଧ୍ୟାୟ ୪: ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା",
            hindi = "अध्याय 4: प्रकाश संश्लेषण (Photosynthesis)"
        ),
        summary = MultilingualString(
            english = "Photosynthesis is the biochemical process by which green plants synthesize organic food (glucose) from carbon dioxide (CO₂) and water (H₂O) using solar energy trapped by chlorophyll, releasing oxygen (O₂) as a byproduct.",
            odia = "ଆଲୋକ ଶ୍ଳେଷଣ ହେଉଛି ଏକ ଜୈବ-ରାସାୟନିକ ପ୍ରକ୍ରିୟା ଯେଉଁଥିରେ ସବୁଜ ଉଦ୍ଭିଦ ସୂର୍ଯ୍ୟାଲୋକ, କ୍ଲୋରୋଫିଲ୍, ବାୟୁମଣ୍ଡଳର ଅଙ୍ଗାରକାମ୍ଳ (CO₂) ଏବଂ ମାଟିରୁ ଜଳ (H₂O) ବ୍ୟବହାର କରି ଶର୍କରା (ଗ୍ଲୁକୋଜ୍) ପ୍ରସ୍ତୁତ କରେ ଏବଂ ଅମ୍ଳଜାନ (O₂) ଉପ-ଉତ୍ପାଦ ଭାବରେ ଛାଡିଥାଏ।",
            hindi = "प्रकाश संश्लेषण वह जैव-रासायनिक प्रक्रिया है जिसके द्वारा हरे पौधे सूर्य के प्रकाश और क्लोरोफिल की उपस्थिति में कार्बन डाइऑक्साइड (CO₂) और जल (H₂O) से ग्लूकोज का निर्माण करते हैं तथा ऑक्सीजन (O₂) गैस मुक्त करते हैं।"
        ),
        sections = listOf(
            MultilingualString(
                english = "Essential Raw Materials & Solar Absorption",
                odia = "ଆଲୋକ ଶ୍ଳେଷଣର ଆବଶ୍ୟକ ଉପାଦାନ",
                hindi = "आवश्यक कच्चा माल और सौर ऊर्जा अवशोषण"
            ),
            MultilingualString(
                english = "Two Stages: Light Reaction & Calvin Cycle",
                odia = "ଦୁଇଟି ମୁଖ୍ୟ ପର୍ଯ୍ୟାୟ: ଆଲୋକ ଓ ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା",
                hindi = "दो मुख्य चरण: प्रकाश और अंधकार अभिक्रिया"
            )
        )
    )

    fun getPilotLearningPack(): LearningPack {
        return LearningPack(
            packId = "relaynet_class9_science_photosynthesis_v1",
            titleOdia = "Chapter 4: Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)",
            titleEnglish = "Chapter 4: Photosynthesis & Plant Biochemistry",
            titleHindi = "अध्याय 4: प्रकाश संश्लेषण (Photosynthesis)",
            board = Board.ODISHA_STATE,
            classGrade = 9,
            subject = "Science (ବିଜ୍ଞାନ / विज्ञान)",
            chapterOdia = "ଅଧ୍ୟାୟ ୪: ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା",
            chapterEnglish = "Chapter 4: Photosynthesis",
            chapterHindi = "अध्याय 4: प्रकाश संश्लेषण प्रक्रिया",
            topicOdia = "ଆଲୋକ ଶ୍ଳେଷଣର କାର୍ଯ୍ୟପ୍ରଣାଳୀ, ଫୋଟୋଲିସିସ୍ ଓ ରାସାୟନିକ ପ୍ରକ୍ରିୟା",
            topicEnglish = "Mechanism of Photosynthesis, Photolysis & Chemical Reactions",
            topicHindi = "प्रकाश संश्लेषण की कार्यप्रणाली, प्रकाश-अपघटन एवं रासायनिक प्रतिक्रियाएं",
            version = 1,
            sizeBytes = 1887436L, // 1.8 MB
            checksumSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            isDownloaded = true,
            isReceivedViaMesh = false,
            sourceAttribution = "Odisha State Board of Secondary Education (BSE) Class 9 Science Curriculum & NCERT",
            lesson = LessonData(
                summaryOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ହେଉଛି ଏକ ଜୈବ-ରାସାୟନିକ ପ୍ରକ୍ରିୟା ଯେଉଁଥିରେ ସବୁଜ ଉଦ୍ଭିଦ ସୂର୍ଯ୍ୟାଲୋକ, କ୍ଲୋରୋଫିଲ୍, ବାୟୁମଣ୍ଡଳର ଅଙ୍ଗାରକାମ୍ଳ (CO₂) ଏବଂ ମାଟିରୁ ଜଳ (H₂O) ବ୍ୟବହାର କରି ଶର୍କରା (ଗ୍ଲୁକୋଜ୍) ପ୍ରସ୍ତୁତ କରେ ଏବଂ ଅମ୍ଳଜାନ (O₂) ଉପ-ଉତ୍ପାଦ ଭାବରେ ଛାଡିଥାଏ।",
                summaryEnglish = "Photosynthesis is the biochemical process by which green plants synthesize organic food (glucose) from carbon dioxide (CO₂) and water (H₂O) using solar energy trapped by chlorophyll, releasing oxygen (O₂) as a byproduct.",
                summaryHindi = "प्रकाश संश्लेषण वह जैव-रासायनिक प्रक्रिया है जिसके द्वारा हरे पौधे सूर्य के प्रकाश और क्लोरोफिल की उपस्थिति में कार्बन डाइऑक्साइड (CO₂) और जल (H₂O) से ग्लूकोज का निर्माण करते हैं तथा ऑक्सीजन (O₂) गैस मुक्त करते हैं।",
                equationOrFormula = "6CO₂ + 12H₂O ──[Chlorophyll / Sunlight]──> C₆H₁₂O₆ + 6O₂ + 6H₂O",
                sections = listOf(
                    LessonSection(
                        titleOdia = "୧. ଆଲୋକ ଶ୍ଳେଷଣର ଆବଶ୍ୟକ ଉପାଦାନ",
                        titleEnglish = "1. Essential Raw Materials & Solar Absorption",
                        titleHindi = "1. आवश्यक कच्चा माल और सौर ऊर्जा अवशोषण",
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
                        contentHindi = "प्रकाश संश्लेषण के लिए चार मुख्य घटक आवश्यक हैं:\n" +
                                "• सौर विकिरण (Solar Radiation): ऊर्जा का मुख्य स्रोत है।\n" +
                                "• क्लोरोफिल (Chlorophyll): क्लोरोप्लास्ट में मौजूद हरा वर्णक जो प्रकाश को अवशोषित करता है।\n" +
                                "• कार्बन डाइऑक्साइड (CO₂): पत्तियों के रंध्रों (Stomata) द्वारा वायुमंडल से प्रवेश करती है।\n" +
                                "• जल (H₂O): जड़ों द्वारा मिट्टी से अवशोषित होकर जाइलम वाहिकाओं द्वारा पत्तियों तक पहुँचता है।",
                        keyTerms = listOf(
                            KeyTerm(
                                termOdia = "Photolysis / ଜଳ ବିଘଟନ",
                                termEnglish = "Photolysis of Water",
                                termHindi = "जल का प्रकाश-अपघटन (Photolysis)",
                                definitionOdia = "ଆଲୋକ ପ୍ରକ୍ରିୟା ସମୟରେ କ୍ଲୋରୋଫିଲ୍ ଶକ୍ତି ଦ୍ୱାରା ଜଳ ଅଣୁ (H₂O) ର ଭାଙ୍ଗିବା ଯାହାଦ୍ୱାରା ଅମ୍ଳଜାନ (O₂) ନିର୍ଗତ ହୁଏ।",
                                definitionEnglish = "Light-driven splitting of water molecules in thylakoids, which releases oxygen gas and free electrons.",
                                definitionHindi = "प्रकाश अभिक्रिया के दौरान क्लोरोफिल ऊर्जा द्वारा जल अणुओं (H₂O) का टूटना, जिससे ऑक्सीजन (O₂) गैस और इलेक्ट्रॉन मुक्त होते हैं।"
                            ),
                            KeyTerm(
                                termOdia = "Stomata / ସ୍ତୋମ",
                                termEnglish = "Stomata & Guard Cells",
                                termHindi = "रंध्र एवं द्वार कोशिकाएं (Stomata)",
                                definitionOdia = "ପତ୍ରର ତଳ ପୃଷ୍ଠରେ ଥିବା କ୍ଷୁଦ୍ର ରନ୍ଧ୍ର ଯାହା ବାଷ୍ପୀୟ ଆଦାନ-ପ୍ରଦାନ (CO₂ & O₂) ଏବଂ ପ୍ରସ୍ୱେଦନ ନିୟନ୍ତ୍ରଣ କରେ।",
                                definitionEnglish = "Microscopic epidermal pores flanked by guard cells regulating photosynthetic gas exchange and transpiration.",
                                definitionHindi = "पत्तियों की सतह पर मौजूद सूक्ष्म छिद्र जो गैसीय विनिमय (CO₂ और O₂) तथा वाष्पोत्सर्जन को नियंत्रित करते हैं।"
                            ),
                            KeyTerm(
                                termOdia = "Chlorophyll / କ୍ଲୋରୋଫିଲ୍",
                                termEnglish = "Chlorophyll Pigment",
                                termHindi = "क्लोरोफिल वर्णक (Chlorophyll)",
                                definitionOdia = "ପତ୍ରର କ୍ଲୋରୋପ୍ଲାଷ୍ଟ ଗ୍ରାନାରେ ଥିବା ପ୍ରାଥମିକ ସବୁଜ ରଙ୍ଗକଣିକା ଯାହା ସୌର ଆଲୋକ ଫୋଟନ୍ ଗ୍ରହଣ କରେ।",
                                definitionEnglish = "Primary photosynthetic pigment located in thylakoid membranes that absorbs blue and red spectrum photons.",
                                definitionHindi = "क्लोरोप्लास्ट के थाइलाकोइड में स्थित मुख्य हरा वर्णक जो नीले और लाल स्पेक्ट्रम के प्रकाश को अवशोषित करता है।"
                            )
                        )
                    ),
                    LessonSection(
                        titleOdia = "୨. ଦୁଇଟି ମୁଖ୍ୟ ପର୍ଯ୍ୟାୟ: ଆଲୋକ ଓ ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା",
                        titleEnglish = "2. Two Stages: Light Reaction & Calvin Cycle",
                        titleHindi = "2. दो मुख्य चरण: प्रकाश और अंधकार अभिक्रिया",
                        contentOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ମୁଖ୍ୟତଃ ଦୁଇଟି କ୍ରମିକ ପର୍ଯ୍ୟାୟରେ ଘଟିଥାଏ:\n" +
                                "କ) ଆଲୋକ ପ୍ରକ୍ରିୟା (Light Reaction / Hill Reaction):\n" +
                                "କ୍ଲୋରୋପ୍ଲାଷ୍ଟର ଗ୍ରାନା (Grana) ରେ ଘଟେ। ଏଠାରେ ଆଲୋକ ଶକ୍ତି ରାସାୟନିକ ଶକ୍ତି (ATP ଓ NADPH) ରେ ରୂପାନ୍ତରିତ ହୁଏ ଏବଂ ଜଳର ଫୋଟୋଲିସିସ୍ (Photolysis) ଘଟି ଅମ୍ଳଜାନ (O₂) ନିର୍ଗତ ହୁଏ।\n\n" +
                                "ଖ) ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା (Dark Reaction / Calvin Cycle):\n" +
                                "କ୍ଲୋରୋପ୍ଲାଷ୍ଟର ଷ୍ଟ୍ରୋମା (Stroma) ରେ ଘଟେ। ଏଥିପାଇଁ ସିଧାସଳଖ ଆଲୋକ ଆବଶ୍ୟକ ହୁଏ ନାହିଁ। ଏଠାରେ CO₂ ବିଜାରିତ ହୋଇ ଗ୍ଲୁକୋଜ୍ (C₆H₁₂O₆) ଶର୍କରା ଉତ୍ପନ୍ନ ହୁଏ।",
                        contentEnglish = "Photosynthesis operates in two coupled sequential phases:\n" +
                                "A) Light-Dependent Reaction (Grana):\n" +
                                "Occurs in chloroplast thylakoid stacks (Grana). Photons excite electrons, generating ATP & NADPH, accompanied by photolysis of water which splits H₂O and liberates O₂.\n\n" +
                                "B) Light-Independent Reaction (Calvin Cycle / Stroma):\n" +
                                "Takes place in chloroplast stroma matrix. Uses assimilation power (ATP & NADPH) to reduce atmospheric CO₂ into carbohydrates (glucose).",
                        contentHindi = "प्रकाश संश्लेषण मुख्य रूप से दो क्रमिक चरणों में होता है:\n" +
                                "क) प्रकाश-निर्भर अभिक्रिया (Light Reaction / Hill Reaction):\n" +
                                "क्लोरोप्लास्ट के ग्रैना (Grana) में होती है। यहाँ सौर ऊर्जा रासायनिक ऊर्जा (ATP और NADPH) में परिवर्तित होती है और जल के प्रकाश-अपघटन से ऑक्सीजन (O₂) गैस निकलती है।\n\n" +
                                "ख) प्रकाश-स्वतंत्र अभिक्रिया (Dark Reaction / Calvin Cycle):\n" +
                                "क्लोरोप्लास्ट के स्ट्रोमा (Stroma) में होती है। इसके लिए सीधे प्रकाश की आवश्यकता नहीं होती। यहाँ CO₂ का अपचयन होकर ग्लूकोज (C₆H₁₂O₆) शर्करा का निर्माण होता है।"
                    )
                )
            ),
            quizQuestions = listOf(
                QuizQuestion(
                    id = "q1_photosynthesis",
                    questionOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟାରେ ଅମ୍ଳଜାନ କେଉଁଥିରୁ ନିର୍ଗତ ହୁଏ?",
                    questionEnglish = "In photosynthesis, oxygen is released from which compound?",
                    questionHindi = "प्रकाश संश्लेषण प्रक्रिया में ऑक्सीजन गैस किससे निकलती है?",
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
                    optionsHindi = listOf(
                        "A) CO₂",
                        "B) H₂O (सही)",
                        "C) क्लोरोफिल",
                        "D) ग्लूकोज"
                    ),
                    correctIndex = 1,
                    explanationOdia = "ଜଳର ଆଲୋକ ବିଭାଜନ (Photolysis of water) ଫଳରେ ଅମ୍ଳଜାନ ଗ୍ୟାସ୍ ନିର୍ଗତ ହୁଏ, ଅଙ୍ଗାରକାମ୍ଳରୁ ନୁହେଁ।",
                    explanationEnglish = "Photolysis of water molecules during the light-dependent reaction splits H₂O into protons, electrons, and releases O₂ gas.",
                    explanationHindi = "जल के प्रकाश-अपघटन (Photolysis of water) के परिणामस्वरूप ऑक्सीजन गैस निकलती है, कार्बन डाइऑक्साइड से नहीं।",
                    conceptTag = "photolysis_water_source",
                    misconceptionHintOdia = "⚠️ Concept Gap Detected: You selected CO₂. However, photolysis of water (H₂O) during light reactions is what splits and releases O₂, not CO₂.",
                    misconceptionHintEnglish = "⚠️ Concept Gap Detected: You selected CO₂. However, photolysis of water (H₂O) during light reactions is what splits and releases O₂, not CO₂.",
                    misconceptionHintHindi = "अवधारणा त्रुटि: ऑक्सीजन CO₂ से नहीं, बल्कि जल (H₂O) के प्रकाशीय अपघटन (Photolysis) से निकलती है।"
                ),
                QuizQuestion(
                    id = "q2_chloroplast_site",
                    questionOdia = "କ୍ଲୋରୋପ୍ଲାଷ୍ଟର କେଉଁ ଭାଗରେ ଆଲୋକ ପ୍ରକ୍ରିୟା (Light reaction) ସମ୍ପନ୍ନ ହୁଏ?",
                    questionEnglish = "In which region of the chloroplast does the light-dependent reaction occur?",
                    questionHindi = "क्लोरोप्लास्ट के किस भाग में प्रकाश अभिक्रिया (Light reaction) संपन्न होती है?",
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
                    optionsHindi = listOf(
                        "A) स्ट्रोमा (Stroma)",
                        "B) ग्रैना (Grana / Thylakoids)",
                        "C) कोशिका द्रव्य (Cytoplasm)",
                        "D) माइटोकॉन्ड्रिया"
                    ),
                    correctIndex = 1,
                    explanationOdia = "କ୍ଲୋରୋପ୍ଲାଷ୍ଟର ଥାଇଲାକଏଡ୍ ଗଦା ବା ଗ୍ରାନା (Grana) ରେ ଆଲୋକ ପ୍ରକ୍ରିୟା ଘଟେ। ଷ୍ଟ୍ରୋମାରେ ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା ଘଟେ।",
                    explanationEnglish = "The light reaction occurs in thylakoid stacks called Grana where chlorophyll is embedded. Dark reactions occur in Stroma.",
                    explanationHindi = "प्रकाश अभिक्रिया क्लोरोप्लास्ट के थाइलाकोइड या ग्रैना में होती है। अंधकार अभिक्रिया स्ट्रोमा में होती है।",
                    conceptTag = "chloroplast_anatomy_site",
                    misconceptionHintOdia = "⚠️ Concept Gap Detected: You selected Stroma. Stroma is where dark reactions/Calvin cycle take place; light reactions occur in Grana.",
                    misconceptionHintEnglish = "⚠️ Concept Gap Detected: You selected Stroma. Stroma is where dark reactions/Calvin cycle take place; light reactions occur in Grana.",
                    misconceptionHintHindi = "अवधारणा त्रुटि: आपने स्ट्रोमा चुना। स्ट्रोमा में अंधकार अभिक्रिया/केल्विन चक्र होता है, जबकि प्रकाश अभिक्रिया ग्रैना में होती है।"
                ),
                QuizQuestion(
                    id = "q3_gas_exchange",
                    questionOdia = "ପତ୍ରରେ ଗ୍ୟାସୀୟ ଆଦାନପ୍ରଦାନ କାହା ଦ୍ୱାରା ନିୟନ୍ତ୍ରିତ ହୁଏ?",
                    questionEnglish = "Which structure regulates photosynthetic gaseous exchange in plant leaves?",
                    questionHindi = "पत्तियों में प्रकाश संश्लेषक गैसीय विनिमय किसके द्वारा नियंत्रित होता है?",
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
                    optionsHindi = listOf(
                        "A) जाइलम (Xylem)",
                        "B) स्टोमेटा / रंध्र और द्वार कोशिकाएं",
                        "C) फ्लोएम (Phloem)",
                        "D) मूलरोम (Root Hairs)"
                    ),
                    correctIndex = 1,
                    explanationOdia = "ଷ୍ଟୋମାଟା ରନ୍ଧ୍ରର ଖୋଲିବା ଓ ବନ୍ଦ ହେବା ରକ୍ଷୀ କୋଷ (Guard cells) ଦ୍ୱାରା ନିୟନ୍ତ୍ରିତ ହୋଇ ବାୟୁମଣ୍ଡଳ ସହ CO₂ ଏବଂ O₂ ବିନିମୟ ହୁଏ।",
                    explanationEnglish = "Stomatal pores open and close via turgor pressure changes in surrounding guard cells to regulate CO₂ intake and transpiration.",
                    explanationHindi = "रंध्र (Stomata) और द्वार कोशिकाएं CO₂ और O₂ के विनिमय और वाष्पोत्सर्जन को नियंत्रित करती हैं।",
                    conceptTag = "stomata_regulation",
                    misconceptionHintOdia = "⚠️ Concept Gap Detected: Xylem and phloem are vascular transport tissues; gas exchange is regulated by stomata.",
                    misconceptionHintEnglish = "⚠️ Concept Gap Detected: Xylem and phloem are vascular transport tissues; gas exchange is regulated by stomata.",
                    misconceptionHintHindi = "अवधारणा त्रुटि: जाइलम जल परिवहन करता है, जबकि रंध्र (Stomata) गैसीय विनिमय को नियंत्रित करते हैं।"
                ),
                QuizQuestion(
                    id = "q4_end_product",
                    questionOdia = "ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା (Calvin Cycle) ର ମୁଖ୍ୟ ସ୍ଥାୟୀ ଉତ୍ପାଦ କ'ଣ?",
                    questionEnglish = "What is the primary organic product synthesized in the Calvin cycle?",
                    questionHindi = "अंधकार अभिक्रिया (केल्विन चक्र) के दौरान प्राथमिक उत्पाद क्या बनता है?",
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
                    optionsHindi = listOf(
                        "A) ग्लूकोज (कार्बोहाइड्रेट)",
                        "B) केवल ऑक्सीजन",
                        "C) लैक्टिक एसिड",
                        "D) पायरोविक एसिड"
                    ),
                    correctIndex = 0,
                    explanationOdia = "କ୍ୟାଲଭିନ୍ ଚକ୍ରରେ CO₂ ର ସ୍ଥିରୀକରଣ ଘଟି ପ୍ରାଥମିକ ଭାବରେ ଗ୍ଲୁକୋଜ୍ ଶର୍କରା ସୃଷ୍ଟି ହୁଏ, ଯାହା ପରେ ଷ୍ଟାର୍ଚରେ ସଞ୍ଚିତ ହୁଏ।",
                    explanationEnglish = "In the Calvin cycle, CO₂ fixation utilizes ATP and NADPH to synthesize glucose, which later condenses into storage starch.",
                    explanationHindi = "केल्विन चक्र स्ट्रोमा में ATP और NADPH की सहायता से CO₂ को ग्लूकोज में अपचयित करता है।",
                    conceptTag = "calvin_cycle_product",
                    misconceptionHintOdia = "⚠️ Concept Gap Detected: Plants synthesize simple glucose monomers first during Calvin cycle before storing as starch.",
                    misconceptionHintEnglish = "⚠️ Concept Gap Detected: Plants synthesize simple glucose monomers first during Calvin cycle before storing as starch.",
                    misconceptionHintHindi = "अवधारणा त्रुटि: प्रकाश अभिक्रिया ATP/NADPH और O₂ उत्पन्न करती है; अंधकार अभिक्रिया ग्लूकोज शर्करा बनाती है।"
                ),
                QuizQuestion(
                    id = "q5_energy_transfer",
                    questionOdia = "ଆଲୋକ ଶ୍ଳେଷଣରେ ସୌରଶକ୍ତି କେଉଁ ଶକ୍ତିକୁ ରୂପାନ୍ତରିତ ହୁଏ?",
                    questionEnglish = "During photosynthesis, solar energy is converted into which form of energy?",
                    questionHindi = "प्रकाश संश्लेषण में सौर ऊर्जा किस ऊर्जा में परिवर्तित होती है?",
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
                    optionsHindi = listOf(
                        "A) यांत्रिक ऊर्जा (Mechanical)",
                        "B) तापीय ऊर्जा (Thermal)",
                        "C) रासायनिक ऊर्जा (Chemical)",
                        "D) चुंबकीय ऊर्जा (Magnetic)"
                    ),
                    correctIndex = 2,
                    explanationOdia = "ସୂର୍ଯ୍ୟଙ୍କ ଆଲୋକ ଶକ୍ତି ଗ୍ଲୁକୋଜ୍ ଅଣୁର ରାସାୟନିକ ବନ୍ଧନ (Chemical bonds) ରେ ରାସାୟନିକ ଶକ୍ତି ଭାବେ ଆବଦ୍ଧ ହୁଏ।",
                    explanationEnglish = "Solar radiant energy is transduced and stored in covalent chemical bonds of organic carbohydrate molecules.",
                    explanationHindi = "सौर ऊर्जा रासायनिक बंधों में संचित होकर रासायनिक ऊर्जा (ग्लूकोज) में रूपांतरित होती है।",
                    conceptTag = "energy_transduction",
                    misconceptionHintOdia = "⚠️ Concept Gap Detected: Solar photon energy is converted into biochemical potential energy stored in covalent bonds.",
                    misconceptionHintEnglish = "⚠️ Concept Gap Detected: Solar photon energy is converted into biochemical potential energy stored in covalent bonds.",
                    misconceptionHintHindi = "अवधारणा त्रुटि: सौर ऊर्जा सहसंयोजक बंधों में संग्रहीत रासायनिक ऊर्जा में परिवर्तित होती है।"
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
                subject = "Science (ବିଜ୍ଞାନ / विज्ञान)",
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
                board = Board.ODISHA_STATE,
                classGrade = 9,
                subject = "Science (ବିଜ୍ଞାନ / विज्ञान)",
                chapterTitleOdia = "ଅଧ୍ୟାୟ ୧: ଜୀବକୋଷର ଗଠନ ଓ କାର୍ଯ୍ୟ",
                chapterTitleEnglish = "Chapter 1: Cell Structure & Function",
                topicTitleOdia = "ମାଇଟୋକଣ୍ଡ୍ରିଆ, ପ୍ଲାଷ୍ଟିଡ୍ ଓ କୋଷଜୀବକ",
                topicTitleEnglish = "Mitochondria, Plastids & Organelles",
                learningObjectiveOdia = "ଉଦ୍ଭିଦ ଓ ପ୍ରାଣୀ କୋଷର ଗଠନଗତ ପାର୍ଥକ୍ୟ ବୁଝିବା।",
                learningObjectiveEnglish = "Differentiate between plant and animal cellular anatomy.",
                packId = "relaynet_class9_science_cell_biology_mesh",
                isAvailableOffline = true
            ),
            CurriculumNode(
                id = "curr-node-brain-1",
                board = Board.ODISHA_STATE,
                classGrade = 9,
                subject = "Science & Biology (ଜୀବବିଜ୍ଞାନ / जीवविज्ञान)",
                chapterTitleOdia = "ଅଧ୍ୟାୟ ୫: ସ୍ନାୟୁତନ୍ତ୍ର ଓ ମାନବ ମସ୍ତିଷ୍କ",
                chapterTitleEnglish = "Chapter 5: Human Nervous System & Brain Anatomy",
                topicTitleOdia = "ମସ୍ତିଷ୍କର ମୁଖ୍ୟ ଭାଗ: ସେରେବ୍ରମ୍, ସେରେବେଲମ୍, ବ୍ରେନ୍-ଷ୍ଟେମ୍ ଓ ଆମିଗଡାଲା",
                topicTitleEnglish = "The Human Brain: The Master Control Center",
                learningObjectiveOdia = "ମାନବ ମସ୍ତିଷ୍କର ୪ଟି ମୁଖ୍ୟ ଅଂଶର ଗଠନ, କାର୍ଯ୍ୟ ଏବଂ ନ୍ୟୁରୋପ୍ଲାଷ୍ଟିସିଟି ବୁଝିବା।",
                learningObjectiveEnglish = "Master functional divisions of the cerebrum, cerebellum, brainstem, and amygdala.",
                packId = "class9_bio_human_brain",
                isAvailableOffline = true
            ),
            CurriculumNode(
                id = "curr-node-3",
                board = Board.CBSE,
                classGrade = 9,
                subject = "Science",
                chapterTitleOdia = "ଅଧ୍ୟାୟ ୬: ଉଦ୍ଭିଦ ଓ ପ୍ରାଣୀ ତନ୍ତୁ (Tissues)",
                chapterTitleEnglish = "Chapter 6: Tissues (Meristematic & Permanent)",
                topicTitleOdia = "ଜାଇଲେମ୍, ଫ୍ଲୋଏମ୍ ଓ ସଂବାହୀ ତନ୍ତୁ",
                topicTitleEnglish = "Vascular Bundles, Xylem & Phloem Transport",
                learningObjectiveOdia = "ସଂବାହୀ କଳାର କାର୍ଯ୍ୟ ଏବଂ ପରିବହନ ପ୍ରକ୍ରିୟା ଜାଣିବା।",
                learningObjectiveEnglish = "Explain bulk water and nutrient translocation.",
                packId = null,
                isAvailableOffline = false
            ),
            CurriculumNode(
                id = "curr-node-4",
                board = Board.CISCE_ICSE,
                classGrade = 9,
                subject = "Biology",
                chapterTitleOdia = "ଅଧ୍ୟାୟ ୭: ଶ୍ୱସନ ପ୍ରକ୍ରିୟା (Respiration in Plants)",
                chapterTitleEnglish = "Chapter 7: Respiration in Plants & Glycolysis",
                topicTitleOdia = "ଏରୋବିକ୍ ଓ ଅନେରୋବିକ୍ ଶ୍ୱସନ",
                topicTitleEnglish = "Aerobic vs Anaerobic Cellular Respiration",
                learningObjectiveOdia = "ଶ୍ୱସନ ଏବଂ ଆଲୋକ ଶ୍ଳେଷଣ ମଧ୍ୟରେ ଥିବା ସମ୍ପର୍କ ବୁଝିବା।",
                learningObjectiveEnglish = "Contrast cellular catabolism with photosynthetic synthesis.",
                packId = null,
                isAvailableOffline = false
            ),
            CurriculumNode(
                id = "curr-node-10-1",
                board = Board.ODISHA_STATE,
                classGrade = 10,
                subject = "Science (ବିଜ୍ଞାନ / विज्ञान)",
                chapterTitleOdia = "ଅଧ୍ୟାୟ ୧: ରାସାୟନିକ ପ୍ରତିକ୍ରିୟା ଓ ସମୀକରଣ",
                chapterTitleEnglish = "Chapter 1: Chemical Reactions and Equations",
                topicTitleOdia = "ରାସାୟନିକ ପ୍ରତିକ୍ରିୟାର ପ୍ରକାରଭେଦ ଓ ସନ୍ତୁଳନ",
                topicTitleEnglish = "Balancing Chemical Reactions & Equations",
                learningObjectiveOdia = "ରାସାୟନିକ ସମୀକରଣ କିପରି ସମତୁଲ କରିବା ଶିଖିବା।",
                learningObjectiveEnglish = "Master balancing chemical equations and reaction types.",
                packId = "relaynet_class10_science_chemical_reactions_v1",
                isAvailableOffline = true
            ),
            CurriculumNode(
                id = "curr-node-10-2",
                board = Board.CBSE,
                classGrade = 10,
                subject = "Science",
                chapterTitleOdia = "ଅଧ୍ୟାୟ ୧: ରାସାୟନିକ ପ୍ରତିକ୍ରିୟା (Chemical Reactions)",
                chapterTitleEnglish = "Chapter 1: Chemical Reactions and Equations",
                topicTitleOdia = "ଅକ୍ସିଡେସନ୍ ଓ ରିଡକ୍ସନ୍ ପ୍ରକ୍ରିୟା",
                topicTitleEnglish = "Oxidation and Reduction Reactions",
                learningObjectiveOdia = "ଜାରଣ ଓ ବିଜାରଣ ପ୍ରକ୍ରିୟା ବୁଝିବା।",
                learningObjectiveEnglish = "Understand redox reactions, corrosion and rancidity.",
                packId = null,
                isAvailableOffline = false
            )
        )
    }

    fun getOfflineTutorResponse(query: String): Pair<String, String> = getLocalTutorResponse(query)

    fun getLocalTutorResponse(query: String): Pair<String, String> {
        val q = query.lowercase()
        return when {
            q.contains("photosynthesis") || q.contains("ଆଲୋକ ଶ୍ଳେଷଣ") || q.contains("प्रकाश संश्लेषण") -> {
                Pair(
                    "ଆଲୋକ ଶ୍ଳେଷଣ ହେଉଛି ଏକ ଜୈବ-ରାସାୟନିକ ପ୍ରକ୍ରିୟା ଯାହା ଦ୍ୱାରା ସବୁଜ ଉଦ୍ଭିଦମାନେ ସୂର୍ଯ୍ୟାଲୋକ ଓ କ୍ଲୋରୋଫିଲ୍ ଉପସ୍ଥିତିରେ ଅଙ୍ଗାରକାମ୍ଳ (CO₂) ଓ ଜଳ (H₂O) ବ୍ୟବହାର କରି ଶର୍କରା ଜାତୀୟ ଖାଦ୍ୟ ପ୍ରସ୍ତୁତି କରନ୍ତି ଏବଂ ଅମ୍ଳଜାନ (O₂) ଗ୍ୟାସ୍ ନିର୍ଗତ କରନ୍ତି।",
                    "Photosynthesis is the biochemical process by which green plants utilize sunlight and chlorophyll to synthesize carbohydrate food (glucose) from carbon dioxide (CO₂) and water (H₂O), liberating oxygen (O₂) gas."
                )
            }
            q.contains("equation") || q.contains("formula") || q.contains("ସମୀକରଣ") || q.contains("समीकरण") -> {
                Pair(
                    "ଆଲୋକ ଶ୍ଳେଷଣର ସମତୁଲ ରାସାୟନିକ ସମୀକରଣ:\n6CO₂ + 12H₂O ──[ସୂର୍ଯ୍ୟାଲୋକ / କ୍ଲୋରୋଫିଲ୍]──> C₆H₁₂O₆ + 6O₂ + 6H₂O",
                    "Balanced chemical equation of photosynthesis:\n6CO₂ + 12H₂O ──[Sunlight / Chlorophyll]──> C₆H₁₂O₆ + 6O₂ + 6H₂O"
                )
            }
            q.contains("oxygen") || q.contains("ଅମ୍ଳଜାନ") || q.contains("h2o") || q.contains("water") || q.contains("ऑक्सीजन") -> {
                Pair(
                    "ଆଲୋକ ଶ୍ଳେଷଣରେ ଅମ୍ଳଜାନ (O₂) ମୁଖ୍ୟତଃ ଜଳର ଫୋଟୋଲିସିସ୍ (Photolysis of H₂O) ପ୍ରକ୍ରିୟାରୁ ନିର୍ଗତ ହୁଏ। ଏହା ଅଙ୍ଗାରକାମ୍ଳ (CO₂) ରୁ ଆସେ ନାହିଁ। କ୍ଲୋରୋପ୍ଲାଷ୍ଟର ଗ୍ରାନା (Grana) ରେ ଆଲୋକ ଉପସ୍ଥିତିରେ 2H₂O ──> 4H⁺ + 4e⁻ + O₂ ପ୍ରକ୍ରିୟା ଘଟେ।",
                    "Oxygen (O₂) is released exclusively from the photolysis of water (H₂O) inside chloroplast thylakoids (Grana), NOT from carbon dioxide (CO₂). The light energy splits water molecules: 2H₂O ──> 4H⁺ + 4e⁻ + O₂."
                )
            }
            q.contains("light") && (q.contains("dark") || q.contains("calvin") || q.contains("ଅନ୍ଧକାର") || q.contains("अंधकार")) -> {
                Pair(
                    "ଆଲୋକ ଶ୍ଳେଷଣ ଦୁଇଟି ପର୍ଯ୍ୟାୟରେ ଘଟେ:\n୧. ଆଲୋକ ପ୍ରକ୍ରିୟା (Light reaction): କ୍ଲୋରୋପ୍ଲାଷ୍ଟର ଗ୍ରାନାରେ ଘଟେ, ଯେଉଁଠାରେ ସୌରଶକ୍ତି ଦ୍ୱାରା ATP, NADPH ଏବଂ O₂ ସୃଷ୍ଟି ହୁଏ।\n୨. ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା (Calvin Cycle): ଷ୍ଟ୍ରୋମାରେ ଘଟେ, ଯେଉଁଠାରେ CO₂ ର ସ୍ଥିରୀକରଣ ଘଟି ଗ୍ଲୁକୋଜ୍ ଶର୍କରା ପ୍ରସ୍ତୁତ ହୁଏ।",
                    "Photosynthesis consists of two coordinated phases:\n1. Light-Dependent Phase: Occurs in Grana thylakoids, capturing photons to produce ATP, NADPH, and byproduct O₂.\n2. Light-Independent Phase (Calvin Cycle): Occurs in Stroma matrix, using ATP/NADPH to reduce CO₂ into glucose."
                )
            }
            q.contains("chlorophyll") || q.contains("କ୍ଲୋରୋଫିଲ୍") || q.contains("क्लोरोफिल") -> {
                Pair(
                    "କ୍ଲୋରୋଫିଲ୍ ହେଉଛି ଉଦ୍ଭିଦର ପତ୍ରରେ ଥିବା ସବୁଜ ରଙ୍ଗକଣିକା (Pigment)। ଏହା କ୍ଲୋରୋପ୍ଲାଷ୍ଟର ଥାଇଲାକଏଡ୍ ଝିଲ୍ଲୀରେ ଥାଏ। ଏହାର ମୁଖ୍ୟ କାର୍ଯ୍ୟ ହେଉଛି ସୂର୍ଯ୍ୟାଲୋକର ନୀଳ ଓ ଲାଲ୍ ତରଙ୍ଗଦୈର୍ଘ୍ୟର ଫୋଟନ୍ କଣିକା ଶୋଷଣ କରି ରାସାୟନିକ ପ୍ରକ୍ରିୟା ଆରମ୍ଭ କରିବା।",
                    "Chlorophyll is the primary photosynthetic pigment embedded in chloroplast thylakoid membranes. It efficiently absorbs blue and red wavelengths of solar radiation while reflecting green light, funneling resonance energy into reaction centers."
                )
            }
            else -> {
                Pair(
                    "ଆପଣଙ୍କ ପ୍ରଶ୍ନ '$query' ପାଇଁ ସ୍ଥାନୀୟ ପାଠ୍ୟକ୍ରମ ଉପଲବ୍ଧ। ଦୟାକରି ବିସ୍ତୃତ ଉତ୍ତର ପାଇଁ ଅନଲାଇନ୍ AI ସହିତ ସଂଯୋଗ କରନ୍ତୁ କିମ୍ବା ଆପଣଙ୍କ ପାଠ୍ୟପୁସ୍ତକ ଅଧ୍ୟାୟ ଅନୁସରଣ କରନ୍ତୁ।",
                    "For your question '$query', please consult your syllabus textbook or connect to online AI for a comprehensive multimodal solution."
                )
            }
        }
    }

    fun getSampleSharedPack(): LearningPack {
        return LearningPack(
            packId = "relaynet_class9_science_cell_biology_mesh",
            titleOdia = "ଅଧ୍ୟାୟ ୧: କୋଷ ଗଠନ ଓ କାର୍ଯ୍ୟ (Cell Biology)",
            titleEnglish = "Chapter 1: Cell Structure & Fundamental Unit of Life",
            titleHindi = "अध्याय 1: कोशिका संरचना एवं जीवन की मौलिक इकाई",
            board = Board.ODISHA_STATE,
            classGrade = 9,
            subject = "Science (ବିଜ୍ଞାନ / विज्ञान)",
            chapterNumber = 1,
            chapterOdia = "ଅଧ୍ୟାୟ ୧: ଜୀବକୋଷର ଗଠନ",
            chapterEnglish = "Chapter 1: The Fundamental Unit of Life",
            chapterHindi = "अध्याय 1: जीवन की मौलिक इकाई",
            topicOdia = "କୋଷ ଅଙ୍ଗିକା, ମାଇଟୋକଣ୍ଡ୍ରିଆ ଓ ପ୍ଲାଷ୍ଟିଡ୍",
            topicEnglish = "Cell Organelles, Mitochondria & Plastids",
            topicHindi = "कोशिकांग, माइटोकॉन्ड्रिया एवं लवक",
            version = 1,
            sizeBytes = 1450000L,
            checksumSha256 = "c9f1a23b5d7e8f90123456789abcdef0123456789abcdef0123456789abcdef0",
            isDownloaded = true,
            isReceivedViaMesh = true,
            origin = com.nmiet.app.domain.model.ContentOrigin.P2P_RECEIVED,
            peerSenderName = "Rahul-Phone (Nearby Peer)",
            sourceAttribution = "Odisha State Board BSE & Shared via ShikshaMesh",
            lesson = LessonData(
                summaryOdia = "କୋଷ ହେଉଛି ସମସ୍ତ ଜୀବମାନଙ୍କର ମୌଳିକ ଗଠନମୂଳକ ଓ କାର୍ଯ୍ୟକାରୀ ଏକକ। ରବର୍ଟ ହୁକ୍ ୧୬୬୫ ଖ୍ରୀଷ୍ଟାବ୍ଦରେ କୋଷ ଆବିଷ୍କାର କରିଥିଲେ।",
                summaryEnglish = "The cell is the basic structural and functional unit of all living organisms. Discovered by Robert Hooke in 1665.",
                summaryHindi = "कोशिका सभी जीवित जीवों की मौलिक संरचनात्मक और कार्यात्मक इकाई है। इसकी खोज रॉबर्ट हुक ने 1665 में की थी।",
                equationOrFormula = null,
                sections = emptyList()
            ),
            quizQuestions = emptyList()
        )
    }

    fun getPilotClass10LearningPack(): LearningPack {
        return LearningPack(
            packId = "relaynet_class10_science_chemical_reactions_v1",
            titleOdia = "ଅଧ୍ୟାୟ ୧: ରାସାୟନିକ ପ୍ରତିକ୍ରିୟା ଓ ସମୀକରଣ",
            titleEnglish = "Chapter 1: Chemical Reactions and Equations",
            titleHindi = "अध्याय 1: रासायनिक अभिक्रियाएँ एवं समीकरण",
            board = Board.ODISHA_STATE,
            classGrade = 10,
            subject = "Science (ବିଜ୍ଞାନ / विज्ञान)",
            chapterNumber = 1,
            chapterOdia = "ଅଧ୍ୟାୟ ୧: ରାସାୟନିକ ପ୍ରତିକ୍ରିୟା",
            chapterEnglish = "Chapter 1: Chemical Reactions",
            chapterHindi = "अध्याय 1: रासायनिक अभिक्रियाएँ",
            topicOdia = "ରାସାୟନିକ ପ୍ରତିକ୍ରିୟାର ପ୍ରକାରଭେଦ ଓ ସନ୍ତୁଳନ",
            topicEnglish = "Balancing Chemical Reactions & Equations",
            topicHindi = "रासायनिक समीकरणों का संतुलन एवं प्रकार",
            version = 1,
            sizeBytes = 2097152L,
            checksumSha256 = "d41d8cd98f00b204e9800998ecf8427e123456789abcdef0123456789abcdef0",
            isDownloaded = true,
            isReceivedViaMesh = false,
            sourceAttribution = "Odisha State Board of Secondary Education (BSE) Class 10 Science Curriculum & NCERT",
            lesson = LessonData(
                summaryOdia = "ରାସାୟନିକ ପ୍ରତିକ୍ରିୟାରେ ପ୍ରତିକାରକଗୁଡ଼ିକ ମିଶି ନୂତନ ଧର୍ମ ବିଶିଷ୍ଟ ଉତ୍ପାଦ ସୃଷ୍ଟି କରନ୍ତି। ଏଥିରେ ଭୌତିକ ଓ ରାସାୟନିକ ପରିବର୍ତ୍ତନ ଘଟେ।",
                summaryEnglish = "A chemical reaction is a process where substances undergo transformation to form new substances with distinct chemical identities.",
                summaryHindi = "रासायनिक अभिक्रिया वह प्रक्रिया है जिसमें एक या अधिक पदार्थ आपस में क्रिया करके नए गुणधर्म वाले पदार्थ बनाते हैं।",
                equationOrFormula = "2Mg + O₂ ──> 2MgO",
                sections = listOf(
                    LessonSection(
                        titleOdia = "୧. ରାସାୟନିକ ସମୀକରଣ ଓ ସନ୍ତୁଳନ",
                        titleEnglish = "1. Chemical Equations & Balancing",
                        titleHindi = "1. रासायनिक समीकरण एवं संतुलन",
                        contentOdia = "ପ୍ରତୀକ ଓ ସଂକେତ ମାଧ୍ୟମରେ ରାସାୟନିକ ପ୍ରତିକ୍ରିୟାର ସଂକ୍ଷିପ୍ତ ପ୍ରକାଶକୁ ରାସାୟନିକ ସମୀକରଣ କୁହାଯାଏ।",
                        contentEnglish = "A chemical equation represents the reactant and product molecules using chemical formulas.",
                        contentHindi = "प्रतीकों और रासायनिक सूत्रों के माध्यम से रासायनिक अभिक्रिया का संक्षिप्त निरूपण रासायनिक समीकरण कहलाता है।"
                    )
                )
            ),
            quizQuestions = emptyList()
        )
    }
}
