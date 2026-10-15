import os
import asyncio
import logging
from typing import List, Callable, Any, Optional

logger = logging.getLogger(__name__)

class KeyResult:
    def __init__(self, value: Any = None, error: Optional[Exception] = None):
        self._value = value
        self._error = error

    def is_success(self) -> bool:
        return self._error is None

    def get_or_throw(self) -> Any:
        if self._error is not None:
            raise self._error
        return self._value

class GeminiKeyRotator:
    """
    Multi-key failover and rotation manager for Gemini API keys.
    Cycles through 5 keys sequentially on failure or rate-limit.
    """
    def __init__(self, api_keys: Optional[List[Optional[str]]] = None):
        keys = [k.strip() for k in (api_keys or []) if k and isinstance(k, str) and k.strip() and not k.startswith("YOUR_")]
        if not keys:
            # Check environment variables
            for i in range(1, 6):
                env_k = os.getenv(f"GEMINI_KEY_{i}") or os.getenv(f"gemini.key{i}")
                if env_k and env_k.strip() and not env_k.startswith("YOUR_"):
                    keys.append(env_k.strip())
            single_k = os.getenv("GEMINI_API_KEY")
            if single_k and single_k.strip() and not single_k.startswith("YOUR_"):
                keys.append(single_k.strip())

        self.api_keys = keys
        self.current_index = 0
        self._lock = asyncio.Lock()

    def get_active_key(self) -> Optional[str]:
        if not self.api_keys:
            return None
        return self.api_keys[self.current_index % len(self.api_keys)]

    async def rotate_key(self, failed_key: Optional[str] = None):
        async with self._lock:
            if not self.api_keys:
                return
            if failed_key is None or failed_key == self.get_active_key():
                self.current_index = (self.current_index + 1) % len(self.api_keys)
                logger.warning(f"[GeminiKeyRotator] Switched to Gemini API Key [Index: {self.current_index}]")

    async def executeWithRetry(self, call: Callable[[str], Any], max_retries: Optional[int] = None) -> KeyResult:
        if not self.api_keys:
            return KeyResult(error=RuntimeError("No active Gemini API keys configured in rotator."))

        retries = max_retries or len(self.api_keys)
        attempts = 0
        last_exc = None
        while attempts < retries:
            key = self.get_active_key()
            try:
                if asyncio.iscoroutinefunction(call):
                    res = await call(key)
                else:
                    res = call(key)
                return KeyResult(value=res)
            except Exception as e:
                last_exc = e
                logger.error(f"[GeminiKeyRotator] Key error: {e}. Rotating to next key...")
                await self.rotate_key(key)
                attempts += 1
        return KeyResult(error=last_exc or RuntimeError("All Gemini API keys exhausted."))
