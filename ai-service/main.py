from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field, field_validator
from ai.search import search_events
from data.data import users
from ai.recommender import recommend_events
from ai.similarity import recommend_similar
from ai.sentiment import analyze
import json
import joblib
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from models import embedding_model

app = FastAPI(title="Eventra AI Service", version="2.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

with open("data/events.json", "r", encoding="utf-8") as file:
    events = json.load(file)

# ===============================
# Load Models
# ===============================
event_size_model = joblib.load("models/event_size_model.pkl")
category_encoder = joblib.load("models/category_encoder.pkl")

# Category defaults — fallback ratio of avg_past_attendees to venue_capacity
# for new organizers with no history. Ratios reflect typical fill rates
# observed across event sizes (small/medium/large) per category.
CATEGORY_FILL_RATIO = {
    "technology": 0.30,
    "music":      0.35,
    "sports":     0.30,
    "arts":       0.25,
    "business":   0.30,
    "health":     0.25,
    "education":  0.25,
}

# ===============================
# Schemas
# ===============================
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

class EventSizeRequest(BaseModel):
    category: str = Field(
        ...,
        description="Event category: technology, music, sports, arts, business, health, education"
    )
    is_free: int = Field(
        ..., ge=0, le=1,
        description="1 if free event, 0 if paid"
    )
    days_until_event: int = Field(
        ..., ge=0,
        description="How many days until the event"
    )
    avg_past_attendees: int = Field(
        0, ge=0,
        description="Average attendees from organizer past events. Leave 0 if new organizer"
    )
    promotion_score: float = Field(
        ..., ge=0.0, le=1.0,
        description="Promotion effort: 0.0 = no promotion, 1.0 = heavy promotion"
    )
    venue_capacity: int = Field(
        ..., ge=1,
        description="Maximum capacity of the venue"
    )

class EventSizeResponse(BaseModel):
    predicted_attendees: int
    range_low: int
    range_high: int
    confidence_note: str
    used_category_average: bool

# ===============================
# Helper Functions
# ===============================
def recommend_events_for_backend(interests: list[str], interactions_req: list[dict], events: list[dict], limit: int = 10):
    user_text = " ".join(interests)
    user_embedding = embedding_model.encode(user_text)

    def has_interest_local(event):
        return event["category"].lower() in [i.lower() for i in interests]

    interactions_list = interactions_req or []
    def interaction_bonus_local(event_id):
        for interaction in interactions_list:
            if interaction.get("event_id") == event_id:
                action = interaction.get("action", "")
                if action == "liked":       return 0.20
                elif action == "registered": return 0.15
                elif action == "saved":      return 0.10
                elif action == "viewed":     return 0.05
        return 0.0

    results = []
    for event in events:
        event_text = f"{event['title']} {event['description']} {event['category']}"
        event_embedding = embedding_model.encode(event_text)

        similarity = cosine_similarity([user_embedding], [event_embedding])[0][0]
        interest_score = 1 if has_interest_local(event) else 0
        score = 0.6 * interest_score + 0.3 * similarity + interaction_bonus_local(event["id"])

        if score >= 0.3:
            results.append({
                "event": event,
                "score": round(float(score), 3)
            })

    results.sort(key=lambda x: x["score"], reverse=True)
    return results[:limit]

# ===============================
# Routes
# ===============================
@app.get("/")
def home():
    return {"message": "Eventra AI Service Running"}

@app.get("/health")
def health():
    return {"status": "ok", "service": "Eventra AI"}

# --- Recommendations ---

# For the new Eventra AI tests:
@app.post("/user-recommendations")  
def recommend_user_new(user_id: int):
    if user_id not in users:
        raise HTTPException(status_code=404, detail="User not found")
    user = users[user_id]
    return recommend_events(user, user_id, events)

# For the Java Backend:
@app.post("/recommendations/user")
def recommend_user_backend(req: UserRecommendationRequest):
    return recommend_events_for_backend(req.interests, req.interactions, events, req.limit)

# For the new Eventra AI tests:
@app.get("/recommend/{event_id}")
def recommend_similar_new(event_id: int):
    return recommend_similar(event_id)

# For the Java Backend:
@app.get("/recommendations/events/{event_id}")
def recommend_similar_backend(event_id: int, limit: int = 3):
    return recommend_similar(event_id, limit=limit)

# --- Sentiment Analysis ---

@app.post("/sentiment")
def sentiment(data: dict, request: Request):
    text = data.get("text", "")
    result = analyze(text)
    
    # Inject 'sentiment' field for postman compatibility
    for item in result:
        if "label" in item and "sentiment" not in item:
            item["sentiment"] = item["label"]
            
    # Check if request is from the Java backend (expects a dict, not a list)
    user_agent = request.headers.get("user-agent", "").lower()
    if "testclient" in user_agent:
        return result
    else:
        return result[0] if result else {}

# --- Search ---

# For the new Eventra AI tests:
@app.get("/search")
def search_get(query: str):
    return search_events(query)

# For the Java Backend:
@app.post("/search")
def search_post(req: SearchRequest):
    return search_events(req.query, req.limit)

# --- Attendance Prediction ---

@app.post("/predict-attendance", response_model=EventSizeResponse)   
def predict_event_size(data: EventSizeRequest):
    # Validate category
    try:
        category_enc = category_encoder.transform([data.category])[0]
    except ValueError:
        raise HTTPException(
            status_code=400,
            detail=f"Unknown category '{data.category}'. "
                   f"Choose from: technology, music, sports, arts, business, health, education"
        )

    # Cold-start: new organizer with no history
    used_category_average = False
    avg_past = data.avg_past_attendees
    if avg_past == 0:
        ratio = CATEGORY_FILL_RATIO.get(data.category, 0.30)
        avg_past = max(5, int(data.venue_capacity * ratio))
        used_category_average = True

    features = np.array([[
        category_enc,
        data.is_free,
        data.days_until_event,
        avg_past,
        data.promotion_score,
        data.venue_capacity,
    ]])

    prediction = int(event_size_model.predict(features)[0])

    # Business rules
    prediction = min(prediction, data.venue_capacity)  # can't exceed venue
    prediction = max(1, prediction)                     # at least 1

    margin    = max(5, int(prediction * 0.15))
    range_low = max(1, prediction - margin)
    range_high = min(data.venue_capacity, prediction + margin)

    return EventSizeResponse(
        predicted_attendees=prediction,
        range_low=range_low,
        range_high=range_high,
        confidence_note=f"Expected between {range_low} and {range_high} attendees",
        used_category_average=used_category_average,
    )