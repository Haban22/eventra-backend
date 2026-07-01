from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_similarity_status_code():

    response = client.get("/recommendations/events/1")

    assert response.status_code == 200


def test_similarity_returns_list():

    response = client.get("/recommendations/events/1")

    data = response.json()

    assert isinstance(data, list)


def test_similarity_not_empty():

    response = client.get("/recommendations/events/1")

    data = response.json()

    assert len(data) > 0


def test_similarity_respects_limit():

    response = client.get("/recommendations/events/1?limit=2")

    data = response.json()

    assert len(data) <= 2


def test_not_recommend_same_event():

    response = client.get("/recommendations/events/1")

    data = response.json()

    for item in data:
        assert item["event"]["id"] != 1


def test_invalid_event():

    response = client.get("/recommendations/events/999")

    assert response.status_code == 404

    assert response.json()["detail"] == "Event not found"
