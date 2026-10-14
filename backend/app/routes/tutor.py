from fastapi import APIRouter, File, Form, Request, UploadFile
from typing import Optional
import json

from app.models import TutorAskRequest, TutorAskResponse
from app.tutor_agent import tutor_agent

router = APIRouter(prefix="/tutor", tags=["AI Tutor"])

@router.post("/ask", response_model=TutorAskResponse)
async def ask_ai_tutor_endpoint(
    request: Request,
    query: Optional[str] = Form(None),
    image: Optional[UploadFile] = File(None),
    curriculum_context: Optional[str] = Form(None),
    language: Optional[str] = Form(None)
):
    """
    Multimodal AI Tutor endpoint supporting:
    1. Multipart Form Data (query, image file upload, curriculum_context, language)
    2. JSON Payload (TutorAskRequest with base64 image or raw query)
    With strict study-only guardrails and Gemini Vision extraction.
    """
    content_type = request.headers.get("content-type", "")

    # Handle application/json payloads
    if "application/json" in content_type:
        body_dict = await request.json()
        req_obj = TutorAskRequest(**body_dict)
        return tutor_agent.process_query(req_obj)

    # Handle multipart form data
    img_bytes = None
    img_mime = None
    if image is not None:
        img_bytes = await image.read()
        img_mime = image.content_type

    effective_query = query or ""
    effective_context = curriculum_context or "Class 9 Science - Photosynthesis"
    effective_language = language or "odia"

    return tutor_agent.process_query_full(
        query=effective_query,
        image_bytes=img_bytes,
        image_mime=img_mime,
        curriculum_context=effective_context,
        language=effective_language
    )
