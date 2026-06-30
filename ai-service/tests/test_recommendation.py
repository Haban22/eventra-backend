from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def recommend_user(interests, interactions=None, limit=10):
    return client.post(
        "/recommendations/user",
        json={"interests": interests, "interactions": interactions or [], "limit": limit},
    )


def test_recommend_user_status_code():

    response = recommend_user(["Technology"])

    assert response.status_code == 200


def test_recommendation_returns_list():

    response = recommend_user(["Technology"])

    data = response.json()

    assert isinstance(data, list)


def test_recommendation_not_empty():

    response = recommend_user(["Technology"])

    data = response.json()

    assert len(data) > 0


def test_recommendation_respects_limit():

    response = recommend_user(["Technology", "Business"], limit=3)

    data = response.json()

    assert len(data) <= 3


def test_each_result_has_event_and_score():

    response = recommend_user(["Technology"])

    data = response.json()

    for item in data:
        assert "event" in item
        assert "score" in item


def test_liked_event_ranks_first():

    response = recommend_user(
        ["Technology", "Business"],
        interactions=[{"event_id": 3, "action": "liked"}],
    )

    data = response.json()

    assert data[0]["event"]["id"] == 3


def test_empty_interests_rejected():

    response = recommend_user([])

    assert response.status_code == 422
