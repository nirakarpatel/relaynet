import time
import hashlib
import secrets
from typing import Optional, Dict
from pydantic import BaseModel, EmailStr
from app.models import BoardType, Language

class UserRegisterRequest(BaseModel):
    name: str
    email: str
    password: str
    class_grade: int = 9
    board: BoardType = BoardType.ODISHA_STATE
    preferred_language: Language = Language.BILINGUAL

class UserLoginRequest(BaseModel):
    email: str
    password: str

class GoogleAuthRequest(BaseModel):
    id_token: Optional[str] = None
    email: str
    name: str
    photo_url: Optional[str] = None
    class_grade: Optional[int] = 9
    board: Optional[BoardType] = BoardType.ODISHA_STATE

class UserProfileResponse(BaseModel):
    user_id: str
    name: str
    email: str
    avatar_url: Optional[str] = None
    class_grade: int
    board: BoardType
    preferred_language: Language
    auth_provider: str  # "EMAIL" or "GOOGLE"
    token: str
    created_at: int

# In-memory user store for demo/realtime session sync
_USERS_DB: Dict[str, dict] = {
    "student@nmiet.edu.in": {
        "user_id": "usr_pilot_001",
        "name": "Soumya Ranjan",
        "email": "student@nmiet.edu.in",
        "password_hash": hashlib.sha256("relaynet123".encode()).hexdigest(),
        "avatar_url": "https://api.dicebear.com/7.x/bottts/svg?seed=Soumya",
        "class_grade": 9,
        "board": BoardType.ODISHA_STATE,
        "preferred_language": Language.BILINGUAL,
        "auth_provider": "EMAIL",
        "token": "tok_seed_001",
        "created_at": int(time.time() * 1000)
    }
}

_TOKENS_DB: Dict[str, str] = {
    "tok_seed_001": "student@nmiet.edu.in"
}

def hash_password(password: str) -> str:
    return hashlib.sha256(password.encode()).hexdigest()

def register_user(req: UserRegisterRequest) -> UserProfileResponse:
    email_key = req.email.lower().strip()
    if email_key in _USERS_DB:
        raise ValueError(f"Account with email {req.email} already exists.")
    
    user_id = f"usr_{secrets.token_hex(4)}"
    token = f"tok_{secrets.token_hex(16)}"
    user_obj = {
        "user_id": user_id,
        "name": req.name.strip(),
        "email": email_key,
        "password_hash": hash_password(req.password),
        "avatar_url": f"https://api.dicebear.com/7.x/bottts/svg?seed={req.name}",
        "class_grade": req.class_grade,
        "board": req.board,
        "preferred_language": req.preferred_language,
        "auth_provider": "EMAIL",
        "token": token,
        "created_at": int(time.time() * 1000)
    }
    _USERS_DB[email_key] = user_obj
    _TOKENS_DB[token] = email_key
    return UserProfileResponse(**user_obj)

def login_user(req: UserLoginRequest) -> UserProfileResponse:
    email_key = req.email.lower().strip()
    user = _USERS_DB.get(email_key)
    if not user:
        raise ValueError("Invalid email or password.")
    if user.get("auth_provider") == "EMAIL" and user.get("password_hash") != hash_password(req.password):
        raise ValueError("Invalid email or password.")
    
    new_token = f"tok_{secrets.token_hex(16)}"
    user["token"] = new_token
    _TOKENS_DB[new_token] = email_key
    return UserProfileResponse(**user)

def authenticate_google(req: GoogleAuthRequest) -> UserProfileResponse:
    email_key = req.email.lower().strip()
    user = _USERS_DB.get(email_key)
    token = f"tok_google_{secrets.token_hex(16)}"
    
    if user:
        user["token"] = token
        if req.name:
            user["name"] = req.name
        if req.photo_url:
            user["avatar_url"] = req.photo_url
        _TOKENS_DB[token] = email_key
        return UserProfileResponse(**user)
    
    # Auto-register new Google user
    user_id = f"usr_g_{secrets.token_hex(4)}"
    user_obj = {
        "user_id": user_id,
        "name": req.name,
        "email": email_key,
        "password_hash": "",
        "avatar_url": req.photo_url or f"https://api.dicebear.com/7.x/bottts/svg?seed={req.name}",
        "class_grade": req.class_grade or 9,
        "board": req.board or BoardType.ODISHA_STATE,
        "preferred_language": Language.BILINGUAL,
        "auth_provider": "GOOGLE",
        "token": token,
        "created_at": int(time.time() * 1000)
    }
    _USERS_DB[email_key] = user_obj
    _TOKENS_DB[token] = email_key
    return UserProfileResponse(**user_obj)

def get_user_by_token(token: str) -> Optional[UserProfileResponse]:
    email = _TOKENS_DB.get(token)
    if not email:
        return None
    user = _USERS_DB.get(email)
    if not user:
        return None
    return UserProfileResponse(**user)
