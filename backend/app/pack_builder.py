import os
import json
import zipfile
import hashlib
from typing import Dict, Any

PACK_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "static", "packs")

def build_photosynthesis_pack_zip() -> str:
    """
    Builds the canonical RelayNet learning pack ZIP as specified in Section 9:
    relaynet_class9_science_photosynthesis_v1.zip
    """
    os.makedirs(PACK_DIR, exist_ok=True)
    zip_path = os.path.join(PACK_DIR, "relaynet_class9_science_photosynthesis_v1.zip")

    # Manifest dictionary
    manifest_data = {
        "pack_id": "relaynet_class9_science_photosynthesis_v1",
        "version": "1.2.0",
        "board": "ODISHA_STATE",
        "class_grade": 9,
        "subject": "Science (Biology)",
        "chapter": "Chapter 1: ପୋଷଣ (Nutrition)",
        "topic": "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା (Photosynthesis)",
        "languages": ["ODIA", "ENGLISH"],
        "app_schema_version": "1.0.0",
        "publisher_identity": "RelayNet Verification Authority (Verified)",
        "licence": "CC-BY-NC-SA 4.0 / SCERT Odisha Educational Fair-Use",
        "created_date": "2026-10-01",
        "file_list": [
            "manifest.json",
            "lesson/odia.json",
            "lesson/english.json",
            "quiz/questions.json",
            "quiz/answer_key.json",
            "attribution.txt"
        ]
    }

    lesson_odia = {
        "title": "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା ଏବଂ ଉଦ୍ଭିଦ ପୋଷଣ",
        "overview": "ଆଲୋକ ଶ୍ଳେଷଣ ହେଉଛି ଏକ ଜୈବ ରାସାୟନିକ ପ୍ରକ୍ରିୟା ଯାହା ଦ୍ୱାରା ସବୁଜ ଉଦ୍ଭିଦମାନେ ସୂର୍ଯ୍ୟାଲୋକ, ଜଳ (H₂O) ଏବଂ ଅଙ୍ଗାରକାମ୍ଳ (CO₂) ବ୍ୟବହାର କରି ଶର୍କରା ପ୍ରସ୍ତୁତ କରନ୍ତି ଏବଂ ଅମ୍ଳଜାନ (O₂) ନିର୍ଗତ କରନ୍ତି।",
        "equation": "6CO₂ + 12H₂O ──[ସୂର୍ଯ୍ୟାଲୋକ / କ୍ଲୋରୋଫିଲ୍]──► C₆H₁₂O₆ + 6H₂O + 6O₂ ↑",
        "sections": [
            "୧. ଆଲୋକ ପ୍ରକ୍ରିୟା: ହରିତକଣାର ଗ୍ରାନାରେ ସଂଘଟିତ ହୁଏ। ଜଳର ଆଲୋକ ବିଶ୍ଳେଷଣ (Photolysis of water) ଘଟି ଅମ୍ଳଜାନ (O₂) ନିର୍ଗତ ହୁଏ।",
            "୨. ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା: ହରିତକଣାର ଷ୍ଟ୍ରୋମାରେ ସଂଘଟିତ ହୁଏ। RuBisCO ଏନଜାଇମ ସାହାଯ୍ୟରେ CO₂ ବିଜାରଣ ଘଟି ଗ୍ଲୁକୋଜ ସୃଷ୍ଟି ହୁଏ।"
        ]
    }

    lesson_english = {
        "title": "Photosynthesis Process & Plant Nutrition",
        "overview": "Photosynthesis is an essential autotrophic biochemical process converting sunlight, carbon dioxide, and water into carbohydrate nutrition with oxygen gas byproduct.",
        "equation": "6CO₂ + 12H₂O ──[Sunlight / Chlorophyll]──► C₆H₁₂O₆ + 6H₂O + 6O₂ ↑",
        "sections": [
            "1. Light Reactions: Occur within chloroplast grana / thylakoids; photolysis of water releases O₂ and energy carrier ATP.",
            "2. Dark Reactions (Calvin Cycle): Occurs in chloroplast stroma; RuBisCO enzyme fixes carbon dioxide into hexose carbohydrates."
        ]
    }

    quiz_questions = [
        {
            "id": "q1",
            "question_odia": "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟାରେ ନିର୍ଗତ ହେଉଥିବା ଅମ୍ଳଜାନ (O₂) ର ମୂଳ ଉତ୍ସ କଣ?",
            "question_english": "What is the biochemical source of oxygen evolved during photosynthesis?",
            "options": [
                {"id": "a", "text": "Water (H₂O)"},
                {"id": "b", "text": "Carbon Dioxide (CO₂)"},
                {"id": "c", "text": "Glucose"},
                {"id": "d", "text": "Chlorophyll"}
            ],
            "correct_option": "a",
            "concept_tag": "photolysis_water_source"
        },
        {
            "id": "q2",
            "question_odia": "ହରିତକଣାର କେଉଁ ଅଂଶରେ ଆଲୋକ ପ୍ରକ୍ରିୟା ସଂପାଦିତ ହୁଏ?",
            "question_english": "Which sub-structure of chloroplast hosts light reactions?",
            "options": [
                {"id": "a", "text": "Stroma"},
                {"id": "b", "text": "Grana / Thylakoid"},
                {"id": "c", "text": "Outer membrane"},
                {"id": "d", "text": "Cytoplasm"}
            ],
            "correct_option": "b",
            "concept_tag": "chloroplast_anatomy_site"
        }
    ]

    answer_key = {
        "q1": {"correct": "a", "concept": "photolysis_water_source", "explanation": "Photolysis of water splits H2O into O2, protons and electrons."},
        "q2": {"correct": "b", "concept": "chloroplast_anatomy_site", "explanation": "Thylakoid membranes in grana contain chlorophyll pigments."}
    }

    attribution_txt = (
        "RelayNet Learning Resource Pack\n"
        "Curriculum Mapping: Odisha State Board (BSE Class 9 Science) & CBSE Class 9 Science\n"
        "Licence: CC-BY-NC-SA 4.0\n"
        "Verification: Verified by RelayNet Master Teachers\n"
    )

    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        zipf.writestr("manifest.json", json.dumps(manifest_data, indent=2, ensure_ascii=False))
        zipf.writestr("lesson/odia.json", json.dumps(lesson_odia, indent=2, ensure_ascii=False))
        zipf.writestr("lesson/english.json", json.dumps(lesson_english, indent=2, ensure_ascii=False))
        zipf.writestr("quiz/questions.json", json.dumps(quiz_questions, indent=2, ensure_ascii=False))
        zipf.writestr("quiz/answer_key.json", json.dumps(answer_key, indent=2, ensure_ascii=False))
        zipf.writestr("attribution.txt", attribution_txt)

    return zip_path

def compute_file_sha256(filepath: str) -> str:
    sha = hashlib.sha256()
    with open(filepath, 'rb') as f:
        while chunk := f.read(8192):
            sha.update(chunk)
    return sha.hexdigest()
