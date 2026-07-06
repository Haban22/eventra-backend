from fastapi.testclient import TestClient
from main import app

client = TestClient(app)


def test_similarity_status_code():
    response = client.get("/recommend/1")
    assert response.status_code == 200


def test_similarity_returns_list():
    response = client.get("/recommend/1")
    data = response.json()
    assert isinstance(data, list)


def test_similarity_not_empty():
    response = client.get("/recommend/1")
    data = response.json()
    assert len(data) > 0


def test_first_similar_event_is_cybersecurity():
    # Event 1 (AI Workshop - Technology)
    # After category boost, Cybersecurity (id=5) scores highest
    response = client.get("/recommend/1")
    data = response.json()
    assert data[0]["event"]["id"] == 5


def test_not_recommend_same_event():
    response = client.get("/recommend/1")
    data = response.json()
    for item in data:
        assert item["event"]["id"] != 1


def test_max_3_results():
    response = client.get("/recommend/1")
    data = response.json()
    assert len(data) <= 3


def test_scores_sorted_descending():
    response = client.get("/recommend/1")
    data = response.json()
    scores = [item["score"] for item in data]
    assert scores == sorted(scores, reverse=True)


def test_technology_events_for_technology_event():
    # Event 1 is Technology — top results should be Technology
    response = client.get("/recommend/1")
    data = response.json()
    assert data[0]["event"]["category"] == "Technology"


def test_no_similar_events_returns_message():
    # Event 8 (Fitness/Health) — unique category, no similar events above threshold
    response = client.get("/recommend/8")
    data = response.json()
    assert "message" in data
    assert data["message"] == "No similar events found for this event."


def test_invalid_event():
    response = client.get("/recommend/999")
    assert response.status_code == 404
    assert response.json()["detail"] == "Event not found"