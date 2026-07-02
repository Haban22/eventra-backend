import json
from models import embedding_model

# Default fallback initialization
try:
    with open("data/events.json", "r", encoding="utf-8") as file:
        default_events = json.load(file)
except Exception:
    default_events = []

def compute_embeddings(events_list):
    texts = [
        f"{event.get('title', '')} {event.get('title', '')} {event.get('description', '')} {event.get('category', '')} {event.get('location', '')}"
        for event in events_list
    ]
    return embedding_model.encode(texts) if texts else []

def compute_similarity_embeddings(events_list):
    texts = [
        f"{event.get('title', '')} {event.get('description', '')} {event.get('category', '')}"
        for event in events_list
    ]
    return embedding_model.encode(texts) if texts else []

# Global caches
current_events = default_events
search_embeddings = compute_embeddings(default_events)
similarity_embeddings = compute_similarity_embeddings(default_events)

def update_events_state(new_events):
    global current_events, search_embeddings, similarity_embeddings
    current_events = new_events
    search_embeddings = compute_embeddings(new_events)
    similarity_embeddings = compute_similarity_embeddings(new_events)
