from typing import List, Dict, Optional
from app.models import (
    CurriculumNode, BoardType, LessonContent,
    KeyGlossaryTerm, QuizQuestion, QuizOption, ContentPackSummary
)

CURRICULUM_CATALOG: List[CurriculumNode] = [
    # Odisha State Board
    CurriculumNode(
        id="odisha_c9_sci_photosynthesis",
        board=BoardType.ODISHA_STATE,
        class_grade=9,
        subject="Science (Biology)",
        unit_chapter="Chapter 1: ପୋଷଣ (Nutrition)",
        topic="ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା (Photosynthesis Process)",
        learning_objective="Understand light reactions, photolysis of water, stroma vs granum reactions, and chloroplast function.",
        supported_languages=["ODIA", "ENGLISH"],
        has_offline_pack=True,
        content_version="v1.2.0"
    ),
    CurriculumNode(
        id="odisha_c9_sci_respiration",
        board=BoardType.ODISHA_STATE,
        class_grade=9,
        subject="Science (Biology)",
        unit_chapter="Chapter 2: ଶ୍ଵାସକ୍ରିୟା (Respiration)",
        topic="କୋଷୀୟ ଶ୍ଵାସକ୍ରିୟା ଏବଂ ଗ୍ଲାଇକୋଲିସିସ୍ (Cellular Respiration)",
        learning_objective="Differentiate between aerobic and anaerobic respiration and ATP synthesis in mitochondria.",
        supported_languages=["ODIA", "ENGLISH"],
        has_offline_pack=False,
        content_version="v1.0.0"
    ),
    CurriculumNode(
        id="odisha_c10_sci_heredity",
        board=BoardType.ODISHA_STATE,
        class_grade=10,
        subject="Science (Biology)",
        unit_chapter="Chapter 7: ବଂଶାନୁକ୍ରମ (Heredity)",
        topic="ମେଣ୍ଡେଲଙ୍କ ନିୟମ (Mendel's Laws of Inheritance)",
        learning_objective="Monohybrid & dihybrid cross experiments and dominant vs recessive alleles.",
        supported_languages=["ODIA", "ENGLISH"],
        has_offline_pack=False,
        content_version="v1.0.0"
    ),
    # CBSE
    CurriculumNode(
        id="cbse_c9_sci_cell",
        board=BoardType.CBSE,
        class_grade=9,
        subject="Science (Biology)",
        unit_chapter="Chapter 5: The Fundamental Unit of Life",
        topic="Cell Organelles: Chloroplast & Mitochondria",
        learning_objective="Structural organization of plant cells, plastids, and energy transformation organelles.",
        supported_languages=["ENGLISH", "ODIA"],
        has_offline_pack=True,
        content_version="v1.1.0"
    ),
    CurriculumNode(
        id="cbse_c10_sci_life_processes",
        board=BoardType.CBSE,
        class_grade=10,
        subject="Science (Biology)",
        unit_chapter="Chapter 6: Life Processes",
        topic="Autotrophic Nutrition & Light Dependent Reactions",
        learning_objective="Equation of photosynthesis, role of stomata in gas exchange, and necessity of sunlight.",
        supported_languages=["ENGLISH", "ODIA"],
        has_offline_pack=True,
        content_version="v1.2.0"
    ),
    # CISCE / ICSE
    CurriculumNode(
        id="icse_c9_sci_plant_physiology",
        board=BoardType.CISCE_ICSE,
        class_grade=9,
        subject="Biology",
        unit_chapter="Theme 3: Plant Physiology",
        topic="Photosynthesis: Carbon Assimilation & Pigments",
        learning_objective="Hill reaction, role of NADP, Chlorophyll-a absorption spectra, and starch formation.",
        supported_languages=["ENGLISH", "ODIA"],
        has_offline_pack=True,
        content_version="v1.0.0"
    ),
    # Additional Classes 6-8 nodes for rich multi-board coverage
    CurriculumNode(
        id="odisha_c6_sci_food",
        board=BoardType.ODISHA_STATE,
        class_grade=6,
        subject="Science",
        unit_chapter="Chapter 1: ଆମ ଖାଦ୍ୟ (Our Food)",
        topic="ଖାଦ୍ୟର ଉତ୍ସ ଏବଂ ପୋଷକ ତତ୍ତ୍ଵ (Sources of Food & Nutrients)",
        learning_objective="Understand dietary components, carbohydrates, proteins, vitamins, and minerals.",
        supported_languages=["ODIA", "ENGLISH"],
        has_offline_pack=True,
        content_version="v1.0.0"
    ),
    CurriculumNode(
        id="odisha_c7_sci_nutrition_plants",
        board=BoardType.ODISHA_STATE,
        class_grade=7,
        subject="Science",
        unit_chapter="Chapter 1: ଉଦ୍ଭିଦରେ ପୋଷଣ (Nutrition in Plants)",
        topic="ସ୍ୱୟଂପୋଷୀ ଏବଂ ପରପୋଷୀ ପୋଷଣ (Autotrophic & Heterotrophic Nutrition)",
        learning_objective="Study saprophytic, parasitic, and symbiotic modes of nutrition in flora.",
        supported_languages=["ODIA", "ENGLISH"],
        has_offline_pack=True,
        content_version="v1.0.0"
    ),
    CurriculumNode(
        id="odisha_c8_sci_microorganisms",
        board=BoardType.ODISHA_STATE,
        class_grade=8,
        subject="Science",
        unit_chapter="Chapter 2: ଅଣୁଜୀବ: ବନ୍ଧୁ ଓ ଶତ୍ରୁ (Microorganisms)",
        topic="ଅଣୁଜୀବଙ୍କ ଶ୍ରେଣୀବିଭାଗ ଏବଂ ଉପଯୋଗିତା (Classification of Microbes)",
        learning_objective="Examine bacteria, fungi, algae, protozoa, and their agricultural/medical roles.",
        supported_languages=["ODIA", "ENGLISH"],
        has_offline_pack=True,
        content_version="v1.0.0"
    ),
    CurriculumNode(
        id="cbse_c6_sci_components_food",
        board=BoardType.CBSE,
        class_grade=6,
        subject="Science",
        unit_chapter="Chapter 2: Components of Food",
        topic="Balanced Diet and Deficiency Diseases",
        learning_objective="Identify major nutrient groups, balanced diets, and prevent nutritional deficiencies.",
        supported_languages=["ENGLISH", "ODIA"],
        has_offline_pack=True,
        content_version="v1.0.0"
    ),
    CurriculumNode(
        id="cbse_c7_sci_nutrition_plants",
        board=BoardType.CBSE,
        class_grade=7,
        subject="Science",
        unit_chapter="Chapter 1: Nutrition in Plants",
        topic="Mode of Nutrition in Plants and Photosynthesis",
        learning_objective="Analyze how green plants produce simple carbohydrates and utilize chlorophyll.",
        supported_languages=["ENGLISH", "ODIA"],
        has_offline_pack=True,
        content_version="v1.0.0"
    ),
    CurriculumNode(
        id="cbse_c8_sci_crop_production",
        board=BoardType.CBSE,
        class_grade=8,
        subject="Science",
        unit_chapter="Chapter 1: Crop Production and Management",
        topic="Agricultural Practices and Irrigation",
        learning_objective="Learn sowing, soil preparation, modern irrigation, and storage techniques.",
        supported_languages=["ENGLISH", "ODIA"],
        has_offline_pack=True,
        content_version="v1.0.0"
    )
]


PILOT_LESSON_CONTENT = LessonContent(
    title_odia="ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା ଏବଂ ଉଦ୍ଭିଦ ପୋଷଣ",
    title_english="Photosynthesis Process & Plant Nutrition",
    overview_odia="ଆଲୋକ ଶ୍ଳେଷଣ ହେଉଛି ଏକ ଜୈବ ରାସାୟନିକ ପ୍ରକ୍ରିୟା ଯାହା ଦ୍ୱାରା ସବୁଜ ଉଦ୍ଭିଦମାନେ ସୂର୍ଯ୍ୟାଲୋକ, ଜଳ (H₂O) ଏବଂ ଅଙ୍ଗାରକାମ୍ଳ (CO₂) ବ୍ୟବହାର କରି ଶର୍କରା ପ୍ରସ୍ତୁତ କରନ୍ତି ଏବଂ ଅମ୍ଳଜାନ (O₂) ନିର୍ଗତ କରନ୍ତି।",
    overview_english="Photosynthesis is a vital biochemical process wherein autotrophic green plants utilize sunlight, water (H₂O), and atmospheric carbon dioxide (CO₂) to synthesize carbohydrate energy while discharging oxygen (O₂).",
    chemical_equation="6CO₂ + 12H₂O ──[ସୂର୍ଯ୍ୟାଲୋକ / କ୍ଲୋରୋଫିଲ୍]──► C₆H₁₂O₆ + 6H₂O + 6O₂ ↑",
    detailed_sections_odia=[
        "୧. ଆଲୋକ ପ୍ରକ୍ରିୟା (Light-dependent Reaction): ଏହା ହରିତକଣାର ଗ୍ରାନାରେ ସଂଘଟିତ ହୁଏ। ସୂର୍ଯ୍ୟାଲୋକର ଉପସ୍ଥିତିରେ ଜଳର ଆଲୋକ ବିଶ୍ଳେଷଣ (Photolysis of Water) ଘଟି ଅମ୍ଳଜାନ (O₂), ATP ଏବଂ NADPH₂ ସୃଷ୍ଟି ହୁଏ।",
        "୨. ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା (Light-independent / Calvin Cycle): ଏହା ହରିତକଣାର ଷ୍ଟ୍ରୋମାରେ ସଂଘଟିତ ହୁଏ। ଏଠାରେ RuBisCO ଏନଜାଇମ ସାହାଯ୍ୟରେ CO₂ ବିଜାରଣ ଘଟି ଗ୍ଲୁକୋଜ ସୃଷ୍ଟି ହୁଏ।",
        "୩. ପରିବେଶୀୟ ମହତ୍ତ୍ୱ: ଆଲୋକ ଶ୍ଳେଷଣ ପୃଥିବୀରେ ସମସ୍ତ ଜୀବଜଗତ ପାଇଁ ଖାଦ୍ୟ ଏବଂ ଜୀବନଧାରଣକାରୀ ଅମ୍ଳଜାନର ମୂଳ ଉତ୍ସ।"
    ],
    detailed_sections_english=[
        "1. Light Reaction: Occurs within the thylakoid membrane/grana of chloroplasts. Photolysis of water splits H₂O using solar photons, evolving oxygen and producing energy currency (ATP & NADPH).",
        "2. Dark Reaction (Calvin Cycle): Transpires in the chloroplast stroma. Carbon dioxide is enzymatically reduced into hexose sugars (glucose) catalyzed by the RuBisCO enzyme.",
        "3. Ecological Significance: Maintains atmospheric oxygen equilibrium and acts as the universal base for biological food chains."
    ],
    key_terms=[
        KeyGlossaryTerm(
            odia_term="ହରିତକଣା (Chloroplast)",
            english_term="Chloroplast",
            definition="The double-membrane cell organelle containing thylakoids and chlorophyll where photosynthesis occurs."
        ),
        KeyGlossaryTerm(
            odia_term="ଜଳର ଆଲୋକ ବିଶ୍ଳେଷଣ (Photolysis)",
            english_term="Photolysis of Water",
            definition="Splitting of water molecules into hydrogen ions, electrons, and oxygen gas in the presence of light."
        ),
        KeyGlossaryTerm(
            odia_term="ଷ୍ଟ୍ରୋମା (Stroma)",
            english_term="Stroma",
            definition="Fluid matrix inside chloroplasts containing enzymes where light-independent enzymatic carbon fixation takes place."
        )
    ]
)

PILOT_QUIZ_QUESTIONS: List[QuizQuestion] = [
    QuizQuestion(
        id="q1",
        question_odia="ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟାରେ ନିର୍ଗତ ହେଉଥିବା ଅମ୍ଳଜାନ (O₂) ର ମୂଳ ଉତ୍ସ କଣ?",
        question_english="What is the actual biochemical source of oxygen (O₂) evolved during photosynthesis?",
        options=[
            QuizOption(id="a", text_odia="ଜଳ (H₂O)", text_english="Water (H₂O)"),
            QuizOption(id="b", text_odia="ଅଙ୍ଗାରକାମ୍ଳ (CO₂)", text_english="Carbon Dioxide (CO₂)", misconception_explanation="Common misconception: Many students think O₂ comes from CO₂, but Robert Hill proved it originates from the photolysis of H₂O."),
            QuizOption(id="c", text_odia="ଗ୍ଲୁକୋଜ (C₆H₁₂O₆)", text_english="Glucose (C₆H₁₂O₆)", misconception_explanation="Glucose is the synthesized carbohydrate output, not the origin of free oxygen gas."),
            QuizOption(id="d", text_odia="କ୍ଲୋରୋଫିଲ୍ (Chlorophyll)", text_english="Chlorophyll", misconception_explanation="Chlorophyll is the light-absorbing pigment, not the reactant source of oxygen atoms.")
        ],
        correct_option_id="a",
        explanation_odia="ଆଲୋକ ପ୍ରକ୍ରିୟା ସମୟରେ ଜଳର ଆଲୋକ ବିଶ୍ଳେଷଣ (Photolysis of Water) ଘଟି H₂O ରୁ ଅମ୍ଳଜାନ ଗ୍ୟାସ ଉତ୍ପନ୍ନ ହୁଏ।",
        explanation_english="During the light-dependent phase, photolysis of water splits H₂O into protons, electrons, and free molecular oxygen (O₂).",
        concept_tag="photolysis_water_source"
    ),
    QuizQuestion(
        id="q2",
        question_odia="ହରିତକଣାର କେଉଁ ଅଂଶରେ ଆଲୋକ ପ୍ରକ୍ରିୟା (Light reaction) ସଂପାଦିତ ହୁଏ?",
        question_english="Which microscopic sub-structure of the chloroplast hosts the light-dependent reactions?",
        options=[
            QuizOption(id="a", text_odia="ଷ୍ଟ୍ରୋମା (Stroma)", text_english="Stroma", misconception_explanation="Stroma is the fluid matrix hosting the dark reaction (Calvin cycle), not the light reactions."),
            QuizOption(id="b", text_odia="ଗ୍ରାନା (Grana / Thylakoid)", text_english="Grana / Thylakoids"),
            QuizOption(id="c", text_odia="ବାହ୍ୟ ଝିଲ୍ଲୀ (Outer membrane)", text_english="Outer membrane", misconception_explanation="The outer membrane only serves as a selective protective envelope."),
            QuizOption(id="d", text_odia="କୋଷଜୀବକ (Cytoplasm)", text_english="Cytoplasm", misconception_explanation="Photosynthesis takes place strictly inside the chloroplast organelle.")
        ],
        correct_option_id="b",
        explanation_odia="ଆଲୋକ ପର୍ଯ୍ୟାୟ ଥାଇଲାକଏଡ୍ ପରଦାରେ ଥିବା ଗ୍ରାନାରେ ଅବସ୍ଥିତ କ୍ଲୋରୋଫିଲ୍ ଦ୍ୱାରା ସଂଘଟିତ ହୁଏ।",
        explanation_english="Light reactions take place in the thylakoid membranes arranged in stacks called grana where chlorophyll pigments are embedded.",
        concept_tag="chloroplast_anatomy_site"
    ),
    QuizQuestion(
        id="q3",
        question_odia="ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା (Calvin cycle) ପାଇଁ ନିମ୍ନଲିଖିତ ମଧ୍ୟରୁ କେଉଁଟି ପ୍ରମୁଖ ଏନଜାଇମ?",
        question_english="Which premier enzyme catalyzes carbon dioxide fixation in the dark reaction?",
        options=[
            QuizOption(id="a", text_odia="ରୁବିସ୍କୋ (RuBisCO)", text_english="RuBisCO (Ribulose-1,5-bisphosphate carboxylase-oxygenase)"),
            QuizOption(id="b", text_odia="ପେପସିନ୍ (Pepsin)", text_english="Pepsin", misconception_explanation="Pepsin is a stomach digestive enzyme in animals."),
            QuizOption(id="c", text_odia="ଆମାଇଲେଜ୍ (Amylase)", text_english="Amylase", misconception_explanation="Amylase hydrolyzes starch into sugars in digestion."),
            QuizOption(id="d", text_odia="ଡିଏନ୍ଏ ପଲିମରେଜ୍ (DNA Polymerase)", text_english="DNA Polymerase", misconception_explanation="DNA Polymerase is used in nucleic acid replication.")
        ],
        correct_option_id="a",
        explanation_odia="RuBisCO ପୃଥିବୀର ସବୁଠାରୁ ପ୍ରଚୁର ଏନଜାଇମ ଯାହା CO₂ କୁ ଜୈବିକ ଶର୍କରାରେ ପରିଣତ କରିବାରେ ସାହାଯ୍ୟ କରେ।",
        explanation_english="RuBisCO is the primary catalyst responsible for fixing atmospheric CO₂ into 3-PGA in the stroma.",
        concept_tag="enzymatic_carbon_fixation"
    )
]

SAMPLE_PACKS_SUMMARY: List[ContentPackSummary] = [
    ContentPackSummary(
        pack_id="relaynet_class9_science_photosynthesis_v1",
        title_odia="ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା (ନବମ ଶ୍ରେଣୀ ବିଜ୍ଞାନ)",
        title_english="Photosynthesis & Plant Nutrition (Class 9 Science)",
        board="ODISHA_STATE",
        class_grade=9,
        subject="Science (Biology)",
        version="1.2.0",
        size_mb=2.4,
        sha256_hash="e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
        download_url="/api/v1/content-packs/relaynet_class9_science_photosynthesis_v1/download"
    )
]
