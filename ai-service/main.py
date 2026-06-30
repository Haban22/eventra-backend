from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, field_validator

from recommender import recommend_events
from similarity import recommend_similar
from sentiment import analyze
from search import search_events

import json

app = FastAPI(title="Eventra AI Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

with open("events.json", "r", encoding="utf-8") as file:
    events = json.load(file)


class UserRecommendationRequest(BaseModel):
    interests: list[str]
    interactions: list[dict] = []
    limit: int = 10

    @field_validator("interests")
    @classmethod
    def interests_not_empty(cls, v):
        if not v:
            raise ValueError("interests must not be empty")
        return v


class SentimentRequest(BaseModel):
    text: str


class SearchRequest(BaseModel):
    query: str
    limit: int = 5


@app.get("/health")
def health():
    return {"status": "ok", "service": "Eventra AI"}


@app.post("/recommendations/user")
def recommend_user(req: UserRecommendationRequest):
    return recommend_events(req.interests, req.interactions, events, req.limit)


@app.get("/recommendations/events/{event_id}")
def recommend_event(event_id: int, limit: int = 3):
    return recommend_similar(event_id, limit)


@app.post("/sentiment")
def sentiment(req: SentimentRequest):
    return analyze(req.text)


@app.post("/search")
def search(req: SearchRequest):
    return search_events(req.query, req.limit)
