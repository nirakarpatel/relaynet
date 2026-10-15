package com.nmiet.app.data.local.entities

import android.content.Context
import com.nmiet.app.data.local.LearningDao
import com.nmiet.app.data.local.LearningPackEntity
import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.ContentOrigin
import com.nmiet.app.domain.model.KeyTerm
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.LessonData
import com.nmiet.app.domain.model.LessonSection
import com.nmiet.app.domain.model.QuizQuestion
import com.nmiet.app.domain.model.TopicMastery
import com.nmiet.app.ui.components.MultilingualString
import java.io.File

object BrainPackSeed {

    const val PACK_ID = "class9_bio_human_brain"
    const val VIDEO_LOCAL_ASSET = "videos/dr_binocs_human_brain.mp4"

    val TITLE = MultilingualString(
        english = "The Human Brain: The Master Control Center",
        odia = "ମାନବ ମସ୍ତିଷ୍କ: ମୁଖ୍ୟ ନିୟନ୍ତ୍ରଣ କେନ୍ଦ୍ର",
        hindi = "मानव मस्तिष्क: मुख्य नियंत्रण केंद्र"
    )

    fun createLearningPack(): LearningPack {
        return LearningPack(
            packId = PACK_ID,
            titleOdia = TITLE.odia,
            titleEnglish = TITLE.english,
            titleHindi = TITLE.hindi,
            board = Board.ODISHA_STATE,
            classGrade = 9,
            subject = "Science & Biology (ଜୀବବିଜ୍ଞାନ / जीवविज्ञान)",
            chapterNumber = 5,
            chapterOdia = "ଅଧ୍ୟାୟ ୫: ସ୍ନାୟୁତନ୍ତ୍ର ଓ ମାନବ ମସ୍ତିଷ୍କ",
            chapterEnglish = "Chapter 5: Human Nervous System & Brain Anatomy",
            chapterHindi = "अध्याय 5: तंत्रिका तंत्र एवं मानव मस्तिष्क",
            topicOdia = "ମସ୍ତିଷ୍କର ମୁଖ୍ୟ ଭାଗ: ସେରେବ୍ରମ୍, ସେରେବେଲମ୍, ବ୍ରେନ୍-ଷ୍ଟେମ୍ ଓ ଆମିଗଡାଲା",
            topicEnglish = "Core Brain Anatomy: Cerebrum, Cerebellum, Brainstem & Amygdala",
            topicHindi = "मस्तिष्क के मुख्य भाग: सेरेब्रम, सेरेबेलम, ब्रेनस्टेम एवं अमिग्डाला",
            version = 1,
            sizeBytes = 8_420_000L,
            checksumSha256 = "b7a69f4308e2d41b5698b964319c5b20a108b98e1fae9cf19a3b6f98a284e311",
            isDownloaded = true,
            isReceivedViaMesh = false,
            sourceAttribution = "NCERT Biology Class 9-10 & Dr. Binocs Educational Series (Peekaboo Kidz)",
            lesson = LessonData(
                summaryOdia = "ମାନବ ମସ୍ତିଷ୍କ ଶରୀରର ମୁଖ୍ୟ ନିୟନ୍ତ୍ରଣ କେନ୍ଦ୍ର ଭାବରେ କାର୍ଯ୍ୟ କରେ। ଏହା ସମସ୍ତ ଇନ୍ଦ୍ରିୟାନୁଭୂତି ପ୍ରକ୍ରିୟାକରଣ କରିବା ସହ ଉଭୟ ଐଚ୍ଛିକ ଏବଂ ଅନୈଚ୍ଛିକ କାର୍ଯ୍ୟକୁ ଦ୍ରୁତ ଗତିରେ ପରିଚାଳନା କରେ। ମସ୍ତିଷ୍କର ମୁଖ୍ୟ ଭାଗଗୁଡ଼ିକ ହେଲା: ସେରେବ୍ରମ୍ (୮୫% ଅଂଶ, ଚିନ୍ତାଶକ୍ତି ଓ ବୁଦ୍ଧିମତା), ସେରେବେଲମ୍ (ଭାରସାମ୍ୟ ଓ ସମନ୍ୱୟ), ବ୍ରେନ୍-ଷ୍ଟେମ୍ (ହୃଦସ୍ପନ୍ଦନ ଓ ଶ୍ୱାସକ୍ରିୟା ଭଳି ଜୀବନ ରକ୍ଷାକାରୀ କାର୍ଯ୍ୟ), ଏବଂ ଆମିଗଡାଲା (ଭାବନା ଓ ସ୍ମୃତି)। ଏହା ୧୨-୨୫ ୱାଟ୍ ବିଦ୍ୟୁତ୍ ଶକ୍ତି ଉତ୍ପନ୍ନ କରେ ଏବଂ ବ୍ୟାୟାମ ପରେ ନ୍ୟୁରୋପ୍ଲାଷ୍ଟିସିଟି ବୃଦ୍ଧି ପାଏ।",
                summaryEnglish = "The human brain acts as the central command center of the human body, processing sensory inputs and coordinating both conscious and involuntary actions faster than modern supercomputers. Key anatomical divisions include: Cerebrum (~85% of brain mass, cognition, memory, voluntary locomotion), Cerebellum (vestibular balance, posture, fine motor coordination), Brainstem (autonomic life-support: heartbeat, respiration, digestion), and Amygdala (emotions, survival fight-or-flight, event memories). Generates 12–25 W electrical power and benefits from exercise-induced neuroplasticity.",
                summaryHindi = "मानव मस्तिष्क मानव शरीर के केंद्रीय नियंत्रण केंद्र के रूप में कार्य करता है। मुख्य संरचनाएं: सेरेब्रम (85% द्रव्यमान, सोच, स्मृति, ऐच्छिक गति), सेरेबेलम (संतुलन, मुद्रा एवं गति समन्वय), ब्रेनस्टेम (हृदय गति, श्वास एवं अनैच्छिक जीवन कार्य), और अमिग्डाला (भावनाएं एवं स्मृति)। यह 12–25 वाट विद्युत ऊर्जा उत्पन्न करता है तथा व्यायाम से न्यूरोप्लास्टिसिटी बढ़ती है।",
                equationOrFormula = "Bioelectrical Output: P = 12–25 W (Powers an LED bulb) | Neuroplasticity: Aerobic Exercise ──> BDNF Release ──> Synaptic Potentiation",
                sections = listOf(
                    LessonSection(
                        titleOdia = "୧. ସେରେବ୍ରମ୍ (Cerebrum): ବୁଦ୍ଧିମତା ଓ ଐଚ୍ଛିକ କାର୍ଯ୍ୟ",
                        titleEnglish = "1. Cerebrum: Cognition, Memory & Voluntary Movement",
                        titleHindi = "1. सेरेब्रम: संज्ञान, स्मृति एवं ऐच्छिक गति",
                        contentOdia = "• ମସ୍ତିଷ୍କର ପ୍ରାୟ ୮୫% ଅଂଶ ସେରେବ୍ରମ୍ ଦ୍ୱାରା ଗଠିତ।\n• ଏହାର ଭଙ୍ଗିଯୁକ୍ତ ଗୋଲାର୍ଦ୍ଧ (Folded Hemispheres) ପୃଷ୍ଠଭାଗ କ୍ଷେତ୍ରଫଳ ବୃଦ୍ଧି କରି କୋଟି କୋଟି ନ୍ୟୁରନ୍ ଧାରଣ କରେ।\n• ଚିନ୍ତା କରିବା, ଶିଖିବା, ଗାଣିତିକ ସମସ୍ୟା ସମାଧାନ ଏବଂ ଚାଲିବା, ନାଚିବା ଭଳି ଐଚ୍ଛିକ ମାଂସପେଶୀ ଗତିବିଧିକୁ ନିୟନ୍ତ୍ରଣ କରେ।",
                        contentEnglish = "• Makes up approximately 85% of total brain mass with folded hemispheres maximizing surface area for neural connections.\n• Controls conscious thought, analytical thinking, academic learning, long-term memory, and voluntary motor actions (walking, dancing, reading).",
                        contentHindi = "• मस्तिष्क के कुल द्रव्यमान का लगभग 85% हिस्सा सेरेब्रम है।\n• यह सचेत सोच, बुद्धि, स्मृति और ऐच्छिक मांसपेशियों की गति (चलना, दौड़ना, पढ़ना) को नियंत्रित करता है।"
                    ),
                    LessonSection(
                        titleOdia = "୨. ସେରେବେଲମ୍ (Cerebellum): ଶାରୀରିକ ଭାରସାମ୍ୟ ଓ ସମନ୍ୱୟ",
                        titleEnglish = "2. Cerebellum: Vestibular Balance & Motor Coordination",
                        titleHindi = "2. सेरेबेलम: संतुलन एवं गति समन्वय",
                        contentOdia = "• ସେରେବ୍ରମ୍ ର ଠିକ୍ ତଳେ ଅବସ୍ଥିତ ଏକ କମ୍ପାକ୍ଟ ସଂରଚନା।\n• ଶରୀରର ମୁଦ୍ରା (Posture), ସ୍ଥିରତା (Equilibrium) ଏବଂ ସଠିକ୍ ଭାରସାମ୍ୟ ରକ୍ଷା କରେ।\n• ଯଦି ଏହା କ୍ଷତିଗ୍ରସ୍ତ ହୁଏ, ତେବେ ମଣିଷ ଚାଲିବାକୁ ଇଚ୍ଛା କଲେ ମଧ୍ୟ ସନ୍ତୁଳନ ହରାଇ ବାରମ୍ବାର ଝୁଣ୍ଟି ପଡିଥାଏ।",
                        contentEnglish = "• A compact structure tucked directly beneath the posterior cerebrum.\n• Fine-tunes motor execution, posture, and vestibular balance.\n• Pathology/Failure: If impaired, a person can decide to walk (cerebrum intact) but will stumble and lose balance repeatedly due to uncoordinated motor signals.",
                        contentHindi = "• सेरेब्रम के नीचे स्थित कॉम्पैक्ट संरचना जो शरीर के संतुलन और मुद्रा को नियंत्रित करती है।\n• इसके क्षतिग्रस्त होने पर व्यक्ति चलने का निर्णय तो ले सकता है किंतु संतुलन खोकर लड़खड़ाने लगता है।"
                    ),
                    LessonSection(
                        titleOdia = "୩. ବ୍ରେନ୍-ଷ୍ଟେମ୍ (Brainstem): ଅନୈଚ୍ଛିକ ଜୀବନ ରକ୍ଷାକାରୀ କେନ୍ଦ୍ର",
                        titleEnglish = "3. Brainstem: Autonomic Life-Sustaining Functions",
                        titleHindi = "3. ब्रेनस्टेम: अनैच्छिक जीवन-रक्षक कार्य",
                        contentOdia = "• ଏହା ମସ୍ତିଷ୍କକୁ ସିଧାସଳଖ ମେରୁଦଣ୍ଡ (Spinal Cord) ସହ ସଂଯୋଗ କରୁଥିବା ଏକ ସ୍ତମ୍ଭାକାର ସଂରଚନା।\n• ଶ୍ୱାସକ୍ରିୟା, ହୃଦସ୍ପନ୍ଦନ (Cardiac cycle), ଏବଂ ହଜମ କ୍ରିୟା (Digestion/Peristalsis) ଭଳି ଅନୈଚ୍ଛିକ ଜୀବନ ରକ୍ଷାକାରୀ କାର୍ଯ୍ୟ ପରିଚାଳନା କରେ।\n• ଏହି ଅଂଶ ଆମେ ଶୋଇଥିବା ସମୟରେ ମଧ୍ୟ ବିନା ବିରାମରେ ନିରନ୍ତର କାର୍ଯ୍ୟ କରିଥାଏ।",
                        contentEnglish = "• Columnar stalk bridging the brain directly to the spinal cord.\n• Autonomously governs involuntary vital functions: cardiac pace, respiration cadence, blood pressure, and gastrointestinal peristalsis.\n• Crucial fact: Never sleeps; maintains life-sustaining rhythms around the clock.",
                        contentHindi = "• मस्तिष्क को रीढ़ की हड्डी (Spinal Cord) से जोड़ने वाली स्तंभकार संरचना।\n• हृदय गति, श्वास एवं पाचन जैसी अनैच्छिक जीवन-रक्षक क्रियाओं को 24 घंटे नियंत्रित करती है।"
                    ),
                    LessonSection(
                        titleOdia = "୪. ଆମିଗଡାଲା (Amygdala) ଓ ମସ୍ତିଷ୍କର ବିଦ୍ୟୁତ୍ ଶକ୍ତି",
                        titleEnglish = "4. Amygdala & Neurophysiology Trivia",
                        titleHindi = "4. अमिग्डाला एवं तंत्रिका-शारीरिक तथ्य",
                        contentOdia = "• ଆମିଗଡାଲା: ବାଦାମ ଆକାରର ଏକ ସଂରଚନା ଯାହା ଭୟ, ଉତ୍ସାହ ଏବଂ 'Fight-or-Flight' ଜୀବନ ରକ୍ଷାକାରୀ ଭାବନା ପ୍ରକ୍ରିୟାକରଣ କରେ।\n• ବାୟୋ-ଇଲେକ୍ଟ୍ରିକାଲ୍ ଶକ୍ତି: ସକ୍ରିୟ ମାନବ ମସ୍ତିଷ୍କ ନିରନ୍ତର ୧୨ ରୁ ୨୫ ୱାଟ୍ ବିଦ୍ୟୁତ୍ ଶକ୍ତି ଉତ୍ପନ୍ନ କରେ, ଯାହା ଗୋଟିଏ ଏଲ୍.ଇ.ଡି (LED) ବଲ୍ବ ଜଳାଇବା ପାଇଁ ଯଥେଷ୍ଟ!\n• ବ୍ୟାୟାମ ଓ ନ୍ୟୁରୋପ୍ଲାଷ୍ଟିସିଟି: ଶାରୀରିକ ବ୍ୟାୟାମ ମସ୍ତିଷ୍କରେ BDNF ରାସାୟନିକ ପଦାର୍ଥ କ୍ଷରଣ କରେ ଯାହା କଠିନ ପାଠ ମନେରଖିବାରେ ଏବଂ ଗୃହକାର୍ଯ୍ୟ ସମାଧାନରେ ସାହାଯ୍ୟ କରେ।",
                        contentEnglish = "• Amygdala: An almond-shaped limbic structure central to emotional arousal (fear, excitement) and survival fight-or-flight instincts.\n• Electrical Output: The human brain continuously generates 12 to 25 Watts of bioelectricity—enough power to illuminate a low-voltage LED lightbulb.\n• Exercise-Induced Neuroplasticity: Aerobic exercise prompts secretion of neurotrophic factors (BDNF), enhancing synaptic plasticity and cognitive readiness before studying.",
                        contentHindi = "• अमिग्डाला: बादाम के आकार की संरचना जो भय, उत्तेजना और जीवन-रक्षा 'लड़ो या भागो' प्रतिक्रियाओं को संभालती है।\n• विद्युत उत्पादन: सक्रिय मस्तिष्क 12 से 25 वाट विद्युत ऊर्जा उत्पन्न करता है (एक एलईडी बल्ब जलाने योग्य)।\n• व्यायाम और न्यूरोप्लास्टिसिटी: एरोबिक व्यायाम से मस्तिष्क में न्यूरोट्रोफिक कारक स्रावित होते हैं जो सीखने की क्षमता बढ़ाते हैं।"
                    )
                )
            ),
            quizQuestions = getBrainQuizQuestions()
        )
    }

    fun getBrainQuizQuestions(): List<QuizQuestion> {
        return listOf(
            // Question 1: Cerebrum cognition
            QuizQuestion(
                id = "brain_q1_cerebrum_mass",
                questionOdia = "ମାନବ ମସ୍ତିଷ୍କର ପ୍ରାୟ ୮୫% ଅଂଶ କେଉଁ ଭାଗ ଦ୍ୱାରା ଗଠିତ ଯାହା ଚିନ୍ତାଶକ୍ତି ଓ ଐଚ୍ଛିକ ଗତିବିଧି ନିୟନ୍ତ୍ରଣ କରେ?",
                questionEnglish = "Which structure makes up approximately 85% of total brain mass and governs conscious thought, memory, and voluntary movement?",
                optionsOdia = listOf("A) ସେରେବେଲମ୍ (Cerebellum)", "B) ସେରେବ୍ରମ୍ (Cerebrum)", "C) ବ୍ରେନ୍-ଷ୍ଟେମ୍ (Brainstem)", "D) ଆମିଗଡାଲା (Amygdala)"),
                optionsEnglish = listOf("A) Cerebellum", "B) Cerebrum", "C) Brainstem", "D) Amygdala"),
                correctIndex = 1,
                explanationOdia = "ସେରେବ୍ରମ୍ ମସ୍ତିଷ୍କର ସର୍ବବୃହତ୍ ଭାଗ (୮୫%) ଏବଂ ଏହା ଚିନ୍ତା କରିବା, ଶିଖିବା ଓ ଐଚ୍ଛିକ ଗତିବିଧି ପରିଚାଳନା କରେ।",
                explanationEnglish = "The cerebrum comprises ~85% of total brain mass and controls conscious intelligence, memory, and voluntary muscle action.",
                conceptTag = "CEREBRUM_ANATOMY",
                misconceptionHintOdia = "ମନେରଖନ୍ତୁ: ସେରେବ୍ରମ୍ ହେଉଛି ୮୫% ସର୍ବବୃହତ୍ ଅଂଶ; ସେରେବେଲମ୍ କେବଳ ଭାରସାମ୍ୟ ରକ୍ଷା କରେ।",
                misconceptionHintEnglish = "Note: Cerebrum is the largest 85% cognitive segment; Cerebellum is the smaller balance coordinator."
            ),

            // Question 2: Cerebellar balance failure (DIAGNOSTIC INTERCEPTOR 1)
            QuizQuestion(
                id = "brain_q2_cerebellum_balance",
                questionOdia = "ଯଦି ଜଣେ ବ୍ୟକ୍ତି ଚାଲିବାକୁ ସ୍ଥିର କରନ୍ତି ମାତ୍ର ବାରମ୍ବାର ଭାରସାମ୍ୟ ହରାଇ ଝୁଣ୍ଟି ପଡନ୍ତି, ତେବେ ମସ୍ତିଷ୍କର କେଉଁ ଅଂଶ ପ୍ରଭାବିତ ହୋଇଛି?",
                questionEnglish = "If a patient can consciously decide to walk but continuously stumbles and loses equilibrium, which brain structure is impaired?",
                optionsOdia = listOf("A) ସେରେବ୍ରମ୍ (Cerebrum)", "B) ସେରେବେଲମ୍ (Cerebellum)", "C) ବ୍ରେନ୍-ଷ୍ଟେମ୍ (Brainstem)", "D) ଆମିଗଡାଲା (Amygdala)"),
                optionsEnglish = listOf("A) Cererum", "B) Cerebellum", "C) Brainstem", "D) Amygdala"),
                correctIndex = 1,
                explanationOdia = "ସେରେବ୍ରମ୍ ଚାଲିବାର ନିଷ୍ପତ୍ତି ନିଏ, କିନ୍ତୁ ସେରେବେଲମ୍ ଶାରୀରିକ ଭାରସାମ୍ୟ ଏବଂ ମାଂସପେଶୀର ସମନ୍ୱିତ ଗତି ରକ୍ଷା କରେ।",
                explanationEnglish = "The motor cortex in the cerebrum initiates the voluntary intent to move, but the cerebellum coordinates vestibular balance, posture, and smooth execution.",
                conceptTag = "CEREBELLAR_COORDINATION",
                misconceptionHintOdia = "⚠️ ନିଦାନ ଚେତାବନୀ: ସେରେବ୍ରମ୍ ମାଂସପେଶୀ ଗତି ଆରମ୍ଭ କରେ, ମାତ୍ର ଭାରସାମ୍ୟ ଓ ସନ୍ତୁଳନ ସେରେବେଲମ୍ ଦ୍ୱାରା ନିୟନ୍ତ୍ରିତ ହୁଏ।",
                misconceptionHintEnglish = "⚠️ Diagnostic Warning: Cerebral cortex initiates the voluntary intent to walk, but the Cerebellum fine-tunes vestibular balance and postural stability."
            ),

            // Question 3: Autonomic life support (DIAGNOSTIC INTERCEPTOR 2)
            QuizQuestion(
                id = "brain_q3_brainstem_autonomic",
                questionOdia = "ମସ୍ତିଷ୍କକୁ ସିଧାସଳଖ ମେରୁଦଣ୍ଡ ସହ ସଂଯୋଗ କରୁଥିବା କେଉଁ ସଂରଚନା ହୃଦସ୍ପନ୍ଦନ ଓ ଶ୍ୱାସକ୍ରିୟା ଭଳି ଅନୈଚ୍ଛିକ ଜୀବନ ରକ୍ଷାକାରୀ କାର୍ଯ୍ୟ ଚଳାଇଥାଏ?",
                questionEnglish = "Which columnar structure connects the brain directly to the spinal cord and regulates autonomic life-sustaining functions such as respiration and heartbeat?",
                optionsOdia = listOf("A) ସେରେବ୍ରମ୍ (Cerebrum)", "B) ବ୍ରେନ୍-ଷ୍ଟେମ୍ (Brainstem)", "C) ସେରେବେଲମ୍ (Cerebellum)", "D) ଆମିଗଡାଲା (Amygdala)"),
                optionsEnglish = listOf("A) Cerebrum", "B) Brainstem", "C) Cerebellum", "D) Amygdala"),
                correctIndex = 1,
                explanationOdia = "ବ୍ରେନ୍-ଷ୍ଟେମ୍ ମସ୍ତିଷ୍କ ଓ ମେରୁଦଣ୍ଡ ମଧ୍ୟରେ ସେତୁ ସଦୃଶ କାର୍ଯ୍ୟ କରି ଅନୈଚ୍ଛିକ ହୃଦୟ ଓ ଶ୍ୱାସକ୍ରିୟା ନିୟନ୍ତ୍ରଣ କରେ।",
                explanationEnglish = "The brainstem connects the brain to the spinal cord, autonomically regulating life-support rhythms including cardiac cycle and respiration.",
                conceptTag = "BRAINSTEM_AUTONOMIC",
                misconceptionHintOdia = "⚠️ ନିଦାନ ଚେତାବନୀ: ବ୍ରେନ୍-ଷ୍ଟେମ୍ ମେରୁଦଣ୍ଡ ସହ ସଂଯୁକ୍ତ ହୋଇ ହୃଦସ୍ପନ୍ଦନ ଓ ଶ୍ୱାସକ୍ରିୟା ଚଳାଏ।",
                misconceptionHintEnglish = "⚠️ Diagnostic Warning: The Brainstem links directly to the spinal cord to govern involuntary cardiac and respiratory life support."
            ),

            // Question 4: Amygdala emotions & memory
            QuizQuestion(
                id = "brain_q4_amygdala_role",
                questionOdia = "ମାନବ ସ୍ନାୟୁତନ୍ତ୍ରରେ ଆମିଗଡାଲା (Amygdala) ର ପ୍ରାଥମିକ କାର୍ଯ୍ୟ କ'ଣ?",
                questionEnglish = "What is the primary role of the amygdala in the human nervous system?",
                optionsOdia = listOf(
                    "A) ଐଚ୍ଛିକ ମାଂସପେଶୀ ନିୟନ୍ତ୍ରଣ",
                    "B) ଭୟ, ଉତ୍ସାହ ଓ ସ୍ମୃତି ଭଳି ଭାବନା ପ୍ରକ୍ରିୟାକରଣ",
                    "C) ରକ୍ତଚାପ ନିୟନ୍ତ୍ରଣ",
                    "D) ଶରୀରର ଭାରସାମ୍ୟ ରକ୍ଷା"
                ),
                optionsEnglish = listOf(
                    "A) Voluntary muscle locomotion",
                    "B) Processing emotional responses (fear, excitement) and encoding event memories",
                    "C) Blood pressure regulation",
                    "D) Fine vestibular balance"
                ),
                correctIndex = 1,
                explanationOdia = "ଆମିଗଡାଲା ହେଉଛି ଲିମ୍ବିକ୍ ସିଷ୍ଟମ୍ ର ଏକ ବାଦାମ ଆକାରର ଅଂଶ ଯାହା ଭାବନା ଓ ସର୍ଭାଇଭାଲ୍ ଇନଷ୍ଟିଙ୍କ୍ଟ ନିୟନ୍ତ୍ରଣ କରେ।",
                explanationEnglish = "The amygdala processes survival emotions (fear, excitement) and emotional memory consolidation.",
                conceptTag = "AMYGDALA_EMOTIONS",
                misconceptionHintOdia = "ଆମିଗଡାଲା ମୁଖ୍ୟତଃ ଭାବନା ଓ ଭୟ ପରିଚାଳନା କରେ, ମାଂସପେଶୀ ଗତିବିଧି ନୁହେଁ।",
                misconceptionHintEnglish = "The amygdala specializes in survival emotions and fight-or-flight arousal, not motor coordination."
            ),

            // Question 5: Wattage output (DIAGNOSTIC INTERCEPTOR 3)
            QuizQuestion(
                id = "brain_q5_electrical_wattage",
                questionOdia = "ଏକ ସକ୍ରିୟ ମାନବ ମସ୍ତିଷ୍କ ନିରନ୍ତର କେତେ ପରିମାଣର ବିଦ୍ୟୁତ୍ ଶକ୍ତି (Wattage) ଉତ୍ପନ୍ନ କରେ?",
                questionEnglish = "How much electrical power does an active human brain continuously generate?",
                optionsOdia = listOf("A) ୦.୧ ରୁ ୦.୫ ୱାଟ୍", "B) ୧୨ ରୁ ୨୫ ୱାଟ୍ (ଏଲ୍.ଇ.ଡି ବଲ୍ବ ଜଳାଇବା ପାଇଁ ଯଥେଷ୍ଟ)", "C) ୧୦୦ ରୁ ୨୫୦ ୱାଟ୍", "D) ୧୦୦୦ ୱାଟ୍"),
                optionsEnglish = listOf("A) 0.1 to 0.5 Watts", "B) 12 to 25 Watts (Sufficient to illuminate an LED bulb)", "C) 100 to 250 Watts", "D) 1000 Watts"),
                correctIndex = 1,
                explanationOdia = "ନ୍ୟୁରୋନଗୁଡ଼ିକର ବାୟୋ-ଇଲେକ୍ଟ୍ରିକାଲ୍ ସଙ୍କେତ ପ୍ରାୟ ୧୨ ରୁ ୨୫ ୱାଟ୍ ଶକ୍ତି ଉତ୍ପନ୍ନ କରେ ଯାହା ଗୋଟିଏ LED ବଲ୍ବ ଜଳାଇପାରେ।",
                explanationEnglish = "Active neural networks generate between 12 W and 25 W of electrical power—enough to illuminate a low-voltage LED bulb.",
                conceptTag = "NEURAL_BIOELECTRICITY",
                misconceptionHintOdia = "⚠️ ନିଦାନ ଚେତାବନୀ: ମସ୍ତିଷ୍କ ପ୍ରାୟ ୧୨-୨୫ ୱାଟ୍ ଶକ୍ତି ଉତ୍ପନ୍ନ କରେ—ଏକ କମ୍ ଭୋଲ୍ଟେଜ୍ LED ବଲ୍ବ ପାଇଁ ଯଥେଷ୍ଟ!",
                misconceptionHintEnglish = "⚠️ Diagnostic Warning: Neural bioelectricity generates between 12–25 W—sufficient to power a low-voltage LED lightbulb!"
            ),

            // Question 6: Exercise & neuroplasticity
            QuizQuestion(
                id = "brain_q6_exercise_plasticity",
                questionOdia = "ଏରୋବିକ୍ ବ୍ୟାୟାମ କିପରି ଛାତ୍ରଛାତ୍ରୀମାନଙ୍କୁ କଠିନ ଗୃହକାର୍ଯ୍ୟ ଓ ପାଠ ମନେରଖିବାରେ ସାହାଯ୍ୟ କରେ?",
                questionEnglish = "How does aerobic exercise directly enhance cognitive readiness and problem-solving capability when tackling difficult homework?",
                optionsOdia = listOf(
                    "A) ମସ୍ତିଷ୍କରୁ ଗ୍ଲୁକୋଜ୍ କମାଇଦିଏ",
                    "B) ନ୍ୟୁରୋଟ୍ରୋଫିକ୍ ଫ୍ୟାକ୍ଟର (BDNF) କ୍ଷରଣ ବୃଦ୍ଧି କରି ସାଇନାପ୍ଟିକ୍ ପ୍ଲାଷ୍ଟିସିଟି ବଢ଼ାଏ",
                    "C) ସେରେବ୍ରମ୍ କୁ ନିଷ୍କ୍ରିୟ କରିଦିଏ",
                    "D) ସ୍ନାୟୁ ପ୍ରବାହ ବନ୍ଦ କରିଦିଏ"
                ),
                optionsEnglish = listOf(
                    "A) By depleting cerebral glucose reserves",
                    "B) By prompting secretion of neurotrophic factors that enhance synaptic plasticity",
                    "C) By deactivating cerebral cortex",
                    "D) By pausing nerve impulses"
                ),
                correctIndex = 1,
                explanationOdia = "ଶାରୀରିକ ବ୍ୟାୟାମ ନ୍ୟୁରୋପ୍ଲାଷ୍ଟିସିଟି ବଢ଼ାଏ, ଯାହା ନୂତନ ତଥ୍ୟ ଶିଖିବାରେ ସାହାଯ୍ୟ କରେ।",
                explanationEnglish = "Aerobic exertion stimulates neurotrophic factors (like BDNF) enhancing synaptic plasticity and cognitive receptivity.",
                conceptTag = "NEUROPLASTICITY_EXERCISE",
                misconceptionHintOdia = "ବ୍ୟାୟାମ ମସ୍ତିଷ୍କରେ ଶକ୍ତିଶାଳୀ ନ୍ୟୁରୋକେମିକାଲ୍ ସୃଷ୍ଟି କରି ବୁଝିବା ଶକ୍ତି ବୃଦ୍ଧି କରେ।",
                misconceptionHintEnglish = "Exercise stimulates neuroplasticity-enhancing growth factors, increasing cognitive problem-solving ability."
            ),

            // Question 7: Survival fight-or-flight
            QuizQuestion(
                id = "brain_q7_fight_or_flight",
                questionOdia = "ଏକ ଜରୁରୀକାଳୀନ ବିପଦ ସମୟରେ 'Fight-or-Flight' ପ୍ରତିକ୍ରିୟା କେଉଁ ସଂରଚନା ଦ୍ୱାରା ତୁରନ୍ତ ଉଦ୍ଦୀପିତ ହୁଏ?",
                questionEnglish = "In an emergency survival situation, which structure rapidly triggers the fight-or-flight instinct?",
                optionsOdia = listOf("A) ସେରେବେଲମ୍", "B) ଆମିଗଡାଲା (Amygdala)", "C) ଷ୍ଟୋମାଟା", "D) କର୍ଣ୍ଣପଟହ"),
                optionsEnglish = listOf("A) Cerebellum", "B) Amygdala", "C) Stomata", "D) Tympanum"),
                correctIndex = 1,
                explanationOdia = "ଆମିଗଡାଲା ବିପଦକୁ ଦ୍ରୁତ ଗତିରେ ଅନୁଭବ କରି ଶରୀରକୁ ରକ୍ଷା ପାଇବା ପାଇଁ ପ୍ରସ୍ତୁତ କରେ।",
                explanationEnglish = "The amygdala detects sudden threats and activates the immediate survival fight-or-flight response.",
                conceptTag = "FIGHT_OR_FLIGHT",
                misconceptionHintOdia = "ଆମିଗଡାଲା ବିପଦ ସମୟରେ ତୁରନ୍ତ ଆବେଗିକ ପ୍ରତିକ୍ରିୟା ଦେଇଥାଏ।",
                misconceptionHintEnglish = "The amygdala processes rapid emotional survival responses before conscious reflection."
            ),

            // Question 8: Brainstem cessation
            QuizQuestion(
                id = "brain_q8_brainstem_damage",
                questionOdia = "ବ୍ରେନ୍-ଷ୍ଟେମ୍ କ୍ଷତିଗ୍ରସ୍ତ ହେଲେ ତୁରନ୍ତ ପ୍ରାଣଘାତୀ ସ୍ଥିତି କାହିଁକି ଉପୁଜେ?",
                questionEnglish = "Why does severe trauma to the brainstem result in an immediate life-threatening emergency?",
                optionsOdia = listOf(
                    "A) ସ୍ମୃତିଶକ୍ତି ହଜିଯାଏ",
                    "B) ଅନୈଚ୍ଛିକ ହୃଦସ୍ପନ୍ଦନ ଓ ଶ୍ୱାସକ୍ରିୟା ସଙ୍ଗେ ସଙ୍ଗେ ବନ୍ଦ ହୋଇଯାଏ",
                    "C) ଶରୀର ଚାଲିବା ବନ୍ଦ କରେ",
                    "D) ଆଖି ଲାଲ ହୋଇଯାଏ"
                ),
                optionsEnglish = listOf(
                    "A) It impairs long-term memory formation",
                    "B) It causes immediate cessation of involuntary life functions (breathing and heartbeat)",
                    "C) It prevents conscious walking decisions",
                    "D) It only alters eye pupil color"
                ),
                correctIndex = 1,
                explanationOdia = "ବ୍ରେନ୍-ଷ୍ଟେମ୍ ଶ୍ୱାସକ୍ରିୟା ଏବଂ ରକ୍ତ ପ୍ରସାରଣର କେନ୍ଦ୍ର, ତେଣୁ ଏହା ବନ୍ଦ ହେଲେ ଜୀବନହାନି ଘଟେ।",
                explanationEnglish = "The brainstem controls autonomic respiratory and cardiac pacing; its failure immediately arrests life-support.",
                conceptTag = "BRAINSTEM_VITAL_SUPPORT",
                misconceptionHintOdia = "ବ୍ରେନ୍-ଷ୍ଟେମ୍ ହୃତ୍‌ପିଣ୍ଡ ଓ ଫୁସଫୁସର ଅନୈଚ୍ଛିକ କାର୍ଯ୍ୟ ପରିଚାଳନା କରେ।",
                misconceptionHintEnglish = "The brainstem autonomously drives vital heartbeat and respiratory cycles necessary for survival."
            ),

            // Question 9: Fine motor regulation
            QuizQuestion(
                id = "brain_q9_fine_motor",
                questionOdia = "ଗୋଟିଏ ଗୋଡରେ ସନ୍ତୁଳିତ ହୋଇ ଠିଆ ହେବା କିମ୍ବା ଛୁଞ୍ଚିରେ ସୂତା ପରଖିବା ଭଳି ସୂକ୍ଷ୍ମ କାର୍ଯ୍ୟ କାହା ଉପରେ ନିର୍ଭର କରେ?",
                questionEnglish = "Fine-tuning postural stability, such as balancing on one leg or delicate motor execution, relies directly on which structure?",
                optionsOdia = listOf("A) ସେରେବ୍ରମ୍", "B) ସେରେବେଲମ୍ (Cerebellum)", "C) ଖପୁରୀ (Skull)", "D) ମେରୁଦଣ୍ଡ ମଜ୍ଜା"),
                optionsEnglish = listOf("A) Cerebrum", "B) Cerebellum", "C) Cranium", "D) Bone marrow"),
                correctIndex = 1,
                explanationOdia = "ସେରେବେଲମ୍ ସୂକ୍ଷ୍ମ ମାଂସପେଶୀ ସନ୍ତୁଳନ ଏବଂ ଭାରସାମ୍ୟ ନିୟନ୍ତ୍ରଣ କରିଥାଏ।",
                explanationEnglish = "The cerebellum continuously coordinates vestibular feedback to maintain equilibrium and fine motor stability.",
                conceptTag = "FINE_MOTOR_BALANCE",
                misconceptionHintOdia = "ସେରେବେଲମ୍ ହେଉଛି ଭାରସାମ୍ୟ ଓ ସୂକ୍ଷ୍ମ ସନ୍ତୁଳନର ମୁଖ୍ୟ କେନ୍ଦ୍ର।",
                misconceptionHintEnglish = "Fine motor adjustments and posture are governed directly by cerebellar coordination."
            ),

            // Question 10: Cortical folding surface area
            QuizQuestion(
                id = "brain_q10_cortical_folding",
                questionOdia = "ସେରେବ୍ରମ୍ ର ଭଙ୍ଗିଯୁକ୍ତ ଗୋଲାର୍ଦ୍ଧ (Folded Hemispheres) ର ମୁଖ୍ୟ ଜୈବିକ ଉଦ୍ଦେଶ୍ୟ କ'ଣ?",
                questionEnglish = "The folded hemispheres of the cerebrum are an evolutionary adaptation that primarily allows:",
                optionsOdia = listOf(
                    "A) ମସ୍ତିଷ୍କକୁ ହାଲୁକା କରିବା ପାଇଁ",
                    "B) ସୀମିତ ଖପୁରୀ ମଧ୍ୟରେ ଅଧିକ କ୍ଷେତ୍ରଫଳ ଓ ନ୍ୟୁରନ୍ ଧାରଣ କରି ଜ୍ଞାନଶକ୍ତି ବୃଦ୍ଧି କରିବା",
                    "C) ରକ୍ତ ପ୍ରବାହ ବନ୍ଦ କରିବା",
                    "D) ନିଦ୍ରା ସମୟ ବୃଦ୍ଧି କରିବା"
                ),
                optionsEnglish = listOf(
                    "A) Reducing the weight of the head",
                    "B) Maximizing surface area to accommodate billions of interconnected neurons in a compact skull",
                    "C) Restricting cerebral blood perfusion",
                    "D) Inducing deeper sleep cycles"
                ),
                correctIndex = 1,
                explanationOdia = "ସେରେବ୍ରମ୍ ର କୁଞ୍ଚିତ ସଂରଚନା ପୃଷ୍ଠଭାଗ କ୍ଷେତ୍ରଫଳକୁ ଅନେକ ଗୁଣ ବୃଦ୍ଧି କରି କୋଟି କୋଟି ନ୍ୟୁରନ୍ ସଂଯୋଗକୁ ସମ୍ଭବ କରେ।",
                explanationEnglish = "Cortical folding (gyri and sulci) maximizes surface area, allowing billions of interconnected neurons to fit within the human cranium.",
                conceptTag = "CEREBRAL_SURFACE_AREA",
                misconceptionHintOdia = "ଭଙ୍ଗିଯୁକ୍ତ ଗୋଲାର୍ଦ୍ଧ ଅଧିକ ନ୍ୟୁରନ୍ ଓ ବୁଦ୍ଧିମତା ପାଇଁ ପୃଷ୍ଠଭାଗ ବୃଦ୍ଧି କରେ।",
                misconceptionHintEnglish = "Folding packs extensive surface area into the skull, enabling sophisticated cognitive processing."
            )
        )
    }

    suspend fun seedBrainPackIntoDatabase(dao: LearningDao, context: Context? = null) {
        val pack = createLearningPack()
        val entity = LearningPackEntity.fromDomain(pack).copy(
            origin = ContentOrigin.INTERNET_DOWNLOADED,
            isCachedLocally = true
        )
        dao.insertPack(entity)

        // Seed initial Topic Mastery for Human Brain (developing tier 50%)
        dao.insertOrUpdateMastery(
            com.nmiet.app.data.local.TopicMasteryEntity(
                topicId = PACK_ID,
                topicTitleOdia = TITLE.odia,
                topicTitleEnglish = TITLE.english,
                subject = pack.subject,
                masteryScore = 50,
                lastPracticedDate = System.currentTimeMillis(),
                attemptsCount = 0,
                recommendedNextAction = "Watch Brain Video Companion & Take 10-Question Diagnostic"
            )
        )

        // Ensure internal files dir video placeholder exists so ExoPlayer can resolve it offline
        if (context != null) {
            try {
                val videoDir = File(context.filesDir, "videos")
                if (!videoDir.exists()) videoDir.mkdirs()
                val videoFile = File(videoDir, "dr_binocs_human_brain.mp4")
                if (!videoFile.exists()) {
                    // Create a minimal placeholder video file if absent
                    videoFile.writeBytes(ByteArray(1024))
                }
            } catch (_: Exception) {
                // Non-fatal if filesystem is restricted
            }
        }
    }
}
