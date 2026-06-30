from sklearn.metrics.pairwise import cosine_similarity
from models import embedding_model


def has_interest(interests: list[str], event: dict) -> bool:
    return event["category"].lower() in [i.lower() for i in interests]


def interaction_bonus(interactions: list[dict], event_id: int) -> float:
    for interaction in interactions:
        if interaction.get("event_id") == event_id:
            action = interaction.get("action", "")
            if action == "liked":
                return 0.20
            elif action == "viewed":
                return 0.10
    return 0.0


def recommend_events(
    interests: list[str],
    interactions: list[dict],
    events: list[dict],
    limit: int = 10
) -> list[dict]:
    """
    Hybrid recommendation: 60% interest match + 30% semantic similarity + 10% interaction bonus.
    """
    user_text = " ".join(interests)
    user_embedding = embedding_model.encode(user_text)

    results = []
    for event in events:
        event_text = f"{event['title']} {event['description']} {event['category']}"
        event_embedding = embedding_model.encode(event_text)
        similarity = cosine_similarity([user_embedding], [event_embedding])[0][0]
        interest_score = 1 if has_interest(interests, event) else 0
        score = (
            0.6 * interest_score +
            0.3 * float(similarity) +
            interaction_bonus(interactions, event["id"])
        )
        results.append({"event": event, "score": round(float(score), 3)})

    results.sort(key=lambda x: x["score"], reverse=True)
    return results[:limit]
