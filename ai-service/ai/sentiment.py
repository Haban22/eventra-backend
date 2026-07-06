from fastapi import HTTPException
from models import sentiment_model


def analyze(text):

    if not text.strip():
        raise HTTPException(
            status_code=400,
            detail="Text cannot be empty"
        )

    return sentiment_model(text)