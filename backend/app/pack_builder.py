import os
import hashlib
from typing import Dict, Any

from services.pack_generator import build_section9_learning_pack

def build_photosynthesis_pack_zip() -> str:
    """
    Builds the canonical RelayNet learning pack ZIP as specified in Section 9
    with integrated NVIDIA NIM FLUX.2 Klein visual diagrams:
    relaynet_class9_science_photosynthesis_v1.zip
    """
    result = build_section9_learning_pack(
        pack_id="relaynet_class9_science_photosynthesis_v1",
        topic_title="Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ / प्रकाश संश्लेषण)"
    )
    return result["zip_path"]

def compute_file_sha256(filepath: str) -> str:
    sha = hashlib.sha256()
    with open(filepath, 'rb') as f:
        while chunk := f.read(8192):
            sha.update(chunk)
    return sha.hexdigest()
