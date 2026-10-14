import os
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import Optional, List

from services.visual_generator import construct_diagram_prompt, generate_and_save_diagram
from services.pack_generator import extract_primary_concepts

router = APIRouter(prefix="/diagrams", tags=["Visual Diagrams"])

BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
STATIC_DIAGRAMS_DIR = os.path.join(BASE_DIR, "static", "diagrams")

class DiagramGenerateRequest(BaseModel):
    topic_id: str = Field(..., description="Unique topic identifier")
    topic_title: str = Field(..., description="Curriculum lesson title")
    notes_text: Optional[str] = Field("", description="Raw notes text or textbook snippet")
    language_preference: Optional[str] = Field("bilingual_en_or", description="Language mode: english, odia, hindi, or bilingual")

class DiagramGenerateResponse(BaseModel):
    status: str
    diagram_url: str
    prompt_used: str
    storage_size_kb: float
    model: str
    is_ai_generated: bool

@router.post("/generate", response_model=DiagramGenerateResponse)
def generate_diagram_endpoint(req: DiagramGenerateRequest):
    """
    Module 3 Endpoint:
    Generates a high-contrast educational visual flowchart / diagram
    powered by NVIDIA NIM FLUX.2 Klein model (black-forest-labs/flux.2-klein-4b).
    """
    os.makedirs(STATIC_DIAGRAMS_DIR, exist_ok=True)

    key_concepts = extract_primary_concepts(req.topic_title, req.notes_text or "")
    prompt = construct_diagram_prompt(
        topic_title=req.topic_title,
        key_concepts=key_concepts,
        language_mode=req.language_preference or "bilingual"
    )

    clean_topic_id = req.topic_id.replace(" ", "_").lower()
    result = generate_and_save_diagram(
        topic_id=clean_topic_id,
        prompt=prompt,
        output_dir=STATIC_DIAGRAMS_DIR,
        topic_title=req.topic_title,
        key_concepts=key_concepts
    )

    diagram_filename = os.path.basename(result["file_path"])
    diagram_url = f"/static/diagrams/{diagram_filename}"

    return DiagramGenerateResponse(
        status="success",
        diagram_url=diagram_url,
        prompt_used=result["prompt_used"],
        storage_size_kb=result["storage_size_kb"],
        model=result["model"],
        is_ai_generated=result["is_ai_generated"]
    )
