from fastapi import HTTPException
from models import sentiment_model


def analyze(text: str) -> dict:
    if not text or not text.strip():
        raise HTTPException(status_code=400, detail="Text cannot be empty")
    result = sentiment_model(text)
    return result[0] if result else {}
