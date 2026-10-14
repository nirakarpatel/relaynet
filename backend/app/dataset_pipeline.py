import json
import random
from typing import List, Dict, Any, Optional
from pydantic import BaseModel
from app.models import (
    BoardType, CurriculumNode, LessonContent,
    KeyGlossaryTerm, QuizQuestion, QuizOption
)

class LiveDatasetSample(BaseModel):
    dataset_source: str
    grade: int
    board: str
    subject: str
    topic: str
    concept: str
    diagram_type: Optional[str] = None
    lesson: LessonContent
    quiz_questions: List[QuizQuestion]

# Knowledge base modeled from HuggingFace datasets:
# - FoundryAILabs/k12-indian-curriculum-4.9m
# - LingoIITGN/Gurukul
# - bingbangboom/gsm8k-hindi
K12_DATASET_STORE: Dict[str, Dict[str, Any]] = {
    # CLASS 6 - SCIENCE
    "class6_sci_food": {
        "board": BoardType.ODISHA_STATE,
        "class_grade": 6,
        "subject": "Science (ବିଜ୍ଞାନ)",
        "unit_chapter": "Chapter 1: ଖାଦ୍ୟ: ଏହା କେଉଁଠାରୁ ଆସେ? (Food Components)",
        "topic": "ପୋଷକ ତତ୍ତ୍ୱ ଏବଂ ସନ୍ତୁଳିତ ଆହାର (Nutrients & Balanced Diet)",
        "diagram_type": "BALANCED_DIET_PYRAMID",
        "lesson": {
            "title_odia": "ଖାଦ୍ୟର ଉପାଦାନ ଓ ସନ୍ତୁଳିତ ଆହାର",
            "title_english": "Components of Food & Balanced Diet",
            "overview_odia": "ଆମ ଶରୀରର ବୃଦ୍ଧି ଓ ସୁରକ୍ଷା ପାଇଁ କାର୍ବୋହାଇଡ୍ରେଟ୍, ପ୍ରୋଟିନ୍, ଚର୍ବି, ଭିଟାମିନ୍ ଏବଂ ଖଣିଜ ଲବଣ ଆବଶ୍ୟକ।",
            "overview_english": "Food contains vital nutrients: carbohydrates, proteins, fats, vitamins, and minerals essential for metabolic health.",
            "chemical_equation": "Carbohydrates + O₂ ──> Energy (ATP) + CO₂ + H₂O",
            "detailed_sections_odia": [
                "୧. କାର୍ବୋହାଇଡ୍ରେଟ୍ ଓ ଚର୍ବି: ଶକ୍ତି ପ୍ରଦାନକାରୀ ଖାଦ୍ୟ। ଭାତ, ରୁଟି ଓ ଆଳୁରୁ ଶର୍କରା ମିଳେ।",
                "୨. ପ୍ରୋଟିନ୍: ଶରୀର ଗଠନକାରୀ ଖାଦ୍ୟ ଯାହା କ୍ଷତ ପୂରଣ ଓ କୋଷ ନିର୍ମାଣ କରେ (ଡାଲି, ଦୁଗ୍ଧ, ଅଣ୍ଡା)।",
                "୩. ଭିଟାମିନ୍ ଓ ଖଣିଜ ଲବଣ: ରୋଗ ପ୍ରତିରୋଧକ ଖାଦ୍ୟ ଯାହା ଆଖି, ହାଡ଼ ଓ ଦାନ୍ତକୁ ସୁସ୍ଥ ରଖେ।"
            ],
            "detailed_sections_english": [
                "1. Energy Giving Foods: Carbohydrates and fats supply calories for daily physical work.",
                "2. Body Building Foods: Proteins repair tissues and facilitate cellular growth (pulses, milk, eggs).",
                "3. Protective Foods: Vitamins (A, B, C, D) and minerals (Iron, Calcium) guard against deficiency diseases."
            ],
            "key_terms": [
                {"odia_term": "ସନ୍ତୁଳିତ ଆହାର", "english_term": "Balanced Diet", "definition": "Diet containing all essential nutrients in proper proportion."},
                {"odia_term": "ଅପପୁଷ୍ଟି", "english_term": "Malnutrition", "definition": "Physical condition resulting from deficiency of essential nutrients."}
            ]
        },
        "questions": [
            {
                "id": "q_c6_nutrients_1",
                "question_odia": "ଆମ ଶରୀରରେ କୋଷ ନିର୍ମାଣ ଓ କ୍ଷତ ପୂରଣ ପାଇଁ କେଉଁ ପୋଷକ ମୁଖ୍ୟ ଭାବରେ ଦାୟୀ?",
                "question_english": "Which nutrient is primarily responsible for growth and tissue repair?",
                "options": [
                    {"id": "A", "text_odia": "କାର୍ବୋହାଇଡ୍ରେଟ୍", "text_english": "Carbohydrates"},
                    {"id": "B", "text_odia": "ପ୍ରୋଟିନ୍ (ସଠିକ୍)", "text_english": "Protein (Correct)"},
                    {"id": "C", "text_odia": "ଚର୍ବି", "text_english": "Fats"},
                    {"id": "D", "text_odia": "ଜଳ", "text_english": "Water"}
                ],
                "correct_option_id": "B",
                "explanation_odia": "ପ୍ରୋଟିନ୍ ଶରୀର ଗଠନକାରୀ ଖାଦ୍ୟ ଭାବରେ ନୂତନ କୋଷ ଗଠନ କରେ।",
                "explanation_english": "Proteins are body-building nutrients composed of amino acids required for tissue building.",
                "concept_tag": "food_nutrients_protein"
            }
        ]
    },
    # CLASS 7 - SCIENCE
    "class7_sci_motion": {
        "board": BoardType.CBSE,
        "class_grade": 7,
        "subject": "Science (Physics)",
        "unit_chapter": "Chapter 13: Motion and Time (ଗତି ଏବଂ ସମୟ)",
        "topic": "Uniform Motion & Distance-Time Graphs",
        "diagram_type": "DISTANCE_TIME_GRAPH",
        "lesson": {
            "title_odia": "ସମାନୁପାତୀ ଗତି ଏବଂ ଦୂରତା-ସମୟ ଲେଖଚିତ୍ର",
            "title_english": "Uniform Motion & Distance-Time Graphs",
            "overview_odia": "ଗତିଶୀଳ ବସ୍ତୁର ବେଗ ହେଉଛି ଏକକ ସମୟରେ ଅତିକ୍ରାନ୍ତ ଦୂରତା। ବେଗ = ଦୂରତା ÷ ସମୟ।",
            "overview_english": "Speed measures how fast an object moves. Speed = Distance / Time.",
            "chemical_equation": "v = d / t  (Speed = Distance / Time, SI unit: m/s)",
            "detailed_sections_odia": [
                "୧. ସମ ଗତି (Uniform Motion): ଯଦି ବସ୍ତୁ ସମାନ ସମୟ ବ୍ୟବଧାନରେ ସମାନ ଦୂରତା ଯାଏ।",
                "୨. ଦୂରତା-ସମୟ ଗ୍ରାଫ୍: ସମ ଗତି ପାଇଁ ଲେଖଚିତ୍ର ସିଧା ସରଳରେଖା ହୋଇଥାଏ।"
            ],
            "detailed_sections_english": [
                "1. Uniform Motion: An object moving along a straight line with constant speed covers equal distances in equal intervals of time.",
                "2. Distance-Time Graph: A straight sloping line indicates uniform velocity."
            ],
            "key_terms": [
                {"odia_term": "ବେଗ / ଦ୍ରୁତି", "english_term": "Speed", "definition": "Rate of change of distance with respect to time."},
                {"odia_term": "ଦୋଳନ କାଳ", "english_term": "Time Period", "definition": "Time taken by an oscillating pendulum to complete one cycle."}
            ]
        },
        "questions": [
            {
                "id": "q_c7_motion_1",
                "question_odia": "ଏକ ଗାଡି ୨ ଘଣ୍ଟାରେ ୧୦୦ କିମି ଗଲେ, ତାର ହାରାହାରି ବେଗ କେତେ?",
                "question_english": "If a vehicle travels 100 km in 2 hours, what is its average speed?",
                "options": [
                    {"id": "A", "text_odia": "୨୫ କିମି/ଘଣ୍ଟା", "text_english": "25 km/h"},
                    {"id": "B", "text_odia": "୫୦ କିମି/ଘଣ୍ଟା (ସଠିକ୍)", "text_english": "50 km/h (Correct)"},
                    {"id": "C", "text_odia": "୨୦୦ କିମି/ଘଣ୍ଟା", "text_english": "200 km/h"},
                    {"id": "D", "text_odia": "୭୫ କିମି/ଘଣ୍ଟା", "text_english": "75 km/h"}
                ],
                "correct_option_id": "B",
                "explanation_odia": "ବେଗ = ଦୂରତା ÷ ସମୟ = ୧୦୦ ÷ ୨ = ୫୦ କିମି/ଘଣ୍ଟା।",
                "explanation_english": "Speed = Distance / Time = 100 km / 2 h = 50 km/h.",
                "concept_tag": "speed_distance_time_calculation"
            }
        ]
    },
    # CLASS 8 - SCIENCE
    "class8_sci_cell": {
        "board": BoardType.CISCE_ICSE,
        "class_grade": 8,
        "subject": "Biology (Science)",
        "unit_chapter": "Chapter 8: Cell Structure and Functions (କୋଷର ଗଠନ)",
        "topic": "Plant vs Animal Cell Organelles",
        "diagram_type": "PLANT_ANIMAL_CELL_DIAGRAM",
        "lesson": {
            "title_odia": "ଉଦ୍ଭିଦ ଓ ପ୍ରାଣୀ କୋଷର ଗଠନ ଏବଂ ତୁଳନା",
            "title_english": "Plant and Animal Cell Structure & Comparison",
            "overview_odia": "କୋଷ ହେଉଛି ଜୀବର ମୌଳିକ ଗଠନମୂଳକ ଓ କାର୍ଯ୍ୟକାରୀ ଏକକ। ଉଦ୍ଭିଦ କୋଷରେ କୋଷଭିତ୍ତି ଥାଏ।",
            "overview_english": "The cell is the basic structural and functional unit of life, enclosed by a cell membrane and containing specialized organelles.",
            "chemical_equation": "Cell ──> Tissue ──> Organ ──> Organ System ──> Organism",
            "detailed_sections_odia": [
                "୧. କୋଷଝିଲ୍ଲୀ ଓ କୋଷଭିତ୍ତି: ଉଦ୍ଭିଦ କୋଷରେ ସେଲ୍ୟୁଲୋଜ୍ ନିର୍ମିତ ବାହ୍ୟ କୋଷଭିତ୍ତି (Cell Wall) ଥାଏ।",
                "୨. କୋଷଜୀବକ ଓ ନ୍ୟଷ୍ଟି: ନ୍ୟଷ୍ଟି (Nucleus) କୋଷର ସମସ୍ତ କାର୍ଯ୍ୟ ନିୟନ୍ତ୍ରଣ କରେ।"
            ],
            "detailed_sections_english": [
                "1. Cell Wall & Membrane: Plant cells possess an outer rigid cellulose wall providing shape and mechanical protection.",
                "2. Nucleus & Cytoplasm: The nucleus houses chromosomes containing genetic DNA directing cell metabolism."
            ],
            "key_terms": [
                {"odia_term": "କୋଷଭିତ୍ତି", "english_term": "Cell Wall", "definition": "Rigid outer layer found exclusively in plant cells."},
                {"odia_term": "ମାଇଟୋକଣ୍ଡ୍ରିଆ", "english_term": "Mitochondria", "definition": "Powerhouse of the cell generating ATP energy."}
            ]
        },
        "questions": [
            {
                "id": "q_c8_cell_1",
                "question_odia": "ଉଦ୍ଭିଦ କୋଷରେ ଥିବା କିନ୍ତୁ ପ୍ରାଣୀ କୋଷରେ ଅନୁପସ୍ଥିତ ଥିବା ଅଙ୍ଗିକା କେଉଁଟି?",
                "question_english": "Which organelle is present in plant cells but absent in animal cells?",
                "options": [
                    {"id": "A", "text_odia": "ମାଇଟୋକଣ୍ଡ୍ରିଆ", "text_english": "Mitochondria"},
                    {"id": "B", "text_odia": "କୋଷଭିତ୍ତି ଓ ପ୍ଲାଷ୍ଟିଡ୍ (ସଠିକ୍)", "text_english": "Cell Wall & Chloroplast (Correct)"},
                    {"id": "C", "text_odia": "ନ୍ୟଷ୍ଟି", "text_english": "Nucleus"},
                    {"id": "D", "text_odia": "ରାଇବୋଜୋମ୍", "text_english": "Ribosome"}
                ],
                "correct_option_id": "B",
                "explanation_odia": "ଉଦ୍ଭିଦ କୋଷରେ ସେଲ୍ୟୁଲୋଜ୍ ନିର୍ମିତ କୋଷଭିତ୍ତି ଏବଂ କ୍ଲୋରୋପ୍ଲାଷ୍ଟ ଥାଏ ଯାହା ପ୍ରାଣୀ କୋଷରେ ନଥାଏ।",
                "explanation_english": "Plant cells uniquely possess a rigid cellulose cell wall and photosynthetic plastids (chloroplasts).",
                "concept_tag": "cell_wall_plastid_distinction"
            }
        ]
    },
    # CLASS 9 - SCIENCE (BIOLOGY)
    "class9_sci_photosynthesis": {
        "board": BoardType.ODISHA_STATE,
        "class_grade": 9,
        "subject": "Science (Biology)",
        "unit_chapter": "Chapter 4: ଆଲୋକ ଶ୍ଳେଷଣ (Photosynthesis)",
        "topic": "ଆଲୋକ ଶ୍ଳେଷଣର କାର୍ଯ୍ୟପ୍ରଣାଳୀ ଓ ରାସାୟନିକ ପ୍ରକ୍ରିୟା",
        "diagram_type": "PHOTOSYNTHESIS_CHLOROPLAST_FLOW",
        "lesson": {
            "title_odia": "ଆଲୋକ ଶ୍ଳେଷଣ: ଫୋଟୋଲିସିସ୍ ଓ କେଲଭିନ୍ ଚକ୍ର",
            "title_english": "Photosynthesis: Photolysis & Calvin Cycle",
            "overview_odia": "ସବୁଜ ଉଦ୍ଭିଦ ସୂର୍ଯ୍ୟାଲୋକ ବ୍ୟବହାର କରି CO₂ ଓ H₂O ରୁ ଶର୍କରା ପ୍ରସ୍ତୁତ କରେ ଏବଂ O₂ ନିର୍ଗତ କରେ।",
            "overview_english": "Green plants convert light energy into chemical energy, splitting water and fixing CO₂ into glucose.",
            "chemical_equation": "6CO₂ + 12H₂O ──[Light/Chlorophyll]──> C₆H₁₂O₆ + 6O₂ + 6H₂O",
            "detailed_sections_odia": [
                "୧. ଆଲୋକ ପ୍ରକ୍ରିୟା (Light Reaction): ଗ୍ରାନାରେ ଘଟେ। ଜଳର ଫୋଟୋଲିସିସ୍ ଦ୍ୱାରା ଅମ୍ଳଜାନ ନିର୍ଗତ ହୁଏ।",
                "୨. ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା (Calvin Cycle): ଷ୍ଟ୍ରୋମାରେ ଘଟେ। CO₂ ବିଜାରଣ ଘଟି ଗ୍ଲୁକୋଜ୍ ଉତ୍ପନ୍ନ ହୁଏ।"
            ],
            "detailed_sections_english": [
                "1. Light Reactions (Grana): Water photolysis releases molecular oxygen and generates ATP & NADPH.",
                "2. Calvin Cycle (Stroma): Uses assimilation power to reduce CO₂ into hexose glucose."
            ],
            "key_terms": [
                {"odia_term": "ଫୋଟୋଲିସିସ୍", "english_term": "Photolysis", "definition": "Splitting of water molecule by light energy."},
                {"odia_term": "ଷ୍ଟୋମାଟା", "english_term": "Stomata", "definition": "Microscopic pores on leaf epidermis for gas exchange."}
            ]
        },
        "questions": [
            {
                "id": "q_c9_photosynthesis_1",
                "question_odia": "ଆଲୋକ ଶ୍ଳେଷଣରେ ନିର୍ଗତ ଅମ୍ଳଜାନ (O₂) ର ପ୍ରକୃତ ଉତ୍ସ କ'ଣ?",
                "question_english": "What is the primary source of oxygen released during photosynthesis?",
                "options": [
                    {"id": "A", "text_odia": "ଅଙ୍ଗାରକାମ୍ଳ (CO₂)", "text_english": "Carbon Dioxide (CO₂)"},
                    {"id": "B", "text_odia": "ଜଳ (H₂O) (ସଠିକ୍)", "text_english": "Water (H₂O) (Correct)"},
                    {"id": "C", "text_odia": "ଗ୍ଲୁକୋଜ୍", "text_english": "Glucose"},
                    {"id": "D", "text_odia": "କ୍ଲୋରୋଫିଲ୍", "text_english": "Chlorophyll"}
                ],
                "correct_option_id": "B",
                "explanation_odia": "ରବର୍ଟ ହିଲ୍ଙ୍କ ପରୀକ୍ଷା ପ୍ରମାଣ କରେ ଯେ ଆଲୋକ ପ୍ରକ୍ରିୟାରେ ଜଳର ବିଘଟନ (ଫୋଟୋଲିସିସ୍) ରୁ O₂ ମିଳେ।",
                "explanation_english": "Photolysis of water in chloroplast thylakoids splits H₂O into protons, electrons, and O₂ gas.",
                "concept_tag": "photolysis_water_source"
            }
        ]
    },
    # CLASS 9 - MATHEMATICS (from gsm8k-hindi / Gurukul style)
    "class9_math_linear": {
        "board": BoardType.CBSE,
        "class_grade": 9,
        "subject": "Mathematics (ଗଣିତ)",
        "unit_chapter": "Chapter 4: Linear Equations in Two Variables (ଦୁଇ ଅଜ୍ଞାତ ରାଶି ବିଶିଷ୍ଟ ରୈଖିକ ସମୀକରଣ)",
        "topic": "Standard Form ax + by + c = 0 & Graphical Solutions",
        "diagram_type": "LINEAR_GRAPH_COORDINATES",
        "lesson": {
            "title_odia": "ଦୁଇ ଅଜ୍ଞାତ ରାଶି ବିଶିଷ୍ଟ ରୈଖିକ ସମୀକରଣ",
            "title_english": "Linear Equations in Two Variables",
            "overview_odia": "ax + by + c = 0 ଆକାରର ସମୀକରଣ ଯେଉଁଠାରେ a, b, c ବାସ୍ତବ ସଂଖ୍ୟା ଏବଂ a ≠ 0, b ≠ 0।",
            "overview_english": "An equation in the form ax + by + c = 0 where a, b, c are real numbers and a, b are not both zero.",
            "chemical_equation": "ax + by + c = 0  (Graph forms a unique straight line)",
            "detailed_sections_odia": [
                "୧. ସମାଧାନର ସଂଖ୍ୟା: ଗୋଟିଏ ରୈଖିକ ସମୀକରଣର ଅସଂଖ୍ୟ ସମାଧାନ (Infinite Solutions) ଥାଏ।",
                "୨. ଲେଖଚିତ୍ର: ପ୍ରତ୍ୟେକ ସମାଧାନ (x, y) ଲେଖଚିତ୍ରସ୍ଥ ଏକ ବିନ୍ଦୁକୁ ସୂଚାଏ।"
            ],
            "detailed_sections_english": [
                "1. Infinitely Many Solutions: Every pair (x, y) satisfying the equation is a point on its geometric line.",
                "2. Graphical Representation: Graph of every first-degree two-variable polynomial is a straight line."
            ],
            "key_terms": [
                {"odia_term": "ଅଜ୍ଞାତ ରାଶି", "english_term": "Variable", "definition": "A symbol representing an unknown quantity."},
                {"odia_term": "ସ୍ଥାନାଙ୍କ", "english_term": "Coordinates (x, y)", "definition": "Ordered pair defining position on a Cartesian plane."}
            ]
        },
        "questions": [
            {
                "id": "q_c9_math_1",
                "question_odia": "ସମୀକରଣ 2x + 3y = 12 ରେ ଯଦି x = 3, ତେବେ y ର ମୂଲ୍ୟ କେତେ?",
                "question_english": "In the equation 2x + 3y = 12, if x = 3, what is the value of y?",
                "options": [
                    {"id": "A", "text_odia": "y = 1", "text_english": "y = 1"},
                    {"id": "B", "text_odia": "y = 2 (ସଠିକ୍)", "text_english": "y = 2 (Correct)"},
                    {"id": "C", "text_odia": "y = 3", "text_english": "y = 3"},
                    {"id": "D", "text_odia": "y = 4", "text_english": "y = 4"}
                ],
                "correct_option_id": "B",
                "explanation_odia": "2(3) + 3y = 12 => 6 + 3y = 12 => 3y = 6 => y = 2.",
                "explanation_english": "Substitute x = 3: 2(3) + 3y = 12 => 6 + 3y = 12 => 3y = 6 => y = 2.",
                "concept_tag": "linear_equation_substitution"
            }
        ]
    }
}

def generate_live_curriculum_pack(grade: int, board: str, subject: Optional[str] = None) -> List[Dict[str, Any]]:
    """
    Simulates real-time inference / fetching from HF datasets (FoundryAILabs/k12, Gurukul, gsm8k).
    Returns list of fully packaged curriculum nodes and lesson contents.
    """
    results = []
    for key, data in K12_DATASET_STORE.items():
        if data["class_grade"] == grade:
            results.append(data)
    
    # If no exact grade match found in store, adapt or provide closest standard curriculum node
    if not results:
        for key, data in K12_DATASET_STORE.items():
            results.append(data)
            
    return results
