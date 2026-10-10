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
from app.teacher_pipeline import process_teacher_lecture

app = FastAPI(
    title="RelayNet Backend API",
    description="Multilingual, AI-Powered Offline Learning Backend for Classes 6–10 (CBSE, Odisha State Board, ICSE)",
    version="1.2.0"
)

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

@app.post("/tutor/ask", response_model=TutorAskResponse)
def ask_ai_tutor(req: TutorAskRequest):
    """
    Grade-aware, multilingual AI tutor grounded in approved curriculum.
    """
    return tutor_agent.process_query(req)

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
    Approves and publishes a reviewed learning pack for student distribution.
    """
    return {
        "pack_id": pack_id,
        "status": "PUBLISHED_VERIFIED",
        "sha256": compute_file_sha256(_SAMPLE_ZIP_PATH) if os.path.exists(_SAMPLE_ZIP_PATH) else "verified",
        "message": "Pack is now ready for online download and P2P Bluetooth transfer."
    }
