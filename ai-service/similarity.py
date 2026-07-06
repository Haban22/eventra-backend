from fastapi import HTTPException
import json
from sklearn.metrics.pairwise import cosine_similarity
from models import embedding_model

with open("events.json", "r", encoding="utf-8") as file:
    events = json.load(file)

texts = [
    f"{event['title']} {event['description']} {event['category']}"
    for event in events
]
embeddings = embedding_model.encode(texts)


def recommend_similar(event_id: int, limit: int = 3) -> list[dict]:
    target_index = next(
        (i for i, e in enumerate(events) if e["id"] == event_id),
        None
    )

    if target_index is None:
        raise HTTPException(status_code=404, detail="Event not found")

    similarities = cosine_similarity([embeddings[target_index]], embeddings)[0]

    recommendations = [
        {"event": events[i], "score": round(float(score), 3)}
        for i, score in enumerate(similarities)
        if events[i]["id"] != event_id
    ]

    recommendations.sort(key=lambda x: x["score"], reverse=True)
    return recommendations[:limit]
