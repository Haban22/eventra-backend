from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

NEW_EVENTS = [
    {
        "id": 101,
        "title": "Quantum Hacking",
        "description": "Learn quantum cryptography and ethical hacking.",
        "category": "Technology",
        "location": "Giza",
        "date": "2026-10-10"
    },
    {
        "id": 102,
        "title": "Salsa Dance Gala",
        "description": "Art and musical salsa dance festival.",
        "category": "Music",
        "location": "Alexandria",
        "date": "2026-10-11"
    }
]

def test_sync_events_success():
    response = client.post("/events/sync", json=NEW_EVENTS)
    assert response.status_code == 200
    assert response.json() == {"status": "success", "synced_events_count": 2}

def test_search_uses_synced_events():
    # Search for 'quantum' should find 'Quantum Hacking'
    response = client.get("/search?query=quantum")
    assert response.status_code == 200
    data = response.json()
    assert len(data) > 0
    assert data[0]["event"]["id"] == 101
    assert data[0]["event"]["title"] == "Quantum Hacking"

def test_recommend_similar_uses_synced_events():
    # Event 101 (Quantum Hacking - Technology)
    response = client.get("/recommend/101")
    assert response.status_code == 200
    # Salsa is music, different category and no similarity above 0.3 should return 'No similar events found'
    data = response.json()
    assert "message" in data
    assert data["message"] == "No similar events found for this event."

def test_restore_default_events():
    # Restore the original events so subsequent test suites aren't affected
    import json
    with open("data/events.json", "r", encoding="utf-8") as file:
        original = json.load(file)
    response = client.post("/events/sync", json=original)
    assert response.status_code == 200
    assert response.json()["synced_events_count"] == len(original)
