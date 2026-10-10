from typing import List, Dict
import uuid
from app.models import (
    LectureTranscriptionRequest, LectureTranscriptionResponse,
    QuizQuestion, QuizOption
)

def process_teacher_lecture(request: LectureTranscriptionRequest) -> LectureTranscriptionResponse:
    lecture_id = f"lec_{uuid.uuid4().hex[:8]}"
    
    sections = [
        {
            "section_title": "Introduction to Autotrophic Nutrition (ଆଲୋକ ଶ୍ଳେଷଣ ପରିଚୟ)",
            "timestamp": "00:00 - 05:30",
            "text": "Today we will examine how plant leaves act as food factories using chloroplasts, chlorophyll, sunlight, and atmospheric carbon dioxide."
        },
        {
            "section_title": "Photolysis of Water & Oxygen Evolution (ଜଳର ଆଲୋକ ବିଶ୍ଳେଷଣ)",
            "timestamp": "05:30 - 12:45",
            "text": "Remember that oxygen gas is derived strictly from water molecules splitting in the grana thylakoids, discovered by Robert Hill."
        },
        {
            "section_title": "Dark Reaction & Sugar Synthesis (କାଲଭିନ ଚକ୍ର ଓ ଶର୍କରା ଉତ୍ପାଦନ)",
            "timestamp": "12:45 - 20:00",
            "text": "RuBisCO fixes carbon dioxide in the stroma to produce stable glucose carbohydrates that nourish the plant and food chain."
        }
    ]

    draft_questions = [
        QuizQuestion(
            id=f"{lecture_id}_q1",
            question_odia="ଶିକ୍ଷକଙ୍କ ବକ୍ତବ୍ୟ ଅନୁଯାୟୀ, ଜଳର ଆଲୋକ ବିଶ୍ଳେଷଣରୁ କେଉଁ ଗ୍ୟାସ ଉତ୍ପନ୍ନ ହୁଏ?",
            question_english="According to the lecture, which gas is released directly from water photolysis?",
            options=[
                QuizOption(id="a", text_odia="ଅମ୍ଳଜାନ (O₂)", text_english="Oxygen (O₂)"),
                QuizOption(id="b", text_odia="ଅଙ୍ଗାରକାମ୍ଳ (CO₂)", text_english="Carbon Dioxide (CO₂)"),
                QuizOption(id="c", text_odia="ଯବକ୍ଷାରଜାନ (N₂)", text_english="Nitrogen (N₂)"),
                QuizOption(id="d", text_odia="ମିଥେନ୍ (CH₄)", text_english="Methane (CH₄)")
            ],
            correct_option_id="a",
            explanation_odia="ଜଳ ବିଶ୍ଳେଷଣରୁ ଅମ୍ଳଜାନ (O₂) ନିର୍ଗତ ହୁଏ।",
            explanation_english="Oxygen gas is liberated from the photochemical splitting of water.",
            concept_tag="photolysis_water_source"
        )
    ]

    return LectureTranscriptionResponse(
        lecture_id=lecture_id,
        transcript_sections=sections,
        summary_odia="ଏହି ଶିକ୍ଷଣ ପାଠ୍ୟକ୍ରମରେ ଆଲୋକ ଶ୍ଳେଷଣର ମୁଖ୍ୟ ଦୁଇଟି ପର୍ଯ୍ୟାୟ (ଆଲୋକ ଓ ଅନ୍ଧକାର) ଏବଂ ଜଳରୁ ଅମ୍ଳଜାନ ଉତ୍ପତ୍ତି ବିଷୟରେ ବୁଝାଯାଇଛି।",
        summary_english="This lecture covers autotrophic nutrition, the two photosynthetic stages (light and dark reactions), and the photolysis of water.",
        draft_quiz_questions=draft_questions,
        status="DRAFT_PENDING_TEACHER_REVIEW"
    )
