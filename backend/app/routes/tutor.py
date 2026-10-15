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


from services.scan_solve import scan_and_solve_pipeline


@router.post("/scan-and-solve")
async def scan_and_solve_endpoint(
    file: Optional[UploadFile] = File(None),
    user_prompt: Optional[str] = Form(None),
    language_preference: str = Form("en_or"),
    curriculum_context: Optional[str] = Form("Class 9 Science - Biology / Life Science"),
    api_key: Optional[str] = Form(None),
    request: Request = None
):
    """
    2-Stage Textbook Question Solver:
    Stage 1: Vision Extraction / OCR using meta/llama-3.2-11b-vision-instruct
    Stage 2: Deep Reasoning & Guardrailed Solving using nvidia/nemotron-3-super-120b-a12b
    """
    img_bytes = b""
    effective_prompt = user_prompt
    effective_lang = language_preference
    effective_context = curriculum_context
    custom_key = api_key

    if request is not None:
        # Extract API key from headers if not in form
        if not custom_key:
            auth_header = request.headers.get("authorization", "")
            if auth_header.lower().startswith("bearer "):
                custom_key = auth_header[7:].strip()
            if not custom_key:
                custom_key = request.headers.get("x-api-key")

        if "application/json" in request.headers.get("content-type", ""):
            try:
                body = await request.json()
                if "image_base64" in body:
                    import base64
                    img_bytes = base64.b64decode(body["image_base64"])
                effective_prompt = body.get("user_prompt", effective_prompt)
                effective_lang = body.get("language_preference", effective_lang)
                effective_context = body.get("curriculum_context", effective_context)
                if not custom_key and "api_key" in body:
                    custom_key = body.get("api_key")
            except Exception:
                pass

    if not img_bytes and file is not None:
        img_bytes = await file.read()

    return scan_and_solve_pipeline(
        image_bytes=img_bytes,
        user_prompt=effective_prompt,
        language_mode=effective_lang,
        curriculum_context=effective_context,
        custom_api_key=custom_key
    )


from pydantic import BaseModel
from fastapi import HTTPException
from services.image_tutor_service import analyze_and_build_blueprint, generate_subject_image


class GenerateRequest(BaseModel):
    user_prompt: str
    subject_context: str = "Class 9 Science - Biology / Life Science"
    language_mode: str = "en_or"


@router.post("/generate-visual")
@router.post("/tutor/generate-visual")
async def tutor_generate_visual(req: GenerateRequest):
    """
    Strictly guardrailed visual diagram generator for AI Tutor using:
    Stage 1: Gemini 1.5 Flash Guardrail & Multi-lingual Blueprint Expansion (Rotating Keys)
    Stage 2: NVIDIA NIM FLUX.2 Klein 4B 4-step image synthesis
    """
    analysis = await analyze_and_build_blueprint(
        req.user_prompt,
        req.subject_context,
        req.language_mode
    )

    if not analysis.get("is_valid", False):
        return {
            "status": "refused",
            "message": analysis.get("refusal_text", "As your assistant, I cannot sketch non-curricular things. Let's focus on Science & Math!"),
            "has_image": False
        }

    try:
        img_b64 = generate_subject_image(analysis["flux_prompt"])
        
        # Localized companion message
        lang = req.language_mode.lower()
        if "or" in lang or "odia" in lang:
            msg = f"ଏହା {req.user_prompt} ର ଏକ ବୈଜ୍ଞାନିକ ଚିତ୍ର ଯାହା ଆପଣଙ୍କ ପାଠ୍ୟକ୍ରମ ସହ ସମ୍ପର୍କିତ:"
        elif "hi" in lang or "hindi" in lang:
            msg = f"यह {req.user_prompt} की एक वैज्ञानिक तस्वीर है जो आपके अध्ययन से संबंधित है:"
        else:
            msg = f"Here is a scientific diagram of {req.user_prompt} grounded in your curriculum:"

        return {
            "status": "success",
            "message": msg,
            "imageBase64": img_b64,
            "has_image": True,
            "source": "[Cloud AI] FLUX.2-Klein (4B)"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


