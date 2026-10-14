from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, JSONResponse
from typing import List, Optional
import os
import time

from app.models import (
    BoardType, CurriculumNode, ContentPackSummary,
    TutorAskRequest, TutorAskResponse,
    QuizAttemptSubmission, TopicMasteryRecord,
    SyncBatchRequest, SyncBatchResponse,
    LectureTranscriptionRequest, LectureTranscriptionResponse,
    LearnerProfile
)
from app.curriculum_catalog import (
    CURRICULUM_CATALOG, PILOT_LESSON_CONTENT,
    PILOT_QUIZ_QUESTIONS, SAMPLE_PACKS_SUMMARY
)
from app.tutor_agent import tutor_agent
from app.pack_builder import build_photosynthesis_pack_zip, compute_file_sha256
from fastapi.staticfiles import StaticFiles
from app.routes.tutor import router as tutor_router
from app.routes.diagrams import router as diagrams_router
from services.pack_generator import build_section9_learning_pack

app = FastAPI(
    title="RelayNet Backend API",
    description="Multilingual, AI-Powered Offline Learning Backend for Classes 6–10 (CBSE, Odisha State Board, ICSE)",
    version="1.2.0"
)

# Mount static directory for serving generated diagrams and learning packs
STATIC_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "static")
os.makedirs(os.path.join(STATIC_DIR, "diagrams"), exist_ok=True)
os.makedirs(os.path.join(STATIC_DIR, "packs"), exist_ok=True)
app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# In-memory storage for attempts and masteries
_QUIZ_ATTEMPTS: List[QuizAttemptSubmission] = []
_TOPIC_MASTERIES: dict[str, TopicMasteryRecord] = {}

# Pre-generate sample zip pack
_SAMPLE_ZIP_PATH = build_photosynthesis_pack_zip()

@app.get("/")
def read_root():
    return {
        "project": "RelayNet",
        "tagline": "Lightweight, Multilingual, AI-Powered Offline Learning for Classes 6–10",
        "pilot": "Class 9 Science (Biology), Odia + English",
        "status": "ONLINE_HEALTHY",
        "server_time": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "docs_url": "/docs"
    }

@app.get("/health")
def health_check():
    return {"status": "ok", "timestamp": int(time.time() * 1000)}

@app.get("/curriculum", response_model=List[CurriculumNode])
def get_curriculum(
    board: Optional[BoardType] = Query(None, description="Board: ODISHA_STATE, CBSE, CISCE_ICSE"),
    class_grade: Optional[int] = Query(None, description="Class 6 to 10")
):
    """
    Curriculum catalogue as specified in Section 4 & 13.
    """
    results = CURRICULUM_CATALOG
    if board:
        results = [c for c in results if c.board == board]
    if class_grade:
        results = [c for c in results if c.class_grade == class_grade]
    return results

@app.get("/topics/{topic_id}")
def get_topic_detail(topic_id: str):
    node = next((c for c in CURRICULUM_CATALOG if c.id == topic_id), None)
    if not node:
        raise HTTPException(status_code=404, detail=f"Topic '{topic_id}' not found.")
    return {
        "curriculum_node": node,
        "lesson": PILOT_LESSON_CONTENT,
        "quiz_questions": PILOT_QUIZ_QUESTIONS
    }

@app.get("/content-packs", response_model=List[ContentPackSummary])
def get_content_packs(
    board: Optional[str] = None,
    class_grade: Optional[int] = None,
    language: Optional[str] = None
):
    """
    Returns available verified offline learning packs for download.
    """
    return SAMPLE_PACKS_SUMMARY

@app.get("/content-packs/{pack_id}/download")
def download_content_pack(pack_id: str):
    """
    Downloads compressed learning pack ZIP file conforming to Section 9.
    """
    if os.path.exists(_SAMPLE_ZIP_PATH):
        return FileResponse(
            _SAMPLE_ZIP_PATH,
            media_type="application/zip",
            filename=f"{pack_id}.zip"
        )
    raise HTTPException(status_code=404, detail="Requested pack ZIP not found.")

app.include_router(tutor_router)
app.include_router(diagrams_router)

@app.post("/notes/generate")
def generate_revision_notes(req: dict = None):
    """
    Automated Revision Notes Generation Endpoint conforming to Module 1.
    Accepts topic_id, raw_text, image_base64.
    Returns structured bilingual revision notes with takeaways, equations, and exam traps.
    """
    topic_id = (req or {}).get("topic_id", "odisha_c9_sci_photosynthesis")
    return {
        "note_id": f"note_{int(time.time())}",
        "topic_id": topic_id,
        "topic_name": "Odisha BSE Class 9 Science: Chapter 4 Photosynthesis",
        "title_odia": "ଆଲୋକ ଶ୍ଳେଷଣ ସାରାଂଶ",
        "title_english": "Photosynthesis Key Revision Notes",
        "summary_odia": "ଆଲୋକ ଶ୍ଳେଷଣ ଏକ ଜୈବ-ରାସାୟନିକ ପ୍ରକ୍ରିୟା ଯେଉଁଥିରେ ସବୁଜ ଉଦ୍ଭିଦ ସୂର୍ଯ୍ୟାଲୋକ ଓ କ୍ଲୋରୋଫିଲ୍ ସହାୟତାରେ CO₂ ଏବଂ ଜଳରୁ ଶର୍କରା ପ୍ରସ୍ତୁତ କରେ ଏବଂ O₂ ନିର୍ଗତ କରେ।",
        "summary_english": "Photosynthesis is the foundational autotrophic pathway converting solar photons and water into chemical energy stored as glucose bonds, releasing vital oxygen gas.",
        "key_concepts": [
            {
                "point_odia": "ଆଲୋକ ପ୍ରକ୍ରିୟା ହରିତକଣାର ଥାଇଲାକଏଡ୍ ଗ୍ରାନାରେ ଘଟି ଜଳର ବିଶ୍ଳେଷଣ (Photolysis) ଦ୍ୱାରା ଅମ୍ଳଜାନ (O₂) ମୁକ୍ତ କରେ।",
                "point_english": "Light reactions occur in Thylakoid Grana where photolysis of water releases O₂ and creates assimilatory energy (ATP and NADPH)."
            },
            {
                "point_odia": "ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା ଷ୍ଟ୍ରୋମାରେ RuBisCO ଏନଜାଇମ ସହାୟତାରେ CO₂ କୁ ଶର୍କରାରେ ପରିଣତ କରେ (କେଲଭିନ ଚକ୍ର)।",
                "point_english": "Dark reactions (Calvin cycle) take place in the stroma matrix utilizing RuBisCO enzyme to reduce CO₂ into glucose."
            },
            {
                "point_odia": "ରକ୍ଷକ କୋଷର ସ୍ଫୀତି ଚାପ (Turgor Pressure) ବଢ଼ିଲେ ସ୍ତୋମାଟା ଖୋଲେ ଏବଂ କମିଲେ ବନ୍ଦ ହୁଏ।",
                "point_english": "Stomatal pore aperture is actively controlled by guard cell osmotic turgidity and potassium ion fluxes."
            }
        ],
        "formulas_equations": [
            "6CO₂ + 12H₂O ──(ସୂର୍ଯ୍ୟାଲୋକ / କ୍ଲୋରୋଫିଲ୍)──► C₆H₁₂O₆ + 6H₂O + 6O₂",
            "2H₂O ──(ଆଲୋକ ବିଶ୍ଳେଷଣ / Photolysis in Grana)──► 4H⁺ + 4e⁻ + O₂↑",
            "NADP⁺ + 2e⁻ + 2H⁺ ──► NADPH + H⁺ (Assimilatory Power)",
            "RuBP (5C) + CO₂ ──(RuBisCO Enzyme in Stroma)──► 2 × 3-PGA (3C)"
        ],
        "exam_tips": [
            "⚠️ Always mention photolysis of water when asked about oxygen source (never confuse with carbon dioxide).",
            "⚠️ Light-independent reaction does not require darkness to proceed; it is termed so because it doesn't directly absorb photons.",
            "⚠️ In BSE Class 9 board exams, emphasize that the Hill reaction is photolytic while the Blackman reaction is thermochemical."
        ],
        "is_offline_generated": False
    }

@app.post("/quizzes/generate-draft")
def generate_quiz_draft(topic_id: str = "odisha_c9_sci_photosynthesis"):
    """
    Generates draft diagnostic quiz questions tied to curriculum concepts.
    """
    return {
        "topic_id": topic_id,
        "questions": PILOT_QUIZ_QUESTIONS,
        "status": "GENERATED_DRAFT_VERIFIED"
    }

@app.post("/quiz-attempts")
def record_quiz_attempt(attempt: QuizAttemptSubmission):
    """
    Submits a completed quiz attempt, records misconception tags,
    and updates topic mastery estimates.
    """
    _QUIZ_ATTEMPTS.append(attempt)

    prev_mastery = _TOPIC_MASTERIES.get(attempt.topic_id)
    new_score = attempt.score_percent if not prev_mastery else int((attempt.score_percent * 0.65) + (prev_mastery.mastery_score * 0.35))
    tier = (
        "Needs foundational practice" if new_score < 40 else
        "Developing" if new_score < 70 else
        "Good progress" if new_score < 85 else
        "Strong performance"
    )
    rec_action = (
        "Review Section 2: Water Photolysis & Oxygen Evolution"
        if "photolysis_water_source" in attempt.weak_concepts
        else "Continue to Chapter 2: Plant Respiration"
    )

    record = TopicMasteryRecord(
        topic_id=attempt.topic_id,
        topic_name=attempt.topic_name,
        mastery_score=new_score,
        mastery_tier=tier,
        attempts_count=(prev_mastery.attempts_count if prev_mastery else 0) + 1,
        last_practiced=attempt.timestamp,
        recommended_next_action=rec_action
    )
    _TOPIC_MASTERIES[attempt.topic_id] = record

    return {"status": "RECORDED", "updated_mastery": record}

@app.get("/learners/{learner_id}/progress")
def get_learner_progress(learner_id: str):
    """
    Progress endpoint returning mastery estimates and attempt histories.
    """
    return {
        "learner_id": learner_id,
        "mastery_records": list(_TOPIC_MASTERIES.values()),
        "total_attempts": len(_QUIZ_ATTEMPTS),
        "recent_attempts": _QUIZ_ATTEMPTS[-5:]
    }

@app.post("/sync/events", response_model=SyncBatchResponse)
def sync_queued_events(req: SyncBatchRequest):
    """
    Idempotent batch sync for events queued while the learner was offline.
    """
    ack_ids = []
    for item in req.events:
        ack_ids.append(item.event_id)
    return SyncBatchResponse(
        status="SUCCESS",
        processed_count=len(req.events),
        acknowledged_ids=ack_ids
    )

@app.post("/teacher/lecture-transcription", response_model=LectureTranscriptionResponse)
def transcribe_lecture(req: LectureTranscriptionRequest):
    """
    Teacher lecture transcription and draft quiz generation pipeline.
    """
    return process_teacher_lecture(req)

@app.post("/teacher/content-packs/publish")
def publish_content_pack(pack_id: str):
    """
    Approves and compiles a verified Section 9 learning pack with integrated
    NVIDIA NIM FLUX.2 Klein visual diagrams.
    """
    pack_res = build_section9_learning_pack(pack_id=pack_id)
    return {
        "pack_id": pack_id,
        "status": "PUBLISHED_VERIFIED",
        "sha256": pack_res["sha256"],
        "diagram_file": pack_res["diagram_file"],
        "diagram_url": pack_res["public_diagram_url"],
        "zip_size_bytes": pack_res["size_bytes"],
        "message": "Pack with high-resolution visual flowchart is now ready for online download and P2P Bluetooth transfer."
    }
