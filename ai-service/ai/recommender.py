from sklearn.metrics.pairwise import cosine_similarity
from models import embedding_model
from data.data import interactions

# Minimum score threshold � events below this are not recommended.
# A score of 0.3 cleanly separates interest-matched events from
# unrelated ones based on our cosine similarity formula:
# score = 0.6 * interest_match + 0.3 * semantic_similarity + interaction_bonus
# Any event matching user interests scores >= 0.6 minimum.
# Unrelated events score < 0.1 (semantic similarity only, no interest match).
SCORE_THRESHOLD = 0.3


def has_interest(user, event):
    """
    Returns True if the event category matches one
    of the user's selected interests.
    """
    return event["category"].lower() in [
        interest.lower()
        for interest in user["interests"]
    ]


def interaction_bonus(user_id, event_id):
    """
    Gives a small bonus based on previous interactions.
    liked    ? +0.20 (strongest signal)
    registered ? +0.15 (committed action)
    saved    ? +0.10 (bookmarked for later)
    viewed   ? +0.05 (weakest signal)
    """
    for interaction in interactions:
        if (
            interaction["user_id"] == user_id
            and interaction["event_id"] == event_id
        ):
            action = interaction["action"]
            if action == "liked":       return 0.20
            elif action == "registered": return 0.15
            elif action == "saved":      return 0.10
            elif action == "viewed":     return 0.05
    return 0.0


def recommend_events(user, user_id, events):
    """
    Hybrid recommendation combining:
    - User interests match (highest weight: 0.6)
    - Semantic similarity via sentence embeddings (weight: 0.3)
    - Interaction history bonus (up to +0.20)

    Only returns events above SCORE_THRESHOLD (0.3) to avoid
    recommending irrelevant events with near-zero scores.
    """
    user_text = " ".join(user["interests"])
    user_embedding = embedding_model.encode(user_text)

    results = []

    for event in events:
        event_text = (
            f"{event['title']} "
            f"{event['description']} "
            f"{event['category']}"
        )
        event_embedding = embedding_model.encode(event_text)

        similarity = cosine_similarity(
            [user_embedding],
            [event_embedding]
        )[0][0]

        interest_score = 1 if has_interest(user, event) else 0

        score = (
            0.6 * interest_score
            + 0.3 * similarity
            + interaction_bonus(user_id, event["id"])
        )

        # Only include events above threshold
        if score >= SCORE_THRESHOLD:
            results.append({
                "event": event,
                "score": round(float(score), 3)
            })

    results.sort(key=lambda x: x["score"], reverse=True)
    return results