"""
Alias for backend.services.scan_solve
"""
import sys
from pathlib import Path

# Add backend directory to path if needed
backend_root = Path(__file__).resolve().parent.parent.parent
if str(backend_root) not in sys.path:
    sys.path.insert(0, str(backend_root))

from services.scan_solve import (
    extract_text_from_image,
    solve_with_nemotron,
    scan_and_solve_pipeline,
    VISION_MODEL,
    REASONING_MODEL
)

__all__ = [
    "extract_text_from_image",
    "solve_with_nemotron",
    "scan_and_solve_pipeline",
    "VISION_MODEL",
    "REASONING_MODEL"
]
