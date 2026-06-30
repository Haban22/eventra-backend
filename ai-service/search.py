import json
from sklearn.metrics.pairwise import cosine_similarity
from models import embedding_model

with open("events.json", "r", encoding="utf-8") as file:
    events = json.load(file)

texts = [
    f"{event['title']} {event['title']} {event['description']} {event['category']} {event['location']}"
    for event in events
]
embeddings = embedding_model.encode(texts)

ALIASES = {
    "tech": "technology",
    "it": "technology",
    "ai": "artificial intelligence",
    "ml": "machine learning",
    "ds": "data science",
    "biz": "business",
    "sport": "sports",
    "game": "gaming",
}

RELATED_CATEGORIES = {
    "technology": ["technology"],
    "business": ["business"],
    "sports": ["sports", "gaming"],
    "gaming": ["gaming", "sports"],
    "health": ["health"],
    "music": ["music"],
    "art": ["art"],
}


def search_events(query: str, limit: int = 5) -> list[dict] | dict:
    if not query or not query.strip():
        return {"error": "Search query cannot be empty"}

    query = ALIASES.get(query.lower().strip(), query.lower().strip())

    if query in RELATED_CATEGORIES:
        results = []
        for category in RELATED_CATEGORIES[query]:
            for event in events:
                if event["category"].lower() == category:
                    score = 1.0 if category == query else 0.90
                    results.append({"event": event, "score": score})
        return results[:limit]

    query_embedding = embedding_model.encode([query])
    similarities = cosine_similarity(query_embedding, embeddings)[0]

    results = [
        {"event": events[i], "score": round(float(score), 3)}
        for i, score in enumerate(similarities)
        if score >= 0.25
    ]
    results.sort(key=lambda x: x["score"], reverse=True)

    if not results:
        return {"message": "No matching events found."}

    return results[:limit]
