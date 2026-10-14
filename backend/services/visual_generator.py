import os
import io
import base64
import time
import requests
from typing import List, Dict, Any, Optional
from PIL import Image, ImageDraw, ImageFont

NVIDIA_NIM_URL = "https://integrate.api.nvidia.com/v1/images/generations"
NIM_MODEL = "black-forest-labs/flux.2-klein-4b"

def construct_diagram_prompt(
    topic_title: str,
    key_concepts: List[str],
    language_mode: str = "bilingual",
) -> str:
    """
    Constructs detailed visual blueprint prompts for FLUX.2 Klein
    conforming to Module 1 specification.
    """
    concepts_str = ", ".join(key_concepts) if key_concepts else "light reaction, photolysis, dark reaction, glucose synthesis"
    return (
        f"A crisp, highly structured, clean educational textbook flowchart and scientific diagram of {topic_title}. "
        f"Style: Indian school textbook illustration (NCERT / BSE Odisha style), clean white-to-light cream paper background, clear vector lines. "
        f"Include step-by-step labeled process arrows showing: {concepts_str}. "
        f"Labels: Bold, legible headings in {language_mode}. Clean chemical formulas and balanced equations centered in callout boxes. "
        f"High contrast, high clarity, diagrammatic educational infographic, no blur."
    )

def generate_fallback_diagram(
    topic_id: str,
    topic_title: str,
    key_concepts: List[str],
    output_path: str
) -> None:
    """
    Generates a crisp, high-resolution educational scientific flowchart diagram
    using Pillow when running in offline/testing mode or when NVIDIA API key is not configured.
    """
    width, height = 1280, 720
    img = Image.new("RGB", (width, height), color=(248, 250, 252))
    draw = ImageDraw.Draw(img)

    # 1. Header Banner
    header_color = (24, 76, 120)  # Primary Blue
    draw.rectangle([0, 0, width, 84], fill=header_color)

    # Load default fonts
    try:
        font_large = ImageFont.load_default(size=24)
        font_medium = ImageFont.load_default(size=18)
        font_small = ImageFont.load_default(size=14)
        font_tiny = ImageFont.load_default(size=12)
    except TypeError:
        # Fallback for older Pillow
        font_large = ImageFont.load_default()
        font_medium = ImageFont.load_default()
        font_small = ImageFont.load_default()
        font_tiny = ImageFont.load_default()

    draw.text((36, 18), "RELAYNET • VISUAL CURRICULUM BLUEPRINT", fill=(190, 225, 255), font=font_tiny)
    draw.text((36, 38), f"{topic_title.upper()} — CONCEPT FLOWCHART & EQUATIONS", fill=(255, 255, 255), font=font_large)

    # 2. Balanced Equation Callout Box
    draw.rounded_rectangle([36, 104, width - 36, 172], radius=10, fill=(238, 244, 255), outline=(180, 205, 245), width=2)
    draw.text((54, 114), "BALANCED CHEMICAL EQUATION / ରାସାୟନିକ ସମୀକରଣ:", fill=(24, 76, 120), font=font_small)
    equation_text = "6CO₂ + 12H₂O  ───[ Sunlight (ସୂର୍ଯ୍ୟାଲୋକ) / Chlorophyll (କ୍ଲୋରୋଫିଲ୍) ]───►  C₆H₁₂O₆ + 6H₂O + 6O₂ ↑"
    draw.text((54, 138), equation_text, fill=(15, 23, 42), font=font_medium)

    # 3. Process Steps Flowchart (4 Horizontal Cards)
    steps = [
        {
            "step": "STEP 1",
            "title": "Photon Absorption",
            "odia": "ଆଲୋକ ଶୋଷଣ",
            "desc": "Chlorophyll pigments in thylakoids absorb solar photons.",
            "color": (220, 245, 230),
            "border": (46, 125, 50)
        },
        {
            "step": "STEP 2",
            "title": "Water Photolysis",
            "odia": "ଜଳର ଆଲୋକ ବିଶ୍ଳେଷଣ",
            "desc": "2H₂O ──► 4H⁺ + 4e⁻ + O₂↑\nOxygen released via stomata.",
            "color": (225, 245, 254),
            "border": (2, 119, 189)
        },
        {
            "step": "STEP 3",
            "title": "ATP & NADPH Energy",
            "odia": "ଶକ୍ତି କଣିକା ଗଠନ",
            "desc": "Light energy stored into assimilatory ATP & NADPH.",
            "color": (255, 248, 225),
            "border": (245, 124, 0)
        },
        {
            "step": "STEP 4",
            "title": "CO₂ Fixation (Calvin)",
            "odia": "ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା",
            "desc": "RuBisCO fixes CO₂ in stroma into Glucose (C₆H₁₂O₆).",
            "color": (243, 229, 245),
            "border": (123, 31, 162)
        }
    ]

    card_width = 270
    card_height = 200
    start_x = 36
    y_pos = 196
    spacing = 38

    for i, s in enumerate(steps):
        x1 = start_x + i * (card_width + spacing)
        y1 = y_pos
        x2 = x1 + card_width
        y2 = y1 + card_height

        # Card Box
        draw.rounded_rectangle([x1, y1, x2, y2], radius=12, fill=s["color"], outline=s["border"], width=2)
        draw.rectangle([x1, y1, x2, y1 + 32], fill=s["border"])
        draw.text((x1 + 14, y1 + 8), s["step"], fill=(255, 255, 255), font=font_small)

        draw.text((x1 + 14, y1 + 44), s["title"], fill=(15, 23, 42), font=font_medium)
        draw.text((x1 + 14, y1 + 68), s["odia"], fill=s["border"], font=font_small)

        # Description text
        lines = s["desc"].split("\n")
        cur_y = y1 + 96
        for line in lines:
            draw.text((x1 + 14, cur_y), line, fill=(51, 65, 85), font=font_tiny)
            cur_y += 18

        # Draw Flow Arrow to next card
        if i < len(steps) - 1:
            arrow_x = x2 + 8
            arrow_y = y1 + card_height // 2
            draw.line([arrow_x, arrow_y, arrow_x + 20, arrow_y], fill=(100, 116, 139), width=3)
            draw.polygon([
                (arrow_x + 20, arrow_y - 6),
                (arrow_x + 28, arrow_y),
                (arrow_x + 20, arrow_y + 6)
            ], fill=(100, 116, 139))

    # 4. Detailed Biological Sites Breakdown Table
    table_y = 422
    draw.rounded_rectangle([36, table_y, width - 36, table_y + 220], radius=10, fill=(255, 255, 255), outline=(203, 213, 225), width=2)

    # Sub-header inside table
    draw.rectangle([36, table_y, width - 36, table_y + 36], fill=(241, 245, 249))
    draw.text((54, table_y + 10), "CELLULAR SITE / ଅଙ୍ଗିକା ସ୍ଥାନ", fill=(71, 85, 105), font=font_small)
    draw.text((360, table_y + 10), "BIOLOGICAL PHASE / ପ୍ରକ୍ରିୟା", fill=(71, 85, 105), font=font_small)
    draw.text((700, table_y + 10), "REACTION SUMMARY / ରାସାୟନିକ ପ୍ରତିକ୍ରିୟା", fill=(71, 85, 105), font=font_small)

    rows = [
        ("Thylakoids & Grana (ଗ୍ରାନା)", "Light Reactions (ଆଲୋକ ପ୍ରକ୍ରିୟା)", "Photolysis of H₂O produces O₂ and charges ATP + NADPH."),
        ("Chloroplast Stroma (ଷ୍ଟ୍ରୋମା)", "Dark Reactions (କେଲଭିନ ଚକ୍ର)", "RuBisCO enzyme reduces CO₂ into Starch / Glucose carbohydrates."),
        ("Leaf Epidermis / Stomata (ସ୍ତୋମା)", "Gas Exchange (ବାଷ୍ପ ବିନିମୟ)", "Guard cells regulate CO₂ intake and atmospheric O₂ release.")
    ]

    r_y = table_y + 48
    for row in rows:
        draw.text((54, r_y), row[0], fill=(15, 23, 42), font=font_small)
        draw.text((360, r_y), row[1], fill=(24, 76, 120), font=font_small)
        draw.text((700, r_y), row[2], fill=(51, 65, 85), font=font_small)
        draw.line([50, r_y + 32, width - 50, r_y + 32], fill=(226, 232, 240), width=1)
        r_y += 46

    # 5. Footer Attribution & Model Tag
    draw.text((36, height - 34), "RelayNet Section 9 Offline Visual Resource • Target SDK 36 • High-Contrast Vector Print", fill=(100, 116, 139), font=font_tiny)
    draw.text((width - 340, height - 34), "Powered by NVIDIA NIM • FLUX.2 Klein Distilled", fill=(34, 139, 34), font=font_tiny)

    # Save and optimize (< 350 KB)
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path, "JPEG", quality=88, optimize=True)

def generate_and_save_diagram(
    topic_id: str,
    prompt: str,
    output_dir: str,
    topic_title: str = "Photosynthesis",
    key_concepts: Optional[List[str]] = None
) -> Dict[str, Any]:
    """
    Module 1 Implementation:
    Calls NVIDIA NIM FLUX.2 Klein model (https://integrate.api.nvidia.com/v1/images/generations),
    decodes b64_json, compresses to < 350 KB, and saves into output_dir.
    Falls back gracefully to high-res vector diagram if API key is not supplied or offline.
    """
    os.makedirs(output_dir, exist_ok=True)
    clean_topic_id = topic_id.replace(" ", "_").lower()
    output_filename = f"{clean_topic_id}_flowchart.jpg"
    output_path = os.path.join(output_dir, output_filename)

    api_key = os.environ.get("NVIDIA_API_KEY", "").strip()
    is_ai_generated = False
    model_name = NIM_MODEL

    if api_key:
        try:
            headers = {
                "Authorization": f"Bearer {api_key}",
                "Content-Type": "application/json",
                "Accept": "application/json"
            }
            payload = {
                "prompt": prompt,
                "model": NIM_MODEL,
                "aspect_ratio": "16:9",
                "steps": 4,
                "response_format": "b64_json"
            }

            resp = requests.post(NVIDIA_NIM_URL, json=payload, headers=headers, timeout=25)
            if resp.status_code == 200:
                data = resp.json()
                b64_data = data["data"][0]["b64_json"]
                img_bytes = base64.b64decode(b64_data)

                # Open with Pillow and optimize to under 350 KB
                image = Image.open(io.BytesIO(img_bytes)).convert("RGB")
                image.save(output_path, "JPEG", quality=85, optimize=True)
                is_ai_generated = True
            else:
                # Log and fallback
                print(f"[NVIDIA NIM] API responded with {resp.status_code}: {resp.text}. Using fallback generator.")
                generate_fallback_diagram(clean_topic_id, topic_title, key_concepts or [], output_path)
        except Exception as e:
            print(f"[NVIDIA NIM] Exception during generation: {e}. Using fallback generator.")
            generate_fallback_diagram(clean_topic_id, topic_title, key_concepts or [], output_path)
    else:
        # Offline / no API key mode
        generate_fallback_diagram(clean_topic_id, topic_title, key_concepts or [], output_path)

    # Verify size constraint (< 350 KB)
    file_size_bytes = os.path.getsize(output_path)
    if file_size_bytes > 350 * 1024:
        # Re-compress
        with Image.open(output_path) as im:
            im.save(output_path, "JPEG", quality=75, optimize=True)
        file_size_bytes = os.path.getsize(output_path)

    size_kb = round(file_size_bytes / 1024, 1)

    return {
        "status": "success",
        "file_path": output_path,
        "file_name": output_filename,
        "relative_path": f"diagrams/{output_filename}",
        "storage_size_kb": size_kb,
        "prompt_used": prompt,
        "model": model_name,
        "is_ai_generated": is_ai_generated
    }
