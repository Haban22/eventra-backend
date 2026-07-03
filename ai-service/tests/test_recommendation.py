from fastapi.testclient import TestClient
from main import app

client = TestClient(app)


def test_recommend_user_status_code():
    response = client.post("/user-recommendations?user_id=1")
    assert response.status_code == 200


def test_recommendation_returns_list():
    response = client.post("/user-recommendations?user_id=1")
    data = response.json()
    assert isinstance(data, list)


def test_recommendation_not_empty():
    response = client.post("/user-recommendations?user_id=1")
    data = response.json()
    assert len(data) > 0


def test_first_event_is_startup_pitch():
    # User 1 top result is Startup Pitch (id=6) — registered bonus gives it highest score
    response = client.post("/user-recommendations?user_id=1")
    data = response.json()
    assert data[0]["event"]["id"] == 6


def test_second_event_is_ai_workshop():
    # User 1 second result is AI Workshop (id=1) — liked bonus
    response = client.post("/user-recommendations?user_id=1")
    data = response.json()
    assert data[1]["event"]["id"] == 1


def test_only_interest_categories_returned():
    # User 1 interests: Technology, Business
    # No Gaming, Sports, Health, Music, Art should appear
    response = client.post("/user-recommendations?user_id=1")
    data = response.json()
    irrelevant = {"Gaming", "Sports", "Health", "Music", "Art"}
    for item in data:
        assert item["event"]["category"] not in irrelevant


def test_scores_are_sorted_descending():
    response = client.post("/user-recommendations?user_id=1")
    data = response.json()
    scores = [item["score"] for item in data]
    assert scores == sorted(scores, reverse=True)


def test_all_scores_above_threshold():
    # Threshold is 0.3 — no event below this should appear
    response = client.post("/user-recommendations?user_id=1")
    data = response.json()
    for item in data:
        assert item["score"] >= 0.3


def test_user2_only_gaming_and_sports():
    # User 2 interests: Gaming, Sports
    response = client.post("/user-recommendations?user_id=2")
    data = response.json()
    valid = {"Gaming", "Sports"}
    for item in data:
        assert item["event"]["category"] in valid


def test_user3_only_health_and_music():
    # User 3 interests: Health, Music
    response = client.post("/user-recommendations?user_id=3")
    data = response.json()
    valid = {"Health", "Music"}
    for item in data:
        assert item["event"]["category"] in valid


def test_invalid_user():
    response = client.post("/user-recommendations?user_id=999")
    assert response.status_code == 404
    assert response.json()["detail"] == "User not found"