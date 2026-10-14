import os
import json
import zipfile
import hashlib
from typing import List, Dict, Any, Optional

from services.visual_generator import construct_diagram_prompt, generate_and_save_diagram

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STATIC_PACK_DIR = os.path.join(BASE_DIR, "static", "packs")
STATIC_DIAGRAMS_DIR = os.path.join(BASE_DIR, "static", "diagrams")

def extract_primary_concepts(topic_title: str, notes_text: str = "") -> List[str]:
    """
    Extracts key visual/biochemical concepts from curriculum notes or title
    for prompt feeding into the visual generator.
    """
    concepts = []
    text_lower = (topic_title + " " + notes_text).lower()

    if "photosynthesis" in text_lower or "ଆଲୋକ ଶ୍ଳେଷଣ" in text_lower or "प्रकाश" in text_lower:
        concepts = [
            "Sunlight & Photon Capture in Thylakoids",
            "Water Photolysis (2H₂O ──► 4H⁺ + 4e⁻ + O₂↑)",
            "Assimilatory Power (ATP + NADPH Generation)",
            "Carbon Dioxide Fixation to Glucose via RuBisCO (Calvin Cycle)"
        ]
    elif "respiration" in text_lower or "ଶ୍ୱାସକ୍ରିୟା" in text_lower:
        concepts = [
            "Glycolysis in Cytoplasm",
            "Krebs Cycle in Mitochondria",
            "Electron Transport Chain",
            "ATP Energy Synthesis"
        ]
    elif "cell" in text_lower or "କୋଷ" in text_lower:
        concepts = [
            "Cell Membrane & Cell Wall",
            "Nucleus & Genetic Material",
            "Mitochondria & Chloroplasts",
            "Endoplasmic Reticulum & Ribosomes"
        ]
    else:
        # Generic heuristic extraction
        concepts = [
            f"Core Mechanism of {topic_title}",
            "Reactants & Energy Transformation",
            "Intermediate Biochemical Phase",
            "Final Biological Products & Byproducts"
        ]
    return concepts

def build_section9_learning_pack(
    pack_id: str = "relaynet_class9_science_photosynthesis_v1",
    topic_title: str = "Photosynthesis & Plant Nutrition (ଆଲୋକ ଶ୍ଳେଷଣ)",
    notes_text: str = "",
    language_mode: str = "bilingual"
) -> Dict[str, Any]:
    """
    Builds the canonical Section 9 Learning Pack ZIP:
    relaynet_{pack_id}_v1.zip
      ├── manifest.json
      ├── lesson/
      │   ├── odia.json
      │   ├── hindi.json
      │   └── english.json
      ├── diagrams/
      │   └── {pack_id}_flowchart.jpg
      └── quiz/
          └── questions.json

    Injects the diagram metadata into manifest.json and computes the SHA-256 hash.
    """
    os.makedirs(STATIC_PACK_DIR, exist_ok=True)
    os.makedirs(STATIC_DIAGRAMS_DIR, exist_ok=True)

    zip_filename = f"{pack_id}.zip" if not pack_id.endswith(".zip") else pack_id
    zip_path = os.path.join(STATIC_PACK_DIR, zip_filename)

    # 1. Extract visual concepts & generate diagram
    key_concepts = extract_primary_concepts(topic_title, notes_text)
    prompt = construct_diagram_prompt(topic_title, key_concepts, language_mode)

    temp_diagram_dir = os.path.join(STATIC_DIAGRAMS_DIR, pack_id)
    diagram_meta = generate_and_save_diagram(
        topic_id=pack_id,
        prompt=prompt,
        output_dir=temp_diagram_dir,
        topic_title=topic_title,
        key_concepts=key_concepts
    )

    diagram_local_file = diagram_meta["file_path"]
    diagram_archive_name = f"diagrams/{os.path.basename(diagram_local_file)}"

    # Copy diagram to public static diagrams folder for direct HTTP serving
    public_diagram_path = os.path.join(STATIC_DIAGRAMS_DIR, os.path.basename(diagram_local_file))
    if os.path.abspath(diagram_local_file) != os.path.abspath(public_diagram_path):
        import shutil
        shutil.copy2(diagram_local_file, public_diagram_path)

    # 2. Manifest conforming to Section 9 schema
    manifest_data = {
        "pack_id": pack_id,
        "version": "1.2.0",
        "board": "ODISHA_STATE",
        "class_grade": 9,
        "subject": "Science (Biology)",
        "chapter": "Chapter 1: ପୋଷଣ (Nutrition)",
        "topic": "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା (Photosynthesis)",
        "languages": ["ODIA", "HINDI", "ENGLISH"],
        "app_schema_version": "1.2.0",
        "publisher_identity": "RelayNet Verification Authority (Verified)",
        "licence": "CC-BY-NC-SA 4.0 / SCERT Odisha Educational Fair-Use",
        "created_date": "2026-10-14",
        "diagrams": [
            {
                "id": f"diag_{pack_id}_01",
                "file": diagram_archive_name,
                "caption": f"Concept Flowchart and Biochemical Pathway for {topic_title}",
                "size_kb": diagram_meta["storage_size_kb"],
                "model": diagram_meta["model"]
            }
        ],
        "file_list": [
            "manifest.json",
            "lesson/odia.json",
            "lesson/hindi.json",
            "lesson/english.json",
            diagram_archive_name,
            "quiz/questions.json",
            "attribution.txt"
        ]
    }

    # 3. Lesson Content (Odia, Hindi, English)
    lesson_odia = {
        "title": "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା ଏବଂ ଉଦ୍ଭିଦ ପୋଷଣ",
        "overview": "ଆଲୋକ ଶ୍ଳେଷଣ ହେଉଛି ଏକ ଜୈବ ରାସାୟନିକ ପ୍ରକ୍ରିୟା ଯାହା ଦ୍ୱାରା ସବୁଜ ଉଦ୍ଭିଦମାନେ ସୂର୍ଯ୍ୟାଲୋକ, ଜଳ (H₂O) ଏବଂ ଅଙ୍ଗାରକାମ୍ଳ (CO₂) ବ୍ୟବହାର କରି ଶର୍କରା ପ୍ରସ୍ତୁତ କରନ୍ତି ଏବଂ ଅମ୍ଳଜାନ (O₂) ନିର୍ଗତ କରନ୍ତି।",
        "equation": "6CO₂ + 12H₂O ──[ସୂର୍ଯ୍ୟାଲୋକ / କ୍ଲୋରୋଫିଲ୍]──► C₆H₁₂O₆ + 6H₂O + 6O₂ ↑",
        "diagram_file": diagram_archive_name,
        "sections": [
            "୧. ଆଲୋକ ପ୍ରକ୍ରିୟା: ହରିତକଣାର ଥାଇଲାକଏଡ୍ ଗ୍ରାନାରେ ଘଟେ। ଜଳର ଆଲୋକ ବିଶ୍ଳେଷଣ (Photolysis) ଦ୍ୱାରା ଅମ୍ଳଜାନ ମୁକ୍ତ ହୁଏ।",
            "୨. ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା: ହରିତକଣାର ଷ୍ଟ୍ରୋମାରେ RuBisCO ଏନଜାଇମ ସହାୟତାରେ CO₂ ଗ୍ଲୁକୋଜରେ ପରିଣତ ହୁଏ।"
        ]
    }

    lesson_hindi = {
        "title": "प्रकाश संश्लेषण प्रक्रिया एवं पादप पोषण",
        "overview": "प्रकाश संश्लेषण एक जैव-रासायनिक प्रक्रिया है जिसके द्वारा हरे पौधे सूर्य के प्रकाश, जल (H₂O) और कार्बन डाइऑक्साइड (CO₂) का उपयोग करके कार्बोहाइड्रेट बनाते हैं तथा ऑक्सीजन (O₂) मुक्त करते हैं।",
        "equation": "6CO₂ + 12H₂O ──[सूर्य का प्रकाश / क्लोरोफिल]──► C₆H₁₂O₆ + 6H₂O + 6O₂ ↑",
        "diagram_file": diagram_archive_name,
        "sections": [
            "1. प्रकाशिक अभिक्रिया: क्लोरोप्लास्ट के ग्रैना में जल का प्रकाशीय अपघटन (Photolysis) होता है तथा O₂ उत्सर्जित होती है।",
            "2. अप्रकाशिक अभिक्रिया: क्लोरोप्लास्ट के स्ट्रोमा में RuBisCO एंजाइम द्वारा CO₂ का स्थिरीकरण होकर ग्लूकोज बनता है।"
        ]
    }

    lesson_english = {
        "title": "Photosynthesis Process & Plant Nutrition",
        "overview": "Photosynthesis is the foundational autotrophic pathway converting solar photons and water into chemical energy stored as glucose bonds, releasing vital oxygen gas.",
        "equation": "6CO₂ + 12H₂O ──[Sunlight / Chlorophyll]──► C₆H₁₂O₆ + 6H₂O + 6O₂ ↑",
        "diagram_file": diagram_archive_name,
        "sections": [
            "1. Light Reactions: Occur within chloroplast grana / thylakoids; photolysis of water releases O₂ and creates ATP + NADPH.",
            "2. Dark Reactions (Calvin Cycle): Occur in chloroplast stroma; RuBisCO enzyme fixes CO₂ into hexose glucose."
        ]
    }

    # 4. Diagnostic Quiz Questions
    quiz_questions = [
        {
            "id": "q1",
            "question_odia": "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟାରେ ନିର୍ଗତ ହେଉଥିବା ଅମ୍ଳଜାନ (O₂) ର ମୂଳ ଉତ୍ସ କଣ?",
            "question_hindi": "प्रकाश संश्लेषण में निकलने वाली ऑक्सीजन (O₂) का प्राथमिक स्रोत क्या है?",
            "question_english": "What is the biochemical source of oxygen evolved during photosynthesis?",
            "options": [
                {"id": "a", "text": "Water (H₂O) / ଜଳ / जल"},
                {"id": "b", "text": "Carbon Dioxide (CO₂) / ଅଙ୍ଗାରକାମ୍ଳ / कार्बन डाइऑक्साइड"},
                {"id": "c", "text": "Glucose / ଗ୍ଲୁକୋଜ / ग्लूकोज"},
                {"id": "d", "text": "Chlorophyll / କ୍ଲୋରୋଫିଲ୍ / क्लोरोफिल"}
            ],
            "correct_option": "a",
            "concept_tag": "photolysis_water_source"
        },
        {
            "id": "q2",
            "question_odia": "ହରିତକଣାର କେଉଁ ଅଂଶରେ ଆଲୋକ ପ୍ରକ୍ରିୟା ସଂପାଦିତ ହୁଏ?",
            "question_hindi": "क्लोरोप्लास्ट के किस भाग में प्रकाशिक अभिक्रिया संपन्न होती है?",
            "question_english": "Which sub-structure of the chloroplast hosts light reactions?",
            "options": [
                {"id": "a", "text": "Stroma / ଷ୍ଟ୍ରୋମା / स्ट्रोमा"},
                {"id": "b", "text": "Grana / Thylakoid / ଗ୍ରାନା / ग्रैना"},
                {"id": "c", "text": "Outer membrane / ବାହ୍ୟ ଝିଲ୍ଲୀ / बाहरी झिल्ली"},
                {"id": "d", "text": "Cytoplasm / କୋଷଜୀବକ / कोशिकाद्रव्य"}
            ],
            "correct_option": "b",
            "concept_tag": "chloroplast_anatomy_site"
        }
    ]

    attribution_txt = (
        "RelayNet Learning Resource Pack\n"
        "Curriculum Mapping: Odisha State Board (BSE Class 9 Science) & CBSE Class 9 Science\n"
        "Visual Assets: NVIDIA NIM FLUX.2 Klein (black-forest-labs/flux.2-klein-4b)\n"
        "Licence: CC-BY-NC-SA 4.0\n"
        "Verification: Verified by RelayNet Master Teachers\n"
    )

    # 5. Pack everything into the canonical Section 9 ZIP
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        zipf.writestr("manifest.json", json.dumps(manifest_data, indent=2, ensure_ascii=False))
        zipf.writestr("lesson/odia.json", json.dumps(lesson_odia, indent=2, ensure_ascii=False))
        zipf.writestr("lesson/hindi.json", json.dumps(lesson_hindi, indent=2, ensure_ascii=False))
        zipf.writestr("lesson/english.json", json.dumps(lesson_english, indent=2, ensure_ascii=False))
        zipf.writestr("quiz/questions.json", json.dumps(quiz_questions, indent=2, ensure_ascii=False))
        zipf.writestr("attribution.txt", attribution_txt)

        # Include the diagram file
        if os.path.exists(diagram_local_file):
            zipf.write(diagram_local_file, arcname=diagram_archive_name)

    # 6. Compute bundle SHA-256 hash of the complete archive
    sha = hashlib.sha256()
    with open(zip_path, 'rb') as f:
        while chunk := f.read(8192):
            sha.update(chunk)
    calculated_sha256 = sha.hexdigest()

    return {
        "pack_id": pack_id,
        "zip_path": zip_path,
        "sha256": calculated_sha256,
        "diagram_file": diagram_archive_name,
        "public_diagram_url": f"/static/diagrams/{os.path.basename(diagram_local_file)}",
        "size_bytes": os.path.getsize(zip_path),
        "diagram_meta": diagram_meta
    }
