from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from enum import Enum
import time

class BoardType(str, Enum):
    ODISHA_STATE = "ODISHA_STATE"
    CBSE = "CBSE"
    CISCE_ICSE = "CISCE_ICSE"

class Language(str, Enum):
    ODIA = "ODIA"
    ENGLISH = "ENGLISH"
    BILINGUAL = "BILINGUAL"

class LearnerProfile(BaseModel):
    learner_id: str = "learner_local_001"
    class_grade: int = 9
    board: BoardType = BoardType.ODISHA_STATE
    preferred_language: Language = Language.BILINGUAL
    target_subject: str = "Science (Biology)"
    study_streak_days: int = 4

class CurriculumNode(BaseModel):
    id: str
    board: BoardType
    class_grade: int
    subject: str
    unit_chapter: str
    topic: str
    learning_objective: str
    supported_languages: List[str]
    has_offline_pack: bool = True
    content_version: str = "v1.2.0"
    last_reviewed: str = "2026-09-15"
    licence: str = "CC-BY-NC-SA 4.0 / SCERT Odisha Educational Fair-Use"

class KeyGlossaryTerm(BaseModel):
    odia_term: str
    english_term: str
    definition: str

class LessonContent(BaseModel):
    title_odia: str
    title_english: str
    overview_odia: str
    overview_english: str
    chemical_equation: Optional[str] = None
    detailed_sections_odia: List[str]
    detailed_sections_english: List[str]
    key_terms: List[KeyGlossaryTerm]

class QuizOption(BaseModel):
    id: str
    text_odia: str
    text_english: str
    misconception_explanation: Optional[str] = None

class QuizQuestion(BaseModel):
    id: str
    question_odia: str
    question_english: str
    options: List[QuizOption]
    correct_option_id: str
    explanation_odia: str
    explanation_english: str
    concept_tag: str

class PackManifest(BaseModel):
    pack_id: str
    version: str
    board: str
    class_grade: int
    subject: str
    chapter: str
    topic: str
    languages: List[str]
    app_schema_version: str = "1.0.0"
    sha256_checksum: str
    file_count: int
    size_bytes: int
    publisher_identity: str = "RelayNet Educational Verification Authority"
    licence: str = "CC-BY-NC-SA 4.0"
    created_date: str = "2026-10-01"

class ContentPackSummary(BaseModel):
    pack_id: str
    title_odia: str
    title_english: str
    board: str
    class_grade: int
    subject: str
    version: str
    size_mb: float
    sha256_hash: str
    download_url: str

class TutorAskRequest(BaseModel):
    query: Optional[str] = None
    question: Optional[str] = None
    image_base64: Optional[str] = None
    curriculum_context: str = "Class 9 Science - Photosynthesis"
    language: str = "odia"
    learner_id: str = "learner_001"
    class_grade: int = 9
    board: BoardType = BoardType.ODISHA_STATE
    subject: str = "Science (Biology)"
    topic: str = "Photosynthesis"
    preferred_language: Language = Language.BILINGUAL

class TutorAskResponse(BaseModel):
    reply: str
    source_tags: List[str] = Field(default_factory=list)
    is_study_related: bool = True
    answer_odia: Optional[str] = None
    answer_english: Optional[str] = None
    grounding_source: str = "Odisha Board & CBSE Class 9 Science: Nutrition in Plants"
    is_offline_grounded: bool = False
    recommended_quiz_id: Optional[str] = None

class QuizAttemptSubmission(BaseModel):
    attempt_id: str
    learner_id: str = "learner_001"
    topic_id: str
    topic_name: str
    score_percent: int
    correct_count: int
    total_count: int
    weak_concepts: List[str]
    timestamp: int = Field(default_factory=lambda: int(time.time() * 1000))

class TopicMasteryRecord(BaseModel):
    topic_id: str
    topic_name: str
    mastery_score: int
    mastery_tier: str
    attempts_count: int
    last_practiced: int
    recommended_next_action: str

class SyncQueueItem(BaseModel):
    event_id: str
    event_type: str
    payload: Dict[str, Any]
    client_timestamp: int
    retry_count: int = 0

class SyncBatchRequest(BaseModel):
    learner_id: str
    events: List[SyncQueueItem]

class SyncBatchResponse(BaseModel):
    status: str = "SUCCESS"
    processed_count: int
    acknowledged_ids: List[str]
    server_timestamp: int = Field(default_factory=lambda: int(time.time() * 1000))

class LectureTranscriptionRequest(BaseModel):
    teacher_name: str
    board: BoardType
    class_grade: int
    subject: str
    chapter_title: str
    audio_filename: Optional[str] = "lecture_photosynthesis_sample.wav"
    raw_transcript_text: Optional[str] = None

class LectureTranscriptionResponse(BaseModel):
    lecture_id: str
    transcript_sections: List[Dict[str, str]]
    summary_odia: str
    summary_english: str
    draft_quiz_questions: List[QuizQuestion]
    status: str = "DRAFT_PENDING_TEACHER_REVIEW"
