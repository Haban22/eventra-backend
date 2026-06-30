from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_recommend_user_status_code():

    response = client.post("/recommend-user?user_id=1")

    assert response.status_code == 200


def test_recommendation_returns_list():

    response = client.post("/recommend-user?user_id=1")

    data = response.json()

    assert isinstance(data, list)


def test_recommendation_not_empty():

    response = client.post("/recommend-user?user_id=1")

    data = response.json()

    assert len(data) > 0


def test_first_event_is_ai_workshop():

    response = client.post("/recommend-user?user_id=1")

    data = response.json()

    assert data[0]["event"]["id"] == 1


def test_technology_before_business():

    response = client.post("/recommend-user?user_id=1")

    data = response.json()

    assert data[1]["event"]["id"] == 3

def test_invalid_user():

    response = client.post(
        "/recommend-user?user_id=999"
    )

    assert response.status_code == 404

    assert response.json()["detail"] == "User not found"