from fastapi import HTTPException
import json
from sklearn.metrics.pairwise import cosine_similarity
from models import embedding_model

with open("data/events.json", "r", encoding="utf-8") as file:
    events = json.load(file)

texts = [
    f"{event['title']} {event['description']} {event['category']}"
    for event in events
]

embeddings = embedding_model.encode(texts)

# Same-category events get a boost to prevent cross-category
# results when scores are close
CATEGORY_BOOST = 0.15

# Minimum score � results below this are not meaningful enough
# to recommend. Prevents returning noise for unique-category events.
MIN_SCORE = 0.30


def recommend_similar(event_id, limit: int = 3):

    target_index = None
    for i, event in enumerate(events):
        if event["id"] == event_id:
            target_index = i
            break

    if target_index is None:
        raise HTTPException(status_code=404, detail="Event not found")

    target_category = events[target_index]["category"].lower()

    similarities = cosine_similarity(
        [embeddings[target_index]],
        embeddings
    )[0]

    recommendations = []
    for i, score in enumerate(similarities):
        if events[i]["id"] != event_id:
            boost      = CATEGORY_BOOST if events[i]["category"].lower() == target_category else 0.0
            final_score = float(score) + boost

            # Only include results above minimum quality threshold
            if final_score >= MIN_SCORE:
                recommendations.append({
                    "event": events[i],
                    "score": round(final_score, 3)
                })

    recommendations.sort(key=lambda x: x["score"], reverse=True)

    # If no results meet threshold, return informative message
    if not recommendations:
        return {"message": "No similar events found for this event."}

    return recommendations[:limit]