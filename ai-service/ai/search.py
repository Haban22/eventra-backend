import json
from sklearn.metrics.pairwise import cosine_similarity
from models import embedding_model

# Raised from 0.25 to 0.30 to match recommender threshold
SEMANTIC_THRESHOLD = 0.30


def search_events(query: str, limit: int = 5):
    from ai import state
    events = state.current_events
    embeddings = state.search_embeddings

    # Validate
    if not query or query.strip() == "":
        return {"error": "Search query cannot be empty"}

    query = query.lower().strip()

    # Normalize abbreviations
    aliases = {
        "tech":  "technology",
        "it":    "technology",
        "ai":    "artificial intelligence",
        "ml":    "machine learning",
        "ds":    "data science",
        "biz":   "business",
        "sport": "sports",
        "game":  "gaming",
        "edu":   "education",
    }
    query = aliases.get(query, query)

    # Category search � fixed: added education, gaming, art, health, sports
    related_categories = {
        "technology": ["technology"],
        "business":   ["business"],
        "sports":     ["sports", "gaming"],
        "gaming":     ["gaming", "sports"],
        "health":     ["health"],
        "music":      ["music"],
        "art":        ["art"],
        "education":  ["education"],       # ? was missing
    }

    if query in related_categories:
        results = []
        for category in related_categories[query]:
            for event in events:
                if event["category"].lower() == category:
                    score = 1.0 if category == query else 0.90
                    results.append({"event": event, "score": score})
        return results


    # Exact location search
    locations = {event["location"].lower() for event in events}

    if query in locations:
        results = []

        for event in events:
            if event["location"].lower() == query:
                results.append({
                    "event": event,
                    "score": 1.0
                })

        return results

    # Semantic AI Search
    query_embedding = embedding_model.encode([query])
    similarities = cosine_similarity(query_embedding, embeddings)[0]

    results = []
    for i, score in enumerate(similarities):
        if score >= SEMANTIC_THRESHOLD:
            results.append({
                "event": events[i],
                "score": round(float(score), 3)
            })

    results.sort(key=lambda x: x["score"], reverse=True)

    if len(results) == 0:
        return {"message": "No matching events found."}

    return results[:limit]