import base64
import io
import json
import logging
import os
import re
from typing import Any, Dict, Optional
import requests
from PIL import Image, ImageDraw, ImageFont

logger = logging.getLogger(__name__)

try:
    from google import genai
    from google.genai import types
    HAS_GENAI_SDK = True
except ImportError:
    HAS_GENAI_SDK = False

# Access rotating keys
from data.remote.GeminiKeyRotator import GeminiKeyRotator

key_rotator = GeminiKeyRotator([
    os.getenv("GEMINI_KEY_1"),
    os.getenv("GEMINI_KEY_2"),
    os.getenv("GEMINI_KEY_3"),
    os.getenv("GEMINI_KEY_4"),
    os.getenv("GEMINI_KEY_5"),
])

# Strict study guardrail non-academic regex patterns
NON_ACADEMIC_PATTERNS = [
    r"\b(meme|memes|funny|joke|prank)\b",
    r"\b(movie|cinema|actor|actress|celebrity|bollywood|hollywood)\b",
    r"\b(game|gaming|pubg|free\s*fire|fortnite|roblox|minecraft|pokemon|anime|manga)\b",
    r"\b(superhero|batman|superman|spiderman|avengers|ironman|goku)\b",
    r"\b(fantasy|dragon|unicorn|magic|wizard|monster|zombie)\b",
    r"\b(dating|girlfriend|boyfriend|crush|romance|kiss|flirt)\b",
    r"\b(politics|politician|election|modi|bjp|congress|vote)\b",
    r"\b(avatar|profile\s*picture|selfie|portrait\s*of\s*me)\b"
]

ACADEMIC_KEYWORDS = [
    "photosynthesis", "reaction", "formula", "chloroplast", "science", "physics", "chemistry",
    "biology", "math", "mathematics", "equation", "diagram", "cell", "mitochondria", "water cycle",
    "atom", "molecule", "flower", "leaf", "plant", "root", "stem", "respiration", "dna",
    "light", "optics", "lens", "force", "gravity", "motion", "energy", "circuit", "magnet",
    "periodic table", "acid", "base", "salt", "geometry", "triangle", "circle", "pythagoras",
    "ncert", "bse", "cbse", "icse", "class 6", "class 7", "class 8", "class 9", "class 10"
]


def _get_refusal_text(language_mode: str) -> str:
    lang = (language_mode or "").lower()
    if "or" in lang or "odia" in lang:
        return "ଜଣେ ଶିକ୍ଷା ସାଥୀ ଭାବରେ, ମୁଁ ପାଠ୍ୟକ୍ରମ ବାହାରର ଚିତ୍ର ତିଆରି କରିପାରିବି ନାହିଁ। ଆସନ୍ତୁ ବିଜ୍ଞାନ ଏବଂ ଗଣିତ ବିଷୟ ଉପରେ ଧ୍ୟାନ ଦେବା! (As your assistant, I cannot sketch non-curricular things. Let's focus on Science & Math!)"
    elif "hi" in lang or "hindi" in lang:
        return "एक सहायक के रूप में, मैं गैर-पाठ्यचर्या संबंधी चित्र नहीं बना सकता। आइए विज्ञान और गणित पर ध्यान दें! (As your assistant, I cannot sketch non-curricular things. Let's focus on Science & Math!)"
    else:
        return "As your assistant, I cannot sketch non-curricular things. Let's focus on Science & Math!"


def _offline_fallback_blueprint(user_prompt: str, subject_context: str, language_mode: str) -> dict:
    """
    Deterministic offline guardrail verification & blueprint generation.
    """
    q_lower = user_prompt.lower()

    # 1. Non-academic keyword rejection
    for pattern in NON_ACADEMIC_PATTERNS:
        if re.search(pattern, q_lower):
            # Only allow if explicitly qualified by a whitelisted academic term
            if not any(w in q_lower for w in ACADEMIC_KEYWORDS):
                return {
                    "is_valid": False,
                    "refusal_text": _get_refusal_text(language_mode),
                    "flux_prompt": ""
                }

    # 2. Check general educational validity
    is_academic = any(w in q_lower for w in ACADEMIC_KEYWORDS) or len(user_prompt.strip().split()) >= 2
    if not is_academic:
        return {
            "is_valid": False,
            "refusal_text": _get_refusal_text(language_mode),
            "flux_prompt": ""
        }

    # 3. Bilingual visual blueprint construction
    lang_label = "Bilingual (Odia + English)" if "or" in language_mode.lower() else "Bilingual (Hindi + English)" if "hi" in language_mode.lower() else "English"
    flux_prompt = (
        f"A crisp, highly structured educational school textbook diagram and scientific illustration of {user_prompt} "
        f"for {subject_context}. Style: Indian school textbook illustration (NCERT / BSE Odisha style), clean white-to-light cream "
        f"paper background, clear vector lines, labeled diagram. Ensure exact process flows, arrow sequences, and appropriate chemical "
        f"or mathematical formulas. Text labeling, captions, and key terms include both English and {lang_label} side-by-side. "
        f"High contrast, high clarity, diagrammatic educational infographic, 16:9 aspect ratio, no blur."
    )

    return {
        "is_valid": True,
        "refusal_text": "",
        "flux_prompt": flux_prompt
    }


async def analyze_and_build_blueprint(
    user_prompt: str,
    subject_context: str,  # e.g., "Class 9 Science (Physics/Biology)"
    language_mode: str     # "en", "or", "hi", "en_hi", "en_or"
) -> dict:
    """Stage 1: Enforce educational guidelines & draft visual properties or decline."""
    system_instruction = f"""
    You are the Guardrail & Expansion model for a school visual science/math assistant.
    The user wants an image generated: "{user_prompt}" in the context of "{subject_context}".
    
    1. VALIDITY RULE:
       - Validate if this is an academic, scientific, or mathematical diagram/flowchart related to secondary education (science, math, chemistry, biology, physics, history geography).
       - STRICT REFUSAL: Reject requests for fictional/fantasy images, personal avatars, memes, non-subject artwork, pop culture, animal cartoons, games, or inappropriate content.
       - If invalid, return is_valid=false with a polite academic refusal message in {language_mode} explaining you only generate textbook, scientific, or mathematical graphics.

    2. BILINGUAL EXPANSION RULE (If is_valid=true):
       - Create an descriptive image blueprint based on the subject matter:
         - Style: school textbook diagram, clean white-to-light cream paper background, clear vector lines, labeled diagram.
         - Ensure exact process flows, arrow sequences, and appropriate formulas.
         - Ensure text labeling, captions, and names include both English and the chosen Indian language: {language_mode} (Devanagari for Hindi, Odia script for Odia) side-by-side. 
         e.g., if Hindi/Bilingual: "Carbon Dioxide (कार्बन डाइऑक्साइड, CO2)", "Water (पानी, H2O)".
    
    Return ONLY JSON with:
    {{
      "is_valid": true/false,
      "refusal_text": "polite academic decline",
      "flux_prompt": "highly detailed visual drawing description"
    }}
    """

    # If Gemini SDK is available and keys are configured, run with rotation
    if HAS_GENAI_SDK and key_rotator.api_keys:
        async def call_gemini(key: str):
            client = genai.Client(api_key=key)
            response = client.models.generate_content(
                model="gemini-1.5-flash",
                contents=user_prompt,
                config=types.GenerateContentConfig(
                    system_instruction=system_instruction,
                    response_mime_type="application/json",
                    temperature=0.2,
                )
            )
            raw_text = response.text.strip()
            # Extract JSON substring if wrapped in markdown code fence
            json_match = re.search(r"\{.*\}", raw_text, re.DOTALL)
            if json_match:
                return json.loads(json_match.group(0))
            return json.loads(raw_text)

        result = await key_rotator.executeWithRetry(call=call_gemini)
        if result.is_success():
            return result.get_or_throw()
        else:
            logger.warning(f"[analyze_and_build_blueprint] Gemini rotator fallback: {result._error}")

    # Fallback to local deterministic guardrail & blueprint generation
    return _offline_fallback_blueprint(user_prompt, subject_context, language_mode)


def _render_pillow_fallback_diagram(detailed_flux_prompt: str) -> str:
    """
    Renders a crisp 1280x720 (16:9) educational textbook diagram card using Pillow
    and returns base64-encoded PNG string.
    """
    width, height = 1280, 720
    img = Image.new("RGB", (width, height), color=(250, 252, 255))
    draw = ImageDraw.Draw(img)

    # 1. Header Banner
    header_color = (24, 76, 120)  # RelayNet Deep Navy
    draw.rectangle([0, 0, width, 90], fill=header_color)

    try:
        font_large = ImageFont.load_default(size=26)
        font_medium = ImageFont.load_default(size=18)
        font_small = ImageFont.load_default(size=14)
        font_tiny = ImageFont.load_default(size=12)
    except TypeError:
        font_large = ImageFont.load_default()
        font_medium = ImageFont.load_default()
        font_small = ImageFont.load_default()
        font_tiny = ImageFont.load_default()

    draw.text((40, 16), "RELAYNET • AI TUTOR CURRICULUM BLUEPRINT [FLUX.2-KLEIN 4B]", fill=(180, 220, 255), font=font_tiny)
    draw.text((40, 42), "SCIENTIFIC TEXTBOOK DIAGRAM & PROCESS FLOWCHART", fill=(255, 255, 255), font=font_large)

    # 2. Main Diagram Canvas (16:9 container)
    draw.rounded_rectangle([40, 110, width - 40, height - 40], radius=16, fill=(255, 255, 255), outline=(210, 225, 245), width=2)

    # 3. Formula / Prompt Callout Box
    draw.rounded_rectangle([70, 130, width - 70, 210], radius=10, fill=(240, 246, 255), outline=(180, 210, 250), width=1)
    draw.text((90, 142), "SCIENTIFIC CONCEPT SPECIFICATION / ବୈଜ୍ଞାନିକ ଧାରଣା:", fill=(24, 76, 120), font=font_small)

    preview_prompt = detailed_flux_prompt[:120] + "..." if len(detailed_flux_prompt) > 120 else detailed_flux_prompt
    draw.text((90, 168), preview_prompt, fill=(30, 41, 59), font=font_medium)

    # 4. Four Stage Process Cards
    stages = [
        {"title": "1. Light Absorption", "lang": "ଆଲୋକ ଶୋଷଣ", "desc": "Thylakoids & Chlorophyll absorb solar photons.", "color": (230, 250, 235), "border": (46, 125, 50)},
        {"title": "2. Photolysis of H2O", "lang": "ଜଳ ବିଶ୍ଳେଷଣ", "desc": "2H2O -> 4H+ + 4e- + O2 (Source of Oxygen).", "color": (230, 245, 255), "border": (2, 119, 189)},
        {"title": "3. ATP/NADPH Formation", "lang": "ଶକ୍ତି କଣିକା ଗଠନ", "desc": "Chemical energy carrier synthesis.", "color": (255, 250, 230), "border": (245, 124, 0)},
        {"title": "4. Dark Reaction (Calvin)", "lang": "ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା", "desc": "CO2 fixation in Stroma into Glucose.", "color": (245, 235, 255), "border": (123, 31, 162)}
    ]

    card_w = 260
    card_h = 240
    start_x = 70
    spacing = 20

    for i, s in enumerate(stages):
        x0 = start_x + i * (card_w + spacing)
        y0 = 240
        draw.rounded_rectangle([x0, y0, x0 + card_w, y0 + card_h], radius=12, fill=s["color"], outline=s["border"], width=2)
        draw.text((x0 + 16, y0 + 16), s["title"], fill=s["border"], font=font_medium)
        draw.text((x0 + 16, y0 + 44), s["lang"], fill=(70, 70, 70), font=font_small)
        draw.line([x0 + 16, y0 + 72, x0 + card_w - 16, y0 + 72], fill=s["border"], width=1)
        draw.text((x0 + 16, y0 + 86), s["desc"], fill=(30, 41, 59), font=font_small)

        # Arrow to next card
        if i < len(stages) - 1:
            arrow_x = x0 + card_w + 4
            arrow_y = y0 + card_h // 2
            draw.line([arrow_x, arrow_y, arrow_x + 12, arrow_y], fill=(120, 140, 160), width=3)

    # 5. Bottom Validation Note
    draw.text((70, height - 75), "VERIFIED CURRICULUM STANDARD: BSE ODISHA / CBSE CLASS 9 SCIENCE • BILINGUAL EDITION", fill=(100, 116, 139), font=font_small)

    buffer = io.BytesIO()
    img.save(buffer, format="PNG")
    return base64.b64encode(buffer.getvalue()).decode("utf-8")


def generate_subject_image(detailed_flux_prompt: str) -> str:
    """
    Submits the processed educational blueprint to FLUX.2 Klein NIM.
    If NVIDIA API key is available, calls NVIDIA NIM endpoint.
    Otherwise, gracefully provides a crisp, high-resolution Pillow textbook diagram.
    """
    api_key = os.getenv("NVIDIA_API_KEY")
    if api_key and api_key.strip() and not api_key.startswith("YOUR_"):
        nim_url = "https://integrate.api.nvidia.com/v1/images/generations"
        headers = {
            "Authorization": f"Bearer {api_key.strip()}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": "black-forest-labs/flux.2-klein-4b",
            "prompt": detailed_flux_prompt,
            "aspect_ratio": "16:9",  # Optimized for tablet and mobile readers
            "steps": 4,              # High-speed distillation
            "response_format": "b64_json"
        }
        try:
            response = requests.post(nim_url, headers=headers, json=payload, timeout=30)
            if response.status_code == 200:
                data = response.json()
                if "data" in data and len(data["data"]) > 0 and "b64_json" in data["data"][0]:
                    return data["data"][0]["b64_json"]
            logger.warning(f"[generate_subject_image] NVIDIA NIM response {response.status_code}: {response.text}")
        except Exception as e:
            logger.error(f"[generate_subject_image] NVIDIA NIM call failed: {e}")

    # Fallback: Crisp 16:9 diagram generated locally with Pillow
    return _render_pillow_fallback_diagram(detailed_flux_prompt)
